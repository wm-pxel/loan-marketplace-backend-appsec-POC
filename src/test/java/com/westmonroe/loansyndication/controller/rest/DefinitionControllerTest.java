package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.integration.UserDto;
import com.westmonroe.loansyndication.utils.ModelUtil;
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

import static com.westmonroe.loansyndication.utils.ModelUtil.createTestUserDto;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenNoRolesInDatabase_whenGettingAll_thenVerifyEmptyList() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/roles")
                                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                            .jwt(jwt -> jwt.claims(claims -> {
                                                claims.put("sub", TEST_USER_UUID_1);
                                                claims.put("email", TEST_USER_EMAIL_1);
                                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                            }))))
                                    .andExpect(status().isOk())
                                    .andReturn();

        List<Role> roles = objectMapper.readerForListOf(Role.class).readValue(result.getResponse().getContentAsString());
        assertThat(roles).hasSize(14);
    }

    @Test
    @Transactional
    void givenNoRolesInDatabase_whenSavingNewRole_thenVerifyInsert() throws Exception {

        Role role = ModelUtil.createTestRole(1, null, null, null);

        // Insert the role in the database.
        MvcResult result = mockMvc.perform(post("/api/roles")
                                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                        .jwt(jwt -> jwt.claims(claims -> {
                                            claims.put("sub", TEST_USER_UUID_1);
                                            claims.put("email", TEST_USER_EMAIL_1);
                                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                        })))
                                    .content(objectMapper.writeValueAsString(role))
                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isCreated())
                                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).matches("/api/roles/[0-9]+");

        // Get the id for the previously inserted Role.
        Long roleId = Long.valueOf(location.replace("/api/roles/", ""));

        // Get all of the roles in the database.
        result = mockMvc.perform(get("/api/roles")
                            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                }))))
                        .andExpect(status().isOk())
                        .andReturn();

        List<Role> roles = objectMapper.readerForListOf(Role.class).readValue(result.getResponse().getContentAsString());
        assertThat(roles).hasSize(15);

        // Get the role by its id.
        result = mockMvc.perform(get(location)
                            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                }))))
                        .andExpect(status().isOk())
                        .andReturn();
        Role savedRole = objectMapper.readValue(result.getResponse().getContentAsString(), Role.class);
        assertThat(savedRole.getId()).isEqualTo(roleId);
        assertThat(savedRole.getName()).isEqualTo(role.getName());
        assertThat(savedRole.getDescription()).isEqualTo(role.getDescription());
    }

    @Test
    void givenNoRolesInDatabase_whenRetrievingNonExistentRole_thenVerifyException() throws Exception {

        String url = "/api/roles/99";

        MvcResult result = mockMvc.perform(get(url)
                                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                            .jwt(jwt -> jwt.claims(claims -> {
                                                claims.put("sub", TEST_USER_UUID_1);
                                                claims.put("email", TEST_USER_EMAIL_1);
                                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                            }))))
                                    .andExpect(status().isNotFound())
                                    .andReturn();
    }

    @Test
    void givenNoRolesInDatabase_whenDeletingRole_thenVerifyResult() throws Exception {

        String url = "/api/roles/99";

        // Delete will always return NO_CONTENT, whether the role exists or not.
        MvcResult result = mockMvc.perform(delete(url)
                                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                            .jwt(jwt -> jwt.claims(claims -> {
                                                claims.put("sub", TEST_USER_UUID_1);
                                                claims.put("email", TEST_USER_EMAIL_1);
                                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                            }))))
                                    .andExpect(status().isNoContent())
                                    .andReturn();
    }

    @Test
    @Transactional
    void givenCreatedRole_whenRoleUpdated_thenVerifyChange() throws Exception {

        Role role = ModelUtil.createTestRole(4, null, null, null);
        String updatedRoleName = "Test Role 5";

        // Insert the role in the database.
        MvcResult result = mockMvc.perform(post("/api/roles")
                                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                        .jwt(jwt -> jwt.claims(claims -> {
                                            claims.put("sub", TEST_USER_UUID_1);
                                            claims.put("email", TEST_USER_EMAIL_1);
                                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                        })))
                                    .content(objectMapper.writeValueAsString(role))
                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isCreated())
                                .andReturn();

        String location = result.getResponse().getHeader("Location");
        Role savedRole = objectMapper.readValue(result.getResponse().getContentAsString(), Role.class);

        // Change the role name and update the role in the database.
        role.setId(savedRole.getId());
        role.setName(updatedRoleName);
        result = mockMvc.perform(put(location)
                            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                })))
                            .content(objectMapper.writeValueAsString(role))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isOk())
                        .andReturn();

        // Get the role by its id.
        result = mockMvc.perform(get(location)
                            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                }))))
                        .andExpect(status().isOk())
                        .andReturn();
        Role updatedRole = objectMapper.readValue(result.getResponse().getContentAsString(), Role.class);

        assertThat(savedRole.getName()).isNotEqualTo(updatedRole.getName());
        assertThat(updatedRole.getName()).isEqualTo(updatedRoleName);
        assertThat(savedRole.getDescription()).isEqualTo(updatedRole.getDescription());     // This wasn't updated.
    }

    @Test
    void givenInvalidRole_whenUpdating_thenVerifyException() throws Exception {

        Role role = ModelUtil.createTestRole(6, null, null, null);
        String url = "/api/roles/7";

        // Perform an update without setting the id in the role object.
        MvcResult result = mockMvc.perform(put(url)
                                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                            .jwt(jwt -> jwt.claims(claims -> {
                                                claims.put("sub", TEST_USER_UUID_1);
                                                claims.put("email", TEST_USER_EMAIL_1);
                                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                            })))
                                    .content(objectMapper.writeValueAsString(role))
                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isUnprocessableEntity())
                                .andReturn();

        // The id in the path and role object are different, which will generate an exception.
        role.setId(6L);
        result = mockMvc.perform(put(url)
                            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_1);
                                    claims.put("email", TEST_USER_EMAIL_1);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                })))
                            .content(objectMapper.writeValueAsString(role))
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(status().isUnprocessableEntity())
                        .andReturn();
    }

    @Test
    @Transactional
    void givenInactiveUser_whenGettingInstitutionUsers_thenVerifyException() throws Exception {

        String url = String.format("/api/ext/institutions/%s/users", TEST_INSTITUTION_UUID_2);
        UserDto userDto = createTestUserDto("Jimmy", "Dale", TEST_NEW_USER_EMAIL, "N");

        // Insert the new inactive user.
        MvcResult result = mockMvc.perform(post(url)
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();

        // Attempt to get the list of institution users
        mockMvc.perform(get(url)
            .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                .jwt(jwt -> jwt.claims(claims -> {
                    claims.put("sub", TEST_NEW_USER_UUID);
                    claims.put("email", TEST_NEW_USER_EMAIL);
                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                }))))
            .andExpect(status().isUnauthorized());
    }

}