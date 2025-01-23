package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.event.*;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.event.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.ALLOCATION_AMOUNT_SET;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;

@Controller
@Slf4j
@Validated
public class EventGQLController {

    private final EventService eventService;
    private final EventParticipantService eventParticipantService;
    private final EventParticipantFacilityService eventParticipantFacilityService;
    private final EventLeadFacilityService eventLeadFacilityService;
    private final EventDealFacilityService eventDealFacilityService;
    private final AuthorizationService authorizationService;
    private final ActivityService activityService;
    private final EventOriginationParticipantService eventOriginationParticipantService;

    public EventGQLController(EventService eventService, EventParticipantService eventParticipantService
            , EventParticipantFacilityService eventParticipantFacilityService, EventLeadFacilityService eventLeadFacilityService
            , EventDealFacilityService eventDealFacilityService, AuthorizationService authorizationService
            , ActivityService activityService, EventOriginationParticipantService eventOriginationParticipantService) {
        this.eventService = eventService;
        this.eventParticipantService = eventParticipantService;
        this.eventParticipantFacilityService = eventParticipantFacilityService;
        this.eventLeadFacilityService = eventLeadFacilityService;
        this.eventDealFacilityService = eventDealFacilityService;
        this.authorizationService = authorizationService;
        this.activityService = activityService;
        this.eventOriginationParticipantService = eventOriginationParticipantService;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Event> getEventsByDealUid(@Argument String dealUid, @AuthenticationPrincipal User currentUser) {

        // Authorize the user for this deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        return eventService.getEventsByDealId(dealEvent.getId());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Event getEventByUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        // Authorize the user for this event.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, uid);

        return event;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Event createEvent(@Argument @Valid Event input, @AuthenticationPrincipal User currentUser) {
        return eventService.save(input, currentUser, SYSTEM_MARKETPLACE);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Event updateEvent(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {

        // Make sure this user has permissions to this deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, input.get("uid").toString());

        return eventService.update(input, currentUser, SYSTEM_MARKETPLACE);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Event deleteEvent(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        Event event;

        try {

            // Make sure this user has permissions to this event.
            event = authorizationService.authorizeUserForDealByEventUid(currentUser, uid);

        } catch ( DataNotFoundException e ) {
            // Throw specific error and message when event not found.
            throw new DataNotFoundException("Event could not be deleted because it does not exist.");
        }

        // Delete the event.
        eventService.deleteById(event.getId());

        return event;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EventOriginationParticipant getEventOriginationParticipantByEventUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        return eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(uid, currentUser.getInstitution().getUid());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventOriginationParticipant> getEventOriginationParticipantsByEventUid(@Argument String uid) {
        return eventOriginationParticipantService.getEventOriginationParticipantsByEventUid(uid);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant createEventOriginationParticipant(@Argument EventOriginationParticipant input, @AuthenticationPrincipal User currentUser) {
        return eventOriginationParticipantService.save(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant updateEventOriginationParticipant(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {
        return eventOriginationParticipantService.update(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant deleteEventOriginationParticipant(@Argument Long id, @AuthenticationPrincipal User currentUser) {

        EventOriginationParticipant eventOriginationParticipant;

        try {

            // Get the event origination participant object.
            eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(id);

        } catch ( DataNotFoundException e ) {

            log.error(String.format("Event participant could not be deleted because it does not exist. (current user = %s)", currentUser.getUid()));
            throw new DataNotFoundException("Event participant could not be deleted because it does not exist.");

        }

        // Get the number of rows deleted.
        eventParticipantFacilityService.deleteAllForEventParticipantId(id);
        eventOriginationParticipantService.deleteById(id);
        eventParticipantService.deleteById(id);

        return eventOriginationParticipant;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventParticipantFacility> getEventParticipantFacilitiesByEventParticipantId(@Argument Long id
            , @AuthenticationPrincipal User currentUser) {
        //TODO: validate
        // authorizationService.authorizeUserForDeal((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal(), uid);
        return eventParticipantFacilityService.getEventParticipantFacilitiesByEventParticipantId(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EventParticipantFacility getEventParticipantFacilityByEventParticipantFacility(@Argument EventParticipantFacility input
            , @AuthenticationPrincipal User currentUser) {

        // Verify that the user has access to this deal and event.
        authorizationService.authorizeUserForDealByEventUid(currentUser, input.getEventParticipant().getEvent().getUid());

        return eventParticipantFacilityService.getEventParticipantFacility(input);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventDealFacility> getEventDealFacilitiesByEventUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        // Verify that the user has access to this deal and event.
        authorizationService.authorizeUserForDealByEventUid(currentUser, uid);

        return eventDealFacilityService.getEventDealFacilitiesForEvent(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventLeadFacility> getEventLeadFacilitiesByEventUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        // Authorize the user for the deal and event.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, uid);

        return eventLeadFacilityService.getEventLeadFacilitiesByEventId(event.getId());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EventLeadFacility getEventLeadFacilityByEventDealFacility(@Argument EventDealFacility input, @AuthenticationPrincipal User currentUser) {

        // Authorize the user for the deal and event.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, input.getEvent().getUid());

        return eventLeadFacilityService.getEventLeadFacility(event.getId(), input.getDealFacility().getId());
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventDealFacility deleteEventDealFacility(@Argument Long eventDealFacilityId, @AuthenticationPrincipal User currentUser) {

        EventDealFacility eventDealFacility;

        try {

            // Get the facility to make sure it exists and to return it.
            eventDealFacility = eventDealFacilityService.getEventDealFacilityForId(eventDealFacilityId);

        } catch ( DataNotFoundException e ) {

            // Throw specific error and message when deal facility not found.
            throw new DataNotFoundException("Deal Facility could not be deleted because it does not exist.");

        }

        // Verify that the user has access to this deal and event.
        authorizationService.authorizeUserForDealByEventUid(currentUser, eventDealFacility.getEvent().getUid());

        // Delete the event deal facility record.
        eventDealFacilityService.deleteById(eventDealFacilityId);

        return eventDealFacility;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventDealFacility createEventDealFacility(@Argument EventDealFacility input, @AuthenticationPrincipal User currentUser) {
        return eventDealFacilityService.save(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventParticipantFacility createEventParticipantFacility(@Argument EventParticipantFacility input
            , @AuthenticationPrincipal User currentUser) {
        return eventParticipantFacilityService.save(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventParticipantFacility updateEventParticipantFacility(@Argument Map<String, Object> input
            , @AuthenticationPrincipal User currentUser) {
        return eventParticipantFacilityService.update(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventParticipantFacility deleteEventParticipantFacility(@Argument EventParticipantFacility input
            , @AuthenticationPrincipal User currentUser) {

        EventParticipantFacility epf;

        try {

            // Get the event participant facility object.
            epf = eventParticipantFacilityService.getEventParticipantFacility(input);

        } catch ( DataNotFoundException e ) {

            log.error(String.format("Event participant facility could not be deleted because it does not exist. (current user = %s)", currentUser.getUid()));
            throw new DataNotFoundException("Event participant facility could not be deleted because it does not exist.");

        }

        // Get the number of rows deleted.
        eventParticipantFacilityService.delete(epf);

        return epf;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventLeadFacility updateEventLeadFacility(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {
        return eventLeadFacilityService.update(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventLeadFacility> updateEventLeadFacilityAllocations(@Argument String eventUid
            , @Argument List<Map<String, Object>> allocations, @AuthenticationPrincipal User currentUser) {

        // Verify that the user has access to this deal and event.
        authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);

        // Perform the update of the event lead facilities.
        List<EventLeadFacility> eventLeadFacilities = eventLeadFacilityService.updateAllocations(eventUid, allocations, currentUser);

        // Get the event for updating the allocation date and activity.
        Event event = eventService.getEventByUid(eventUid);

        // Timestamp the lead allocation date.
        eventService.updateLeadAllocationDate(event.getId(), currentUser);

        // Create the activity map and activity record.
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("facilities", eventLeadFacilities);
        activityService.createActivity(ALLOCATION_AMOUNT_SET, event.getDeal().getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        return eventLeadFacilities;
    }

    @SchemaMapping
    public Event event(EventParticipant eventParticipant) {
        return eventService.getEventById(eventParticipant.getEvent().getId());
    }

    @SchemaMapping
    public Event event(EventOriginationParticipant eventOriginationParticipant) {
        return eventService.getEventById(eventOriginationParticipant.getEvent().getId());
    }

}