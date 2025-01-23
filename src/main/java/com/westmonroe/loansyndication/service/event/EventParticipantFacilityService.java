package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.dao.event.EventDealFacilityDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantFacilityDao;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_1;

@Service
@Slf4j
public class EventParticipantFacilityService {

    private final EventParticipantFacilityDao eventParticipantFacilityDao;
    private final EventParticipantDao eventParticipantDao;
    private final EventDealFacilityDao eventDealFacilityDao;

    public EventParticipantFacilityService(EventParticipantFacilityDao eventParticipantFacilityDao
            , EventParticipantDao eventParticipantDao, EventDealFacilityDao eventDealFacilityDao) {
        this.eventParticipantFacilityDao = eventParticipantFacilityDao;
        this.eventParticipantDao = eventParticipantDao;
        this.eventDealFacilityDao = eventDealFacilityDao;
    }

    public EventParticipantFacility getEventParticipantFacility(EventParticipantFacility eventParticipantFacility) {

        EventParticipantFacility epf = eventParticipantFacilityDao.findByEventParticipantFacility(eventParticipantFacility);

        // Get full event participant and event deal facility objects.
        epf.setEventParticipant(eventParticipantDao.findById(epf.getEventParticipant().getId()));
        epf.setEventDealFacility(eventDealFacilityDao.findById(epf.getEventDealFacility().getId()));

        return epf;
    }

    public List<EventParticipantFacility> getEventParticipantFacilitiesByEventId(Long eventId) {
        return eventParticipantFacilityDao.findAllByEventId(eventId);
    }

    public List<EventParticipantFacility> getEventParticipantFacilitiesByEventAndParticipantId(Long eventId, Long participantId) {
        return eventParticipantFacilityDao.findAllByEventAndParticipantId(eventId, participantId);
    }

    public List<EventParticipantFacility> getEventParticipantFacilitiesByEventParticipantId(Long eventParticipantId) {
        return eventParticipantFacilityDao.findAllByEventParticipantId(eventParticipantId);
    }

    public EventParticipantFacility save(EventParticipantFacility eventParticipantFacility, User currentUser) {

        // Add the created by user to the event participant.
        eventParticipantFacility.setCreatedBy(currentUser);

        try {

            // Save the deal participant.
            eventParticipantFacilityDao.save(eventParticipantFacility);

        } catch ( DuplicateKeyException dke ) {

            log.error("save(): Unique index or primary key violation saving event participant facility.", dke);
            throw new DataIntegrityException("A participant facility can only be added once for each event.");

        }

        // Return the full event participant object.
        return eventParticipantFacilityDao.findByEventParticipantFacility(eventParticipantFacility);
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * event participant facility fields that were sent.
     *
     * @param  participantFacilityMap
     * @return EventParticipantFacility
     */
    public EventParticipantFacility update(Map<String, Object> participantFacilityMap, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        // Get the event participant facility by the id.
        EventParticipantFacility eventParticipantFacility = eventParticipantFacilityDao.findByParticipantIdAndEventDealFacilityId(
                Long.valueOf(((Map) participantFacilityMap.get("eventParticipant")).get("id").toString()),
                Long.valueOf(((Map) participantFacilityMap.get("eventDealFacility")).get("id").toString())
        );

        /*
         * Check the fields in the map and update the institution object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */

        if ( participantFacilityMap.containsKey("invitationAmount") ) {
            if (participantFacilityMap.get("invitationAmount") == null) {
                eventParticipantFacility.setInvitationAmount(null);
            } else {
                eventParticipantFacility.setInvitationAmount(BigDecimal.valueOf(Double.valueOf(participantFacilityMap.get("invitationAmount").toString())));
            }
        }

        if ( participantFacilityMap.containsKey("commitmentAmount") ) {
            if ( participantFacilityMap.get("commitmentAmount") == null ) {
                eventParticipantFacility.setCommitmentAmount(null);
            } else {
                eventParticipantFacility.setCommitmentAmount(BigDecimal.valueOf(Double.valueOf(participantFacilityMap.get("commitmentAmount").toString())));
            }
        }

        if ( participantFacilityMap.containsKey("allocationAmount") ) {
            if ( participantFacilityMap.get("allocationAmount") == null ) {
                eventParticipantFacility.setAllocationAmount(null);
            } else {
                eventParticipantFacility.setAllocationAmount(BigDecimal.valueOf(Double.valueOf(participantFacilityMap.get("allocationAmount").toString())));
            }
        }

        // Add the current user as the updated by user.
        eventParticipantFacility.setUpdatedBy(currentUser);

        // Updating the provided deal participant information.
        eventParticipantFacilityDao.update(eventParticipantFacility);

        // Return the full deal participant object.
        return getEventParticipantFacility(eventParticipantFacility);
    }

    public EventParticipantFacility update(EventParticipantFacility eventParticipantFacility, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        // NOTE: This only updates the status of the deal participant.
        eventParticipantFacilityDao.update(eventParticipantFacility);

        // Return the full deal participant object.
        return getEventParticipantFacility(eventParticipantFacility);
    }

    public int deleteDraftEventParticipantFacilitiesOnEvent(String eventUid) {
        return eventParticipantFacilityDao.deleteAllByEventUidAndStepOrder(eventUid, STEP_1.getOrder());
    }

    public int delete(EventParticipantFacility eventParticipantFacility) {

        //TODO: Verify that user has delete permissions for this deal.

        return eventParticipantFacilityDao.delete(eventParticipantFacility);
    }

    public int deleteAllForEventId(Long eventId) {

        //TODO: Verify that user has delete permissions for this event.

        return eventParticipantFacilityDao.deleteAllByEventId(eventId);
    }

    public int deleteAllForEventParticipantId(Long eventParticipantId){

        //TODO: Verify that user has delete permissions for this event.

        return eventParticipantFacilityDao.deleteAllByEventParticipantId(eventParticipantId);
    }

    public void deleteAllocationsForEventParticipantId(Long eventParticipantId){
        eventParticipantFacilityDao.deleteAllocationsByEventParticipantId(eventParticipantId);
    }

    public void createEventParticipantFacilitiesForEventParticipant(EventParticipant eventParticipant, User currentUser) {

        // Get the list of the event deal facilities for this event.
        List<EventDealFacility> eventDealFacilities = eventDealFacilityDao.findAllByEventId(eventParticipant.getEvent().getId());

        // Loop through the deal facilities and add the initial deal participant facility record.
        for ( EventDealFacility eventDealFacility : eventDealFacilities ) {

            EventParticipantFacility eventParticipantFacility = new EventParticipantFacility();
            eventParticipantFacility.setEventParticipant(eventParticipant);
            eventParticipantFacility.setEventDealFacility(eventDealFacility);
            eventParticipantFacility.setCreatedBy(currentUser);
            eventParticipantFacility.setUpdatedBy(currentUser);

            eventParticipantFacilityDao.save(eventParticipantFacility);

        }

    }

}