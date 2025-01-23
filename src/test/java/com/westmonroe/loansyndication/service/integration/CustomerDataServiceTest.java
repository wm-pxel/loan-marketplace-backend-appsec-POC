package com.westmonroe.loansyndication.service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.integration.*;
import com.westmonroe.loansyndication.service.UserService;
import com.westmonroe.loansyndication.service.deal.DealCovenantService;
import com.westmonroe.loansyndication.service.deal.DealFacilityService;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.utils.DtoModelUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.TestConstants.TEST_INSTITUTION_UUID_1;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@Testcontainers
class CustomerDataServiceTest {

    @Autowired
    private CustomerDataService customerDataService;

    @Autowired
    private DealService dealService;

    @Autowired
    private DealFacilityService dealFacilityService;

    @Autowired
    private DealCovenantService dealCovenantService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentBatchService documentBatchService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenNewCustomerData_whenSavingData_thenVerify() throws JsonProcessingException {

        Long userId = 2L;                                                       // Annie Palinto

        // Deal information
        String dealExternalId = "7fdcd233-7e5e-4bde-ba1a-869ca9be69ae";
        String originatorId = TEST_INSTITUTION_UUID_1;                          // Farm Credit Bank of Texas
        String name = "Test Deal";
        String dealIndustry = "Farm Credit";
        String dealStructure = "Participation";
        String description = "This is an example deal";
        BigDecimal dealAmount = BigDecimal.valueOf(13000000.33);
        Integer defaultProbability = 11;
        BigDecimal currYearEbita = BigDecimal.valueOf(2750000.77);

        // Applicant information
        String applicantExternalId = "97fbe0b9-4442-4292-bbf9-d50e133286fd";
        String borrowerDesc = "This is a sample description";
        String borrowerName = "Sammy Davis";
        String cityName = "Mount Vernon";
        String stateCode = "IL";
        String countyName = "Jefferson County";
        String farmCreditEligOption = "Association Eligible";
        String taxId = "11-223344";
        String industryCode = "424930";
        Integer businessAge = 19;

        // Covenant information
        String covenantExternalId = "b44ab8f3-b15b-4b82-afa6-5e1f1fe7dd93";
        String entityName = "Test Covenant 1";
        String categoryName = "Test Category";
        String covenantType = "Farm";
        String frequency = "Weekly";
        LocalDate nextEvalDate = LocalDate.parse("2024-04-20");
        LocalDate effectiveDate = LocalDate.parse("2024-04-01");

        // Facility information
        String facilityExternalId = "33393379-ef49-4359-98ef-3c7a037e1e05";
        BigDecimal facilityAmount = BigDecimal.valueOf(12570000.22);
        String facilityType = "Revolving Term Loan";
        String collateral = "Secured excluding real estate";
        Integer tenor = 33;
        String pricing = "SOFR + 140.0bps";
        String creditSpreadAdj = "Test CSA";
        String facilityPurpose = "New Construction";
        String purposeDetail = "Same detail text";
        String dayCount = "Actual/365";
        String regulatoryLoanType = "Agribusiness - Loans to Cooperatives";
        String guarInvFlag = "N";
        String patronagePayingFlag = "N";
        String farmCreditType = "FLCA";
        Integer revolverUtil = null;
        String upfrontFees = "lots of fees";
        String unusedFees = "not unused";
        String amortization = "test amortization";
        String lgdOption = "C";

        // Document information
        String documentExternalId = "123412351324";
        String url = "https://cobank--lmsbx.sandbox.my.salesforce.com/services/data/v56.0/sobjects/ContentVersion/068Ek000004MmYLIA0/VersionData";
        String displayName = "CoBankAPIMappingsV1.xlsx";
        String extension = "xlsx";
        String category = "Entity Documents";

        /*
         *  Create a new customer data record.
         */
        CustomerData data = new CustomerData();
        Payload payload = new Payload();

        payload.setCreatedById(userId);

        MarketplaceData marketplaceData = new MarketplaceData();

        DealDto dealDto = DtoModelUtil.createTestDealDto(dealExternalId, originatorId, name, dealIndustry, dealStructure
                , description, dealAmount, defaultProbability, currYearEbita);

        ApplicantDto applicantDto = DtoModelUtil.createTestApplicantDto(applicantExternalId, borrowerDesc, borrowerName
                , cityName, stateCode, countyName, farmCreditEligOption, taxId, industryCode, businessAge);

        DealCovenantDto covenantDto = DtoModelUtil.createTestDealCovenantDto(covenantExternalId, entityName, categoryName
                , covenantType, frequency, nextEvalDate, effectiveDate);

        DealFacilityDto facilityDto = DtoModelUtil.createTestDealFacilityDto(facilityExternalId, facilityAmount
                , facilityType, collateral, tenor, pricing, creditSpreadAdj, facilityPurpose, purposeDetail, dayCount
                , guarInvFlag, patronagePayingFlag, farmCreditType, revolverUtil, upfrontFees, unusedFees, amortization
                , lgdOption, regulatoryLoanType);

        DealDocumentDto documentDto = DtoModelUtil.createTestDealDocumentDto(documentExternalId, url, displayName
                , extension, category);

        marketplaceData.setDeal(dealDto);
        marketplaceData.setApplicants(Arrays.asList(applicantDto));
        marketplaceData.setCovenants(Arrays.asList(covenantDto));
        marketplaceData.setFacilities(Arrays.asList(facilityDto));
        marketplaceData.setDocuments(Arrays.asList(documentDto));

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
        Map<String, Object> unsupportedData = mapper.readValue(unsupportedJson, Map.class);

        payload.setUnsupportedData(unsupportedData);
        data.setPayload(payload);

        User currentUser = userService.getUserById(userId, true);

        // Insert the customer data record.
        data = customerDataService.save(data, currentUser);

        assertThat(data).isNotNull();
        assertThat(data.getPayload().getBatchId()).isEqualTo(1L);

        CustomerData savedData = customerDataService.getCustomerDataByDealExternalId(payload.getMarketplaceData().getDeal().getDealExternalId(), currentUser);

        assertThat(savedData.getPayload())
            .isNotNull();

        /*
         *  Verify that data was saved to marketplace Deal table.
         */
        Deal deal = dealService.getDealByExternalId(dealExternalId, currentUser);

        assertThat(deal)
            .hasFieldOrPropertyWithValue("name", name)
            .hasFieldOrPropertyWithValue("description", description)
            .hasFieldOrPropertyWithValue("dealAmount", dealAmount)
            .hasFieldOrPropertyWithValue("defaultProbability", defaultProbability)
            .hasFieldOrPropertyWithValue("currYearEbita", currYearEbita)
            .hasFieldOrPropertyWithValue("applicantExternalId", applicantExternalId)
            .hasFieldOrPropertyWithValue("borrowerDesc", borrowerDesc)
            .hasFieldOrPropertyWithValue("borrowerName", borrowerName)
            .hasFieldOrPropertyWithValue("borrowerCityName", cityName)
            .hasFieldOrPropertyWithValue("borrowerStateCode", stateCode)
            .hasFieldOrPropertyWithValue("borrowerCountyName", countyName)
            .hasFieldOrPropertyWithValue("taxId", taxId)
            .hasFieldOrPropertyWithValue("businessAge", businessAge);
        assertThat(deal.getOriginator())
            .isNotNull()
            .hasFieldOrPropertyWithValue("uid", originatorId);
        assertThat(deal.getDealIndustry().getOption()).isEqualTo(dealIndustry);
        assertThat(deal.getDealStructure().getOption()).isEqualTo(dealStructure);
        assertThat(deal.getFarmCreditElig().getOption()).isEqualTo(farmCreditEligOption);
        assertThat(deal.getBorrowerIndustry().getCode()).isEqualTo(industryCode);

        /*
         *  Verify that data was saved to marketplace Deal Covenant table.
         */
        List<DealCovenant> covenants = dealCovenantService.getCovenantsForDeal(deal.getUid());

        assertThat(covenants)
            .isNotNull()
            .hasSize(1);
        assertThat(covenants.get(0))
            .hasFieldOrPropertyWithValue("covenantExternalId", covenantExternalId)
            .hasFieldOrPropertyWithValue("entityName", entityName)
            .hasFieldOrPropertyWithValue("categoryName", categoryName)
            .hasFieldOrPropertyWithValue("covenantType", covenantType)
            .hasFieldOrPropertyWithValue("frequency", frequency)
            .hasFieldOrPropertyWithValue("nextEvalDate", nextEvalDate)
            .hasFieldOrPropertyWithValue("effectiveDate", effectiveDate);

        /*
         *  Verify that data was saved to marketplace Deal Facility table.
         */
        List<DealFacility> facilities = dealFacilityService.getFacilitiesForDeal(deal.getUid());

        assertThat(facilities)
            .isNotNull()
            .hasSize(1);
        assertThat(facilities.get(0))
            .hasFieldOrPropertyWithValue("facilityExternalId", facilityExternalId)
            .hasFieldOrPropertyWithValue("facilityAmount", facilityAmount)
            .hasFieldOrPropertyWithValue("tenor", tenor)
            .hasFieldOrPropertyWithValue("pricing", pricing)
            .hasFieldOrPropertyWithValue("creditSpreadAdj", creditSpreadAdj)
            .hasFieldOrPropertyWithValue("purposeDetail", purposeDetail)
            .hasFieldOrPropertyWithValue("guarInvFlag", guarInvFlag)
            .hasFieldOrPropertyWithValue("patronagePayingFlag", patronagePayingFlag)
            .hasFieldOrPropertyWithValue("farmCreditType", farmCreditType)
            .hasFieldOrPropertyWithValue("revolverUtil", revolverUtil)
            .hasFieldOrPropertyWithValue("upfrontFees", upfrontFees)
            .hasFieldOrPropertyWithValue("unusedFees", unusedFees)
            .hasFieldOrPropertyWithValue("amortization", amortization)
            .hasFieldOrPropertyWithValue("lgdOption", lgdOption);
        assertThat(facilities.get(0).getFacilityType().getOption()).isEqualTo(facilityType);
        assertThat(facilities.get(0).getCollateral().getOption()).isEqualTo(collateral);
        assertThat(facilities.get(0).getFacilityPurpose().getOption()).isEqualTo(facilityPurpose);
        assertThat(facilities.get(0).getDayCount().getOption()).isEqualTo(dayCount);
        assertThat(facilities.get(0).getRegulatoryLoanType().getOption()).isEqualTo(regulatoryLoanType);

        /*
         *  Verify that data was saved to marketplace document staging tables (Document Batch and Document Batch Detail).
         */
        DocumentBatch documentBatch = documentBatchService.getDocumentBatchForId(data.getPayload().getBatchId());

        assertThat(documentBatch).isNotNull();
        assertThat(documentBatch.getDetails()).isNotNull();
        assertThat(documentBatch.getDetails().get(0))
            .hasFieldOrPropertyWithValue("documentExternalId", "123412351324")
            .hasFieldOrPropertyWithValue("url", "https://cobank--lmsbx.sandbox.my.salesforce.com/services/data/v56.0/sobjects/ContentVersion/068Ek000004MmYLIA0/VersionData")
            .hasFieldOrPropertyWithValue("displayName", "CoBankAPIMappingsV1.xlsx")
            .hasFieldOrPropertyWithValue("extension", "xlsx")
            .hasFieldOrPropertyWithValue("category", "Entity Documents");
    }

}