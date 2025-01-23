package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.Stage;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.event.EventService;
import com.westmonroe.loansyndication.utils.ModelUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_1;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_2;
import static com.westmonroe.loansyndication.utils.EventTypeEnum.VOTING;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@Testcontainers
class EventServiceTest {

    @Autowired
    DealService dealService;

    @Autowired
    EventService eventService;

    @Autowired
    UserService userService;

    @Autowired
    DefinitionService definitionService;

    @MockBean
    private SecurityContext securityContext;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenExistingEvents_whenGettingAll_thenVerifySize() {

        List<Event> events = eventService.getEventsByDealId(2L);
        assertThat(events)
            .isNotNull()
            .hasSize(1);
        assertThat(events.get(0).getName()).isEqualTo("Origination");
    }

    @Test
    void givenExistingEvents_whenRetrievingUnknownEvent_thenVerifyException() {

        assertThatThrownBy(() -> eventService.getEventById(99L))
                .isInstanceOf(DataNotFoundException.class);

        assertThatThrownBy(() -> eventService.getEventByUid(TEST_DUMMY_UUID))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void givenDealWithOpenEvent_whenSavingNewEvent_thenVerifyException() {

        // Get the current user for this test.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        Deal deal = dealService.getDealByUid(TEST_DEAL_UUID_1, currentUser);
        Stage stage = definitionService.getStageByOrder(STAGE_1.getOrder());
        EventType eventType = new EventType(VOTING.getId());

        // Create another event with the name "Voting Event" for this deal
        Event event = ModelUtil.createTestEvent(deal, "Voting Event", eventType, stage);

        assertThatThrownBy(() -> eventService.save(event, currentUser, SYSTEM_MARKETPLACE))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void givenExistingEvents_whenSavingEventWithExistingEventName_thenVerifyException() {

        // Get the current user for this test.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1, true);

        // Get open event for the deal and close it.
        Event openEvent = eventService.getOpenEventForDealUid(TEST_DEAL_UUID_2);
        eventService.updateCloseDates(openEvent, LocalDate.now(), currentUser);

        Deal deal = dealService.getDealByUid(TEST_DEAL_UUID_2, currentUser);
        Stage stage = definitionService.getStageByOrder(STAGE_1.getOrder());
        EventType eventType = new EventType(1L);

        // Create another event with the name "Origination" for this deal
        Event event = ModelUtil.createTestEvent(deal, "Origination", eventType, stage);

        // Exception will be generated for duplicate event name.
        assertThatThrownBy(() -> eventService.save(event, currentUser, SYSTEM_MARKETPLACE))
                .hasMessageContaining("The Event Name value already exists and must be unique.");
    }

    @Test
    void givenIncompleteEvent_whenSaving_thenVerifyException() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Get open event for the deal and close it.
        Event openEvent = eventService.getOpenEventForDealUid(TEST_DEAL_UUID_2);
        eventService.updateCloseDates(openEvent, LocalDate.now(), currentUser);

        // Simple test to make sure null deal is handled.
        assertThatThrownBy(() -> eventService.save(null, currentUser, SYSTEM_MARKETPLACE)).isInstanceOf(ValidationException.class);

        // Get the deal, event type and stage for the event.
        Deal deal = dealService.getDealByUid(TEST_DEAL_UUID_2, currentUser);
        Stage stage = definitionService.getStageByOrder(STAGE_1.getOrder());
        EventType eventType = new EventType(99L);

        Event event = ModelUtil.createTestEvent(null, "Test Event", eventType, stage);

        // Deal was not supplied for event, so an exception is thrown.
        assertThatThrownBy(() -> eventService.save(event, currentUser, SYSTEM_MARKETPLACE)).isInstanceOf(ValidationException.class);
    }

    @Test
    void givenNewEvent_whenSaved_thenVerify() {

        // New event values.
        String eventName = "Voting Event";
        Long eventTypeId = 2L;
        LocalDate projectedLaunchDate = LocalDate.parse("2024-09-18");
        LocalDate commitmentDate = LocalDate.parse("2024-09-20");
        LocalDate commentsDueByDate = LocalDate.parse("2024-09-21");
        LocalDate effectiveDate = LocalDate.parse("2024-09-22");
        LocalDate projectedCloseDate = LocalDate.parse("2024-09-23");

        // Create the current user object and deal for new event.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1, true);
        Deal deal = dealService.getDealByUid(TEST_DEAL_UUID_1, currentUser);

        // Get open event for the deal and close it.
        Event openEvent = eventService.getOpenEventForDealUid(TEST_DEAL_UUID_1);
        eventService.updateCloseDates(openEvent, LocalDate.now(), currentUser);

        // Create the event object to be saved.
        Event event = new Event();
        event.setDeal(deal);
        event.setName(eventName);
        event.setEventType(new EventType(eventTypeId));
        event.setProjectedLaunchDate(projectedLaunchDate);
        event.setCommitmentDate(commitmentDate);
        event.setCommentsDueByDate(commentsDueByDate);
        event.setEffectiveDate(effectiveDate);
        event.setProjectedCloseDate(projectedCloseDate);

        Event savedEvent = eventService.save(event, currentUser, SYSTEM_MARKETPLACE);

        assertThat(savedEvent)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", eventName)
            .hasFieldOrPropertyWithValue("projectedLaunchDate", projectedLaunchDate)
            .hasFieldOrPropertyWithValue("commitmentDate", commitmentDate)
            .hasFieldOrPropertyWithValue("commentsDueByDate", commentsDueByDate)
            .hasFieldOrPropertyWithValue("effectiveDate", effectiveDate)
            .hasFieldOrPropertyWithValue("projectedCloseDate", projectedCloseDate);
        assertThat(savedEvent.getEventType().getId()).isEqualTo(eventTypeId);
        assertThat(savedEvent.getStage().getId()).isEqualTo(1L);        // New event will always start at stage 1.

        List<Event> events = eventService.getEventsByDealId(deal.getId());
        assertThat(events)
            .isNotEmpty()
            .hasSize(2);
        assertThat(events.get(1)).isEqualTo(savedEvent);
    }

    @Test
    void givenExistingEvent_whenUpdatingEvent_thenVerifyChange() {

        String eventName = "Updated Origination to Voting Event";
        Long eventTypeId = 2L;
        LocalDate projectedLaunchDate = LocalDate.parse("2024-09-24");
        LocalDate commitmentDate = LocalDate.parse("2024-09-25");
        LocalDate commentsDueByDate = LocalDate.parse("2024-09-26");
        LocalDate effectiveDate = LocalDate.parse("2024-09-27");
        LocalDate projectedCloseDate = LocalDate.parse("2024-09-28");

        // Get an event to be updated.
        Event event = eventService.getEventById(1L);

        // Update the fields on the event.
        event.setName(eventName);
        event.setEventType(new EventType(eventTypeId));
        event.setProjectedLaunchDate(projectedLaunchDate);
        event.setCommitmentDate(commitmentDate);
        event.setCommentsDueByDate(commentsDueByDate);
        event.setEffectiveDate(effectiveDate);
        event.setProjectedCloseDate(projectedCloseDate);

        // Update the event and use the created by user as the current user.
        Event updatedEvent = eventService.update(event, event.getCreatedBy());

        assertThat(updatedEvent)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", eventName)
            .hasFieldOrPropertyWithValue("projectedLaunchDate", projectedLaunchDate)
            .hasFieldOrPropertyWithValue("commitmentDate", commitmentDate)
            .hasFieldOrPropertyWithValue("commentsDueByDate", commentsDueByDate)
            .hasFieldOrPropertyWithValue("effectiveDate", effectiveDate)
            .hasFieldOrPropertyWithValue("projectedCloseDate", projectedCloseDate);
        assertThat(updatedEvent.getEventType().getId()).isEqualTo(eventTypeId);
        assertThat(updatedEvent.getStage().getId()).isEqualTo(1L);        // Stage hs to be updated separately.

        // Update the event stage.
        Event stageUpdatedEvent = eventService.incrementEventToStage(event.getUid(), STAGE_2, event.getCreatedBy());

        assertThat(stageUpdatedEvent.getStage())
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", 2L)
            .hasFieldOrPropertyWithValue("name", STAGE_2.getName())
            .hasFieldOrPropertyWithValue("order", STAGE_2.getOrder());
    }

    @Test
    void givenExistingEvent_whenEventDeleted_thenVerifyRemoval() {

        // Get the list of events for our deal.
        List<Event> events = eventService.getEventsByDealId(1L);

        // Verify the initial event count.
        assertThat(events)
            .isNotEmpty()
            .hasSize(1);

        // Delete the event by uid and verify the removal.
        eventService.deleteById(events.get(0).getId());

        // Get the list of events for our deal after the delete.
        events = eventService.getEventsByDealId(1L);

        // Verify the new event count, which should be zero or the events will have an empty list.
        assertThat(events)
            .isEmpty();
    }

    @Test
    void givenExistingEvents_whenDeletingNonExistentEvent_thenVerifyNoError() {

        assertThatNoException().isThrownBy(() -> { eventService.deleteById(99L); });

        assertThatNoException().isThrownBy(() -> {
            eventService.deleteByUid(TEST_DUMMY_UUID);
        });
    }

}