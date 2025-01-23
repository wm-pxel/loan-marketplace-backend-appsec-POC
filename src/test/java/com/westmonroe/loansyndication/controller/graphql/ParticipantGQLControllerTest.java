package com.westmonroe.loansyndication.controller.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.activity.Activity;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import com.westmonroe.loansyndication.service.event.EventOriginationParticipantService;
import org.awaitility.Awaitility;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.DealStageEnum.*;
import static com.westmonroe.loansyndication.utils.MapUtils.getNodeValue;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.*;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class ParticipantGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Autowired
    private EventOriginationParticipantService eventOriginationParticipantService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeTestClass
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @Order(1)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenNewParticipant_whenAddingParticipantToEvent_thenVerify() {
        graphQlTester
            .document(String.format("""
            mutation {
                createEventOriginationParticipant(input: {
                    event: {
                        uid: "%s"
                    }
                    participant: {
                        uid: "%s"
                    }
                    inviteRecipient: {
                        uid: "%s"
                    }
                }) {
                    id
                    event {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    inviteRecipient {
                        uid
                        firstName
                    }
                }
            }
        """, TEST_EVENT_UUID_1, TEST_INSTITUTION_UUID_4, TEST_USER_UUID_4))
        .execute()
        .path("createEventOriginationParticipant")
        .entity(Map.class)
        .satisfies(eventParticipantMap -> {
            assertThat(((Map) eventParticipantMap.get("event")).get("uid")).isEqualTo(TEST_EVENT_UUID_1);
            assertThat(((Map) eventParticipantMap.get("participant")).get("uid")).isEqualTo(TEST_INSTITUTION_UUID_4);
        });
    }

    @Test
    @Order(2)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenLeadUpdatesDealInfo_whenLeadViewsTimeline_thenVerifyDealInfoUpdatedActivity() {

        String description = "This is an updated test description";
        BigDecimal dealAmount = BigDecimal.valueOf(33000000.33);
        String dealType = "Modification";
        Integer dealStructureId = 5;        // Syndication

        // Update some full and summary fields.
        graphQlTester
            .document(String.format("""
                mutation {
                   updateDeal(input: {
                        uid: "%s"
                        description: "%s"
                        dealAmount: %.2f
                        dealType: "%s"
                        dealStructure: {
                            id: %d
                        }
                    }) {
                        uid
                        description
                        dealAmount
                        dealType
                        dealStructure {
                            id
                            option
                        }
                    }
                }
            """, TEST_DEAL_UUID_1, description, dealAmount, dealType, dealStructureId))
            .execute()
            .path("updateDeal")
            .entity(Map.class)
            .satisfies(dealMap -> {
                assertThat(dealMap)
                        .isNotNull()
                        .containsEntry("description", description)
                        .containsEntry("dealType", dealType);
                assertThat(new BigDecimal(dealMap.get("dealAmount").toString())).isEqualTo(dealAmount);
                assertThat(((Map) dealMap.get("dealStructure"))).containsEntry("id", dealStructureId);
            });

        // Get the timeline and verify the activity.
        graphQlTester
            .document(String.format("""
            query {
                getActivitiesByDealUid(uid: "%s") {
                    id
                    deal {
                        uid
                        name
                    }
                    event {
                        uid
                        name
                        launchDate
                    }
                    participant {
                        uid
                        name
                    }
                    activityType {
                        id
                        name
                    }
                    json
                    source
                    createdBy {
                        uid
                        firstName
                        lastName
                    }
                    createdDate
                }
            }
           """, TEST_DEAL_UUID_1))
            .execute()
            .path("getActivitiesByDealUid")
            .entityList(Activity.class)
            .satisfies(activities -> {
                assertThat(activities)
                    .isNotEmpty()
                    .hasSize(2);

                assertThat(((Activity) activities.get(0)).getEvent())
                    .hasFieldOrPropertyWithValue("uid", "6f865256-e16e-441a-b495-bfb6ea856624")
                    .hasFieldOrPropertyWithValue("name", "Origination")
                    .hasFieldOrPropertyWithValue("launchDate", null);

                Map<String, Object> jsonMap = null;
                try {
                    jsonMap = mapper.readValue(activities.get(0).getJson(), Map.class);
                } catch (JsonProcessingException e) {
                    fail(e.getMessage());
                }

                assertThat(jsonMap)
                    .doesNotContainKey("full")
                    .hasSize(4);
            });

    }

    @Test
    @Order(3)
    @WithMockJwtUser(username = TEST_USER_EMAIL_4)
    void givenExistingActivities_whenParticipantViewsTimelineBeforeDealInvite_thenVerifyDealInfoUpdatedActivityWasRemoved() {

        // Get the timeline and verify the activity.
        graphQlTester
            .document(String.format("""
            query {
                getActivitiesByDealUid(uid: "%s") {
                    id
                    deal {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    activityType {
                        id
                        name
                    }
                    json
                    source
                    createdBy {
                        uid
                        firstName
                        lastName
                    }
                    createdDate
                }
            }
           """, TEST_DEAL_UUID_1))
            .execute()
            .path("getActivitiesByDealUid")
            .entityList(Activity.class)
            .satisfies(activities -> {
                assertThat(activities)
                    .hasSize(1);
            });

    }

    @Test
    @Order(4)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventLeadFacility_whenLeadUpdatesInvitationAmountAndSetsInvitation_thenVerify() {

        Long eventDealFacilityId = 2L;
        BigDecimal invitationAmount = BigDecimal.valueOf(1550000.55);

        graphQlTester
            .document(String.format("""
            mutation {
                updateEventLeadFacility(input: {
                    event: {
                        uid: "%s"
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    invitationAmount: %.2f
               }) {
                    event {
                        uid
                        name
                    }
                    eventDealFacility {
                        id
                        dealFacility {
                            id
                            facilityName
                            facilityAmount
                        }
                    }
                    invitationAmount
                    commitmentAmount
                    allocationAmount
                }
            }
            """, TEST_EVENT_UUID_1, eventDealFacilityId, invitationAmount))
            .execute()
            .path("updateEventLeadFacility")
            .entity(EventLeadFacility.class)
            .satisfies(eventLeadFacility -> {
                assertThat(eventLeadFacility)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("invitationAmount", invitationAmount)
                    .hasFieldOrPropertyWithValue("commitmentAmount", null)
                    .hasFieldOrPropertyWithValue("allocationAmount", null);
                assertThat(eventLeadFacility.getEvent())
                    .hasFieldOrPropertyWithValue("uid", TEST_EVENT_UUID_1);
                assertThat(eventLeadFacility.getEventDealFacility())
                    .hasFieldOrPropertyWithValue("id", eventDealFacilityId);
            });

        graphQlTester
            .document(String.format("""
            mutation {
               setLeadInvitationDate(eventUid: "%s") {
                    uid
                    name
                    leadInvitationDate
                    leadCommitmentDate
                    leadAllocationDate
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("setLeadInvitationDate")
            .entity(Event.class)
            .satisfies(event -> {
                assertThat(event).isNotNull();
                assertThat(event.getLeadInvitationDate()).isNotNull();
                assertThat(event.getLeadCommitmentDate()).isNull();
                assertThat(event.getLeadAllocationDate()).isNull();
            });

    }

    @Test
    @Order(5)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInDraft_whenLeadUpdatesAndSendsInvitation_thenVerify() {

        Long eventParticipantId = 9L;
        Long eventDealFacilityId = 1L;
        BigDecimal invitationAmount = BigDecimal.valueOf(1300000.55);

        graphQlTester
            .document(String.format("""
            mutation {
               updateEventParticipantFacility(input: {
                    eventParticipant: {
                        id: %d
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    invitationAmount: %.2f
                }) {
                    eventParticipant {
                        id
                    }
                    eventDealFacility {
                        id
                    }
                    invitationAmount
                }
            }
            """, eventParticipantId, eventDealFacilityId, invitationAmount))
            .execute()
            .path("updateEventParticipantFacility")
            .entity(LinkedHashMap.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap).isNotNull();
                assertThat(new BigDecimal(updatedMap.get("invitationAmount").toString())).isEqualTo(invitationAmount);
            });

        graphQlTester
            .document(String.format("""
            mutation {
                updateEventOriginationParticipant(input: {
                    id: %d
                    inviteRecipient: {
                        uid: "%s"
                    }
               }) {
                    id
                    event {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    inviteRecipient {
                        uid
                        firstName
                        lastName
                    }
                }
            }
            """, eventParticipantId, TEST_USER_UUID_4))
            .execute()
            .path("updateEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(eventParticipantMap -> {
                assertThat(eventParticipantMap).isNotNull();
                assertThat(eventParticipantMap)
                    .containsEntry("id", eventParticipantId.intValue());
                assertThat(getNodeValue(eventParticipantMap, List.of("inviteRecipient", "uid")))
                    .isEqualTo(TEST_USER_UUID_4);
            });

        graphQlTester
            .document(String.format("""
            mutation {
                sendEventParticipantInvite(eventParticipantId: %d) {
                    id
                    event {
                        uid
                        stage {
                            name
                            order
                        }
                    }
                    step {
                        id
                        name
                        order
                    }
                    inviteRecipient {
                        uid
                        firstName
                        lastName
                    }
                }
            }
            """, eventParticipantId))
            .execute()
            .path("sendEventParticipantInvite")
            .entity(Map.class)
            .satisfies(eventParticipantMap -> {
                assertThat((Map) eventParticipantMap.get("step"))
                    .containsEntry("name", STEP_2.getName())
                    .containsEntry("order", STEP_2.getOrder());
                assertThat(getNodeValue(eventParticipantMap, List.of("event", "uid"))).isEqualTo(TEST_EVENT_UUID_1);
                assertThat((Map) getNodeValue(eventParticipantMap, List.of("event", "stage")))
                    .containsEntry("name", STAGE_2.getName())
                    .containsEntry("order", STAGE_2.getOrder());
            });

    }

    @Test
    @Order(6)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInStep2_whenEventParticipantAcceptsInvitation_thenVerify() {

        Long eventParticipantId = 9L;

        graphQlTester
            .document(String.format("""
            mutation {
                acceptEventParticipantInvite(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
           """, eventParticipantId))
            .execute()
            .path("acceptEventParticipantInvite")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat((Map) participantMap.get("step"))
                    .containsEntry("name", STEP_4.getName())
                    .containsEntry("order", STEP_4.getOrder());
            });

    }

    @Test
    @Order(7)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenExistingActivitiesAfterAcceptingInvite_whenParticipantViewsTimeline_thenVerifyDealInfoUpdatedActivity() {

        String description = "This is an another updated test description";
        BigDecimal dealAmount = BigDecimal.valueOf(44000000.44);
        String dealType = "Renewal";
        Integer dealStructureId = 4;        // Participation

        // Update some full and summary fields.
        graphQlTester
            .document(String.format("""
            mutation {
               updateDeal(input: {
                    uid: "%s"
                    description: "%s"
                    dealAmount: %.2f
                    dealType: "%s"
                    dealStructure: {
                        id: %d
                    }
                }) {
                    uid
                    description
                    dealAmount
                    dealType
                    dealStructure {
                        id
                        option
                    }
                }
            }
            """, TEST_DEAL_UUID_1, description, dealAmount, dealType, dealStructureId))
            .execute()
            .path("updateDeal")
            .entity(Map.class)
            .satisfies(dealMap -> {
                assertThat(dealMap)
                        .isNotNull()
                        .containsEntry("description", description)
                        .containsEntry("dealType", dealType);
                assertThat(new BigDecimal(dealMap.get("dealAmount").toString())).isEqualTo(dealAmount);
                assertThat(((Map) dealMap.get("dealStructure"))).containsEntry("id", dealStructureId);
            });

        // Need to wait because of the async call to the activity service.
        Awaitility.await().atMost(Duration.ofSeconds(1));

        // Get the timeline and verify the activity.
        graphQlTester
            .document(String.format("""
            query {
                getActivitiesByDealUid(uid: "%s") {
                    id
                    deal {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    activityType {
                        id
                        name
                    }
                    json
                    source
                    createdBy {
                        uid
                        firstName
                        lastName
                    }
                    createdDate
                }
            }
           """, TEST_DEAL_UUID_1))
            .execute()
            .path("getActivitiesByDealUid")
            .entityList(Activity.class)
            .satisfies(activities -> {
                assertThat(activities)
                    .isNotEmpty()
                    .hasSize(4);
            });

    }

    @Test
    @Order(8)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInStep4_whenLeadApprovesEventFullDealAccess_thenVerify() {

        Long eventParticipantId = 9L;

        graphQlTester
            .document(String.format("""
            mutation {
                approveEventFullDealAccess(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
           """, eventParticipantId))
            .execute()
            .path("approveEventFullDealAccess")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat((Map) participantMap.get("step"))
                    .containsEntry("name", STEP_5.getName())
                    .containsEntry("order", STEP_5.getOrder());
            });

        EventOriginationParticipant eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);

        // Verify that the full deal access date is not null.
        assertThat(eventOriginationParticipant.getFullDealAccessDate())
            .isNotNull()
            .isOfAnyClassIn(OffsetDateTime.class);

        // Verify that there are now deal participant facility records.
        graphQlTester
            .document(String.format("""
            query {
                getEventParticipantFacilitiesByEventParticipantId(id: %d) {
                    eventParticipant {
                        id
                    }
                    eventDealFacility {
                        id
                    }
                }
            }
           """, eventParticipantId))
            .execute()
            .path("getEventParticipantFacilitiesByEventParticipantId")
            .entityList(Map.class)
            .satisfies(participantFacilityMap -> {
                assertThat(participantFacilityMap)
                    .isNotEmpty()
                    .hasSize(4);
            });
    }

    @Test
    @Order(9)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenParticipantInStep4_whenLeadLaunchesEvent_thenVerify() {

        String commitmentDate = "2024-02-19";
        String projectedCloseDate = "2024-03-19";

        graphQlTester
            .document(String.format("""
            mutation {
                launchEvent(input: {
                        uid: "%s"
                        commitmentDate: "%s"
                        projectedCloseDate: "%s"
                }) {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                    launchDate
                    commitmentDate
                    projectedCloseDate
                }
            }
           """, TEST_EVENT_UUID_1, commitmentDate, projectedCloseDate))
            .execute()
            .path("launchEvent")
            .entity(Map.class)
            .satisfies(eventMap -> {
                assertThat(eventMap)
                    .isNotEmpty()
                    .containsEntry("commitmentDate", commitmentDate)
                    .containsEntry("projectedCloseDate", projectedCloseDate);
                assertThat(eventMap.get("launchDate")).isNotNull();
                assertThat((Map) eventMap.get("stage"))
                    .containsEntry("name", STAGE_4.getName())
                    .containsEntry("order", STAGE_4.getOrder());
            });

    }

    /**
     * This is a test for the edge case of when the deal (event) is launched before full deal access was granted to the
     * participant.  In this case, the participant should be advanced to step 5.
     */
    @Test
    @Order(10)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInDraft_whenEventLaunchedBeforeDealAccessApproved_thenVerify() {

        Long eventParticipantId = 1L;
        BigDecimal invitationAmount = BigDecimal.valueOf(2750000.33);

        //Remove existing deal participant

        graphQlTester
            .document(String.format("""
            mutation {
                deleteEventOriginationParticipant(id: %d) {
                    id
                    participant {
                        uid
                        name
                    }
                }
            }
            """, eventParticipantId))
            .execute()
            .path("deleteEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(epMap -> {
                assertThat(epMap)
                    .isNotNull()
                    .isNotEmpty();
            });

        graphQlTester
            .document(String.format("""
            mutation {
                createEventOriginationParticipant(input: {
                    event: {
                        uid: "%s"
                    }
                    participant: {
                        uid: "%s"
                    }
                    inviteRecipient: {
                        uid: "%s"
                    }
                }) {
                    id
                    event {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    inviteRecipient {
                        uid
                        firstName
                    }
                }
            }
            """, TEST_EVENT_UUID_1, TEST_INSTITUTION_UUID_2, TEST_USER_UUID_3))
            .execute()
            .path("createEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(eventParticipantMap -> {
                assertThat(((Map) eventParticipantMap.get("event")).get("uid")).isEqualTo(TEST_EVENT_UUID_1);
                assertThat(((Map) eventParticipantMap.get("participant")).get("uid")).isEqualTo(TEST_INSTITUTION_UUID_2);
            });

        // Update the existing Event Participant Facilities because it won't pass validation.
        eventParticipantId = 2L;
        Long eventDealFacilityId = 1L;

        graphQlTester
            .document(String.format("""
            mutation {
               updateEventParticipantFacility(input: {
                    eventParticipant: {
                        id: %d
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    invitationAmount: %.2f
                }) {
                    eventParticipant {
                        id
                    }
                    eventDealFacility {
                        id
                    }
                    invitationAmount
                }
            }
            """, eventParticipantId, eventDealFacilityId, invitationAmount))
            .execute()
            .path("updateEventParticipantFacility")
            .entity(LinkedHashMap.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap).isNotNull();
                assertThat(new BigDecimal(updatedMap.get("invitationAmount").toString())).isEqualTo(invitationAmount);
            });

        // Participant is invited to the deal.
        graphQlTester
            .document(String.format("""
            mutation {
                sendEventParticipantInvite(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
           """, eventParticipantId))
            .execute()
            .path("sendEventParticipantInvite")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat((Map) participantMap.get("step"))
                    .containsEntry("name", STEP_2.getName())
                    .containsEntry("order", STEP_2.getOrder());
            });

        // Verify that stage has changed after deal (event) launch, as it has advanced to stage 4.
        graphQlTester
            .document(String.format("""
            query {
                getEventByUid(uid: "%s") {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("getEventByUid")
            .entity(Map.class)
            .satisfies(dealMap -> {
                assertThat(dealMap)
                    .isNotEmpty();
                assertThat((Map) dealMap.get("stage"))
                    .containsEntry("name", STAGE_4.getName())
                    .containsEntry("order", STAGE_4.getOrder());
            });

        graphQlTester
            .document(String.format("""
            mutation {
                acceptEventParticipantInvite(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
           """, eventParticipantId))
            .execute()
            .path("acceptEventParticipantInvite")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat((Map) participantMap.get("step"))
                    .containsEntry("name", STEP_5.getName())
                    .containsEntry("order", STEP_5.getOrder());
            });

    }

    @Test
    @Order(11)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventLeadFacility_whenLeadUpdatesCommitmentAmountAndSetsCommitment_thenVerify() {

        Long eventDealFacilityId = 2L;
        BigDecimal invitationAmount = BigDecimal.valueOf(1550000.55);
        BigDecimal commitmentAmount = BigDecimal.valueOf(1660000.66);

        graphQlTester
            .document(String.format("""
            mutation {
                updateEventLeadFacility(input: {
                    event: {
                        uid: "%s"
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    commitmentAmount: %.2f
               }) {
                    event {
                        uid
                        name
                    }
                    eventDealFacility {
                        id
                        dealFacility {
                            id
                            facilityName
                            facilityAmount
                        }
                    }
                    invitationAmount
                    commitmentAmount
                    allocationAmount
                }
            }
            """, TEST_EVENT_UUID_1, eventDealFacilityId, commitmentAmount))
            .execute()
            .path("updateEventLeadFacility")
            .entity(EventLeadFacility.class)
            .satisfies(eventLeadFacility -> {
                assertThat(eventLeadFacility)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("invitationAmount", invitationAmount)
                    .hasFieldOrPropertyWithValue("commitmentAmount", commitmentAmount)
                    .hasFieldOrPropertyWithValue("allocationAmount", null);
                assertThat(eventLeadFacility.getEvent())
                    .hasFieldOrPropertyWithValue("uid", TEST_EVENT_UUID_1);
                assertThat(eventLeadFacility.getEventDealFacility())
                    .hasFieldOrPropertyWithValue("id", eventDealFacilityId);
            });

        graphQlTester
            .document(String.format("""
            mutation {
               setLeadCommitmentDate(eventUid: "%s") {
                    uid
                    name
                    leadInvitationDate
                    leadCommitmentDate
                    leadAllocationDate
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("setLeadCommitmentDate")
            .entity(Event.class)
            .satisfies(event -> {
                assertThat(event).isNotNull();
                assertThat(event.getLeadInvitationDate()).isNotNull();
                assertThat(event.getLeadCommitmentDate()).isNotNull();
                assertThat(event.getLeadAllocationDate()).isNull();
            });

    }

    @Test
    @Order(12)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInStep5_whenEventParticipantSendsCommitment_thenVerify() {

        Long eventParticipantId = 9L;
        Long eventDealFacilityId = 1L;
        BigDecimal commitmentAmount1 = BigDecimal.valueOf(1700000.76);
        BigDecimal allocationAmount1 = BigDecimal.valueOf(1500000.55);

        // Set a commitment and allocation amount for the test.
        LinkedHashMap facilityMap = graphQlTester
            .document(String.format("""
            mutation {
               updateEventParticipantFacility(input: {
                    eventParticipant: {
                        id: %d
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    commitmentAmount: %.2f
                    allocationAmount: %.2f
                }) {
                    commitmentAmount
                    allocationAmount
                }
            }
            """, eventParticipantId, eventDealFacilityId, commitmentAmount1, allocationAmount1))
            .execute()
            .path("updateEventParticipantFacility")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(facilityMap).isNotNull();
        assertThat(new BigDecimal(facilityMap.get("commitmentAmount").toString())).isEqualTo(commitmentAmount1);
        assertThat(new BigDecimal(facilityMap.get("allocationAmount").toString())).isEqualTo(allocationAmount1);

        eventDealFacilityId = 2L;
        BigDecimal commitmentAmount2 = BigDecimal.valueOf(1350000.33);
        BigDecimal allocationAmount2 = BigDecimal.valueOf(1270000.22);

        // Set another commitment and allocation for the test.
        facilityMap = graphQlTester
            .document(String.format("""
            mutation {
               updateEventParticipantFacility(input: {
                    eventParticipant: {
                        id: %d
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    commitmentAmount: %.2f
                    allocationAmount: %.2f
                }) {
                    commitmentAmount
                    allocationAmount
                }
            }
            """, eventParticipantId, eventDealFacilityId, commitmentAmount2, allocationAmount2))
            .execute()
            .path("updateEventParticipantFacility")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(facilityMap).isNotEmpty();
        assertThat(new BigDecimal(facilityMap.get("commitmentAmount").toString())).isEqualTo(commitmentAmount2);
        assertThat(new BigDecimal(facilityMap.get("allocationAmount").toString())).isEqualTo(allocationAmount2);

        Map<String, Object> participantMap = graphQlTester
            .document(String.format("""
            mutation {
                sendEventCommitment(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                    totalCommitmentAmount
                    totalAllocationAmount
                }
            }
           """, eventParticipantId))
            .execute()
            .path("sendEventCommitment")
            .entity(Map.class)
            .get();

        assertThat(participantMap).isNotEmpty();
        assertThat(new BigDecimal(participantMap.get("totalCommitmentAmount").toString())).isEqualTo(commitmentAmount1.add(commitmentAmount2));
        assertThat(new BigDecimal(participantMap.get("totalAllocationAmount").toString())).isEqualTo(allocationAmount1.add(allocationAmount2));
        assertThat((Map) participantMap.get("step"))
            .containsEntry("name", STEP_6.getName())
            .containsEntry("order", STEP_6.getOrder());

    }

    @Test
    @Order(13)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventLeadFacility_whenLeadUpdatesAllocationAmountAndSetsAllocation_thenVerify() {

        Long eventDealFacilityId = 2L;
        BigDecimal invitationAmount = BigDecimal.valueOf(1550000.55);
        BigDecimal commitmentAmount = BigDecimal.valueOf(1660000.66);
        BigDecimal allocationAmount = BigDecimal.valueOf(1770000.77);

        graphQlTester
            .document(String.format("""
            mutation {
                updateEventLeadFacility(input: {
                    event: {
                        uid: "%s"
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    allocationAmount: %.2f
               }) {
                    event {
                        uid
                        name
                    }
                    eventDealFacility {
                        id
                        dealFacility {
                            id
                            facilityName
                            facilityAmount
                        }
                    }
                    invitationAmount
                    commitmentAmount
                    allocationAmount
                }
            }
            """, TEST_EVENT_UUID_1, eventDealFacilityId, allocationAmount))
            .execute()
            .path("updateEventLeadFacility")
            .entity(EventLeadFacility.class)
            .satisfies(eventLeadFacility -> {
                assertThat(eventLeadFacility)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("invitationAmount", invitationAmount)
                    .hasFieldOrPropertyWithValue("commitmentAmount", commitmentAmount)
                    .hasFieldOrPropertyWithValue("allocationAmount", allocationAmount);
                assertThat(eventLeadFacility.getEvent())
                    .hasFieldOrPropertyWithValue("uid", TEST_EVENT_UUID_1);
                assertThat(eventLeadFacility.getEventDealFacility())
                    .hasFieldOrPropertyWithValue("id", eventDealFacilityId);
            });

        graphQlTester
            .document(String.format("""
            mutation {
               setLeadAllocationDate(eventUid: "%s") {
                    uid
                    name
                    leadInvitationDate
                    leadCommitmentDate
                    leadAllocationDate
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("setLeadAllocationDate")
            .entity(Event.class)
            .satisfies(event -> {
                assertThat(event).isNotNull();
                assertThat(event.getLeadInvitationDate()).isNotNull();
                assertThat(event.getLeadCommitmentDate()).isNotNull();
                assertThat(event.getLeadAllocationDate()).isNotNull();
            });

    }

    @Test
    @Order(14)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInStep6_whenLeadSendsAllocation_thenVerify() {

        Long eventParticipantId = 9L;

        graphQlTester
            .document(String.format("""
            mutation {
                sendEventAllocation(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
           """, eventParticipantId))
            .execute()
            .path("sendEventAllocation")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat((Map) participantMap.get("step"))
                    .containsEntry("name", STEP_8.getName())
                    .containsEntry("order", STEP_8.getOrder());
            });

        graphQlTester
            .document(String.format("""
            query {
                getEventByUid(uid: "%s") {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("getEventByUid")
            .entity(Map.class)
            .satisfies(eventMap -> {
                assertThat(eventMap)
                    .isNotEmpty();
                assertThat((Map) eventMap.get("stage"))
                    .containsEntry("name", STAGE_4.getName())
                    .containsEntry("order", STAGE_4.getOrder());
            });

    }

    @Test
    @Order(15)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventInStage4_whenLeadConfirmsDraftLoanUploaded_thenVerify() {

        String commentsDueByDate = "2024-05-19";

        graphQlTester
            .document(String.format("""
            mutation {
                confirmEventDraftLoanUploaded(
                    eventUid: "%s",
                    commentsDueByDate: "%s"
                ) {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                    commentsDueByDate
                }
            }
           """, TEST_EVENT_UUID_1, commentsDueByDate))
            .execute()
            .path("confirmEventDraftLoanUploaded")
            .entity(Map.class)
            .satisfies(eventMap -> {
                assertThat(eventMap)
                    .isNotEmpty()
                    .containsEntry("commentsDueByDate", commentsDueByDate);
                assertThat((Map) eventMap.get("stage"))
                    .containsEntry("name", STAGE_5.getName())
                    .containsEntry("order", STAGE_5.getOrder());
            });

    }

    @Test
    @Order(16)
    @WithMockJwtUser(username = TEST_USER_EMAIL_4)
    void givenEventParticipantInStep7_whenLeadConfirmsDraftLoanUploaded_thenVerifyAdvancedToStep8() {

        graphQlTester
            .document(String.format("""
            query {
                getEventOriginationParticipantByEventUid(uid: "%s") {
                    id
                    participant {
                        uid
                        name
                    }
                    step {
                        id
                        name
                        order
                    }
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("getEventOriginationParticipantByEventUid")
            .entity(Map.class)
            .satisfies(eventParticipantMap -> {
                assertThat(eventParticipantMap)
                    .isNotEmpty();
                assertThat((Map) eventParticipantMap.get("step"))
                    .containsEntry("name", STEP_8.getName())
                    .containsEntry("order", STEP_8.getOrder());
            });

    }

    @Test
    @Order(17)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventInStage5_whenLeadNotifiesFinalLoanUploaded_thenVerify() {

        graphQlTester
            .document(String.format("""
            mutation {
                notifyEventFinalLoanUploaded(eventUid: "%s") {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                }
            }
           """, TEST_EVENT_UUID_1))
            .execute()
            .path("notifyEventFinalLoanUploaded")
            .entity(Map.class)
            .satisfies(eventMap -> {
                assertThat(eventMap)
                    .isNotEmpty();
                assertThat((Map) eventMap.get("stage"))
                    .containsEntry("name", STAGE_8.getName())
                    .containsEntry("order", STAGE_8.getOrder());
            });

    }

    @Test
    @Order(18)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInStep8_whenLeadConfirmsParticipantCertificateSent_thenVerify() {

        Long eventParticipantId = 9L;

        graphQlTester
            .document(String.format("""
            mutation {
                confirmEventLeadSentParticipantCertificate(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
            """, eventParticipantId))
            .execute()
            .path("confirmEventLeadSentParticipantCertificate")
            .entity(Map.class)
            .satisfies(eventOriginationParticipantMap -> {
                assertThat(eventOriginationParticipantMap)
                    .isNotEmpty();
                assertThat((Map) eventOriginationParticipantMap.get("step"))
                    .containsEntry("name", STEP_9.getName())
                    .containsEntry("order", STEP_9.getOrder());
            });

    }

    @Test
    @Order(19)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInStep9_whenParticipantConfirmsParticipantCertificateSent_thenVerify() {

        Long eventParticipantId = 9L;

        graphQlTester
            .document(String.format("""
            mutation {
                confirmEventParticipantSentParticipantCertificate(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
            """, eventParticipantId))
            .execute()
            .path("confirmEventParticipantSentParticipantCertificate")
            .entity(Map.class)
            .satisfies(eventParticipantMap -> {
                assertThat(eventParticipantMap)
                    .isNotEmpty();
                assertThat((Map) eventParticipantMap.get("step"))
                    .containsEntry("name", STEP_10.getName())
                    .containsEntry("order", STEP_10.getOrder());
            });

        // Verify that the stage was advanced to 7.
        graphQlTester
            .document(String.format("""
            query {
                getEventByUid(uid: "%s") {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                }
            }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("getEventByUid")
            .entity(Map.class)
            .satisfies(eventMap -> {
                assertThat(eventMap)
                    .isNotEmpty();
                assertThat((Map) eventMap.get("stage"))
                    .containsEntry("name", STAGE_8.getName())
                    .containsEntry("order", STAGE_8.getOrder());
            });

    }

    @Test
    @Order(20)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventParticipantInDraftAndInStage7_whenLeadUpdatesAndSendsInvitation_thenVerify() {

        Long eventParticipantId = 2L;
        BigDecimal invitationAmount = BigDecimal.valueOf(2750000.33);

        //Remove existing deal participant

        graphQlTester
            .document(String.format("""
            mutation {
                deleteEventOriginationParticipant(id: %d) {
                    id
                    participant {
                        uid
                        name
                    }
                }
            }
            """, eventParticipantId))
            .execute()
            .path("deleteEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(epMap -> {
                assertThat(epMap)
                    .isNotNull()
                    .isNotEmpty();
            });

        graphQlTester
        .document(String.format("""
            mutation {
                createEventOriginationParticipant(input: {
                    event: {
                        uid: "%s"
                    }
                    participant: {
                        uid: "%s"
                    }
                    inviteRecipient: {
                        uid: "%s"
                    }
                }) {
                    id
                    event {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    inviteRecipient {
                        uid
                        firstName
                    }
                }
            }
            """, TEST_EVENT_UUID_1, TEST_INSTITUTION_UUID_3, TEST_USER_UUID_5))
            .execute()
            .path("createEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(eventParticipantMap -> {
                assertThat(((Map) eventParticipantMap.get("event")).get("uid")).isEqualTo(TEST_EVENT_UUID_1);
                assertThat(((Map) eventParticipantMap.get("participant")).get("uid")).isEqualTo(TEST_INSTITUTION_UUID_3);
            });

        // Update the existing Event Participant Facilities because it won't pass validation.
        eventParticipantId = 3L;
        Long eventDealFacilityId = 8L;

        graphQlTester
            .document(String.format("""
            mutation {
               updateEventParticipantFacility(input: {
                    eventParticipant: {
                        id: %d
                    }
                    eventDealFacility: {
                        id: %d
                    }
                    invitationAmount: %.2f
                }) {
                    eventParticipant {
                        id
                    }
                    eventDealFacility {
                        id
                    }
                    invitationAmount
                }
            }
        """, eventParticipantId, eventDealFacilityId, invitationAmount))
            .execute()
            .path("updateEventParticipantFacility")
            .entity(LinkedHashMap.class)
            .satisfies(updatedMap -> {
                assertThat(updatedMap).isNotNull();
                assertThat(new BigDecimal(updatedMap.get("invitationAmount").toString())).isEqualTo(invitationAmount);
            });

        // Participant is invited to the deal.
        graphQlTester
            .document(String.format("""
            mutation {
                sendEventParticipantInvite(eventParticipantId: %d) {
                    id
                    step {
                        id
                        name
                        order
                    }
                }
            }
            """, eventParticipantId))
            .execute()
            .path("sendEventParticipantInvite")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat((Map) participantMap.get("step"))
                    .containsEntry("name", STEP_2.getName())
                    .containsEntry("order", STEP_2.getOrder());
            });
    }

    @Test
    @Order(21)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventInStage8_whenLeadUpdateDealDates_thenVerify() {

        String updatedProjectedLaunchDate = "2024-10-16";
        String updatedCommitmentDate = "2024-11-02";
        String updatedProjectedCloseDate = "2024-12-06";
        String updatedCommentsDueByDate = "2024-11-18";
        String updatedEffectiveDate = "2024-12-07";

        // Emulate the auto-save feature and update the declined message.
        graphQlTester
            .document(String.format("""
            mutation {
                updateEventDates(input: {
                    uid: "%s"
                    projectedLaunchDate: "%s"
                    commitmentDate: "%s"
                    projectedCloseDate: "%s"
                    commentsDueByDate: "%s"
                    effectiveDate: "%s"
               }) {
                    uid
                    projectedLaunchDate
                    commitmentDate
                    projectedCloseDate
                    commentsDueByDate
                    effectiveDate
                }
            }
            """, TEST_EVENT_UUID_1, updatedProjectedLaunchDate, updatedCommitmentDate, updatedProjectedCloseDate
                        , updatedCommentsDueByDate, updatedEffectiveDate))
                .execute()
                .path("updateEventDates")
                .entity(Map.class)
                .satisfies(dateMap -> {
                    assertThat(dateMap)
                        .isNotEmpty();
                    assertThat(dateMap)
                        .containsEntry("projectedLaunchDate", updatedProjectedLaunchDate)
                        .containsEntry("commitmentDate", updatedCommitmentDate)
                        .containsEntry("projectedCloseDate", updatedProjectedCloseDate)
                        .containsEntry("commentsDueByDate", updatedCommentsDueByDate)
                        .containsEntry("effectiveDate", updatedEffectiveDate);
                });

    }

    @Test
    @Order(22)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenEventInStage8_whenLeadClosesTheEvent_thenVerify() {

        String effectiveDate = "2024-05-14";

        graphQlTester
            .document(String.format("""
            mutation {
                closeEvent(
                    eventUid: "%s",
                    effectiveDate: "%s"
                ) {
                    uid
                    stage {
                        id
                        name
                        order
                    }
                    effectiveDate
                    closeDate
                }
            }
           """, TEST_EVENT_UUID_1, effectiveDate))
            .execute()
            .path("closeEvent")
            .entity(Map.class)
            .satisfies(eventMap -> {
                assertThat(eventMap)
                    .isNotEmpty()
                    .containsEntry("effectiveDate", effectiveDate);
                assertThat(eventMap.get("closeDate")).isNotNull();
                assertThat((Map) eventMap.get("stage"))
                    .containsEntry("name", STAGE_9.getName())
                    .containsEntry("order", STAGE_9.getOrder());
            });

    }

    @Test
    @Order(23)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenClosedDeal_whenLeadViewsTimeline_thenVerify() {

        graphQlTester
            .document(String.format("""
            query {
                getActivitiesByDealUid(uid: "%s") {
                    id
                    deal {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    activityType {
                        id
                        name
                    }
                    json
                    source
                    createdBy {
                        uid
                        firstName
                        lastName
                    }
                    createdDate
                }
            }
           """, TEST_DEAL_UUID_1))
            .execute()
            .path("getActivitiesByDealUid")
            .entityList(Activity.class)
            .satisfies(activities -> {
                assertThat(activities)
                    .isNotEmpty()
                    .hasSize(15);
            });

    }

    @Test
    @Order(24)
    @WithMockJwtUser(username = TEST_USER_EMAIL_4)
    void givenClosedDeal_whenParticipantViewsTimeline_thenVerify() {

        graphQlTester
            .document(String.format("""
            query {
                getActivitiesByDealUid(uid: "%s") {
                    id
                    deal {
                        uid
                        name
                    }
                    participant {
                        uid
                        name
                    }
                    activityType {
                        id
                        name
                    }
                    source
                    json
                    createdBy {
                        uid
                        firstName
                        lastName
                    }
                    createdDate
                }
            }
           """, TEST_DEAL_UUID_1))
            .execute()
            .path("getActivitiesByDealUid")
            .entityList(Activity.class)
            .satisfies(activities -> {
                assertThat(activities)
                    .isNotEmpty()
                    .hasSize(12);
            });

    }

    @Test
    @Order(25)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenExistingEventParticipant_whenParticipantDeclinesTheEvent_thenVerify() {

        Long eventParticipantId = 3L;
        String declinedMessage = "Test declined message.";
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Instant.now().toEpochMilli()));

        // Emulate the auto-save feature and update the declined message.
        graphQlTester
            .document(String.format("""
            mutation {
                updateEventOriginationParticipant(input: {
                    id: %d
                    declinedMessage: "%s"
               }) {
                    id
                    declinedMessage
                }
            }
            """, eventParticipantId, declinedMessage))
            .execute()
            .path("updateEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty();
                assertThat(participantMap)
                    .containsEntry("declinedMessage", declinedMessage);
            });

        graphQlTester
            .document(String.format("""
            mutation {
                declineEvent(eventParticipantId: %d) {
                    id
                    declinedFlag
                    declinedDate
                }
            }
           """, eventParticipantId))
            .execute()
            .path("declineEvent")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty()
                    .containsEntry("declinedFlag", "Y");
                assert(participantMap.get("declinedDate").toString().substring(0, 10)).equals(currentDate);
            });

    }

    @Test
    @Order(26)
    @WithMockJwtUser(username = TEST_USER_EMAIL_2)
    void givenExistingEventParticipant_whenLeadRemovesParticipantFromEvent_thenVerify() {

        Long eventParticipantId = 3L;
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Instant.now().toEpochMilli()));

        graphQlTester
            .document(String.format("""
            mutation {
                removeParticipantFromEvent(eventParticipantId: %d) {
                    id
                    removedFlag
                    removedDate
                }
            }
           """, eventParticipantId))
            .execute()
            .path("removeParticipantFromEvent")
            .entity(Map.class)
            .satisfies(participantMap -> {
                assertThat(participantMap)
                    .isNotEmpty()
                    .containsEntry("removedFlag", "Y");
                assert(participantMap.get("removedDate").toString().substring(0, 10)).equals(currentDate);
            });
    }

}