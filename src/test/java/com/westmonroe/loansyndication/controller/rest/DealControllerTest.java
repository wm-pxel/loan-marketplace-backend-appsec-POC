package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.deal.Deal;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureMockMvc
@Testcontainers
class DealControllerTest {

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
    void givenExistingDealsInDatabase_whenGettingAll_thenVerifySize() throws Exception {

        String url = String.format("/api/institutions/%s/deals", TEST_INSTITUTION_UUID_2);

        MvcResult result = mockMvc.perform(get(url)
                                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                            .jwt(jwt -> jwt.claims(claims -> {
                                                claims.put("sub", TEST_USER_UUID_1);
                                                claims.put("email", TEST_USER_EMAIL_1);
                                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                            }))))
                                    .andExpect(status().isOk())
                                    .andReturn();

        List<Deal> deals = objectMapper.readerForListOf(Deal.class).readValue(result.getResponse().getContentAsString());
        assertThat(deals).hasSize(1);
    }

    @Test
    void givenNoDealsInDatabase_whenRetrievingNonExistentDeal_thenVerifyException() throws Exception {

        String url = "/institutions/" + TEST_INSTITUTION_UUID_2 + "/api/deals/" + TEST_DEAL_UUID_2;

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

}