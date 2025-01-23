package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.integration.RoleDto;
import com.westmonroe.loansyndication.model.integration.UserDto;
import com.westmonroe.loansyndication.utils.ModelUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureMockMvc
@Testcontainers
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenExistingRoles_whenGettingAllVisibleRoles_thenVerifySize() throws Exception {

        String url = "/api/ext/roles";

        MvcResult result = mockMvc.perform(get(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        }))))
                .andExpect(status().isOk())
                .andReturn();

        List<RoleDto> roleDtos = objectMapper.readerForListOf(RoleDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(roleDtos).hasSize(14);
    }

    @Test
    void givenExistingUsersInDatabase_whenGettingAllForInstitution_thenVerifySize() throws Exception {

        String url = String.format("/api/ext/institutions/%s/users", TEST_INSTITUTION_UUID_2);

        MvcResult result = mockMvc.perform(get(url)
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        }))))
                    .andExpect(status().isOk())
                    .andReturn();

        List<UserDto> userDtos = objectMapper.readerForListOf(UserDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(userDtos).hasSize(4);
    }

    @Test
    @Transactional
    void givenNoUsersInNewInstitution_whenSavingNewUser_thenVerifyInsert() throws Exception {

        // Create an institution to associate a user.
        Institution institution = ModelUtil.createTestInstitution(1);
        MvcResult result = mockMvc.perform(post("/api/institutions")
                                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                                        .jwt(jwt -> jwt.claims(claims -> {
                                            claims.put("sub", TEST_USER_UUID_1);
                                            claims.put("email", TEST_USER_EMAIL_1);
                                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                        })))
                                    .content(objectMapper.writeValueAsString(institution))
                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isCreated())
                                .andReturn();
        institution = objectMapper.readValue(result.getResponse().getContentAsString(), Institution.class);

        // Create the user JSON string because the password field is WRITE_ONLY.
        String userJson = """
            {
                "firstName" : "Test First 1",
                "lastName" : "Test Last 1",
                "email" : "test.user1@westmonroe.com",
                "password" : "123456",
                "active" : "N",
                "enabled" : false,
                "accountNonExpired" : true,
                "accountNonLocked" : true,
                "credentialsNonExpired" : true,
                "roles": [
                    { "id": 2 },
                    { "id": 3 },
                    { "id": 4 }
                ]
            }
            """;
        String url = String.format("/api/ext/institutions/%s/users", institution.getUid());

        // Insert the user in the database.
        result = mockMvc.perform(post(url)
                            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                })))
                            .content(userJson)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isCreated())
                        .andReturn();

        UserDto userDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        String location = result.getResponse().getHeader("Location");
        assertThat(location).matches("/api/ext/users/[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}");
        assertThat(userDto.getRoles()).hasSize(3);

        // Get all of the users in the database.
        result = mockMvc.perform(get(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                        .andExpect(status().isOk())
                        .andReturn();

        List<UserDto> userDtos = objectMapper.readerForListOf(UserDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(userDtos).hasSize(1);
        assertThat(userDtos.get(0).getRoles()).hasSize(3);

        // Get the user by its uid.
        result = mockMvc.perform(get(location)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                        .andExpect(status().isOk())
                        .andReturn();
        UserDto savedUser = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(savedUser.getUid()).isEqualTo(userDto.getUid());
        assertThat(savedUser.getFirstName()).isEqualTo(userDto.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(userDto.getLastName());
        assertThat(savedUser.getActive()).isEqualTo(userDto.getActive());
        assertThat(savedUser.getRoles()).hasSize(3);
    }

    @Test
    void givenExistingUser_whenUpdatingUser_thenVerify() throws Exception {

        String url = String.format("/api/ext/users/%s", TEST_USER_UUID_1);

        // Get the user info.
        MvcResult result = mockMvc.perform(get(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                .andExpect(status().isOk())
                .andReturn();

        UserDto userDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(userDto)
            .isNotNull()
            .hasFieldOrPropertyWithValue("firstName", "Lenor")
            .hasFieldOrPropertyWithValue("lastName", "Anderson")
            .hasFieldOrPropertyWithValue("email", TEST_USER_EMAIL_1)
            .hasFieldOrPropertyWithValue("active", "Y");

        // Update the fields.
        userDto.setFirstName("Lenora");
        userDto.setLastName("Anders");
        userDto.setEmail("lenora.anders@westmonroe.com");
        userDto.setActive("N");

        result = mockMvc.perform(put(url)
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        UserDto updatedUserDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(userDto).isNotNull();
        assertThat(updatedUserDto.getFirstName()).isEqualTo(userDto.getFirstName());
        assertThat(updatedUserDto.getLastName()).isEqualTo(userDto.getLastName());
        assertThat(updatedUserDto.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(updatedUserDto.getActive()).isEqualTo(userDto.getActive());
    }

    @Test
    void givenUsersInInstitution_whenDeletingUser_thenVerify() throws Exception {

        String url = String.format("/api/ext/institutions/%s/users", TEST_INSTITUTION_UUID_2);

        MvcResult result = mockMvc.perform(get(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                }))))
                .andExpect(status().isOk())
                .andReturn();

        List<UserDto> userDtos = objectMapper.readerForListOf(UserDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(userDtos).hasSize(4);

        url = String.format("/api/ext/users/%s", TEST_USER_UUID_3);

        // Delete Leon T. Amerson.
        mockMvc.perform(delete(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                    .jwt(jwt -> jwt.claims(claims -> {
                        claims.put("sub", TEST_USER_UUID_1);
                        claims.put("email", TEST_USER_EMAIL_1);
                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                    }))))
                .andExpect(status().isNoContent())
                .andReturn();

        // Leon could not be deleted, so verify that he is now inactive.
        result = mockMvc.perform(get(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                    .jwt(jwt -> jwt.claims(claims -> {
                        claims.put("sub", TEST_USER_UUID_1);
                        claims.put("email", TEST_USER_EMAIL_1);
                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                    }))))
                .andExpect(status().isOk())
                .andReturn();

        UserDto userDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);
        assertThat(userDto)
            .isNotNull()
            .hasFieldOrPropertyWithValue("email", TEST_USER_EMAIL_3)
            .hasFieldOrPropertyWithValue("active", "N");
    }

    @Test
    void givenExistingUser_whenPerformingCrudOperationsOnRoles_thenVerify() throws Exception {

        String url = String.format("/api/ext/users/%s/roles", TEST_USER_UUID_3);

        MvcResult result = mockMvc.perform(get(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                    .jwt(jwt -> jwt.claims(claims -> {
                        claims.put("sub", TEST_USER_UUID_1);
                        claims.put("email", TEST_USER_EMAIL_1);
                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                    }))))
                .andExpect(status().isOk())
                .andReturn();

        List<RoleDto> roleDtos = objectMapper.readerForListOf(RoleDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(roleDtos).hasSize(3);

        RoleDto roleDto = new RoleDto(14L, "RECV_ALL_INST_INVS", "Deal Invitation Recipient");

        // Add Role RECV_ALL_INST_INVS to Leon T. Ameson
        mockMvc.perform(post(url)
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                    .content(objectMapper.writeValueAsString(roleDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();

        // Get the updated list of roles to verify that the new role was added.
        result = mockMvc.perform(get(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                    .jwt(jwt -> jwt.claims(claims -> {
                        claims.put("sub", TEST_USER_UUID_1);
                        claims.put("email", TEST_USER_EMAIL_1);
                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                    }))))
                .andExpect(status().isOk())
                .andReturn();

        roleDtos = objectMapper.readerForListOf(RoleDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(roleDtos)
            .isNotNull()
            .hasSize(4)
            .contains(roleDto);

        // Delete Role RECV_ALL_INST_INVS from Leon T. Ameson
        mockMvc.perform(delete(url.concat("/14"))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .content(objectMapper.writeValueAsString(roleDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();

        // Get the list of roles to verify that the role has been deleted.
        result = mockMvc.perform(get(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                    .jwt(jwt -> jwt.claims(claims -> {
                        claims.put("sub", TEST_USER_UUID_1);
                        claims.put("email", TEST_USER_EMAIL_1);
                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                    }))))
                .andExpect(status().isOk())
                .andReturn();

        roleDtos = objectMapper.readerForListOf(RoleDto.class).readValue(result.getResponse().getContentAsString());
        assertThat(roleDtos)
            .isNotNull()
            .hasSize(3)
            .doesNotContain(roleDto);
    }

    @Test
    void givenNonAdminUser_whenAddingAdminRoleToUserAndDeletingAdminRoleFromUser_thenVerifyException() throws Exception {

        String url = "/api/ext/users/3aa836ce-5c8d-466c-b644-d7c6a9f9db34/roles";

        // Leon T. (Tim) Amerson will try to add SUPER_ADM to Chris Lender, which should fail because he is not an admin.
        mockMvc.perform(post(url)
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_3);
                                claims.put("email", TEST_USER_EMAIL_3);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                    .content(objectMapper.writeValueAsString(new Role(1L)))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden())
                .andReturn();

        url = String.format("/api/ext/users/%s/roles/1", TEST_USER_UUID_1);

        // Leon T. (Tim) Amerson will try to remove SUPER_ADM from Lenor Anderson, which should fail because he is not an admin.
        mockMvc.perform(delete(url)
                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                    .jwt(jwt -> jwt.claims(claims -> {
                        claims.put("sub", TEST_USER_UUID_3);
                        claims.put("email", TEST_USER_EMAIL_3);
                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                    }))))
                .andExpect(status().isForbidden())
                .andReturn();
    }

}