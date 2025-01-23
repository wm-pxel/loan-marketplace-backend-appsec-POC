package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.utils.ModelUtil;
import com.westmonroe.loansyndication.utils.TestConstants;
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
class InstitutionControllerTest {

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
    void givenExistingInstitutionsInDatabase_whenGettingForDealViewerUser_thenVerifyForbidden() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/institutions")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_2);
                                claims.put("email", TEST_USER_EMAIL_2);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                    .andExpect(status().isForbidden())
                    .andReturn();
    }

    @Test
    void givenExistingInstitutionsInDatabase_whenGettingForUnknownUser_thenVerifyUnauthorized() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/institutions")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_NEW_USER_UUID);
                                claims.put("email", TEST_NEW_USER_EMAIL);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                    .andExpect(status().isUnauthorized())
                    .andReturn();
    }

    @Test
    void givenExistingInstitutionsInDatabase_whenGettingAll_thenVerifySize() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/institutions")
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        }))))
                .andExpect(status().isOk())
                .andReturn();

        List<Institution> institutions = objectMapper.readerForListOf(Institution.class).readValue(result.getResponse().getContentAsString());
        assertThat(institutions).hasSize(4);
    }

    @Test
    void givenDealExternalId_whenGettingInstitution_thenVerify() throws Exception {

        // Deal External Id for Kentucky Processing Plant
        String url = "/api/deals/b86517b4-0693-4ec6-b880-06de4c0507f3/institutions";

        MvcResult result = mockMvc.perform(get(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                .andExpect(status().isOk())
                .andReturn();

        Institution institution = objectMapper.readValue(result.getResponse().getContentAsString(), Institution.class);

        assertThat(institution)
            .isNotNull()
            .hasFieldOrPropertyWithValue("uid", "df52a3a8-131c-4b3b-9eec-b7bd6f320270")
            .hasFieldOrPropertyWithValue("name", "AgFirst Farm Credit Bank")
            .hasFieldOrPropertyWithValue("owner", "Leon T. (Tim) Amerson")
            .hasFieldOrPropertyWithValue("dealCount", 4L)
            .hasFieldOrPropertyWithValue("memberCount", 5L);
    }

    @Test
    void givenExistingInstitutionsInDatabase_whenSavingNewInstitution_thenVerifyInsert() throws Exception {

        Institution institution = ModelUtil.createTestInstitution(1);

        // Insert the institution in the database.
        MvcResult result = mockMvc.perform(post("/api/institutions")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(objectMapper.writeValueAsString(institution))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isCreated())
                    .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).matches("/api/institutions/[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12}");

        // Get the uid for the previously inserted Institution.
        String institutionUid = location.replace("/api/institutions/", "");

        // Get all of the institutions in the database.
        result = mockMvc.perform(get("/api/institutions")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                    .andExpect(status().isOk())
                    .andReturn();

        List<Institution> institutions = objectMapper.readerForListOf(Institution.class).readValue(result.getResponse().getContentAsString());
        assertThat(institutions).hasSize(5);

        // Get the institution by its id.
        result = mockMvc.perform(get(location)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            }))))
                    .andExpect(status().isOk())
                    .andReturn();

        Institution savedInstitution = objectMapper.readValue(result.getResponse().getContentAsString(), Institution.class);
        assertThat(savedInstitution.getUid()).isEqualTo(institutionUid);
        assertThat(savedInstitution.getName()).isEqualTo(institution.getName());
    }

    @Test
    void givenNoInstitutionsInDatabase_whenRetrievingNonExistentInstitution_thenVerifyException() throws Exception {

        MvcResult result = mockMvc.perform(get("/api/institutions/1")
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
    void givenExistingInstitutionsInDatabase_whenDeletingInstitution_thenVerifyResult() throws Exception {

        String url = "/api/institutions/" + TEST_INSTITUTION_UUID_2;

        // Delete will always return NO_CONTENT, whether the institution exists or not.
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
    void givenCreatedInstitution_whenInstitutionUpdated_thenVerifyChange() throws Exception {

        Institution institution = ModelUtil.createTestInstitution(2);
        String updatedInstitutionName = "Test Institution 3";

        // Insert the institution in the database.
        MvcResult result = mockMvc.perform(post("/api/institutions")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(objectMapper.writeValueAsString(institution))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isCreated())
                    .andReturn();

        String location = result.getResponse().getHeader("Location");
        Institution savedInstitution = objectMapper.readValue(result.getResponse().getContentAsString(), Institution.class);

        // Change the institution name and update the institution in the database.
        institution.setUid(savedInstitution.getUid());
        institution.setName(updatedInstitutionName);
        result = mockMvc.perform(put(location)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(objectMapper.writeValueAsString(institution))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        // Get the institution by its id.
        result = mockMvc.perform(get(location)
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        }))))
                .andExpect(status().isOk())
                .andReturn();
        Institution updatedInstitution = objectMapper.readValue(result.getResponse().getContentAsString(), Institution.class);

        assertThat(savedInstitution.getUid()).isEqualTo(updatedInstitution.getUid());
        assertThat(savedInstitution.getName()).isNotEqualTo(updatedInstitution.getName());
        assertThat(updatedInstitution.getName()).isEqualTo(updatedInstitutionName);
    }

    @Test
    void givenInvalidInstitution_whenUpdating_thenVerifyException() throws Exception {

        Institution institution = ModelUtil.createTestInstitution(6);
        String url = "/api/institutions/" + TestConstants.TEST_INSTITUTION_UUID_2;

        // Perform an update without setting the UUID (i.e. the id) in the deal object.
        MvcResult result = mockMvc.perform(put(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(objectMapper.writeValueAsString(institution))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        // Make the path id and deal uid different in institution.
        institution.setUid(TestConstants.TEST_DEAL_UUID_2);
        result = mockMvc.perform(put(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(objectMapper.writeValueAsString(institution))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

}