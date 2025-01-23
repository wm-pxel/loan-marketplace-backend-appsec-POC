package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import com.westmonroe.loansyndication.security.WithMockJwtUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_1;
import static com.westmonroe.loansyndication.utils.EventTypeEnum.SIMPLE_RENEWAL;
import static com.westmonroe.loansyndication.utils.EventTypeEnum.VOTING;
import static com.westmonroe.loansyndication.utils.GraphQLUtil.insertTestEvent;
import static com.westmonroe.loansyndication.utils.GraphQLUtil.insertTestEventOriginationParticipant;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_1;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@AutoConfigureHttpGraphQlTester
@Testcontainers
class EventGQLControllerTest {

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_1)
    void givenNewEvent_whenPerformingCrudOperations_thenVerify() {

        graphQlTester
            .document(String.format("""
                mutation {
                    deleteEvent(uid: "%s") {
                        uid
                        name
                    }
                }
            """, TEST_EVENT_UUID_1))
            .execute()
            .path("deleteEvent")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty()
                    .containsEntry("uid", TEST_EVENT_UUID_1);
            });

        String name = "Test Voting Event";
        Long eventTypeId = VOTING.getId();
        String projectedLaunchDate = "2024-09-19";
        String commitmentDate = "2024-10-01";
        String commentsDueByDate = "2024-10-20";
        String effectiveDate = "2024-11-07";
        String projectedCloseDate = "2024-11-15";

        // Perform the GraphQL mutation for inserting a deal.
        Map<String, Object> eventMap = insertTestEvent(graphQlTester, TEST_DEAL_UUID_1, name, eventTypeId, projectedLaunchDate
                , commitmentDate, commentsDueByDate, effectiveDate, projectedCloseDate);

        assertThat(eventMap).isNotNull();
        assertThat(eventMap.get("uid")).isNotNull();
        assertThat(eventMap)
            .containsEntry("name", name)
            .containsEntry("projectedLaunchDate", projectedLaunchDate)
            .containsEntry("commitmentDate", commitmentDate)
            .containsEntry("commentsDueByDate", commentsDueByDate)
            .containsEntry("effectiveDate", effectiveDate)
            .containsEntry("projectedCloseDate", projectedCloseDate);
        assertThat(((Map) eventMap.get("eventType")))
            .containsEntry("id", VOTING.getId().intValue())
            .containsEntry("name", VOTING.getName());
        assertThat(((Map) eventMap.get("stage")))
            .containsEntry("name", STAGE_1.getName());

        // Verify that event lead facilities were created.  This is a voting event, so there will be zero.
        graphQlTester
            .document("""
            query getEventLeadFacilitiesByEventUid($uid: String!) {
                getEventLeadFacilitiesByEventUid(uid: $uid) {
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
            """)
            .variable("uid", eventMap.get("uid").toString())
            .execute()
            .path("getEventLeadFacilitiesByEventUid")
            .entityList(EventLeadFacility.class)
            .satisfies(elfList -> {
                assertThat(elfList)
                    .isNotNull()
                    .hasSize(0);
            });

        // Get the event by its UID.
        Event event = graphQlTester
            .document("""
                query getEventByUid($uid: String!) {
                    getEventByUid(uid: $uid) {
                        uid
                        deal {
                            uid
                            name
                        }
                        name
                        eventType {
                            id
                            name
                        }
                        stage {
                            id
                            name
                        }
                    }
                }
                """)
                .variable("uid", eventMap.get("uid"))
                .execute()
                .path("getEventByUid")
                .entity(Event.class)
                .get();

        assertThat(event)
            .isNotNull()
            .hasFieldOrPropertyWithValue("uid", eventMap.get("uid"))
            .hasFieldOrPropertyWithValue("name", name);
        assertThat(event.getEventType())
            .hasFieldOrPropertyWithValue("name", VOTING.getName());
        assertThat(event.getStage())
            .hasFieldOrPropertyWithValue("name", STAGE_1.getName());

        String updatedName = "Test Simple Renewal Event";
        Long updatedEventTypeId = SIMPLE_RENEWAL.getId();
        String updatedProjectedLaunchDate = "2024-09-22";
        String updatedCommitmentDate = "2024-10-04";
        String updatedCommentsDueByDate = "2024-10-23";
        String updatedEffectiveDate = "2024-11-09";
        String updatedProjectedCloseDate = "2024-11-18";

        // Update the event.
        LinkedHashMap updatedMap = graphQlTester
            .document(String.format("""
                mutation {
                   updateEvent(input: {
                        uid: "%s"
                        name: "%s"
                        eventType: {
                            id: %d
                        }
                        projectedLaunchDate: "%s"
                        commitmentDate: "%s"
                        commentsDueByDate: "%s"
                        effectiveDate: "%s"
                        projectedCloseDate: "%s"
                    }) {
                        uid
                        name
                        eventType {
                            id
                            name
                        }
                        stage {
                            id
                            name
                        }
                        projectedLaunchDate
                        commitmentDate
                        commentsDueByDate
                        effectiveDate
                        projectedCloseDate
                    }
                }
            """, event.getUid(), updatedName, updatedEventTypeId, updatedProjectedLaunchDate, updatedCommitmentDate
            , updatedCommentsDueByDate, updatedEffectiveDate, updatedProjectedCloseDate))
            .execute()
            .path("updateEvent")
            .entity(LinkedHashMap.class)
            .get();

        assertThat(updatedMap)
            .isNotNull()
            .containsEntry("uid", event.getUid())
            .containsEntry("name", updatedName)
            .containsEntry("projectedLaunchDate", updatedProjectedLaunchDate)
            .containsEntry("commitmentDate", updatedCommitmentDate)
            .containsEntry("commentsDueByDate", updatedCommentsDueByDate)
            .containsEntry("effectiveDate", updatedEffectiveDate)
            .containsEntry("projectedCloseDate", updatedProjectedCloseDate);
        assertThat(((Map) updatedMap.get("eventType")))
            .containsEntry("name", SIMPLE_RENEWAL.getName());
        assertThat(((Map) updatedMap.get("stage")))
            .containsEntry("name", STAGE_1.getName());

        // Delete the event.
        graphQlTester
            .document(String.format("""
                mutation {
                    deleteEvent(uid: "%s") {
                        uid
                        name
                    }
                }
            """, event.getUid()))
            .execute()
            .path("deleteEvent")
            .entity(Map.class)
            .satisfies(deletedMap -> {
                assertThat(deletedMap)
                    .isNotNull()
                    .isNotEmpty()
                    .containsEntry("uid", event.getUid())
                    .containsEntry("name", updatedName);
            });

        // Verify the event is deleted.
        graphQlTester
            .document("""
                query getEventByUid($uid: String!) {
                    getEventByUid(uid: $uid) {
                        uid
                        name
                    }
                }
            """)
            .variable("uid", event.getUid())
            .execute().errors().satisfy(errors -> {
                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getMessage()).isEqualTo("Event was not found for uid.");
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_3)   // Leon T. (Tim) Amerson (AgFirst Farm Credit Bank)
    void givenExistingEvent_whenCreatingUpdatingAndDeletingParticipant_thenVerify() {

        /*
         *  Test Deal: Kentucky Processing Plant
         */

        String participantUid1 = TEST_INSTITUTION_UUID_1;                   // Farm Credit Bank of Texas
        String message1 = "Test message 1";
        String response1 = "Test response 1";

        String participantUid2 = TEST_INSTITUTION_UUID_3;                   // Horizon Farm Credit, ACA
        String inviteRecipient2 = "0a5a099b-ee01-4e34-81a5-91421bb1a104";       // Benjamin Bucks
        String message2 = "Test message 2";
        String response2 = "Test response 2";

        /*
         *  Inserting the event participant.
         */
        Map<String, Object> eventParticipant1 = insertTestEventOriginationParticipant(graphQlTester, TEST_EVENT_UUID_2, participantUid1, TEST_USER_UUID_1, message1, response1);
        assertThat(eventParticipant1).isNotNull();

        Map<String, Object> eventParticipant2 = insertTestEventOriginationParticipant(graphQlTester, TEST_EVENT_UUID_2, participantUid2, inviteRecipient2, message2, response2);
        assertThat(eventParticipant2).isNotNull();

        // Get the participant users by the event uid.
        List<Map> participants = graphQlTester
                .document("""
                query getEventOriginationParticipantsByEventUid($uid: String!) {
                    getEventOriginationParticipantsByEventUid(uid: $uid) {
                        id
                        event {
                            uid
                            name
                            stage {
                              order
                              name
                              id
                            }
                            deal {
                                originator {
                                    uid
                                    name
                                }
                            }
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
                        message
                        response
                        step {
                            id
                            name
                            order
                        }
                    }
                }
            """)
            .variable("uid", TEST_EVENT_UUID_2)
            .execute()
            .path("getEventOriginationParticipantsByEventUid")
            .entityList(Map.class)
            .get();

        assertThat(participants)
            .isNotNull()
            .hasSize(2);

        assertThat(((Map) participants.get(1)).get("event"))
            .hasFieldOrPropertyWithValue("uid", TEST_EVENT_UUID_2);
        assertThat(((Map) participants.get(1)).get("participant"))
            .hasFieldOrPropertyWithValue("uid", participantUid2);
        assertThat(((Map) participants.get(1)).get("inviteRecipient"))
            .hasFieldOrPropertyWithValue("uid", inviteRecipient2);
        assertThat(((Map) participants.get(1)).get("step"))
            .hasFieldOrPropertyWithValue("name", STEP_1.getName())
            .hasFieldOrPropertyWithValue("order", STEP_1.getOrder());
        assertThat(participants.get(1))
            .containsEntry("id", 10)
            .containsEntry("message", message2)
            .containsEntry("response", response2);

        // Verify that event participant facilities were created.
        graphQlTester
            .document("""
            query getEventParticipantFacilitiesByEventParticipantId($id: Int!) {
                getEventParticipantFacilitiesByEventParticipantId(id: $id) {
                    eventParticipant {
                        event {
                            uid
                            name
                        }
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
            """)
            .variable("id", Long.valueOf(eventParticipant1.get("id").toString()))
            .execute()
            .path("getEventParticipantFacilitiesByEventParticipantId")
            .entityList(EventParticipantFacility.class)
            .satisfies(epfList -> {
                assertThat(epfList)
                    .isNotNull()
                    .hasSize(3);
            });

        Long participantId = Long.valueOf(participants.get(1).get("id").toString());
        String updatedParticipantUid2 = "383ebe76-fba7-4563-befe-5dde11431c09";         // River Valley AgCredit, ACA
        String updatedInviteRecipient2 = "503d6ef2-8197-4eda-ba1f-267bf00e5bc1";        // Georgia Washington
        String updatedMessage2 = "Test message 3";
        String updatedResponse2 = "Test response 3";

        // Update the second participant.
        graphQlTester
            .document(String.format("""
            mutation {
                updateEventOriginationParticipant(input: {
                    id: %d
                    participant: {
                        uid: "%s"
                    }
                    inviteRecipient: {
                        uid: "%s"
                    }
                    message: "%s"
                    response: "%s"
               }) {
                    id
                    event {
                        uid
                        name
                        stage {
                          order
                          name
                          id
                        }
                        deal {
                            originator {
                                uid
                                name
                            }
                        }
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
                    message
                    response
                }
            }
                """, participantId, updatedParticipantUid2, updatedInviteRecipient2, updatedMessage2, updatedResponse2))
            .execute()
            .path("updateEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(dpdMap -> {
                assertThat(dpdMap)
                    .isNotNull()
                    .isNotEmpty()
                    .containsEntry("message", updatedMessage2)
                    .containsEntry("response", updatedResponse2);
                assertThat(Long.valueOf(dpdMap.get("id").toString())).isEqualTo(participantId);
                assertThat((Map) dpdMap.get("event"))
                    .containsEntry("uid", TEST_EVENT_UUID_2);
                assertThat((Map) dpdMap.get("participant"))
                    .containsEntry("uid", updatedParticipantUid2);
                assertThat((Map) dpdMap.get("inviteRecipient"))
                    .containsEntry("uid", updatedInviteRecipient2);
            });

        // Delete the first participant from the deal.
        graphQlTester
            .document(String.format("""
            mutation {
                deleteEventOriginationParticipant(id: %d) {
                    id
                }
            }
            """, participantId))
            .execute()
            .path("deleteEventOriginationParticipant")
            .entity(Map.class)
            .satisfies(dpMap -> {
                assertThat(dpMap)
                    .isNotNull()
                    .isNotEmpty();
            });

        // Verify the participant list size for the event.
        graphQlTester
            .document("""
            query getEventOriginationParticipantsByEventUid($uid: String!) {
                getEventOriginationParticipantsByEventUid(uid: $uid) {
                    id
                }
            }
            """)
            .variable("uid", TEST_EVENT_UUID_2)
            .execute()
            .path("getEventOriginationParticipantsByEventUid")
            .entityList(EventOriginationParticipant.class)
            .satisfies(dpList -> {
                assertThat(dpList)
                    .isNotNull()
                    .hasSize(1);
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_3)   // Leon T. (Tim) Amerson (AgFirst Farm Credit Bank)
    void givenExistingEvent_whenRetrievingAndUpdatingLeadFacilities_thenVerify() {

        String eventUid = "3add224e-0c1a-46bc-95d1-533fb873226b";       // Origination event for Deal

        // Get the lead facilities were for the event.
        List<EventLeadFacility> eventLeadFacilities = graphQlTester
            .document("""
            query getEventLeadFacilitiesByEventUid($uid: String!) {
                getEventLeadFacilitiesByEventUid(uid: $uid) {
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
            """)
            .variable("uid", eventUid)
            .execute()
            .path("getEventLeadFacilitiesByEventUid")
            .entityList(EventLeadFacility.class)
            .get();

        assertThat(eventLeadFacilities)
            .isNotNull()
            .hasSize(4);

        EventLeadFacility eventLeadFacility = graphQlTester
            .document(String.format("""
            query {
                getEventLeadFacilityByEventDealFacility(input: {
                    event: {
                        uid: "%s"
                    }
                    dealFacility: {
                        id: %d
                    }
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
            """, eventLeadFacilities.get(0).getEvent().getUid(), eventLeadFacilities.get(0).getEventDealFacility().getId()))
            .execute()
            .path("getEventLeadFacilityByEventDealFacility")
            .entity(EventLeadFacility.class)
            .get();

        assertThat(eventLeadFacility).isNotNull();
        assertThat(eventLeadFacility.getEvent())
            .hasFieldOrPropertyWithValue("uid", eventLeadFacilities.get(0).getEvent().getUid())
            .hasFieldOrPropertyWithValue("name", eventLeadFacilities.get(0).getEvent().getName());

        // Set the amounts for the update
        BigDecimal invitationAmount = BigDecimal.valueOf(1220000.22);
        BigDecimal commitmentAmount = BigDecimal.valueOf(1110000.11);
        BigDecimal allocationAmount = BigDecimal.valueOf(1330000.33);

        // Update the event lead facility.
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
                    commitmentAmount: %.2f
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
            """, eventLeadFacility.getEvent().getUid(), eventLeadFacility.getEventDealFacility().getId()
               , invitationAmount, commitmentAmount, allocationAmount))
            .execute()
            .path("updateEventLeadFacility")
            .entity(EventLeadFacility.class)
            .satisfies(elf -> {
                assertThat(elf)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("invitationAmount", invitationAmount)
                    .hasFieldOrPropertyWithValue("commitmentAmount", commitmentAmount)
                    .hasFieldOrPropertyWithValue("allocationAmount", allocationAmount);
                assertThat(elf.getEvent())
                    .hasFieldOrPropertyWithValue("uid", eventLeadFacility.getEvent().getUid())
                    .hasFieldOrPropertyWithValue("name", eventLeadFacility.getEvent().getName());
                assertThat(elf.getEventDealFacility().getDealFacility())
                    .hasFieldOrPropertyWithValue("id", eventLeadFacility.getEventDealFacility().getDealFacility().getId())
                    .hasFieldOrPropertyWithValue("facilityName", eventLeadFacility.getEventDealFacility().getDealFacility().getFacilityName());
            });
    }

    @Test
    @WithMockJwtUser(username = TEST_USER_EMAIL_3)   // Leon T. (Tim) Amerson (AgFirst Farm Credit Bank)
    void givenExistingEvent_whenUpdatingLeadFacilityAllocations_thenVerify() {

        String eventUid = "3add224e-0c1a-46bc-95d1-533fb873226b";       // Origination event for Deal

        // Get the lead facilities were for the event.
        List<EventLeadFacility> eventLeadFacilities = graphQlTester
            .document("""
            query getEventLeadFacilitiesByEventUid($uid: String!) {
                getEventLeadFacilitiesByEventUid(uid: $uid) {
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
            """)
            .variable("uid", eventUid)
            .execute()
            .path("getEventLeadFacilitiesByEventUid")
            .entityList(EventLeadFacility.class)
            .get();

        assertThat(eventLeadFacilities)
            .isNotNull()
            .hasSize(4);

        // Assign updated allocation amount values.
        BigDecimal allocationAmount1 = BigDecimal.valueOf(1110000.11);
        BigDecimal allocationAmount2 = BigDecimal.valueOf(1220000.22);
        BigDecimal allocationAmount3 = BigDecimal.valueOf(1330000.33);
        BigDecimal allocationAmount4 = BigDecimal.valueOf(1440000.44);

        // Update the event lead facility.
        graphQlTester
            .document(String.format("""
            mutation {
                updateEventLeadFacilityAllocations(eventUid: "%s", allocations: [
                    { eventDealFacility: { id: 8 }, allocationAmount: %.2f },
                    { eventDealFacility: { id: 9 }, allocationAmount: %.2f },
                    { eventDealFacility: { id: 10 }, allocationAmount: %.2f },
                    { eventDealFacility: { id: 11 }, allocationAmount: %.2f }
                ]) {
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
            """, eventUid, allocationAmount1, allocationAmount2, allocationAmount3, allocationAmount4))
            .execute()
            .path("updateEventLeadFacilityAllocations")
            .entityList(EventLeadFacility.class)
            .satisfies(elf -> {
                assertThat(elf)
                    .isNotNull();
                assertThat(elf.stream().filter(e -> e.getEventDealFacility().getId() == 8).findFirst().get().getAllocationAmount())
                    .isEqualTo(allocationAmount1);
                assertThat(elf.stream().filter(e -> e.getEventDealFacility().getId() == 9).findFirst().get().getAllocationAmount())
                    .isEqualTo(allocationAmount2);
                assertThat(elf.stream().filter(e -> e.getEventDealFacility().getId() == 10).findFirst().get().getAllocationAmount())
                    .isEqualTo(allocationAmount3);
                assertThat(elf.stream().filter(e -> e.getEventDealFacility().getId() == 11).findFirst().get().getAllocationAmount())
                    .isEqualTo(allocationAmount4);
            });
    }

}