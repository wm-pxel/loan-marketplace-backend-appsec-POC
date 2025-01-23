package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_1;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_3;
import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_EMAIL_1;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@Testcontainers
class DefinitionGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingNaicsCodes_whenGettingAll_thenVerifySize() {
        graphQlTester
            .document("""
            query {
                allNaicsCodes {
                    code
                    title
                }
            }
            """)
            .execute()
            .path("allNaicsCodes")
            .entityList(NaicsCode.class)
            .satisfies(codes -> assertThat(codes).hasSize(1012));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingNaicsCodes_whenGettingNaicsCode_thenVerify() {

        String code = "339920";
        String title = "Sporting and Athletic Goods Manufacturing";

        graphQlTester
            .document(String.format("""
                query {
                    getNaicsCodeByCode(code: "%s") {
                        code
                        title
                    }
                }
                """, code))
            .execute()
            .path("getNaicsCodeByCode")
            .entity(NaicsCode.class)
            .satisfies(naics -> {
                assertThat(naics).isNotNull();
                assertThat(naics.getCode()).isEqualTo(code);
                assertThat(naics.getTitle()).isEqualTo(title);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingNaicsCodes_whenSearchingByTitle_thenVerifySize() {
        graphQlTester
            .document("""
            query {
                searchNaicsCodesByTitle(title: "%Sport%") {
                    code
                    title
                }
            }
            """)
            .execute()
            .path("searchNaicsCodesByTitle")
            .entityList(NaicsCode.class)
            .satisfies(codes -> assertThat(codes).hasSize(41));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenInitialLenderList_whenRetrievingAll_thenVerifySize() {

        graphQlTester
            .document("""
            query {
                allInitialLenders {
                    id
                    lenderName
                    active
                }
            }
            """)
            .execute()
            .path("allInitialLenders")
            .entityList(InitialLender.class)
            .satisfies(lenders -> {
                assertThat(lenders)
                        .isNotNull()
                        .hasSize(10);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewInitialLender_whenPerformingCrudOperations_thenVerify() {

        String lenderName = "Citizens Financial Group";
        String active = "Y";

        // Perform the GraphQL mutation for inserting an initial lender.
        LinkedHashMap lenderMap = graphQlTester
            .document(String.format("""
                mutation {
                   createInitialLender(input: {
                        lenderName: "%s"
                        active: "%s"
                    }) {
                        id
                        lenderName
                        createdDate
                        updatedDate
                        active
                    }
                }
                """, lenderName, active))
            .execute()
            .path("createInitialLender")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(lenderMap)
                .isNotNull()
                .containsEntry("lenderName", lenderName)
                .containsEntry("active", active);

        // Get the initial lender by its ID.
        LinkedHashMap resultMap = graphQlTester
            .document("""
                query getInitialLenderById($lenderId: Int!) {
                    getInitialLenderById(lenderId: $lenderId) {
                        id
                        lenderName
                        active
                    }
                }
                """)
            .variable("lenderId", lenderMap.get("id"))
            .execute()
            .path("getInitialLenderById")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(resultMap)
                .isNotNull()
                .containsEntry("id", lenderMap.get("id"));

        String updatedLenderName = "M&T Bank";
        String updatedActive = "N";

        // Update the initial lender.
        LinkedHashMap updatedMap = graphQlTester
            .document(String.format("""
                mutation {
                   updateInitialLender(input: {
                        id: %d
                        lenderName: "%s"
                        active: "%s"
                    }) {
                        id
                        lenderName
                        active
                    }
                }
                """, lenderMap.get("id"), updatedLenderName, updatedActive))
            .execute()
            .path("updateInitialLender")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(updatedMap)
                .isNotNull()
                .containsEntry("id", lenderMap.get("id"))
                .containsEntry("lenderName", updatedLenderName)
                .containsEntry("active", updatedActive);

        // Delete the initial lender.
        graphQlTester
            .document(String.format("""
                mutation {
                   deleteInitialLender(lenderId: %d) {
                        id
                        lenderName
                        active
                    }
                }
            """, lenderMap.get("id")))
            .execute()
            .path("deleteInitialLender")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                        .isNotNull()
                        .isNotEmpty()
                        .containsEntry("id", lenderMap.get("id"));
            });

        // Verify the initial lender is deleted.
        graphQlTester
            .document("""
                query getInitialLenderById($lenderId: Int!) {
                    getInitialLenderById(lenderId: $lenderId) {
                        id
                        lenderName
                        active
                    }
                }
            """)
            .variable("lenderId", lenderMap.get("id"))
            .execute().errors().satisfy(errors -> {
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getMessage()).isEqualTo("Initial Lender was not found for id.");
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenUnknownInitialLender_whenDeletingInitialLender_thenVerifyException() {

        graphQlTester
            .document("""
                mutation {
                   deleteInitialLender(lenderId: 99) {
                        id
                        lenderName
                        active
                    }
                }
            """)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertThat(errors).isNotEmpty();
                assertThat(errors.get(0).getMessage()).isEqualTo("Initial Lender could not be deleted because it does not exist.");
            });

    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingInitialLenders_whenSearchingByLenderName_thenVerifySize() {
        graphQlTester
            .document("""
                query {
                    searchInitialLendersByLender(lenderName: "%USA%") {
                        id
                        lenderName
                    }
                }
            """)
            .execute()
            .path("searchInitialLendersByLender")
            .entityList(InitialLender.class)
            .satisfies(lenders -> assertThat(lenders).hasSize(2));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingRoles_whenGettingAll_thenVerifySize() {
        graphQlTester
            .document("""
                query {
                    allRoles {
                        id
                        code
                        name
                        description
                    }
                }
                """)
            .execute()
            .path("allRoles")
            .entityList(Role.class)
            .satisfies(roles -> assertThat(roles).hasSize(14));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingStages_whenGettingAll_thenVerifySize() {
        graphQlTester
            .document("""
                query {
                    allStages {
                        id
                        name
                        order
                    }
                }
                """)
            .execute()
            .path("allStages")
            .entityList(Stage.class)
            .satisfies(stages -> assertThat(stages).hasSize(9));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingParticipantSteps_whenGettingAll_thenVerifySize() {
        graphQlTester
            .document("""
                query {
                    allParticipantSteps {
                        id
                        name
                        leadViewStatus
                        participantStatus
                        order
                    }
                }
                """)
            .execute()
            .path("allParticipantSteps")
            .entityList(ParticipantStep.class)
            .satisfies(steps -> assertThat(steps).hasSize(12));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingParticipantStep_whenGettingById_thenVerify() {
        ParticipantStep participantStep = graphQlTester
            .document("""
                query {
                    getParticipantStepById(id: 1) {
                        id
                        name
                        leadViewStatus
                        participantStatus
                        order
                    }
                }
                """)
            .execute()
            .path("getParticipantStepById")
            .entity(ParticipantStep.class)
            .get();

        assertThat(participantStep)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", 1L)
            .hasFieldOrPropertyWithValue("name", STEP_1.getName())
            .hasFieldOrPropertyWithValue("leadViewStatus", "Complete the draft invitation to add a participant")
            .hasFieldOrPropertyWithValue("participantStatus", null)
            .hasFieldOrPropertyWithValue("order", 1);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingParticipantStep_whenGettingByOrder_thenVerify() {
        ParticipantStep participantStep = graphQlTester
            .document("""
                query {
                    getParticipantStepByOrder(order: 3) {
                        id
                        name
                        leadViewStatus
                        participantStatus
                        order
                    }
                }
            """)
            .execute()
            .path("getParticipantStepByOrder")
            .entity(ParticipantStep.class)
            .get();

        assertThat(participantStep)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", 3L)
            .hasFieldOrPropertyWithValue("name", STEP_3.getName())
            .hasFieldOrPropertyWithValue("leadViewStatus", "Approve full deal access to interested participant")
            .hasFieldOrPropertyWithValue("participantStatus", "Interest sent. Wait for the deal to be launched to view full deal Information.")
            .hasFieldOrPropertyWithValue("order", 3);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingPicklistCategories_whenGettingAll_thenVerifySize() {
        graphQlTester
            .document("""
                query {
                    allPicklistCategories {
                        id
                        name
                    }
                }
            """)
            .execute()
            .path("allPicklistCategories")
            .entityList(PicklistCategory.class)
            .satisfies(categories -> assertThat(categories).hasSize(9));
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingPicklists_whenGettingByCategoryId_thenVerifySize() {

        Long categoryId = 2L;

        List<PicklistItem> picklist = graphQlTester
            .document(String.format("""
                query {
                    getPicklistByCategoryId(categoryId: %d) {
                        id
                        category {
                            id
                            name
                        }
                        option
                        order
                    }
                }
            """, categoryId))
            .execute()
            .path("getPicklistByCategoryId")
            .entityList(PicklistItem.class)
            .get();

        assertThat(picklist)
            .isNotNull()
            .isNotEmpty()
            .hasSize(2);
        assertThat(picklist.get(0).getCategory().getId()).isEqualTo(categoryId);
        assertThat(picklist.get(1))
            .hasFieldOrPropertyWithValue("id", 5L)
            .hasFieldOrPropertyWithValue("option", "Syndication")
            .hasFieldOrPropertyWithValue("order", 2);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingPicklists_whenGettingByCategoryName_thenVerifySize() {

        String categoryName = "Farm Credit Eligibility";

        List<PicklistItem> picklist = graphQlTester
            .document(String.format("""
                query {
                    getPicklistByCategoryName(categoryName: "%s") {
                        id
                        category {
                            id
                            name
                        }
                        option
                        order
                    }
                }
            """, categoryName))
            .execute()
            .path("getPicklistByCategoryName")
            .entityList(PicklistItem.class)
            .get();

        assertThat(picklist)
                .isNotNull()
                .isNotEmpty()
                .hasSize(2);
        assertThat(picklist.get(0).getCategory().getName()).isEqualTo(categoryName);
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenExistingEventTypes_whenGettingAllAndOne_thenVerifySizeAndEventType() {

        graphQlTester
            .document("""
                query {
                    allEventTypes {
                        id
                        name
                    }
                }
            """)
            .execute()
            .path("allEventTypes")
            .entityList(EventType.class)
            .satisfies(types -> assertThat(types).hasSize(4));

        EventType eventType = graphQlTester
            .document(String.format("""
                query {
                    getEventTypeById(id: %d) {
                        id
                        name
                    }
                }
            """, 3))
            .execute()
            .path("getEventTypeById")
            .entity(EventType.class)
            .get();

        assertThat(eventType)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", "Simple Renewal");
    }

}