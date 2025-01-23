package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.error.ErrorInfo;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;
import com.westmonroe.loansyndication.model.integration.*;
import com.westmonroe.loansyndication.service.UserService;
import com.westmonroe.loansyndication.service.deal.DealCovenantService;
import com.westmonroe.loansyndication.service.deal.DealFacilityService;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.event.EventDealFacilityService;
import com.westmonroe.loansyndication.service.event.EventLeadFacilityService;
import com.westmonroe.loansyndication.service.event.EventService;
import com.westmonroe.loansyndication.service.integration.DocumentBatchService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureMockMvc
@Testcontainers
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealService dealService;

    @Autowired
    private EventService eventService;

    @Autowired
    private DealFacilityService dealFacilityService;

    @Autowired
    private EventDealFacilityService eventDealFacilityService;

    @Autowired
    private EventLeadFacilityService eventLeadFacilityService;

    @Autowired
    private DealCovenantService dealCovenantService;

    @Autowired
    private DocumentBatchService documentBatchService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenNewDealFromIntegration_whenSavingAndRetrievingData_thenVerifyMarketplaceRecordsCreated() throws Exception {

        CustomerData customerData = new CustomerData();
        Payload payload = new Payload();

        MarketplaceData marketplaceData = new MarketplaceData();

        DealDto dealDto = new DealDto();
        dealDto.setDealExternalId("7fdcd233-7e5e-4bde-ba1a-869ca9be69ae");
        dealDto.setOriginatorId(TEST_INSTITUTION_UUID_1);                       // Farm Credit Bank of Texas
        dealDto.setName("Test Deal");
        dealDto.setDealIndustry("Farm Credit");
        dealDto.setDealStructure("Participation");
        dealDto.setDealType("New");
        dealDto.setDescription("This is an example deal");
        dealDto.setDealAmount(BigDecimal.valueOf(13000000.33));
        dealDto.setDefaultProbability(11);
        dealDto.setCurrYearEbita(BigDecimal.valueOf(2750000.77));

        ApplicantDto applicantDto = new ApplicantDto();
        applicantDto.setApplicantExternalId("97fbe0b9-4442-4292-bbf9-d50e133286fd");
        applicantDto.setBorrowerDesc("This is a sample description");
        applicantDto.setBorrowerName("Sammy Davis");
        applicantDto.setBorrowerCityName("Mount Vernon");
        applicantDto.setBorrowerStateCode("IL");
        applicantDto.setBorrowerCountyName("Jefferson County");
        applicantDto.setFarmCreditElig("Association Eligible");
        applicantDto.setTaxId("11-223344");
        applicantDto.setBorrowerIndustry("424930");
        applicantDto.setBusinessAge(19);

        DealCovenantDto covenantDto = new DealCovenantDto();
        covenantDto.setCovenantExternalId("b44ab8f3-b15b-4b82-afa6-5e1f1fe7dd93");
        covenantDto.setEntityName("Test Covenant 1");
        covenantDto.setCategoryName("Test Category");
        covenantDto.setCovenantType("Farm");
        covenantDto.setFrequency("Weekly");
        covenantDto.setNextEvalDate(LocalDate.parse("2024-04-20"));
        covenantDto.setEffectiveDate(LocalDate.parse("2024-04-01"));

        DealFacilityDto facilityDto = new DealFacilityDto();
        facilityDto.setFacilityExternalId("33393379-ef49-4359-98ef-3c7a037e1e05");
        facilityDto.setFacilityAmount(BigDecimal.valueOf(12570000.22));
        facilityDto.setFacilityType("Revolving Term Loan");
        facilityDto.setCollateral("Secured excluding real estate");
        facilityDto.setTenor(33);
        facilityDto.setPricing("SOFR + 140.0bps");
        facilityDto.setCreditSpreadAdj("Test CSA");
        facilityDto.setFacilityPurpose("New Construction");
        facilityDto.setPurposeDetail("Same detail text");
        facilityDto.setDayCount("Actual/365");
        facilityDto.setRegulatoryLoanType("Agribusiness - Loans to Cooperatives");
        facilityDto.setGuarInvFlag("N");
        facilityDto.setPatronagePayingFlag("N");
        facilityDto.setFarmCreditType("FLCA");
        facilityDto.setRevolverUtil(null);
        facilityDto.setUpfrontFees("lots of fees");
        facilityDto.setUnusedFees("not unused");
        facilityDto.setAmortization("test amortization");
        facilityDto.setLgdOption("C");

        marketplaceData.setDeal(dealDto);
        marketplaceData.setApplicants(Arrays.asList(applicantDto));
        marketplaceData.setCovenants(Arrays.asList(covenantDto));
        marketplaceData.setFacilities(Arrays.asList(facilityDto));

        payload.setMarketplaceData(marketplaceData);

        String unsupportedJson = """
                {
                    "trackedChanges":[{
                        "fieldType":"applicant",
                        "field": "applicationRbcLabel",
                        "date": "1900-01-01"
                    },{
                        "fieldType":"collateral",
                        "field": "applicationRbcLabel"
                    },{
                        "fieldType":"facilities",
                        "field": "applicationRbcLabel"
                    }],
                    "applicants":[{
                        "applicantExternalId": 2147483647,
                        "rbcLabel" : "rbcValue"
                    }],
                    "collateral":[{
                        "collateralExternalId": 2147483647,
                          "rbcLabel" : "rbcValue"
                    }],
                    "covenants":[{
                        "covenantExternalId": 2147483647,
                        "rbcLabel" : "rbcValue"
                    }]
                }
                """;
        Map<String, Object> unsupportedData = objectMapper.readValue(unsupportedJson, Map.class);

        payload.setUnsupportedData(unsupportedData);
        customerData.setPayload(payload);

        // Insert the test customer data from the integration endpoint.
        MvcResult result = mockMvc.perform(post("/api/ext/data")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_2);
                                    claims.put("email", TEST_USER_EMAIL_2);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                })))
                        .content(objectMapper.writeValueAsString(customerData))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andReturn();

        User currentUser = userService.getUserByUid(TEST_USER_UUID_2);

        /*
         *  Verify the marketplace records were created.
         */
        Deal deal = dealService.getDealByExternalId(dealDto.getDealExternalId(), currentUser);

        assertThat(deal)
            .hasFieldOrPropertyWithValue("name", dealDto.getName())
            .hasFieldOrPropertyWithValue("description", dealDto.getDescription())
            .hasFieldOrPropertyWithValue("dealType", dealDto.getDealType())
            .hasFieldOrPropertyWithValue("dealAmount", dealDto.getDealAmount())
            .hasFieldOrPropertyWithValue("defaultProbability", dealDto.getDefaultProbability())
            .hasFieldOrPropertyWithValue("currYearEbita", dealDto.getCurrYearEbita())
            .hasFieldOrPropertyWithValue("applicantExternalId", applicantDto.getApplicantExternalId())
            .hasFieldOrPropertyWithValue("borrowerDesc", applicantDto.getBorrowerDesc())
            .hasFieldOrPropertyWithValue("borrowerName", applicantDto.getBorrowerName())
            .hasFieldOrPropertyWithValue("borrowerCityName", applicantDto.getBorrowerCityName())
            .hasFieldOrPropertyWithValue("borrowerStateCode", applicantDto.getBorrowerStateCode())
            .hasFieldOrPropertyWithValue("borrowerCountyName", applicantDto.getBorrowerCountyName())
            .hasFieldOrPropertyWithValue("taxId", applicantDto.getTaxId())
            .hasFieldOrPropertyWithValue("businessAge", applicantDto.getBusinessAge());
        assertThat(deal.getDealIndustry().getOption()).isEqualTo(dealDto.getDealIndustry());
        assertThat(deal.getDealStructure().getOption()).isEqualTo(dealDto.getDealStructure());
        assertThat(deal.getFarmCreditElig().getOption()).isEqualTo(applicantDto.getFarmCreditElig());
        assertThat(deal.getBorrowerIndustry().getCode()).isEqualTo(applicantDto.getBorrowerIndustry());

        /*
         *  Verify that data was saved to marketplace Deal Facility table.
         */
        List<DealFacility> facilities = dealFacilityService.getFacilitiesForDeal(deal.getUid());

        assertThat(facilities)
            .isNotNull()
            .hasSize(1);
        assertThat(facilities.get(0))
            .hasFieldOrPropertyWithValue("facilityExternalId", facilityDto.getFacilityExternalId())
            .hasFieldOrPropertyWithValue("facilityName", "Facility A")
            .hasFieldOrPropertyWithValue("facilityAmount", facilityDto.getFacilityAmount())
            .hasFieldOrPropertyWithValue("tenor", facilityDto.getTenor())
            .hasFieldOrPropertyWithValue("pricing", facilityDto.getPricing())
            .hasFieldOrPropertyWithValue("creditSpreadAdj", facilityDto.getCreditSpreadAdj())
            .hasFieldOrPropertyWithValue("purposeDetail", facilityDto.getPurposeDetail())
            .hasFieldOrPropertyWithValue("guarInvFlag", facilityDto.getGuarInvFlag())
            .hasFieldOrPropertyWithValue("patronagePayingFlag", facilityDto.getPatronagePayingFlag())
            .hasFieldOrPropertyWithValue("farmCreditType", facilityDto.getFarmCreditType())
            .hasFieldOrPropertyWithValue("revolverUtil", facilityDto.getRevolverUtil())
            .hasFieldOrPropertyWithValue("upfrontFees", facilityDto.getUpfrontFees())
            .hasFieldOrPropertyWithValue("unusedFees", facilityDto.getUnusedFees())
            .hasFieldOrPropertyWithValue("amortization", facilityDto.getAmortization())
            .hasFieldOrPropertyWithValue("lgdOption", facilityDto.getLgdOption());
        assertThat(facilities.get(0).getFacilityType().getOption()).isEqualTo(facilityDto.getFacilityType());
        assertThat(facilities.get(0).getCollateral().getOption()).isEqualTo(facilityDto.getCollateral());
        assertThat(facilities.get(0).getFacilityPurpose().getOption()).isEqualTo(facilityDto.getFacilityPurpose());
        assertThat(facilities.get(0).getDayCount().getOption()).isEqualTo(facilityDto.getDayCount());
        assertThat(facilities.get(0).getRegulatoryLoanType().getOption()).isEqualTo(facilityDto.getRegulatoryLoanType());

        /*
         *  Verify that data was saved to marketplace Deal Covenant table.
         */
        List<DealCovenant> covenants = dealCovenantService.getCovenantsForDeal(deal.getUid());

        assertThat(covenants)
            .isNotNull()
            .hasSize(1);
        assertThat(covenants.get(0))
            .hasFieldOrPropertyWithValue("covenantExternalId", covenantDto.getCovenantExternalId())
            .hasFieldOrPropertyWithValue("entityName", covenantDto.getEntityName())
            .hasFieldOrPropertyWithValue("categoryName", covenantDto.getCategoryName())
            .hasFieldOrPropertyWithValue("covenantType", covenantDto.getCovenantType())
            .hasFieldOrPropertyWithValue("frequency", covenantDto.getFrequency())
            .hasFieldOrPropertyWithValue("nextEvalDate", covenantDto.getNextEvalDate())
            .hasFieldOrPropertyWithValue("effectiveDate", covenantDto.getEffectiveDate());
    }

    @Test
    void givenNewDealFromIntegration_whenSavingAndUpdatingData_thenVerifyDataInMarketplace() throws Exception {

        byte[] customerDataJson = new ClassPathResource("customerdata-new-valid.json").getInputStream().readAllBytes();
        CustomerData customerData = objectMapper.readValue(customerDataJson, CustomerData.class);

        // Insert the test customer data from the integration endpoint.
        MvcResult result = mockMvc.perform(post("/api/ext/data")
                                .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                                    .jwt(jwt -> jwt.claims(claims -> {
                                        claims.put("sub", TEST_USER_UUID_2);
                                        claims.put("email", TEST_USER_EMAIL_2);
                                        claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                    })))
                                .content(customerDataJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isCreated())
                            .andReturn();

        CustomerData customerDataResult = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerData.class);

        // Get the components from customer data for testing.
        DealDto dealDto = customerDataResult.getPayload().getMarketplaceData().getDeal();
        ApplicantDto applicantDto = customerDataResult.getPayload().getMarketplaceData().getApplicants().get(0);
        List<DealCovenantDto> covenantDtos = customerDataResult.getPayload().getMarketplaceData().getCovenants();
        List<DealFacilityDto> facilityDtos = customerDataResult.getPayload().getMarketplaceData().getFacilities();

        User currentUser = userService.getUserByUid(TEST_USER_UUID_2);

        /*
         *  Verify the marketplace records were created.
         */
        Deal deal = dealService.getDealByExternalId(dealDto.getDealExternalId(), currentUser);

        assertThat(deal)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", dealDto.getName())
            .hasFieldOrPropertyWithValue("description", dealDto.getDescription())
            .hasFieldOrPropertyWithValue("dealAmount", dealDto.getDealAmount())
            .hasFieldOrPropertyWithValue("defaultProbability", dealDto.getDefaultProbability())
            .hasFieldOrPropertyWithValue("currYearEbita", dealDto.getCurrYearEbita())
            .hasFieldOrPropertyWithValue("applicantExternalId", applicantDto.getApplicantExternalId())
            .hasFieldOrPropertyWithValue("borrowerDesc", applicantDto.getBorrowerDesc())
            .hasFieldOrPropertyWithValue("borrowerName", applicantDto.getBorrowerName())
            .hasFieldOrPropertyWithValue("borrowerCityName", applicantDto.getBorrowerCityName())
            .hasFieldOrPropertyWithValue("borrowerStateCode", applicantDto.getBorrowerStateCode())
            .hasFieldOrPropertyWithValue("borrowerCountyName", applicantDto.getBorrowerCountyName())
            .hasFieldOrPropertyWithValue("taxId", applicantDto.getTaxId())
            .hasFieldOrPropertyWithValue("businessAge", applicantDto.getBusinessAge());
        assertThat(deal.getDealIndustry().getOption()).isEqualTo(dealDto.getDealIndustry());
        assertThat(deal.getDealStructure().getOption()).isEqualTo(dealDto.getDealStructure());
        assertThat(deal.getFarmCreditElig().getOption()).isEqualTo(applicantDto.getFarmCreditElig());
        assertThat(deal.getBorrowerIndustry().getCode()).isEqualTo(applicantDto.getBorrowerIndustry());

        customerDataJson = new ClassPathResource("customerdata-update-valid.json").getInputStream().readAllBytes();

        // Update the test customer data from the integration endpoint.
        mockMvc.perform(patch("/api/ext/data")
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_2);
                            claims.put("email", TEST_USER_EMAIL_2);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .content(customerDataJson)
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        // Get the test customer data updated from the integration endpoint.
        result = mockMvc.perform(get("/api/ext/data/".concat(deal.getDealExternalId()))
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_2);
                                claims.put("email", TEST_USER_EMAIL_2);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        customerDataResult = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerData.class);

        // Get the components from customer data for testing.
        dealDto = customerDataResult.getPayload().getMarketplaceData().getDeal();
        applicantDto = customerDataResult.getPayload().getMarketplaceData().getApplicants().get(0);
        covenantDtos = customerDataResult.getPayload().getMarketplaceData().getCovenants();
        facilityDtos = customerDataResult.getPayload().getMarketplaceData().getFacilities();

        /*
         *  Verify the marketplace records were updated.
         */
        Deal updatedDeal = dealService.getDealByExternalId(dealDto.getDealExternalId(), currentUser);

        assertThat(updatedDeal)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", dealDto.getName())
            .hasFieldOrPropertyWithValue("description", dealDto.getDescription())
            .hasFieldOrPropertyWithValue("dealAmount", dealDto.getDealAmount())
            .hasFieldOrPropertyWithValue("defaultProbability", dealDto.getDefaultProbability())
            .hasFieldOrPropertyWithValue("currYearEbita", dealDto.getCurrYearEbita())
            .hasFieldOrPropertyWithValue("applicantExternalId", applicantDto.getApplicantExternalId())
            .hasFieldOrPropertyWithValue("borrowerDesc", applicantDto.getBorrowerDesc())
            .hasFieldOrPropertyWithValue("borrowerName", applicantDto.getBorrowerName())
            .hasFieldOrPropertyWithValue("borrowerCityName", applicantDto.getBorrowerCityName())
            .hasFieldOrPropertyWithValue("borrowerStateCode", applicantDto.getBorrowerStateCode())
            .hasFieldOrPropertyWithValue("borrowerCountyName", applicantDto.getBorrowerCountyName())
            .hasFieldOrPropertyWithValue("taxId", applicantDto.getTaxId())
            .hasFieldOrPropertyWithValue("businessAge", applicantDto.getBusinessAge());
        assertThat(updatedDeal.getDealIndustry().getOption()).isEqualTo(dealDto.getDealIndustry());
        assertThat(updatedDeal.getDealStructure().getOption()).isEqualTo(dealDto.getDealStructure());
        assertThat(updatedDeal.getFarmCreditElig().getOption()).isEqualTo(applicantDto.getFarmCreditElig());
        assertThat(updatedDeal.getBorrowerIndustry().getCode()).isEqualTo(applicantDto.getBorrowerIndustry());

        /*
         *  Verify that data was saved to marketplace Deal Facility table.
         */
        List<DealFacility> facilities = dealFacilityService.getFacilitiesForDeal(deal.getUid());

        assertThat(facilities)
            .isNotNull()
            .hasSize(3);

        // Verify the facility was updated.
        DealFacility dealFacility = facilities
            .stream()
            .filter(df -> df.getFacilityExternalId().equals("33393379-ef49-4359-98ef-3c7a037e1e05"))
            .findAny()
            .orElse(null);
        DealFacilityDto dealFacilityDto = facilityDtos
            .stream()
            .filter(df -> df.getFacilityExternalId().equals("33393379-ef49-4359-98ef-3c7a037e1e05"))
            .findAny()
            .orElse(null);

        assertThat(dealFacility)
            .hasFieldOrPropertyWithValue("facilityExternalId", dealFacilityDto.getFacilityExternalId())
            .hasFieldOrPropertyWithValue("facilityName", "Facility A")
            .hasFieldOrPropertyWithValue("facilityAmount", dealFacilityDto.getFacilityAmount())
            .hasFieldOrPropertyWithValue("tenor", dealFacilityDto.getTenor())
            .hasFieldOrPropertyWithValue("pricing", dealFacilityDto.getPricing())
            .hasFieldOrPropertyWithValue("creditSpreadAdj", dealFacilityDto.getCreditSpreadAdj())
            .hasFieldOrPropertyWithValue("purposeDetail", dealFacilityDto.getPurposeDetail())
            .hasFieldOrPropertyWithValue("guarInvFlag", dealFacilityDto.getGuarInvFlag())
            .hasFieldOrPropertyWithValue("patronagePayingFlag", dealFacilityDto.getPatronagePayingFlag())
            .hasFieldOrPropertyWithValue("farmCreditType", dealFacilityDto.getFarmCreditType())
            .hasFieldOrPropertyWithValue("revolverUtil", dealFacilityDto.getRevolverUtil())
            .hasFieldOrPropertyWithValue("upfrontFees", dealFacilityDto.getUpfrontFees())
            .hasFieldOrPropertyWithValue("unusedFees", dealFacilityDto.getUnusedFees())
            .hasFieldOrPropertyWithValue("amortization", dealFacilityDto.getAmortization())
            .hasFieldOrPropertyWithValue("lgdOption", dealFacilityDto.getLgdOption());
        assertThat(dealFacility.getFacilityType().getOption()).isEqualTo(dealFacilityDto.getFacilityType());
        assertThat(dealFacility.getCollateral().getOption()).isEqualTo(dealFacilityDto.getCollateral());
        assertThat(dealFacility.getFacilityPurpose().getOption()).isEqualTo(dealFacilityDto.getFacilityPurpose());
        assertThat(dealFacility.getDayCount().getOption()).isEqualTo(dealFacilityDto.getDayCount());
        assertThat(dealFacility.getRegulatoryLoanType().getOption()).isEqualTo(dealFacilityDto.getRegulatoryLoanType());

        // Verify the facility was created.
        dealFacility = facilities
            .stream()
            .filter(df -> df.getFacilityExternalId().equals("33393379-ef49-4359-98ef-3c7a037e1e06"))
            .findAny()
            .orElse(null);
        dealFacilityDto = facilityDtos
            .stream()
            .filter(df -> df.getFacilityExternalId().equals("33393379-ef49-4359-98ef-3c7a037e1e06"))
            .findAny()
            .orElse(null);

        assertThat(dealFacility)
            .hasFieldOrPropertyWithValue("facilityExternalId", dealFacilityDto.getFacilityExternalId())
            .hasFieldOrPropertyWithValue("facilityName", "Facility B")
            .hasFieldOrPropertyWithValue("facilityAmount", dealFacilityDto.getFacilityAmount())
            .hasFieldOrPropertyWithValue("tenor", dealFacilityDto.getTenor())
            .hasFieldOrPropertyWithValue("pricing", dealFacilityDto.getPricing())
            .hasFieldOrPropertyWithValue("creditSpreadAdj", dealFacilityDto.getCreditSpreadAdj())
            .hasFieldOrPropertyWithValue("purposeDetail", dealFacilityDto.getPurposeDetail())
            .hasFieldOrPropertyWithValue("guarInvFlag", dealFacilityDto.getGuarInvFlag())
            .hasFieldOrPropertyWithValue("patronagePayingFlag", dealFacilityDto.getPatronagePayingFlag())
            .hasFieldOrPropertyWithValue("farmCreditType", dealFacilityDto.getFarmCreditType())
            .hasFieldOrPropertyWithValue("revolverUtil", dealFacilityDto.getRevolverUtil())
            .hasFieldOrPropertyWithValue("upfrontFees", dealFacilityDto.getUpfrontFees())
            .hasFieldOrPropertyWithValue("unusedFees", dealFacilityDto.getUnusedFees())
            .hasFieldOrPropertyWithValue("amortization", dealFacilityDto.getAmortization())
            .hasFieldOrPropertyWithValue("lgdOption", dealFacilityDto.getLgdOption());
        assertThat(dealFacility.getFacilityType().getOption()).isEqualTo(dealFacilityDto.getFacilityType());
        assertThat(dealFacility.getCollateral().getOption()).isEqualTo(dealFacilityDto.getCollateral());
        assertThat(dealFacility.getFacilityPurpose().getOption()).isEqualTo(dealFacilityDto.getFacilityPurpose());
        assertThat(dealFacility.getDayCount().getOption()).isEqualTo(dealFacilityDto.getDayCount());
        assertThat(dealFacility.getRegulatoryLoanType().getOption()).isEqualTo(dealFacilityDto.getRegulatoryLoanType());

        /*
         *  Verify that data was saved to marketplace Deal Covenant table.
         */
        List<DealCovenant> covenants = dealCovenantService.getCovenantsForDeal(deal.getUid());

        assertThat(covenants)
            .isNotNull()
            .hasSize(3);

        // Verify the covenant was updated.
        DealCovenant dealCovenant = covenants
            .stream()
            .filter(dc -> dc.getCovenantExternalId().equals("b44ab8f3-b15b-4b82-afa6-5e1f1fe7dd93"))
            .findAny()
            .orElse(null);
        DealCovenantDto dealCovenantDto = covenantDtos
            .stream()
            .filter(dc -> dc.getCovenantExternalId().equals("b44ab8f3-b15b-4b82-afa6-5e1f1fe7dd93"))
            .findAny()
            .orElse(null);

        assertThat(dealCovenant)
            .hasFieldOrPropertyWithValue("covenantExternalId", dealCovenantDto.getCovenantExternalId())
            .hasFieldOrPropertyWithValue("entityName", dealCovenantDto.getEntityName())
            .hasFieldOrPropertyWithValue("categoryName", dealCovenantDto.getCategoryName())
            .hasFieldOrPropertyWithValue("covenantType", dealCovenantDto.getCovenantType())
            .hasFieldOrPropertyWithValue("frequency", dealCovenantDto.getFrequency())
            .hasFieldOrPropertyWithValue("nextEvalDate", dealCovenantDto.getNextEvalDate())
            .hasFieldOrPropertyWithValue("effectiveDate", dealCovenantDto.getEffectiveDate());

        // Verify the covenant was created.
        dealCovenant = covenants
            .stream()
            .filter(dc -> dc.getCovenantExternalId().equals("b44ab8f3-b15b-4b82-afa6-5e1f1fe7dd94"))
            .findAny()
            .orElse(null);
        dealCovenantDto = covenantDtos
            .stream()
            .filter(dc -> dc.getCovenantExternalId().equals("b44ab8f3-b15b-4b82-afa6-5e1f1fe7dd94"))
            .findAny()
            .orElse(null);

        assertThat(dealCovenant)
            .hasFieldOrPropertyWithValue("covenantExternalId", dealCovenantDto.getCovenantExternalId())
            .hasFieldOrPropertyWithValue("entityName", dealCovenantDto.getEntityName())
            .hasFieldOrPropertyWithValue("categoryName", dealCovenantDto.getCategoryName())
            .hasFieldOrPropertyWithValue("covenantType", dealCovenantDto.getCovenantType())
            .hasFieldOrPropertyWithValue("frequency", dealCovenantDto.getFrequency())
            .hasFieldOrPropertyWithValue("nextEvalDate", dealCovenantDto.getNextEvalDate())
            .hasFieldOrPropertyWithValue("effectiveDate", dealCovenantDto.getEffectiveDate());
    }

    @Test
    void givenNewDealFromIntegration_whenSavingAndDeletingData_thenVerifyDataInMarketplace() throws Exception {

        byte[] customerDataJson = new ClassPathResource("customerdata-new-valid.json").getInputStream().readAllBytes();
        CustomerData customerData = objectMapper.readValue(customerDataJson, CustomerData.class);

        // Insert the test customer data from the integration endpoint.
        MvcResult result = mockMvc.perform(post("/api/ext/data")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_2);
                                claims.put("email", TEST_USER_EMAIL_2);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(customerDataJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isCreated())
                    .andReturn();

        CustomerData customerDataResult = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerData.class);

        // Get the components from customer data for testing.
        DealDto dealDto = customerDataResult.getPayload().getMarketplaceData().getDeal();
        ApplicantDto applicantDto = customerDataResult.getPayload().getMarketplaceData().getApplicants().get(0);
        EventDto eventDto = customerDataResult.getPayload().getMarketplaceData().getEvent();

        User currentUser = userService.getUserByUid(TEST_USER_UUID_2);

        /*
         *  Verify the marketplace records were created.
         */
        Deal deal = dealService.getDealByExternalId(dealDto.getDealExternalId(), currentUser);

        assertThat(deal)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", dealDto.getName())
            .hasFieldOrPropertyWithValue("description", dealDto.getDescription())
            .hasFieldOrPropertyWithValue("dealAmount", dealDto.getDealAmount())
            .hasFieldOrPropertyWithValue("defaultProbability", dealDto.getDefaultProbability())
            .hasFieldOrPropertyWithValue("currYearEbita", dealDto.getCurrYearEbita())
            .hasFieldOrPropertyWithValue("applicantExternalId", applicantDto.getApplicantExternalId())
            .hasFieldOrPropertyWithValue("borrowerDesc", applicantDto.getBorrowerDesc())
            .hasFieldOrPropertyWithValue("borrowerName", applicantDto.getBorrowerName())
            .hasFieldOrPropertyWithValue("borrowerCityName", applicantDto.getBorrowerCityName())
            .hasFieldOrPropertyWithValue("borrowerStateCode", applicantDto.getBorrowerStateCode())
            .hasFieldOrPropertyWithValue("borrowerCountyName", applicantDto.getBorrowerCountyName())
            .hasFieldOrPropertyWithValue("taxId", applicantDto.getTaxId())
            .hasFieldOrPropertyWithValue("businessAge", applicantDto.getBusinessAge());
        assertThat(deal.getDealIndustry().getOption()).isEqualTo(dealDto.getDealIndustry());
        assertThat(deal.getDealStructure().getOption()).isEqualTo(dealDto.getDealStructure());
        assertThat(deal.getFarmCreditElig().getOption()).isEqualTo(applicantDto.getFarmCreditElig());
        assertThat(deal.getBorrowerIndustry().getCode()).isEqualTo(applicantDto.getBorrowerIndustry());

        /*
         *  Verify that data was saved to marketplace Event table.
         */
        Event event = eventService.getEventByExternalId(eventDto.getEventExternalId());

        assertThat(event)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", eventDto.getName())
            .hasFieldOrPropertyWithValue("projectedLaunchDate", eventDto.getProjectedLaunchDate())
            .hasFieldOrPropertyWithValue("effectiveDate", eventDto.getEffectiveDate())
            .hasFieldOrPropertyWithValue("projectedCloseDate", eventDto.getProjectedCloseDate());
        assertThat(event.getEventType().getName()).isEqualTo(eventDto.getEventType());

        /*
         *  Verify that data was saved to marketplace Deal Covenant table.
         */
        List<DealCovenant> covenants = dealCovenantService.getCovenantsForDeal(deal.getUid());

        assertThat(covenants)
            .isNotNull()
            .hasSize(2);

        /*
         *  Verify that data was saved to marketplace Deal Facility table.
         */
        List<DealFacility> facilities = dealFacilityService.getFacilitiesForDeal(deal.getUid());

        assertThat(facilities)
            .isNotNull()
            .hasSize(2);

        /*
         *  Verify that data was saved to marketplace Event Lead Facility table.
         */
        List<EventLeadFacility> eventLeadFacilities = eventLeadFacilityService.getEventLeadFacilitiesByEventId(event.getId());

        assertThat(eventLeadFacilities)
            .isNotNull()
            .hasSize(2);

        /*
         *  Verify that data was saved to marketplace Event Deal Facility table.  NOTE: Is only automatic for ORIGINATION
         */
        List<EventDealFacility> eventDealFacilities = eventDealFacilityService.getEventDealFacilitiesForEvent(event.getUid());

        assertThat(eventDealFacilities)
            .isNotNull()
            .hasSize(2);

        customerDataJson = new ClassPathResource("customerdata-delete-covenants-and-facilities-valid.json").getInputStream().readAllBytes();

        // Update the test customer data from the integration endpoint.  NOTE: Deal UID is replaced
        result = mockMvc.perform(delete("/api/ext/data")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_2);
                                claims.put("email", TEST_USER_EMAIL_2);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(customerDataJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andReturn();

        /*
         *  Verify that data was deleted from marketplace Deal Facility table.
         */
        facilities = dealFacilityService.getFacilitiesForDeal(deal.getUid());

        assertThat(facilities).isEmpty();

        /*
         *  Verify that data was deleted from marketplace Deal Covenant table.
         */
        covenants = dealCovenantService.getCovenantsForDeal(deal.getUid());

        assertThat(covenants).isEmpty();
    }
    
    @Test
    void givenExistingDeals_whenParticipantRetrievesDealList_thenVerify() throws Exception {

        String url = "/api/ext/participants/df52a3a8-131c-4b3b-9eec-b7bd6f320270/deals";    // AgFirst Farm Credit Bank
        String userUid = "3aa836ce-5c8d-466c-b644-d7c6a9f9db34";                            // Chris Lender
        String email = "Chris.Lender@test.com";

        // Happy path ... user is in the institution that they are requesting deal data .
        MvcResult result = mockMvc.perform(get(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", userUid);
                                    claims.put("email", email);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                }))))
                        .andExpect(status().isOk())
                        .andReturn();

        List<DealData> dealDataList = objectMapper.readerForListOf(DealData.class).readValue(result.getResponse().getContentAsString());
        assertThat(dealDataList).hasSize(3);
    }

    @Test
    void givenExistingDeals_whenInvalidParticipantRetrievesDealList_thenVerifyException() throws Exception {

        String url = "/api/ext/participants/df52a3a8-131c-4b3b-9eec-b7bd6f320270/deals";    // AgFirst Farm Credit Bank
        String userUid = "0a5a099b-ee01-4e34-81a5-91421bb1a104";                            // Benjamin Bucks
        String email = "Benjamin.Bucks@test.com";

        // Exception ... user is NOT in the institution that they are requesting deal data .
        MvcResult result = mockMvc.perform(get(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ACCESS_ALL_INST_DEALS")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", userUid);
                                    claims.put("email", email);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                }))))
                        .andExpect(status().isForbidden())
                        .andReturn();
    }

    @Test
    void givenNewDocumentBatchFromIntegration_whenSavingAndRetrieving_thenVerifyDataInMarketplace() throws Exception {

        byte[] documentBatchJson = new ClassPathResource("documentbatch-valid.json").getInputStream().readAllBytes();
        DocumentBatch documentBatch = objectMapper.readValue(documentBatchJson, DocumentBatch.class);

        // Insert the test document batch using an integration endpoint.
        MvcResult result = mockMvc.perform(post("/api/ext/data/documents")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(documentBatchJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isCreated())
                    .andReturn();

        DocumentBatch documentBatchResult = objectMapper.readValue(result.getResponse().getContentAsString(), DocumentBatch.class);

        assertThat(documentBatchResult.getDetails())
            .isNotNull()
            .hasSize(4);

        // Retrieve the test document batch using an integration endpoint.
        result = mockMvc.perform(get("/api/ext/data/documents/".concat(documentBatchResult.getId().toString()))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_1);
                            claims.put("email", TEST_USER_EMAIL_1);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        DocumentBatch documentBatchSaved = objectMapper.readValue(result.getResponse().getContentAsString(), DocumentBatch.class);

        assertThat(documentBatchSaved.getDetails())
            .isNotNull()
            .hasSize(4);

        // Verify every document batch detail record.
        documentBatch.getDetails().stream().forEach(detail -> {

            // Get the associated saved document batch detail record.
            DocumentBatchDetail savedDetail = documentBatchSaved.getDetails().stream()
                .filter(d -> d.getDocumentExternalId().equals(detail.getDocumentExternalId()))
                .findFirst()
                .orElseThrow(DataNotFoundException::new);

            assertThat(detail)
                .hasFieldOrPropertyWithValue("documentExternalId", savedDetail.getDocumentExternalId())
                .hasFieldOrPropertyWithValue("url", savedDetail.getUrl())
                .hasFieldOrPropertyWithValue("extension", savedDetail.getExtension())
                .hasFieldOrPropertyWithValue("displayName", savedDetail.getDisplayName())
                .hasFieldOrPropertyWithValue("category", savedDetail.getCategory());
        });
    }

    @Test
    void givenInvalidNewDocumentBatchFromIntegration_whenSaving_thenVerifyErrors() throws Exception {

        byte[] documentBatchJson = new ClassPathResource("documentbatch-invalid.json").getInputStream().readAllBytes();
        DocumentBatch documentBatch = objectMapper.readValue(documentBatchJson, DocumentBatch.class);

        // Insert the test document batch using an integration endpoint.
        MvcResult result = mockMvc.perform(post("/api/ext/data/documents")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("SUPER_ADM")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(documentBatchJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn();

        ErrorInfo errorInfo = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorInfo.class);

        assertThat(errorInfo)
            .isNotNull();
        assertThat(errorInfo.getErrors())
            .isNotNull()
            .hasSize(9);
    }

    @Test
    void givenNewDealDocumentInLambda_whenSavingDocumentInLamina_thenVerify() throws Exception {

        String dealExternalId = "d1893fb4-09bf-4b8b-8c8a-81f1e8e809f3";         // Texas Dairy Farm
        String documentExternalId = "27832873-b778-4884-aa12-bdff93f18406";     // Unique external from nCino
        String url = String.format("/api/ext/deals/%s/documents", dealExternalId);

        DealDocumentDto documentDto = new DealDocumentDto();
        documentDto.setDocumentExternalId(documentExternalId);
        documentDto.setUrl("https://wmpfinserv--lmdev.my.salesforce.com/services/data/v56.0/sobjects/ContentVersion/0688C000001GQhpQAG/VersionData");
        documentDto.setDisplayName("Test Document.pdf");
        documentDto.setDocumentName("20240722080827.pdf");
        documentDto.setType("application/pdf");
        documentDto.setExtension("pdf");
        documentDto.setCategory("Collateral");
        documentDto.setCreatedById(4L);

        // Create the document record as we would from the Lambda (DocumentDto).
        MvcResult result = mockMvc.perform(post(url)
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("email", TEST_USER_EMAIL_6);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(objectMapper.writeValueAsString(documentDto))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isCreated())
                    .andReturn();

        // Get the DocumentDto from the Lamina endpoint.
        result = mockMvc.perform(get(url.concat("/").concat(documentExternalId))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("email", TEST_USER_EMAIL_6);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        }))))
                .andExpect(status().isOk())
                .andReturn();

        DealDocumentDto documentResult = objectMapper.readValue(result.getResponse().getContentAsString(), DealDocumentDto.class);

        assertThat(documentResult)
            .isNotNull()
            .hasFieldOrPropertyWithValue("documentExternalId", documentDto.getDocumentExternalId())
            .hasFieldOrPropertyWithValue("displayName", documentDto.getDisplayName())
            .hasFieldOrPropertyWithValue("category", documentDto.getCategory())
            .hasFieldOrPropertyWithValue("createdById", 4L);
    }

    @Test
    void givenNewDocumentBatchFromIntegration_whenEmulatingTheBatchProcessingFromLambda_thenVerifyProcessDates() throws Exception {

        byte[] documentBatchJson = new ClassPathResource("documentbatch-valid.json").getInputStream().readAllBytes();
        DocumentBatch documentBatch = objectMapper.readValue(documentBatchJson, DocumentBatch.class);

        // Insert the test document batch using an integration endpoint.
        MvcResult result = mockMvc.perform(post("/api/ext/data/documents")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_1);
                                claims.put("email", TEST_USER_EMAIL_1);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .content(documentBatchJson)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isCreated())
                    .andReturn();

        DocumentBatch documentBatchResult = objectMapper.readValue(result.getResponse().getContentAsString(), DocumentBatch.class);

        assertThat(documentBatchResult.getDetails())
            .isNotNull()
            .hasSize(4);

        // Emulate the start of the batch processing from an integration endpoint.
        mockMvc.perform(patch(String.format("/api/ext/data/documents/%d/start", documentBatchResult.getId()))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_6);
                            claims.put("email", TEST_USER_EMAIL_6);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        // Loop through the first three documents and update the dates.
        for (int index = 0; index < 3; index++) {

            String url = String.format("/api/ext/data/documents/%d/details/%d/", documentBatchResult.getId(), documentBatchResult.getDetails().get(index).getId());

            // Emulate the start of the file processing from an integration endpoint.
            mockMvc.perform(patch(url.concat("start"))
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_6);
                                claims.put("email", TEST_USER_EMAIL_6);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk());

            // Emulate the completion of the file processing from an integration endpoint.
            mockMvc.perform(patch(url.concat("complete"))
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                            .jwt(jwt -> jwt.claims(claims -> {
                                claims.put("sub", TEST_USER_UUID_6);
                                claims.put("email", TEST_USER_EMAIL_6);
                                claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                            })))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk());

        }

        // Emulate the completion of the batch processing from an integration endpoint.  Note: Will not update the end date when all documents are not complete.
        mockMvc.perform(patch(String.format("/api/ext/data/documents/%d/complete", documentBatchResult.getId()))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_6);
                            claims.put("email", TEST_USER_EMAIL_6);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        // Get the batch processing from an integration endpoint.
        result = mockMvc.perform(get(String.format("/api/ext/data/documents/%d", documentBatchResult.getId()))
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_6);
                                    claims.put("email", TEST_USER_EMAIL_6);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                })))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        DocumentBatch updatedDocumentBatch = objectMapper.readValue(result.getResponse().getContentAsString(), DocumentBatch.class);

        assertThat(updatedDocumentBatch.getProcessEndDate()).isNull();

        /*
         *  Get and update the detail record that wasn't processed.
         */

        DocumentBatchDetail detail = updatedDocumentBatch.getDetails().stream().filter(d -> d.getProcessStartDate() == null).findFirst().get();
        String url = String.format("/api/ext/data/documents/%d/details/%d/", documentBatchResult.getId(), detail.getId());

        // Emulate the start of the batch processing from an integration endpoint.
        mockMvc.perform(patch(url.concat("start"))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_6);
                            claims.put("email", TEST_USER_EMAIL_6);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        // Emulate the start of the batch processing from an integration endpoint.
        mockMvc.perform(patch(url.concat("complete"))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_6);
                            claims.put("email", TEST_USER_EMAIL_6);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        // Emulate the completion of the batch processing from an integration endpoint.  Note: We expect end date to be updated now.
        mockMvc.perform(patch(String.format("/api/ext/data/documents/%d/complete", documentBatchResult.getId()))
                    .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                        .jwt(jwt -> jwt.claims(claims -> {
                            claims.put("sub", TEST_USER_UUID_6);
                            claims.put("email", TEST_USER_EMAIL_6);
                            claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                        })))
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        // Get the batch processing from an integration endpoint.
        result = mockMvc.perform(get(String.format("/api/ext/data/documents/%d", documentBatchResult.getId()))
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("APP_SERVICE")))
                                .jwt(jwt -> jwt.claims(claims -> {
                                    claims.put("sub", TEST_USER_UUID_6);
                                    claims.put("email", TEST_USER_EMAIL_6);
                                    claims.put("exp", Instant.now().plus(10, ChronoUnit.MINUTES));
                                })))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        DocumentBatch completeDocumentBatch = objectMapper.readValue(result.getResponse().getContentAsString(), DocumentBatch.class);

        assertThat(completeDocumentBatch.getProcessEndDate()).isNotNull();
    }

}