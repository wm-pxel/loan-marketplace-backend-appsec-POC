package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.dao.event.EventDealFacilityDao;
import com.westmonroe.loansyndication.dao.event.EventLeadFacilityDao;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;
import com.westmonroe.loansyndication.service.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EventLeadFacilityService {

    private final EventLeadFacilityDao eventLeadFacilityDao;
    private final EventDealFacilityDao eventDealFacilityDao;
    private final AuthorizationService authorizationService;

    public EventLeadFacilityService(EventLeadFacilityDao eventLeadFacilityDao, EventDealFacilityDao eventDealFacilityDao, AuthorizationService authorizationService) {
        this.eventLeadFacilityDao = eventLeadFacilityDao;
        this.eventDealFacilityDao = eventDealFacilityDao;
        this.authorizationService = authorizationService;
    }

    public EventLeadFacility getEventLeadFacility(EventLeadFacility eventLeadFacility) {
        return eventLeadFacilityDao.findByEventLeadFacility(eventLeadFacility);
    }

    public EventLeadFacility getEventLeadFacility(Long eventId, Long eventDealFacilityId) {
        return eventLeadFacilityDao.findByEventIdAndEventDealFacilityId(eventId, eventDealFacilityId);
    }

    public List<EventLeadFacility> getEventLeadFacilitiesByEventId(Long eventId) {
        return eventLeadFacilityDao.findAllByEventId(eventId);
    }

    public EventLeadFacility save(EventLeadFacility eventLeadFacility, User currentUser) {

        // Add the created by user to the event participant.
        eventLeadFacility.setCreatedBy(currentUser);

        try {

            // Save the deal participant.
            eventLeadFacilityDao.save(eventLeadFacility, currentUser);

        } catch ( DuplicateKeyException dke ) {

            log.error("save(): Unique index or primary key violation saving event lead facility.", dke);
            throw new DataIntegrityException("A lead facility can only be added once for each event.");

        }

        // Return the full event participant object.
        return eventLeadFacilityDao.findByEventLeadFacility(eventLeadFacility);
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * event lead facility fields that were sent.
     *
     * @param  eventLeadFacilityMap
     * @return EventLeadFacility
     */
    public EventLeadFacility update(Map<String, Object> eventLeadFacilityMap, User currentUser) {

        // Verify that the event uid was provided in the map.
        if ( !( eventLeadFacilityMap.containsKey("event") && ((Map) eventLeadFacilityMap.get("event")).containsKey("uid") ) ) {

            log.error("update(): The event uid was not provided.");
            throw new MissingDataException("The event uid must be provided.");

        }

        // Verify that the event deal facility id was provided in the map.
        if ( !( eventLeadFacilityMap.containsKey("eventDealFacility") && ((Map) eventLeadFacilityMap.get("eventDealFacility")).containsKey("id") ) ) {

            log.error("update(): The event deal facility id was not provided.");
            throw new MissingDataException("The event deal facility id must be provided.");

        }

        // Get the key values from the map.
        String eventUid = ((Map) eventLeadFacilityMap.get("event")).get("uid").toString();
        Long eventDealFacilityId = Long.valueOf(((Map) eventLeadFacilityMap.get("eventDealFacility")).get("id").toString());

        // Verify that user has update permissions for this deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);

        // Get the event lead facility by the key values.
        EventLeadFacility eventLeadFacility = getEventLeadFacility(event.getId(), eventDealFacilityId);

        /*
         * Check the fields in the map and update the institution object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */

        if ( eventLeadFacilityMap.containsKey("invitationAmount") ) {
            if (eventLeadFacilityMap.get("invitationAmount") == null) {
                eventLeadFacility.setInvitationAmount(null);
            } else {
                eventLeadFacility.setInvitationAmount(BigDecimal.valueOf(Double.valueOf(eventLeadFacilityMap.get("invitationAmount").toString())));
            }
        }

        if ( eventLeadFacilityMap.containsKey("commitmentAmount") ) {
            if ( eventLeadFacilityMap.get("commitmentAmount") == null ) {
                eventLeadFacility.setCommitmentAmount(null);
            } else {
                eventLeadFacility.setCommitmentAmount(BigDecimal.valueOf(Double.valueOf(eventLeadFacilityMap.get("commitmentAmount").toString())));
            }
        }

        if ( eventLeadFacilityMap.containsKey("allocationAmount") ) {
            if ( eventLeadFacilityMap.get("allocationAmount") == null ) {
                eventLeadFacility.setAllocationAmount(null);
            } else {
                eventLeadFacility.setAllocationAmount(BigDecimal.valueOf(Double.valueOf(eventLeadFacilityMap.get("allocationAmount").toString())));
            }
        }

        // Add the current user as the updated by user.
        eventLeadFacility.setUpdatedBy(currentUser);

        // Updating the provided event lead facility information.
        eventLeadFacilityDao.update(eventLeadFacility);

        // Return the full deal participant object.
        return getEventLeadFacility(eventLeadFacility);
    }

    public EventLeadFacility update(EventLeadFacility eventLeadFacility, User currentUser) {

        //TODO: Verify that user has update permissions for this event.

        // NOTE: This only updates the status of the deal participant.
        eventLeadFacilityDao.update(eventLeadFacility);

        // Return the full deal participant object.
        return getEventLeadFacility(eventLeadFacility);
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * event lead facility fields that were sent.
     *
     * @param  eventUid
     * @param  allocations
     * @return List<EventLeadFacility>
     */
    public List<EventLeadFacility> updateAllocations(String eventUid, List<Map<String, Object>> allocations
            , User currentUser) {

        for ( Map<String, Object> allocation : allocations ) {
            eventLeadFacilityDao.updateAllocation(
                BigDecimal.valueOf(Double.valueOf(((Map) allocation).get("allocationAmount").toString())),
                currentUser.getId(),
                eventUid,
                Long.valueOf(((Map) ((Map) allocation).get("eventDealFacility")).get("id").toString())
            );
        }

        return eventLeadFacilityDao.findAllByEventUid(eventUid);
    }

    public int delete(EventLeadFacility eventLeadFacility) {

        //TODO: Verify that user has delete permissions for this deal.

        return eventLeadFacilityDao.delete(eventLeadFacility);
    }

    public void deleteAllForEventId(Long eventId) {

        //TODO: Verify that user has delete permissions for this event.

        eventLeadFacilityDao.deleteAllByEventId(eventId);
    }

    public void deleteAllByFacilityExternalId(String dealFacilityExternalId) {
        eventLeadFacilityDao.deleteAllByFacilityExternalId(dealFacilityExternalId);
    }

    public void createEventLeadFacilitiesForEvent(Event event, User currentUser) {

        // Get the list of the event deal facilities for this event.
        List<EventDealFacility> eventDealFacilities = eventDealFacilityDao.findAllByEventId(event.getId());

        // Loop through the deal facilities and add the initial event lead facility record.
        for ( EventDealFacility eventDealFacility : eventDealFacilities ) {
            createEventLeadFacilitiesForEventAndDealFacility(event, eventDealFacility, currentUser);
        }

    }

    public void createEventLeadFacilitiesForEventAndDealFacility(Event event, EventDealFacility eventDealFacility, User currentUser) {
        eventLeadFacilityDao.save(new EventLeadFacility(event, eventDealFacility), currentUser);
    }

}