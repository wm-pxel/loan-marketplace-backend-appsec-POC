package com.westmonroe.loansyndication.controller.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.westmonroe.loansyndication.model.DocumentCategory;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import com.westmonroe.loansyndication.service.AwsService;
import com.westmonroe.loansyndication.service.UserService;
import com.westmonroe.loansyndication.service.deal.DealDocumentService;
import com.westmonroe.loansyndication.utils.GraphQLUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.Constants.ERR_UNAUTH_DEAL_MEMBER;
import static com.westmonroe.loansyndication.utils.Constants.VIEW_TYPE_FULL;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_1;
import static com.westmonroe.loansyndication.utils.EventTypeEnum.ORIGINATION;
import static com.westmonroe.loansyndication.utils.GraphQLUtil.*;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@Testcontainers
class DealGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private UserService userService;

    @MockBean
    AwsService awsService;

    @Autowired
    private DealDocumentService dealDocumentService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenAllDealsRequest_whenUsingSuperAdminUser_thenVerifyAccessAndSize() {

        /*
         *  This user is a SUPER_ADM and can access this endpoint.
         */
        graphQlTester
            .document("""
            query {
                allDeals {
                    uid
                    name
                    active
                }
            }
            """)
            .execute()
            .path("allDeals")
            .entityList(Map.class)
            .satisfies(dealMapList -> {
                assertThat(dealMapList)
                    .isNotEmpty()
                    .hasSize(4);
            });

    }

    @Test
    @WithMockJwtUser(username = "Kyle.Yancey@test.com")
    void givenExistingDeals_whenAccessingDealWithUnauthorizedUser_thenVerifyAccessDenied() {

        // Use "Texas Dairy Farm" as our test deal.
        String dealUid = "6f865256-e16e-441a-b495-bfb6ea856623";

        /*
         *  Kyle's institution does not have access to the deal, so this will generate an UNAUTHORIZED exception.
         */
        graphQlTester
            .document("""
            query getDealByUid($uid: String!) {
                getDealByUid(uid: $uid) {
                    uid
                    name
                    active
                }
            }
            """)
            .variable("uid", dealUid)
            .execute().errors().satisfy(errors -> {
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getMessage()).isEqualTo(ERR_UNAUTH_DEAL_MEMBER);
            });

    }

    @Deprecated
    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenAllDeals_whenRequestingDealSummary_thenVerify() {

        graphQlTester
            .document("""
                query {
                    getDealSummaryByUser {
                        uid
                        name
                        relation
                        originator {
                            uid
                            name
                        }
                        stage {
                            id
                            name
                        }
                        dealAmount
                        closeDate
                        declinedFlag
                        removedFlag
                        step {
                            id
                            name
                        }
                        active
                    }
                }
            """)
            .execute()
            .path("getDealSummaryByUser")
            .entityList(Map.class)
            .satisfies(summaryMap -> {
                assertThat(summaryMap)
                    .isNotEmpty()
                    .hasSize(2);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenAllDeals_whenRequestingDealEventSummary_thenVerify() {

        graphQlTester
            .document("""
            query {
                getDealEventSummaryByUser {
                    uid
                    name
                    relation
                    originator {
                        uid
                        name
                    }
                    dealAmount
                    event {
                        stage {
                            id
                            name
                        }
                        projectedLaunchDate
                        launchDate
                        commitmentDate
                        projectedCloseDate
                        effectiveDate
                        closeDate
                    }
                    eventParticipant {
                        declinedFlag
                        removedFlag
                        step {
                            id
                            name
                        }
                    }
                    active
                }
            }
            """)
            .execute()
            .path("getDealEventSummaryByUser")
            .entityList(Map.class)
            .satisfies(summaryMap -> {
                assertThat(summaryMap)
                    .isNotEmpty()
                    .hasSize(2);
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenExistingDeal_whenRetrievingDealEvent_thenVerify() {

        /*
         *  Annie Palinto is in the Lead institution.
         */
        Map dealEventMap = graphQlTester
            .document("""
            query getDealEventByUid($uid: String!) {
                getDealEventByUid(uid: $uid) {
                    uid
                    name
                    relation
                    viewType
                    event {
                        uid
                        name
                        stage {
                            id
                            name
                            order
                        }
                    }
                    active
                }
            }
            """)
            .variable("uid", TEST_DEAL_UUID_1)
            .execute()
            .path("getDealEventByUid")
            .entity(Map.class)
            .get();

        assertThat(dealEventMap)
            .isNotNull()
            .containsEntry("relation", ORIGINATOR.getDescription())
            .containsEntry("viewType", VIEW_TYPE_FULL);
    }

        @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewDeal_whenPerformingCrudOperations_thenVerify() {

        String dealName = "XYZ Test Deal";
        Integer dealIndustryId = 16;        // Farm Credit
        String originatorUid = TEST_INSTITUTION_UUID_2;
        String initialLenderFlag = "N";
        Integer initialLenderId = null;
        Integer dealStructureId = 4;        // Participation
        String dealType = "Renewal";
        String description = "This is a test description";
        BigDecimal dealAmount = BigDecimal.valueOf(22000000.22);
        String borrowerDesc = "Borrower Description";
        String borrowerName = "Anita Money";
        String borrowerCityName = "Big Banks";
        String borrowerStateCode = "AZ";
        String borrowerCountyName = "Cache County";
        Integer farmCreditEligId = 1;      // Association Eligible
        String taxId = "94-4444444";
        String borrowerIndustryCode = "112330";
        Integer businessAge = 5;
        Integer defaultProbability = 7;
        BigDecimal currYearEbita = BigDecimal.valueOf(1200000.22);
        String active = "Y";

        // Perform the GraphQL mutation for inserting a deal.
        Map<String, Object> dealMap = insertTestDeal(graphQlTester, dealName, dealIndustryId, originatorUid
                , initialLenderFlag, initialLenderId, dealStructureId, dealType, description, dealAmount, borrowerDesc, borrowerName
                , borrowerCityName, borrowerStateCode, borrowerCountyName, farmCreditEligId, taxId, borrowerIndustryCode
                , businessAge, defaultProbability, currYearEbita, active);

        assertThat(dealMap).isNotNull();
        assertThat(dealMap.get("uid")).isNotNull();
        assertThat(dealMap)
            .containsEntry("name", dealName);
        assertThat(((Map) dealMap.get("dealIndustry"))).containsEntry("id", dealIndustryId);
        assertThat(((Map) dealMap.get("originator"))).containsEntry("uid", originatorUid);
        assertThat((Map) dealMap.get("initialLender")).isNull();
        assertThat(dealMap)
            .containsEntry("relation", ORIGINATOR.getDescription())
            .containsEntry("viewType", VIEW_TYPE_FULL)
            .containsEntry("initialLenderFlag", initialLenderFlag);
        assertThat(((Map) dealMap.get("dealStructure"))).containsEntry("id", dealStructureId);
        assertThat(dealMap)
            .containsEntry("dealType", dealType)
            .containsEntry("description", description);
        assertThat(new BigDecimal(dealMap.get("dealAmount").toString())).isEqualTo(dealAmount);
        assertThat(dealMap)
            .containsEntry("borrowerDesc", borrowerDesc)
            .containsEntry("borrowerName", borrowerName)
            .containsEntry("borrowerCityName", borrowerCityName)
            .containsEntry("borrowerStateCode", borrowerStateCode)
            .containsEntry("borrowerCountyName", borrowerCountyName);
        assertThat(((Map) dealMap.get("farmCreditElig"))).containsEntry("id", farmCreditEligId);
        assertThat(dealMap)
            .containsEntry("taxId", taxId);
        assertThat(((Map) dealMap.get("borrowerIndustry")))
            .containsEntry("code", borrowerIndustryCode)
            .containsEntry("title", "Turkey Production");
        assertThat(dealMap)
            .containsEntry("businessAge", businessAge)
            .containsEntry("defaultProbability", defaultProbability)
            .containsEntry("active", active);
        assertThat(new BigDecimal(dealMap.get("currYearEbita").toString())).isEqualTo(currYearEbita);

        String eventName = "Origination Event";
        Long eventTypeId = ORIGINATION.getId();
        String projectedLaunchDate = "2024-09-19";
        String commitmentDate = "2024-10-01";
        String commentsDueByDate = "2024-10-20";
        String effectiveDate = "2024-11-07";
        String projectedCloseDate = "2024-11-15";

        // Perform the GraphQL mutation for inserting an event.
        Map<String, Object> eventMap = insertTestEvent(graphQlTester, dealMap.get("uid").toString(), eventName, eventTypeId
                , projectedLaunchDate, commitmentDate, commentsDueByDate, effectiveDate, projectedCloseDate);

        assertThat(eventMap).isNotNull();
        assertThat(eventMap.get("uid")).isNotNull();
        assertThat(eventMap)
            .containsEntry("name", eventName)
            .containsEntry("projectedLaunchDate", projectedLaunchDate)
            .containsEntry("commitmentDate", commitmentDate)
            .containsEntry("commentsDueByDate", commentsDueByDate)
            .containsEntry("effectiveDate", effectiveDate)
            .containsEntry("projectedCloseDate", projectedCloseDate);
        assertThat(((Map) eventMap.get("eventType")))
            .containsEntry("id", ORIGINATION.getId().intValue())
            .containsEntry("name", ORIGINATION.getName());
        assertThat(((Map) eventMap.get("stage")))
            .containsEntry("name", STAGE_1.getName());

        // Get the deal by its UID.
        LinkedHashMap resultMap = graphQlTester
            .document("""
                query getDealByUid($uid: String!) {
                    getDealByUid(uid: $uid) {
                        uid
                        name
                        dealIndustry {
                            id
                            option
                        }
                        originator {
                            uid
                            name
                        }
                        taxId
                        borrowerIndustry {
                            code
                            title
                        }
                        businessAge
                        defaultProbability
                        active
                    }
                }
                """)
            .variable("uid", dealMap.get("uid"))
            .execute()
            .path("getDealByUid")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(resultMap)
            .isNotNull()
            .containsEntry("uid", dealMap.get("uid"))
            .containsEntry("name", dealName);
        assertThat(((Map) resultMap.get("dealIndustry"))).containsEntry("id", dealIndustryId);
        assertThat(((Map) resultMap.get("originator"))).containsEntry("uid", originatorUid);
        assertThat(resultMap)
            .containsEntry("active", active);

        String updatedName = "123 Test Deal";
        Integer updatedDealIndustryId = 16;         // Other
        String updatedInitialLenderFlag = "Y";
        Integer updatedInitialLenderId = 3;
        Integer updatedDealStructureId = 5;         // Syndication
        String updatedDealType = "Modification";
        String updatedDescription = "This is another test description";
        BigDecimal updatedDealAmount = BigDecimal.valueOf(33300000.33);
        String updatedBorrowerDesc = "Updated Borrower Description";
        String updatedBorrowerName = "Anita More Money";
        String updatedBorrowerCityName = "Bigger Banks";
        String updatedBorrowerStateCode = "CA";
        String updatedBorrowerCountyName = "Cached County";
        Integer updatedFarmCreditEligId = 3;    // Similar Entity
        String updatedTaxId = "97-7777777";
        String updatedBorrowerIndustryCode = "112511";
        Integer updatedBusinessAge = 11;
        Integer updatedDefaultProbability = 8;
        BigDecimal updatedCurrYearEbita = BigDecimal.valueOf(1300000.33);
        String updatedActive = "N";

        // Update the deal.
        LinkedHashMap updatedMap = graphQlTester
            .document(String.format("""
                mutation {
                   updateDeal(input: {
                        uid: "%s"
                        name: "%s"
                        dealIndustry: {
                            id: %d
                        }
                        initialLenderFlag: "%s"
                        initialLender: {
                            id: %d
                        }
                        dealStructure: {
                            id: %d
                        }
                        dealType: "%s"
                        description: "%s"
                        dealAmount: %.2f
                        borrowerDesc: "%s"
                        borrowerName: "%s"
                        borrowerCityName: "%s"
                        borrowerStateCode: "%s"
                        borrowerCountyName: "%s"
                        farmCreditElig: {
                            id: %d
                        }
                        taxId: "%s"
                        borrowerIndustry: {
                            code: "%s"
                        }
                        businessAge: %d
                        defaultProbability: %d
                        currYearEbita: %.2f
                        active: "%s"
                    }) {
                        uid
                        name
                        dealIndustry {
                            id
                            option
                        }
                        originator {
                            uid
                            name
                        }
                        initialLenderFlag
                        initialLender {
                            id
                            lenderName
                        }
                        dealStructure {
                            id
                            option
                        }
                        dealType
                        description
                        dealAmount
                        borrowerDesc
                        borrowerName
                        borrowerCityName
                        borrowerStateCode
                        borrowerCountyName
                        farmCreditElig {
                            id
                            option
                        }
                        taxId
                        borrowerIndustry {
                            code
                            title
                        }
                        businessAge
                        defaultProbability
                        currYearEbita
                        active
                    }
                }
            """, dealMap.get("uid"), updatedName, updatedDealIndustryId, updatedInitialLenderFlag, updatedInitialLenderId
           , updatedDealStructureId, updatedDealType, updatedDescription, updatedDealAmount, updatedBorrowerDesc, updatedBorrowerName
           , updatedBorrowerCityName, updatedBorrowerStateCode, updatedBorrowerCountyName, updatedFarmCreditEligId, updatedTaxId
           , updatedBorrowerIndustryCode, updatedBusinessAge, updatedDefaultProbability
           , updatedCurrYearEbita, updatedActive))
        .execute()
        .path("updateDeal")
        .entity(LinkedHashMap.class)
        .get();

        assertThat(updatedMap)
            .isNotNull()
            .containsEntry("uid", dealMap.get("uid"))
            .containsEntry("name", updatedName)
            .containsEntry("initialLenderFlag", updatedInitialLenderFlag);
        assertThat(((Map) updatedMap.get("dealIndustry"))).containsEntry("id", updatedDealIndustryId);
        assertThat((Map) updatedMap.get("initialLender")).containsEntry("id", updatedInitialLenderId);
        assertThat(((Map) updatedMap.get("dealStructure"))).containsEntry("id", updatedDealStructureId);
        assertThat(updatedMap)
            .containsEntry("dealType", updatedDealType)
            .containsEntry("description", updatedDescription);
        assertThat(new BigDecimal(updatedMap.get("dealAmount").toString())).isEqualTo(updatedDealAmount);
        assertThat(updatedMap)
            .containsEntry("borrowerDesc", updatedBorrowerDesc)
            .containsEntry("borrowerName", updatedBorrowerName)
            .containsEntry("borrowerCityName", updatedBorrowerCityName)
            .containsEntry("borrowerStateCode", updatedBorrowerStateCode)
            .containsEntry("borrowerCountyName", updatedBorrowerCountyName);
        assertThat(((Map) updatedMap.get("farmCreditElig"))).containsEntry("id", updatedFarmCreditEligId);
        assertThat(updatedMap)
            .containsEntry("taxId", updatedTaxId);
        assertThat(((Map) updatedMap.get("borrowerIndustry")))
            .containsEntry("code", updatedBorrowerIndustryCode)
            .containsEntry("title", "Finfish Farming and Fish Hatcheries");
        assertThat(updatedMap)
            .containsEntry("businessAge", updatedBusinessAge)
            .containsEntry("defaultProbability", updatedDefaultProbability)
            .containsEntry("active", updatedActive);
        assertThat(new BigDecimal(updatedMap.get("currYearEbita").toString())).isEqualTo(updatedCurrYearEbita);

        // Delete the institution.
        graphQlTester
            .document(String.format("""
                mutation {
                    deleteDeal(dealUid: "%s") {
                        uid
                        name
                    }
                }
            """, dealMap.get("uid")))
            .execute()
            .path("deleteDeal")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty()
                    .containsEntry("uid", dealMap.get("uid"))
                    .containsEntry("name", updatedName);
            });

        // Verify the deal is deleted.
        graphQlTester
            .document("""
                query getDealByUid($uid: String!) {
                    getDealByUid(uid: $uid) {
                        uid
                        name
                        active
                    }
                }
                """)
            .variable("uid", dealMap.get("uid"))
            .execute().errors().satisfy(errors -> {
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getMessage()).isEqualTo("Deal was not found for uid.");
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewDealWithEmptyFields_whenPerformingInsertAndUpdate_thenVerify() {

        String name = "Test Deal with Empty Fields";
        Integer dealIndustryId = 16;        // Farm Credit
        String originatorUid = TEST_INSTITUTION_UUID_2;
        String initialLenderFlag = "N";
        Integer initialLenderId = null;
        Integer dealStructureId = 4;        // Participation
        String dealType = "Renewal";
        BigDecimal dealAmount = BigDecimal.valueOf(22000000.22);
        String borrowerName = "Anita Money";
        String taxId = "94-4444444";
        String active = "Y";

        // Perform the GraphQL mutation for inserting a deal.
        Map<String, Object> dealMap = graphQlTester
            .document(String.format("""
                mutation {
                   createDeal(input: {
                        name: "%s"
                        dealIndustry: {
                            id: %d
                        }
                        originator: {
                            uid: "%s"
                        }
                        initialLenderFlag: "%s"
                        initialLender: {
                            id: %d
                        }
                        dealStructure: {
                            id: %d
                        }
                        dealType: "%s"
                        dealAmount: %.2f
                        borrowerName: "%s"
                        taxId: "%s"
                        active: "%s"
                    }) {
                        uid
                        name
                        dealIndustry {
                            id
                            option
                        }
                        originator {
                            uid
                            name
                        }
                        initialLenderFlag
                        initialLender {
                            id
                            lenderName
                        }
                        relation
                        viewType
                        dealStructure {
                            id
                            option
                        }
                        dealType
                        description
                        dealAmount
                        borrowerDesc
                        borrowerName
                        borrowerCityName
                        borrowerStateCode
                        borrowerCountyName
                        farmCreditElig {
                            id
                            option
                        }
                        taxId
                        borrowerIndustry {
                            code
                            title
                        }
                        businessAge
                        defaultProbability
                        currYearEbita
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                        active
                    }
                }
            """, name, dealIndustryId, originatorUid, initialLenderFlag, initialLenderId, dealStructureId, dealType
               , dealAmount, borrowerName, taxId, active))
            .execute()
            .path("createDeal")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(dealMap).isNotNull();
        assertThat(dealMap.get("uid")).isNotNull();
        assertThat(dealMap)
            .containsEntry("name", name);
        assertThat(((Map) dealMap.get("dealIndustry"))).containsEntry("id", dealIndustryId);
        assertThat(((Map) dealMap.get("originator"))).containsEntry("uid", originatorUid);
        assertThat((Map) dealMap.get("initialLender")).isNull();
        assertThat(dealMap)
            .containsEntry("relation", ORIGINATOR.getDescription())
            .containsEntry("viewType", VIEW_TYPE_FULL)
            .containsEntry("initialLenderFlag", initialLenderFlag);
        assertThat(((Map) dealMap.get("dealStructure"))).containsEntry("id", dealStructureId);
        assertThat(dealMap)
            .containsEntry("dealType", dealType)
            .containsEntry("description", null)
            .containsEntry("farmCreditElig", null);
        assertThat(new BigDecimal(dealMap.get("dealAmount").toString())).isEqualTo(dealAmount);
        assertThat(dealMap)
            .containsEntry("borrowerDesc", null)
            .containsEntry("borrowerIndustry", null)
            .containsEntry("borrowerName", borrowerName)
            .containsEntry("borrowerCityName", null)
            .containsEntry("borrowerStateCode", null)
            .containsEntry("borrowerCountyName", null)
            .containsEntry("taxId", taxId)
            .containsEntry("businessAge", null)
            .containsEntry("defaultProbability", null)
            .containsEntry("currYearEbita", null)
            .containsEntry("active", active);

        graphQlTester
            .document(String.format("""
                mutation {
                   updateDeal(input: {
                        uid: "%s"
                        description: null
                        borrowerDesc: null
                        borrowerCityName: null
                        borrowerStateCode: null
                        borrowerCountyName: null
                        farmCreditElig: null
                        borrowerIndustry: null
                        businessAge: null
                        defaultProbability: null
                        currYearEbita: null
                    }) {
                        uid
                        description
                        borrowerDesc
                        borrowerCityName
                        borrowerStateCode
                        borrowerCountyName
                        farmCreditElig {
                            id
                            option
                        }
                        borrowerIndustry {
                            code
                            title
                        }
                        businessAge
                        defaultProbability
                        currYearEbita
                    }
                }
            """, dealMap.get("uid")))
            .execute()
            .path("updateDeal")
            .entity(Map.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap)
                    .isNotNull()
                    .containsEntry("description", null)
                    .containsEntry("borrowerDesc", null)
                    .containsEntry("borrowerIndustry", null)
                    .containsEntry("borrowerCityName", null)
                    .containsEntry("borrowerStateCode", null)
                    .containsEntry("borrowerCountyName", null)
                    .containsEntry("businessAge", null)
                    .containsEntry("defaultProbability", null)
                    .containsEntry("currYearEbita", null);
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingTestData_whenCreatingDealMembersByList_thenVerify() throws JsonProcessingException {

        String dealUid = "6416100a-ea7e-45a9-b1d3-a8a248e82262";

        // Create test input JSON object.
        String json = String.format("""
        {
            deal: {
                uid: "%s"
            },
            users: [
                {
                    uid: "4d7ac607-9c66-41bc-bf6c-1458d192ff75"
                },
                {
                    uid: "429a53d3-17af-4be1-bb82-44f48ae1e74e"
                }
            ]
        }
        """, dealUid, TEST_INSTITUTION_UUID_2);

        graphQlTester
            .document(String.format("""
                mutation {
                   createDealMembers(input: %s) {
                        deal {
                            uid
                        }
                        users {
                            uid
                        }
                    }
                }
            """, json))
            .execute()
            .path("createDealMembers")
            .entity(Map.class)
            .satisfies(membersMap -> {
                assertThat(membersMap)
                    .isNotNull()
                    .hasSize(2);
                assertThat(((Map) membersMap.get("deal")))
                    .containsEntry("uid", dealUid);
                assertThat(((List) membersMap.get("users"))).hasSize(2);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenUnknownDeal_whenDeletingDeal_thenVerifyException() {

        graphQlTester
            .document(String.format("""
                mutation {
                   deleteDeal(dealUid: "%s") {
                        uid
                        name
                    }
                }
                """, TEST_DUMMY_UUID))
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Deal could not be deleted because it does not exist.");
            });

    }

    @Test
    void givenNewDealMember_whenAddingAndDeletingMember_thenVerify() {

        String dealUid = "6416100a-ea7e-45a9-b1d3-a8a248e82262";            // Eggland Best of Illinois
        String origUserUid = "b4549afa-2261-48bb-8bdc-321d5c570523";        // Kyle M Yancey
        String origEmail = "Kyle.Yancey@test.com";
        String partUserUid = TEST_USER_UUID_1;                              // Lenor Anderson
        String partEmail = TEST_USER_EMAIL_1;

        /*
         *  Insert the deal member from originating institution.
         */

        // Set the security context for the originating user.
        TestSecurityContextHolder.getContext().setAuthentication(GraphQLUtil.getJwtAuthentication(origEmail, userService));

        // Insert the originator deal member.
        Map<String, Object> origMemberMap = insertTestDealMember(graphQlTester, dealUid, origUserUid);

        // Verify the inserted originator member.
        assertThat(origMemberMap)
            .isNotNull()
            .containsEntry("memberTypeDesc", ORIGINATOR.getDescription());
        assertThat(((Map) origMemberMap.get("deal")).get("uid")).isEqualTo(dealUid);
        assertThat(((Map) origMemberMap.get("user")).get("uid")).isEqualTo(origUserUid);

        // Verify the originator member exists.
        graphQlTester
            .document("""
                query getDealMemberByDealUidAndUserUid($dealUid: String!, $userUid: String!) {
                    getDealMemberByDealUidAndUserUid(dealUid: $dealUid, userUid: $userUid) {
                        deal {
                            uid
                            name
                            userRolesDesc
                        }
                        user {
                            uid
                            firstName
                            lastName
                        }
                        memberTypeCode
                        memberTypeDesc
                    }
                }
            """)
            .variable("dealUid", dealUid)
            .variable("userUid", origUserUid)
            .execute()
            .path("getDealMemberByDealUidAndUserUid")
            .entity(Map.class)
            .satisfies(memberMap -> {
                assertThat(memberMap)
                    .isNotNull()
                    .isNotEmpty();
                assertThat(((Map) memberMap.get("deal")).get("uid")).isEqualTo(dealUid);
                assertThat(((Map) memberMap.get("user")).get("uid")).isEqualTo(origUserUid);
                assertThat(memberMap.get("memberTypeCode")).isEqualTo(ORIGINATOR.getCode());
                assertThat(memberMap.get("memberTypeDesc")).isEqualTo(ORIGINATOR.getDescription());
            });

        /*
         *  Insert the deal member from participating institution.
         */

        // Set the security context for the participating user.
        TestSecurityContextHolder.getContext().setAuthentication(GraphQLUtil.getJwtAuthentication(partEmail, userService));

        // Insert the participating deal member.
        Map<String, Object> partMemberMap = insertTestDealMember(graphQlTester, dealUid, partUserUid);

        // Verify the inserted originator member.
        assertThat(partMemberMap)
            .isNotNull()
            .containsEntry("memberTypeDesc", PARTICIPANT.getDescription());
        assertThat(((Map) partMemberMap.get("deal")).get("uid")).isEqualTo(dealUid);
        assertThat(((Map) partMemberMap.get("user")).get("uid")).isEqualTo(partUserUid);

        // Verify the originator member exists.
        graphQlTester
            .document("""
            query getDealMemberByDealUidAndUserUid($dealUid: String!, $userUid: String!) {
                getDealMemberByDealUidAndUserUid(dealUid: $dealUid, userUid: $userUid) {
                    deal {
                        uid
                        name
                    }
                    user {
                        uid
                        firstName
                        lastName
                    }
                    memberTypeCode
                    memberTypeDesc
                }
            }
        """)
        .variable("dealUid", dealUid)
        .variable("userUid", partUserUid)
        .execute()
        .path("getDealMemberByDealUidAndUserUid")
        .entity(Map.class)
        .satisfies(memberMap -> {
            assertThat(memberMap)
                .isNotNull()
                .isNotEmpty();
            assertThat(((Map) memberMap.get("deal")).get("uid")).isEqualTo(dealUid);
            assertThat(((Map) memberMap.get("user")).get("uid")).isEqualTo(partUserUid);
            assertThat(memberMap.get("memberTypeCode")).isEqualTo(PARTICIPANT.getCode());
            assertThat(memberMap.get("memberTypeDesc")).isEqualTo(PARTICIPANT.getDescription());
        });

        // Set the security context for the originating user.
        TestSecurityContextHolder.getContext().setAuthentication(GraphQLUtil.getJwtAuthentication(origEmail, userService));

        // Delete new originator deal member.
        graphQlTester
            .document(String.format("""
                mutation {
                    deleteDealMember(input: {
                        deal: {
                            uid: "%s"
                        }
                        user: {
                            uid: "%s"
                        }
                    }) {
                        deal {
                            uid
                            name
                        }
                        user {
                            uid
                            firstName
                            lastName
                        }
                        memberTypeCode
                        memberTypeDesc
                    }
                }
            """, dealUid, origUserUid))
            .execute()
            .path("deleteDealMember")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty();
                assertThat(((Map) deletedMap.get("deal")).get("uid")).isEqualTo(dealUid);
                assertThat(((Map) deletedMap.get("user")).get("uid")).isEqualTo(origUserUid);
                assertThat(deletedMap.get("memberTypeCode")).isEqualTo(ORIGINATOR.getCode());
                assertThat(deletedMap.get("memberTypeDesc")).isEqualTo(ORIGINATOR.getDescription());
            });

        // Set the security context for the participating user.
        TestSecurityContextHolder.getContext().setAuthentication(GraphQLUtil.getJwtAuthentication(partEmail, userService));

        // Delete new participating deal member.
        graphQlTester
            .document(String.format("""
                mutation {
                    deleteDealMember(input: {
                            deal: {
                                uid: "%s"
                            }
                            user: {
                                uid: "%s"
                            }
                    }) {
                        deal {
                            uid
                            name
                        }
                        user {
                            uid
                            firstName
                            lastName
                        }
                        memberTypeCode
                        memberTypeDesc
                    }
                }
            """, dealUid, partUserUid))
            .execute()
            .path("deleteDealMember")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty();
                assertThat(((Map) deletedMap.get("deal")).get("uid")).isEqualTo(dealUid);
                assertThat(((Map) deletedMap.get("user")).get("uid")).isEqualTo(partUserUid);
                assertThat(deletedMap.get("memberTypeCode")).isEqualTo(PARTICIPANT.getCode());
                assertThat(deletedMap.get("memberTypeDesc")).isEqualTo(PARTICIPANT.getDescription());
            });

    }

    @Test
    @WithMockJwtUser(username = "Chris.Lender@test.com")
    void givenExistingDealMembers_whenAddingAndDeletingMemberWithoutPermission_thenVerifyAccessDenied() {

        String dealUid = "3eabdf8a-f591-43a7-9f7a-10af85f0e707";            // Kentucky Processing Plant
        String currentUserUid = "3aa836ce-5c8d-466c-b644-d7c6a9f9db34";     // Chris Lender

        /*
         *  Attempt inserting the deal member from originating institution but without MNG_DEAL_MEMBERS role.
         */
        graphQlTester
            .document(String.format("""
                mutation {
                   createDealMember(input: {
                        deal: {
                            uid: "%s"
                        }
                        user: {
                            uid: "%s"
                        }
                    }) {
                        deal {
                            uid
                            name
                        }
                        user {
                            uid
                            firstName
                            lastName
                        }
                    }
                }
            """, dealUid, currentUserUid))
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Access Denied");
            });

        /*
         *  Attempt deleting the deal member from originating institution but without MNG_DEAL_MEMBERS role.
         */
        graphQlTester
            .document(String.format("""
                mutation {
                   deleteDealMember(input: {
                        deal: {
                            uid: "%s"
                        }
                        user: {
                            uid: "%s"
                        }
                    }) {
                        deal {
                            uid
                            name
                        }
                        user {
                            uid
                            firstName
                            lastName
                        }
                    }
                }
            """, dealUid, TEST_USER_UUID_1))
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Access Denied");
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNonDealMember_whenDeletingDealMember_thenVerifyException() {

        graphQlTester
            .document(String.format("""
                mutation {
                   deleteDealMember(input: {
                        deal: {
                            uid: "%s"
                        }
                        user: {
                            uid: "%s"
                        }
                   }) {
                        deal {
                            uid
                        }
                        user {
                            uid
                        }
                    }
                }
                """, TEST_DEAL_UUID_2, "3aa836ce-5c8d-466c-b644-d7c6a9f9db34"))
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    assertThat(errors.get(0).getMessage()).isEqualTo("Deal member was not found for deal and user.");
                });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingCovenants_whenRetrievingAllForDeal_thenVerifySize() {

        graphQlTester
            .document("""
                query getDealCovenantsByDealUid($uid: String!) {
                    getDealCovenantsByDealUid(uid: $uid) {
                        id
                        deal {
                            uid
                        }
                        entityName
                        categoryName
                        covenantType
                        frequency
                        nextEvalDate
                        effectiveDate
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                    }
                }
            """)
            .variable("uid", TEST_DEAL_UUID_2)
            .execute()
            .path("getDealCovenantsByDealUid")
            .entityList(DealCovenant.class)
            .satisfies(covenantMap -> {
                assertThat(covenantMap)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewCovenantWithEmptyFields_whenPerformingInsert_thenVerify() {

        String categoryName = "Collateral";
        String nextEvalDate = "2024-02-02";
        String effectiveDate = "2024-01-01";

        Map covenantMap = graphQlTester
            .document(String.format("""
                mutation {
                    createDealCovenant(input: {
                        deal: {
                            uid: "%s"
                        }
                        categoryName: "%s"
                        nextEvalDate: "%s"
                        effectiveDate: "%s"
                    }) {
                        id
                        deal {
                            uid
                            name
                        }
                        entityName
                        categoryName
                        covenantType
                        frequency
                        nextEvalDate
                        effectiveDate
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                     }
                }
            """, TEST_DEAL_UUID_2, categoryName, nextEvalDate, effectiveDate))
            .execute()
            .path("createDealCovenant")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(covenantMap)
                .isNotNull()
                .containsEntry("id", covenantMap.get("id"))
                .containsEntry("entityName", null)
                .containsEntry("categoryName", categoryName)
                .containsEntry("covenantType", null)
                .containsEntry("frequency", null)
                .containsEntry("nextEvalDate", nextEvalDate)
                .containsEntry("effectiveDate", effectiveDate);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewCovenant_whenCreatingUpdatingAndDeleting_thenVerify() {

        String entityName = "Donald Duck";

        // Insert the test deal covenant.
        Map covenantMap = insertTestDealCovenant(graphQlTester, TEST_DEAL_UUID_2, entityName);

        // Verify the size of the covenant list has increased by one
        graphQlTester
            .document("""
                query getDealCovenantsByDealUid($uid: String!) {
                    getDealCovenantsByDealUid(uid: $uid) {
                        id
                        entityName
                    }
                }
            """)
            .variable("uid", TEST_DEAL_UUID_2)
            .execute()
            .path("getDealCovenantsByDealUid")
            .entityList(DealCovenant.class)
            .satisfies(insertMap -> {
                assertThat(insertMap)
                        .isNotNull()
                        .isNotEmpty()
                        .hasSize(4);
            });

        // Get the covenant by id to verify it was inserted correctly.
        graphQlTester
            .document("""
                query getDealCovenantById($id: Int) {
                    getDealCovenantById(id: $id) {
                        id
                        entityName
                        categoryName
                        covenantType
                        frequency
                        nextEvalDate
                        effectiveDate
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                    }
                }
            """)
            .variable("id", covenantMap.get("id"))
            .execute()
            .path("getDealCovenantById")
            .entity(Map.class)
            .satisfies(resultMap -> {
                assertThat(resultMap)
                    .isNotNull()
                    .containsEntry("id", covenantMap.get("id"))
                    .containsEntry("entityName", entityName)
                    .containsEntry("categoryName", covenantMap.get("categoryName"))
                    .containsEntry("covenantType", covenantMap.get("covenantType"))
                    .containsEntry("frequency", covenantMap.get("frequency"))
                    .containsEntry("nextEvalDate", covenantMap.get("nextEvalDate"))
                    .containsEntry("effectiveDate", covenantMap.get("effectiveDate"));
            });

        /*
         *  Update the test covenant.
         */
        String updatedEntityName = "Daisy Duck";
        String updatedCategoryName = "Financial Indicators";
        String updatedCovenantType = "Financial";
        String updatedFrequency = "Monthly";
        String updatedNextEvalDate = "2024-01-11";
        String updatedEffectiveDate = "2024-02-22";

        graphQlTester
            .document(String.format("""
                mutation {
                   updateDealCovenant(input: {
                        deal: {
                            uid: "%s"
                        }
                        id: %d
                        entityName: "%s"
                        categoryName: "%s"
                        covenantType: "%s"
                        frequency: "%s"
                        nextEvalDate: "%s"
                        effectiveDate: "%s"
                    }) {
                        id
                        entityName
                        categoryName
                        covenantType
                        frequency
                        nextEvalDate
                        effectiveDate
                        createdDate
                        updatedDate
                    }
                }
                """, TEST_DEAL_UUID_2, covenantMap.get("id"), updatedEntityName, updatedCategoryName, updatedCovenantType
                   , updatedFrequency, updatedNextEvalDate, updatedEffectiveDate))
            .execute()
            .path("updateDealCovenant")
            .entity(LinkedHashMap.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap)
                    .isNotNull()
                    .containsEntry("entityName", updatedEntityName)
                    .containsEntry("categoryName", updatedCategoryName)
                    .containsEntry("covenantType", updatedCovenantType)
                    .containsEntry("frequency", updatedFrequency)
                    .containsEntry("nextEvalDate", updatedNextEvalDate)
                    .containsEntry("effectiveDate", updatedEffectiveDate);
            });

        // Verify the covenant was updated.
        graphQlTester
            .document("""
                query getDealCovenantById($id: Int) {
                    getDealCovenantById(id: $id) {
                        id
                        entityName
                        categoryName
                        covenantType
                        frequency
                        nextEvalDate
                        effectiveDate
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                    }
                }
            """)
            .variable("id", covenantMap.get("id"))
            .execute()
            .path("getDealCovenantById")
            .entity(Map.class)
            .satisfies(resultMap -> {
                assertThat(resultMap)
                        .isNotNull()
                        .containsEntry("id", covenantMap.get("id"))
                        .containsEntry("entityName", updatedEntityName)
                        .containsEntry("categoryName", updatedCategoryName)
                        .containsEntry("covenantType", updatedCovenantType)
                        .containsEntry("frequency", updatedFrequency)
                        .containsEntry("nextEvalDate", updatedNextEvalDate)
                        .containsEntry("effectiveDate", updatedEffectiveDate);
            });

        // Delete the covenant.
        graphQlTester
            .document(String.format("""
                mutation {
                    deleteDealCovenant(covenantId: %d) {
                        id
                        entityName
                        categoryName
                        covenantType
                        frequency
                        nextEvalDate
                        effectiveDate
                        createdDate
                        updatedDate
                    }
                }
                """, covenantMap.get("id")))
            .execute()
            .path("deleteDealCovenant")
            .entity(LinkedHashMap.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                        .isNotNull()
                        .containsEntry("entityName", updatedEntityName)
                        .containsEntry("categoryName", updatedCategoryName)
                        .containsEntry("covenantType", updatedCovenantType)
                        .containsEntry("frequency", updatedFrequency)
                        .containsEntry("nextEvalDate", updatedNextEvalDate)
                        .containsEntry("effectiveDate", updatedEffectiveDate);
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenUnknownCovenant_whenDeletingCovenant_thenVerifyException() {

        graphQlTester
            .document("""
            mutation {
                deleteDealCovenant(covenantId: 99) {
                    id
                    deal {
                        uid
                    }
                    entityName
                }
            }
            """)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Deal Covenant could not be deleted because it does not exist.");
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingFacilities_whenRetrievingAllForDeal_thenVerifySize() {

        graphQlTester
            .document("""
            query getDealFacilitiesByDealUid($uid: String!) {
                getDealFacilitiesByDealUid(uid: $uid) {
                    id
                    deal {
                        uid
                    }
                    facilityName
                    facilityAmount
                    facilityType {
                        id
                        option
                    }
                    tenor
                    collateral {
                        id
                        option
                    }
                    pricing
                    creditSpreadAdj
                    facilityPurpose {
                        id
                        option
                    }
                    purposeDetail
                    dayCount {
                        id
                        option
                    }
                    regulatoryLoanType {
                        id
                        option
                    }
                    guarInvFlag
                    patronagePayingFlag
                    farmCreditType
                    lgdOption
                }
            }
            """)
            .variable("uid", TEST_DEAL_UUID_2)
            .execute()
            .path("getDealFacilitiesByDealUid")
            .entityList(DealFacility.class)
            .satisfies(facilityMap -> {
                assertThat(facilityMap)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(3);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewFacilityWithEmptyFields_whenPerformingInsert_thenVerify() {

        BigDecimal facilityAmount = BigDecimal.valueOf(11000000.11);

        // Insert the test deal facility.
        Map facilityMap = graphQlTester
            .document(String.format("""
                mutation {
                    createDealFacility(input: {
                        deal: {
                            uid: "%s"
                        }
                        facilityAmount: %.2f
                    }) {
                        id
                        deal {
                            uid
                            name
                        }
                        facilityName
                        facilityAmount
                        facilityType {
                            id
                            option
                        }
                        tenor
                        collateral {
                            id
                            option
                        }
                        pricing
                        creditSpreadAdj
                        facilityPurpose {
                            id
                            option
                        }
                        purposeDetail
                        dayCount {
                            id
                            option
                        }
                        regulatoryLoanType {
                            id
                            option
                        }
                        guarInvFlag
                        patronagePayingFlag
                        farmCreditType
                        revolverUtil
                        upfrontFees
                        unusedFees
                        amortization
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        maturityDate
                        renewalDate
                        updatedDate
                        lgdOption
                     }
                }
            """, TEST_DEAL_UUID_2, facilityAmount))
            .execute()
            .path("createDealFacility")
            .entity(LinkedHashMap.class)
            .get();;

        // Verify the values were inserted.
        assertThat(facilityMap)
            .isNotNull()
            .containsEntry("facilityName", "Facility D")
            .containsEntry("tenor", null)
            .containsEntry("pricing", null)
            .containsEntry("creditSpreadAdj", null)
            .containsEntry("purposeDetail", null)
            .containsEntry("guarInvFlag", null)
            .containsEntry("patronagePayingFlag", null)
            .containsEntry("farmCreditType", null)
            .containsEntry("revolverUtil", null)
            .containsEntry("upfrontFees", null)
            .containsEntry("unusedFees", null)
            .containsEntry("amortization", null)
            .containsEntry("facilityType", null)
            .containsEntry("facilityPurpose", null)
            .containsEntry("dayCount", null)
            .containsEntry("regulatoryLoanType", null)
            .containsEntry("collateral", null)
            .containsEntry("maturityDate", null)
            .containsEntry("renewalDate", null)
            .containsEntry("lgdOption", null);

        assertThat(new BigDecimal(facilityMap.get("facilityAmount").toString())).isEqualTo(facilityAmount);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewFacility_whenCreatingUpdatingAndDeleting_thenVerify() {

        BigDecimal facilityAmount = BigDecimal.valueOf(11000000.11);
        Integer facilityTypeId = 8;
        String facilityTypeName = "Revolving Term Loan";
        Integer collateralId = 26;
        String collateralName = "Secured";
        Integer tenor = 10;
        String pricing = "SOFR + 140.0bps";
        String creditSpreadAdj = "Test CSA";
        Integer facilityPurposeId = 11;
        String facilityPurposeName = "Existing Business Expansion";
        String purposeDetail = "This is paragraph text.";
        Integer dayCountId = 19;
        String dayCountName = "Actual/360";
        Integer regulatoryLoanTypeId = 32;
        String regulatoryLoanTypeName = "Agribusiness - Processing and Marketing";
        String guarInvFlag = "Y";
        String patronagePayingFlag = "Y";
        String farmCreditType = "FLCA";
        Integer revolverUtil = null;
        String upfrontFees = "Test Upfront Fees";
        String unusedFees = "Test Unused Fees";
        String amortization = "Test Amortization";
        String maturityDate = "2024-01-01";
        String renewalDate = null;
        String lgdOption = "A";

        // Insert the test deal facility.
        Map facilityMap = insertTestDealFacility(graphQlTester, TEST_DEAL_UUID_2, facilityAmount, facilityTypeId, tenor
                , collateralId, pricing, creditSpreadAdj, facilityPurposeId, purposeDetail, dayCountId, guarInvFlag, patronagePayingFlag, farmCreditType
                , revolverUtil, upfrontFees, unusedFees, amortization, maturityDate, renewalDate, lgdOption, regulatoryLoanTypeId);

        // Verify the values were inserted.
        assertThat(facilityMap)
            .isNotNull();
        assertThat(((Map) facilityMap.get("facilityType")))
            .containsEntry("id", facilityTypeId)
            .containsEntry("option", facilityTypeName);
        assertThat(facilityMap)
            .containsEntry("facilityName", "Facility D")
            .containsEntry("tenor", tenor)
            .containsEntry("pricing", pricing)
            .containsEntry("creditSpreadAdj", creditSpreadAdj);
        assertThat(((Map) facilityMap.get("facilityPurpose")))
            .containsEntry("id", facilityPurposeId)
            .containsEntry("option", facilityPurposeName);
        assertThat(((Map) facilityMap.get("dayCount")))
            .containsEntry("id", dayCountId)
            .containsEntry("option", dayCountName);
        assertThat(((Map) facilityMap.get("regulatoryLoanType")))
            .containsEntry("id", regulatoryLoanTypeId)
            .containsEntry("option", regulatoryLoanTypeName);
        assertThat(((Map) facilityMap.get("collateral")))
            .containsEntry("id", collateralId)
            .containsEntry("option", collateralName);
        assertThat(facilityMap)
            .containsEntry("purposeDetail", purposeDetail)
            .containsEntry("guarInvFlag", guarInvFlag)
            .containsEntry("patronagePayingFlag", patronagePayingFlag)
            .containsEntry("farmCreditType", farmCreditType)
            .containsEntry("revolverUtil", revolverUtil)
            .containsEntry("upfrontFees", upfrontFees)
            .containsEntry("unusedFees", unusedFees)
            .containsEntry("amortization", amortization)
            .containsEntry("maturityDate", maturityDate)
            .containsEntry("renewalDate", renewalDate)
            .containsEntry("lgdOption", lgdOption);
        assertThat(new BigDecimal(facilityMap.get("facilityAmount").toString())).isEqualTo(facilityAmount);

        // Verify the size of the facility list has increased by one
        graphQlTester
            .document("""
            query getDealFacilitiesByDealUid($uid: String!) {
                getDealFacilitiesByDealUid(uid: $uid) {
                    id
                    facilityAmount
                    facilityType {
                        id
                        option
                    }
                    tenor
                    collateral {
                        id
                        option
                    }
                    pricing
                    creditSpreadAdj
                    facilityPurpose {
                        id
                        option
                    }
                    purposeDetail
                    dayCount {
                        id
                        option
                    }
                    regulatoryLoanType {
                        id
                        option
                    }
                    guarInvFlag
                    patronagePayingFlag
                    farmCreditType
                    revolverUtil
                    upfrontFees
                    unusedFees
                    amortization
                    lgdOption
                }
            }
            """)
            .variable("uid", TEST_DEAL_UUID_2)
            .execute()
            .path("getDealFacilitiesByDealUid")
            .entityList(DealFacility.class)
            .satisfies(insertMap -> {
                assertThat(insertMap)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSize(4);
            });

        // Get the facility by id to verify it was inserted correctly.
        graphQlTester
            .document("""
            query getDealFacilityById($id: Int) {
                getDealFacilityById(id: $id) {
                    id
                    facilityName
                    facilityAmount
                    facilityType {
                        id
                        option
                    }
                    tenor
                    collateral {
                        id
                        option
                    }
                    pricing
                    creditSpreadAdj
                    facilityPurpose {
                        id
                        option
                    }
                    purposeDetail
                    dayCount {
                        id
                        option
                    }
                    regulatoryLoanType {
                        id
                        option
                    }
                    guarInvFlag
                    patronagePayingFlag
                    farmCreditType
                    revolverUtil
                    upfrontFees
                    unusedFees
                    amortization
                    maturityDate
                    renewalDate
                    lgdOption
                }
            }
            """)
            .variable("id", facilityMap.get("id"))
            .execute()
            .path("getDealFacilityById")
            .entity(Map.class)
            .satisfies(resultMap -> {
                assertThat(resultMap)
                    .isNotNull()
                    .containsEntry("id", facilityMap.get("id"));
                assertThat(((Map) resultMap.get("facilityType")))
                    .containsEntry("id", facilityTypeId)
                    .containsEntry("option", facilityTypeName);
                assertThat(resultMap)
                    .containsEntry("facilityName", "Facility D")
                    .containsEntry("tenor", tenor)
                    .containsEntry("pricing", pricing)
                    .containsEntry("creditSpreadAdj", creditSpreadAdj);
                assertThat(((Map) resultMap.get("facilityPurpose")))
                    .containsEntry("id", facilityPurposeId)
                    .containsEntry("option", facilityPurposeName);
                assertThat(((Map) resultMap.get("dayCount")))
                        .containsEntry("id", dayCountId)
                        .containsEntry("option", dayCountName);
                assertThat(((Map) resultMap.get("collateral")))
                        .containsEntry("id", collateralId)
                        .containsEntry("option", collateralName);
                assertThat(((Map) resultMap.get("regulatoryLoanType")))
                        .containsEntry("id", regulatoryLoanTypeId)
                        .containsEntry("option", regulatoryLoanTypeName);
                assertThat(resultMap)
                    .containsEntry("purposeDetail", purposeDetail)
                    .containsEntry("guarInvFlag", guarInvFlag)
                    .containsEntry("patronagePayingFlag", patronagePayingFlag)
                    .containsEntry("farmCreditType", farmCreditType)
                    .containsEntry("revolverUtil", revolverUtil)
                    .containsEntry("upfrontFees", upfrontFees)
                    .containsEntry("unusedFees", unusedFees)
                    .containsEntry("amortization", amortization)
                    .containsEntry("maturityDate", maturityDate)
                    .containsEntry("renewalDate", renewalDate)
                    .containsEntry("lgdOption", lgdOption);
                assertThat(new BigDecimal(resultMap.get("facilityAmount").toString())).isEqualTo(facilityAmount);
            });

        /*
         *  Update the test facility.
         */
        BigDecimal updatedFacilityAmount = BigDecimal.valueOf(15500000.55);
        Integer updatedFacilityTypeId = 6;
        String updatedFacilityTypeName = "Term";
        Integer updatedTenor = 7;
        Integer updatedCollateralId = 28;
        String updatedCollateralName = "Secured including real estate";
        String updatedPricing = "SOFR + 120.0bps";
        String updatedCreditSpreadAdj = "Updated CSA";
        Integer updatedFacilityPurposeId = 12;
        String updatedFacilityPurposeName = "New Construction";
        String updatedPurposeDetail = "This is updated paragraph text.";
        Integer updatedDayCountId = 20;
        String updatedDayCountName = "Actual/365";
        Integer updatedRegulatoryLoanTypeId = 33;
        String updatedRegulatoryLoanTypeName = "Communications";
        String updatedGuarInvFlag = "N";
        String updatedPatronagePayingFlag = "N";
        String updatedFarmCreditType = "PCA";
        Integer updatedRevolverUtil = 20;
        String updatedUpfrontFees = "Updated Upfront Fees";
        String updatedUnusedFees = "Updated Unused Fees";
        String updatedAmortization = "Updated Amortization";
        String updatedMaturityDate = "2027-02-01";
        String updatedRenewalDate = "2028-01-11";
        String updatedLgdOption = "F";

        graphQlTester
            .document(String.format("""
            mutation {
               updateDealFacility(input: {
                    deal: {
                        uid: "%s"
                    }
                    id: %d
                    facilityAmount: %.2f
                    facilityType: {
                        id: %d
                    }
                    tenor: %d
                    collateral: {
                        id: %d
                    }
                    pricing: "%s"
                    creditSpreadAdj: "%s"
                    facilityPurpose: {
                        id: %d
                    }
                    purposeDetail: "%s"
                    dayCount: {
                        id: %d
                    }
                    guarInvFlag: "%s"
                    patronagePayingFlag: "%s"
                    farmCreditType: "%s"
                    revolverUtil: %d
                    upfrontFees: "%s"
                    unusedFees: "%s"
                    amortization: "%s"
                    maturityDate: "%s"
                    renewalDate: "%s"
                    lgdOption: "%s"
                    regulatoryLoanType: {
                        id: %d
                    }
                }) {
                    id
                    facilityName
                    facilityAmount
                    facilityType {
                        id
                        option
                    }
                    tenor
                    collateral {
                        id
                        option
                    }
                    pricing
                    creditSpreadAdj
                    facilityPurpose {
                        id
                        option
                    }
                    purposeDetail
                    dayCount {
                        id
                        option
                    }
                    regulatoryLoanType {
                        id
                        option
                    }
                    guarInvFlag
                    patronagePayingFlag
                    farmCreditType
                    revolverUtil
                    upfrontFees
                    unusedFees
                    amortization
                    maturityDate
                    renewalDate
                    lgdOption
                    createdDate
                    updatedDate
                }
            }
            """, TEST_DEAL_UUID_2, facilityMap.get("id"), updatedFacilityAmount, updatedFacilityTypeId, updatedTenor
               , updatedCollateralId, updatedPricing, updatedCreditSpreadAdj, updatedFacilityPurposeId, updatedPurposeDetail
               , updatedDayCountId, updatedGuarInvFlag, updatedPatronagePayingFlag, updatedFarmCreditType, updatedRevolverUtil
               , updatedUpfrontFees, updatedUnusedFees, updatedAmortization, updatedMaturityDate, updatedRenewalDate, updatedLgdOption
               , updatedRegulatoryLoanTypeId ))
            .execute()
            .path("updateDealFacility")
            .entity(LinkedHashMap.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap)
                    .isNotNull();
                assertThat(((Map) updatedMap.get("facilityType")))
                    .containsEntry("id", updatedFacilityTypeId)
                    .containsEntry("option", updatedFacilityTypeName);
                assertThat(updatedMap)
                    .containsEntry("facilityName", "Facility D")
                    .containsEntry("tenor", updatedTenor)
                    .containsEntry("pricing", updatedPricing)
                    .containsEntry("creditSpreadAdj", updatedCreditSpreadAdj);
                assertThat(((Map) updatedMap.get("facilityPurpose")))
                    .containsEntry("id", updatedFacilityPurposeId)
                    .containsEntry("option", updatedFacilityPurposeName);
                assertThat(((Map) updatedMap.get("collateral")))
                        .containsEntry("id", updatedCollateralId)
                        .containsEntry("option", updatedCollateralName);
                assertThat(((Map) updatedMap.get("dayCount")))
                        .containsEntry("id", updatedDayCountId)
                        .containsEntry("option", updatedDayCountName);
                assertThat(((Map) updatedMap.get("regulatoryLoanType")))
                        .containsEntry("id", updatedRegulatoryLoanTypeId)
                        .containsEntry("option", updatedRegulatoryLoanTypeName);
                assertThat(updatedMap)
                    .containsEntry("purposeDetail", updatedPurposeDetail)
                    .containsEntry("guarInvFlag", updatedGuarInvFlag)
                    .containsEntry("patronagePayingFlag", updatedPatronagePayingFlag)
                    .containsEntry("farmCreditType", updatedFarmCreditType)
                    .containsEntry("revolverUtil", updatedRevolverUtil)
                    .containsEntry("upfrontFees", updatedUpfrontFees)
                    .containsEntry("unusedFees", updatedUnusedFees)
                    .containsEntry("amortization", updatedAmortization)
                    .containsEntry("maturityDate", updatedMaturityDate)
                    .containsEntry("renewalDate", updatedRenewalDate)
                    .containsEntry("lgdOption", updatedLgdOption);

                assertThat(new BigDecimal(updatedMap.get("facilityAmount").toString())).isEqualTo(updatedFacilityAmount);
            });

        // Verify the facility was updated.
        graphQlTester
            .document("""
            query getDealFacilityById($id: Int) {
                getDealFacilityById(id: $id) {
                    id
                    facilityName
                    facilityAmount
                    facilityType {
                        id
                        option
                    }
                    tenor
                    collateral {
                        id
                        option
                    }
                    pricing
                    creditSpreadAdj
                    facilityPurpose {
                        id
                        option
                    }
                    purposeDetail
                    dayCount {
                        id
                        option
                    }
                    regulatoryLoanType {
                        id
                        option
                    }
                    guarInvFlag
                    patronagePayingFlag
                    farmCreditType
                    revolverUtil
                    upfrontFees
                    unusedFees
                    amortization
                    maturityDate
                    renewalDate
                    lgdOption
                }
            }
            """)
            .variable("id", facilityMap.get("id"))
            .execute()
            .path("getDealFacilityById")
            .entity(Map.class)
            .satisfies(resultMap -> {
                assertThat(resultMap)
                    .isNotNull();
                assertThat(((Map) resultMap.get("facilityType")))
                    .containsEntry("id", updatedFacilityTypeId)
                    .containsEntry("option", updatedFacilityTypeName);
                assertThat(resultMap)
                    .containsEntry("facilityName", "Facility D")
                    .containsEntry("tenor", updatedTenor)
                    .containsEntry("pricing", updatedPricing)
                    .containsEntry("creditSpreadAdj", updatedCreditSpreadAdj);
                assertThat(((Map) resultMap.get("facilityPurpose")))
                    .containsEntry("id", updatedFacilityPurposeId)
                    .containsEntry("option", updatedFacilityPurposeName);
                assertThat(((Map) resultMap.get("dayCount")))
                        .containsEntry("id", updatedDayCountId)
                        .containsEntry("option", updatedDayCountName);
                assertThat(((Map) resultMap.get("collateral")))
                        .containsEntry("id", updatedCollateralId)
                        .containsEntry("option", updatedCollateralName);
                assertThat(((Map) resultMap.get("regulatoryLoanType")))
                        .containsEntry("id", updatedRegulatoryLoanTypeId)
                        .containsEntry("option", updatedRegulatoryLoanTypeName);
                assertThat(resultMap)
                    .containsEntry("purposeDetail", updatedPurposeDetail)
                    .containsEntry("guarInvFlag", updatedGuarInvFlag)
                    .containsEntry("patronagePayingFlag", updatedPatronagePayingFlag)
                    .containsEntry("farmCreditType", updatedFarmCreditType)
                    .containsEntry("revolverUtil", updatedRevolverUtil)
                    .containsEntry("upfrontFees", updatedUpfrontFees)
                    .containsEntry("unusedFees", updatedUnusedFees)
                    .containsEntry("amortization", updatedAmortization)
                    .containsEntry("maturityDate", updatedMaturityDate)
                    .containsEntry("renewalDate", updatedRenewalDate)
                    .containsEntry("lgdOption", updatedLgdOption);
                assertThat(new BigDecimal(resultMap.get("facilityAmount").toString())).isEqualTo(updatedFacilityAmount);
            });

        // Delete the facility.
        graphQlTester
            .document(String.format("""
            mutation {
                deleteDealFacility(facilityId: %d) {
                    id
                    facilityName
                    facilityAmount
                    facilityType {
                        id
                        option
                    }
                    tenor
                    collateral {
                        id
                        option
                    }
                    pricing
                    creditSpreadAdj
                    facilityPurpose {
                        id
                        option
                    }
                    purposeDetail
                    dayCount {
                        id
                        option
                    }
                    regulatoryLoanType {
                        id
                        option
                    }
                    guarInvFlag
                    patronagePayingFlag
                    farmCreditType
                    revolverUtil
                    upfrontFees
                    unusedFees
                    amortization
                    maturityDate
                    renewalDate
                    createdDate
                    updatedDate
                    lgdOption
                }
            }
            """, facilityMap.get("id")))
            .execute()
            .path("deleteDealFacility")
            .entity(LinkedHashMap.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull();
                assertThat(((Map) deletedMap.get("facilityType")))
                    .containsEntry("id", updatedFacilityTypeId)
                    .containsEntry("option", updatedFacilityTypeName);
                assertThat(deletedMap)
                    .containsEntry("facilityName", "Facility D")
                    .containsEntry("tenor", updatedTenor)
                    .containsEntry("pricing", updatedPricing)
                    .containsEntry("creditSpreadAdj", updatedCreditSpreadAdj);
                assertThat(((Map) deletedMap.get("facilityPurpose")))
                    .containsEntry("id", updatedFacilityPurposeId)
                    .containsEntry("option", updatedFacilityPurposeName);
                assertThat(((Map) deletedMap.get("dayCount")))
                        .containsEntry("id", updatedDayCountId)
                        .containsEntry("option", updatedDayCountName);
                assertThat(((Map) deletedMap.get("collateral")))
                        .containsEntry("id", updatedCollateralId)
                        .containsEntry("option", updatedCollateralName);
                assertThat(((Map) deletedMap.get("regulatoryLoanType")))
                        .containsEntry("id", updatedRegulatoryLoanTypeId)
                        .containsEntry("option", updatedRegulatoryLoanTypeName);
                assertThat(deletedMap)
                    .containsEntry("purposeDetail", updatedPurposeDetail)
                    .containsEntry("guarInvFlag", updatedGuarInvFlag)
                    .containsEntry("patronagePayingFlag", updatedPatronagePayingFlag)
                    .containsEntry("farmCreditType", updatedFarmCreditType)
                    .containsEntry("revolverUtil", updatedRevolverUtil)
                    .containsEntry("upfrontFees", updatedUpfrontFees)
                    .containsEntry("unusedFees", updatedUnusedFees)
                    .containsEntry("amortization", updatedAmortization)
                    .containsEntry("maturityDate", updatedMaturityDate)
                    .containsEntry("renewalDate", updatedRenewalDate)
                    .containsEntry("lgdOption", updatedLgdOption);
                assertThat(new BigDecimal(deletedMap.get("facilityAmount").toString())).isEqualTo(updatedFacilityAmount);
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenUnknownFacility_whenDeletingFacility_thenVerifyException() {

        graphQlTester
            .document("""
            mutation {
                deleteDealFacility(facilityId: 99) {
                    id
                    deal {
                        uid
                    }
                    facilityType {
                        id
                        option
                    }
                }
            }
            """)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Deal Facility could not be deleted because it does not exist.");
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_3)
    void givenNewDocument_whenUpdatingAndDeleting_thenVerify() {

        /*
         *  Test Deal: Kentucky Processing Plant
         */

        User currentUser = userService.getUserByUid(TEST_USER_UUID_3, true);
        DealDocument document = new DealDocument();
        document.setDeal(new Deal(2L, TEST_DEAL_UUID_2));
        document.setCategory(new DocumentCategory(1L));
        document.setDisplayName("2024 Financials - Smith Peanuts.xlsx");
        document.setDocumentName("128763817263.xlsx");
        document.setDescription("This is a test document.");
        document.setDocumentType("xlsx");
        document.setSource("M");

        // Create the new document.
        DealDocument newDocument = dealDocumentService.save(document, currentUser);

        // Get the document by id.
        Map<String, Object> documentMap = graphQlTester
            .document("""
                query getDealDocumentById($id: Int) {
                    getDealDocumentById(id: $id) {
                        id
                        deal {
                            uid
                            name
                        }
                        displayName
                        documentName
                        category {
                            id
                            name
                        }
                        documentType
                        description
                        source
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                    }
                }
            """)
            .variable("id", newDocument.getId())
            .execute()
            .path("getDealDocumentById")
            .entity(Map.class)
            .get();

        assertThat(documentMap)
            .isNotNull()
            .containsEntry("displayName", document.getDisplayName())
            .containsEntry("description", document.getDescription());

        // Update the document values.

        /*
         *  Update the deal document.
         */
        String updatedDisplayName = "2025 Financials - Smith Peanuts.xlsx";
        String updatedDescription = "This is a another test document.";

        graphQlTester
            .document(String.format("""
                mutation {
                   updateDealDocument(input: {
                        id: %d
                        deal: {
                            uid: "%s"
                        }
                        displayName: "%s"
                        description: "%s"
                    }) {
                        id
                        deal {
                            uid
                            name
                        }
                        displayName
                        documentName
                        category {
                            id
                            name
                        }
                        documentType
                        description
                        source
                        createdBy {
                            uid
                            firstName
                            lastName
                        }
                        createdDate
                        updatedBy {
                            uid
                            firstName
                            lastName
                        }
                        updatedDate
                    }
                }
            """, newDocument.getId(), TEST_DEAL_UUID_2, updatedDisplayName, updatedDescription))
            .execute()
            .path("updateDealDocument")
            .entity(LinkedHashMap.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap)
                    .isNotNull()
                    .containsEntry("displayName", updatedDisplayName)
                    .containsEntry("description", updatedDescription);
            });

        // We don't want to delete the file from the s3 bucket, so mock out that method in the AwsService
        doNothing().when(awsService).deleteDocument(any());

        // Delete the document.
        graphQlTester
            .document(String.format("""
                mutation {
                    deleteDealDocument(documentId: %d) {
                        id
                        displayName
                        description
                    }
                }
            """, newDocument.getId()))
            .execute()
            .path("deleteDealDocument")
            .entity(LinkedHashMap.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .containsEntry("id", newDocument.getId().intValue())
                    .containsEntry("displayName", updatedDisplayName)
                    .containsEntry("description", updatedDescription);
            });

        graphQlTester
            .document(String.format("""
                mutation {
                    deleteDealDocument(documentId: %d) {
                        id
                        deal {
                            uid
                        }
                        displayName
                        description
                    }
                }
            """, newDocument.getId()))
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Deal Document could not be deleted because it does not exist.");
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewUser_whenAcceptingConfidentialityAgreement_thenVerify() {
        Map<String, Object> institutionMap = insertTestInstitution(graphQlTester, "ABC Credit Association"
                , "Any Brand", "Y");

        String firstName = "George";
        String lastName = "Jetson";
        String email = "gjetson@westmonroe.com";
        String password = "password1234";
        String active = "Y";

        // Perform the GraphQL mutation for inserting a user.
        Map<String, Object> userMap = insertTestUser(graphQlTester, (String) institutionMap.get("uid"), firstName, lastName, email, password, active);

        assertThat(userMap).isNotNull();
        assertThat(userMap.get("uid")).isNotNull();
        assertThat(((Map) userMap.get("institution"))).containsEntry("uid", institutionMap.get("uid"));
        assertThat(userMap)
            .containsEntry("firstName", firstName)
            .containsEntry("lastName", lastName)
            .containsEntry("email", email)
            .containsEntry("active", active);

        Boolean result = graphQlTester
                .document(String.format("""
                    mutation {
                        agreeToConfidentialityAgreement(dealUid: "%s", confidentialityAgreementId: 1)
                    }
                    """, TEST_DEAL_UUID_2))
                .execute()
                .path("agreeToConfidentialityAgreement")
                .entity(Boolean.class)
                .get();

        assertThat(result).isTrue();
    }

}