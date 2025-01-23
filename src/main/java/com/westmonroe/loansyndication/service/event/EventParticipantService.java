package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.dao.InstitutionDao;
import com.westmonroe.loansyndication.dao.ParticipantStepDao;
import com.westmonroe.loansyndication.dao.event.EventDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantFacilityDao;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.DefinitionService;
import com.westmonroe.loansyndication.utils.ParticipantStepEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.*;

@Service
@Slf4j
public class EventParticipantService {

    private final EventParticipantDao eventParticipantDao;
    private final EventDao eventDao;
    private final DefinitionService definitionService;
    private final InstitutionDao institutionDao;
    private final ParticipantStepDao participantStepDao;
    private final EventParticipantFacilityDao eventParticipantFacilityDao;
    private final AuthorizationService authorizationService;
    private final EventParticipantFacilityService eventParticipantFacilityService;

    public EventParticipantService(EventParticipantDao eventParticipantDao, EventDao eventDao
            , DefinitionService definitionService, InstitutionDao institutionDao, ParticipantStepDao participantStepDao
            , EventParticipantFacilityDao eventParticipantFacilityDao, AuthorizationService authorizationService
            , EventParticipantFacilityService eventParticipantFacilityService) {
        this.eventParticipantDao = eventParticipantDao;
        this.eventDao = eventDao;
        this.definitionService = definitionService;
        this.institutionDao = institutionDao;
        this.participantStepDao = participantStepDao;
        this.eventParticipantFacilityDao = eventParticipantFacilityDao;
        this.authorizationService = authorizationService;
        this.eventParticipantFacilityService = eventParticipantFacilityService;
    }

    private EventParticipant getEventParticipant(EventParticipant eventParticipant, boolean refreshFromDb) {

        EventParticipant ep;

        if ( refreshFromDb ) {
            ep = eventParticipantDao.findById(eventParticipant.getId());
        } else {
            ep = eventParticipant;
        }

        // Get the components of the event participant and return the full object.
        ep.setEvent(eventDao.findByUid(ep.getEvent().getUid()));

        if ( !( ep.getParticipant() == null || ep.getParticipant().getUid() == null ) ) {
            ep.setParticipant(institutionDao.findByUid(ep.getParticipant().getUid()));
        }

        if ( eventParticipant.getStep() != null ) {
            ep.setStep(participantStepDao.findById(eventParticipant.getStep().getId()));
        }

        return ep;
    }

    public EventParticipant getEventParticipantById(Long id) {
        return eventParticipantDao.findById(id);
    }

    public EventParticipant getEventParticipantByEventUidAndParticipantUid(String eventUid, String participantUid) {
        return eventParticipantDao.findByEventUidAndParticipantUid(eventUid, participantUid);
    }

    public List<EventParticipant> getEventParticipantsByEventUid(String eventUid) {
        return eventParticipantDao.findAllByEventUid(eventUid);
    }

    public Long save(Long eventId, Long participantId, Long participantStepId, Long createdById) {
        return eventParticipantDao.save(eventId, participantId, participantStepId, createdById);
    }

    /**
     * This method performs the save operation for an event participant.
     *
     * @param eventParticipant  {@link EventOriginationParticipant}
     * @param currentUser       {@link User}
     *
     * @return {@link EventParticipant}
     */
    public EventParticipant save(EventParticipant eventParticipant, User currentUser) {

        // Verify that the event uid was provided.
        if ( eventParticipant.getEvent() == null || eventParticipant.getEvent().getUid() == null ) {
            log.error("The Event unique id is required to save the participant.");
            throw new MissingDataException("The Event unique id is required to save the participant.");
        }

        // Authorize the current user for the event and deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventParticipant.getEvent().getUid());
        Institution participant = null;

        // Only perform participant validation if a participant was selected.
        if ( eventParticipant.getParticipant() != null ) {

            if ( eventParticipant.getParticipant().getUid() != null ) {

                // Assign the full participant object to event participant.
                participant = institutionDao.findByUid(eventParticipant.getParticipant().getUid());

                // Verify that participant institution is not the same as the originating institution.
                if ( participant.getId().equals(event.getDeal().getOriginator().getId()) ) {
                    throw new DataIntegrityException("The originating institution cannot be a participant institution.");
                }

            }

        }

        /*
         *  The initial save of each Event Participant will go into draft mode.
         */
        eventParticipant.setStep(participantStepDao.findByOrder(1));     // Assign to the first step.

        // Add the created by user to the deal participant.
        eventParticipant.setCreatedBy(currentUser);

        /*
         *  First: Save the event participant record.  This is the same operation regardless of the event type.
         */
        try {

            Long participantId = participant == null ? null : participant.getId();

            // Save the event participant and set the unique id.
            eventParticipant.setId(eventParticipantDao.save(event.getId(), participantId, eventParticipant.getStep().getId(), currentUser.getId()));

        } catch ( DuplicateKeyException dke ) {

            log.error("save(): Unique index or primary key violation saving event participant.", dke);
            throw new DataIntegrityException("A participant can only be added once for each deal.");

        }

        // Get the event participant record.
        EventParticipant ep = eventParticipantDao.findById(eventParticipant.getId());
        ep.setEvent(event);
        ep.setParticipant(participant);

        // Create the Deal Participant Facility records for Participant
        eventParticipantFacilityService.createEventParticipantFacilitiesForEventParticipant(ep, currentUser);

        // Return the full deal participant object.
        return ep;
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * event participant fields that were sent.
     *
     * @param  eventParticipantMap
     * @return eventParticipant
     */
    @Transactional
    public EventParticipant update(Map<String, Object> eventParticipantMap, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        if ( !eventParticipantMap.containsKey("id") ) {
            throw new MissingDataException("The event participant must contain the unique id for updates.");
        }

        // Get the event participant by the id.
        EventParticipant eventParticipant = eventParticipantDao.findById(Long.valueOf(eventParticipantMap.get("id").toString()));

        /*
         * Check the fields in the map and update the institution object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( eventParticipantMap.containsKey("participant") ) {
            if ( eventParticipantMap.get("participant") == null || ((Map) eventParticipantMap.get("participant")).get("uid") == null ) {
                eventParticipant.setParticipant(null);
            } else {
                eventParticipant.setParticipant(institutionDao.findByUid(((Map) eventParticipantMap.get("participant")).get("uid").toString()));
            }
        }

        // Add the updated by user to the deal participant.
        eventParticipant.setUpdatedBy(currentUser);

        try {

            // Updating the provided event participant origination information.
            eventParticipantDao.update(eventParticipant);

        } catch ( DuplicateKeyException dke ) {

            log.error("update(): Unique index or primary key violation updating deal participant.", dke);
            throw new DataIntegrityException("A participant can only be added once for each deal.");

        }

        // Return the full deal participant object.
        return getEventParticipant(eventParticipant, true);
    }

    public EventParticipant update(EventParticipant eventParticipant, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        // NOTE: This only updates the status of the deal participant.
        eventParticipantDao.update(eventParticipant);

        // Return the full deal participant object.
        return getEventParticipant(eventParticipant, true);
    }

    public void update(Long eventParticipantId, Institution participant, Long stepId, Long updatedById) {
        eventParticipantDao.update(eventParticipantId, participant, stepId, updatedById);
    }

    public void updateParticipantStep(Long eventParticipantId, Long stepId, Long updatedById) {
        eventParticipantDao.updateParticipantStep(eventParticipantId, stepId, updatedById);
    }

    public void updateUpdatedBy(Long eventParticipantId, Long updatedById) {
        eventParticipantDao.updateUpdatedBy(eventParticipantId, updatedById);
    }

    public int delete(EventParticipant eventParticipant) {

        //TODO: Verify that user has delete permissions for this deal.

        return eventParticipantDao.delete(eventParticipant);
    }

    public int deleteById(Long id) {

        //TODO: Verify that user has delete permissions for this deal.

        return eventParticipantDao.deleteById(id);
    }

    public int deleteAllForEventUid(String eventUid) {

        //TODO: Verify that user has delete permissions for this deal.

        return eventParticipantDao.deleteAllByEventUid(eventUid);
    }

    public int deleteDraftEventParticipantsOnEvent(String eventUid) {
        return eventParticipantDao.deleteAllByEventUidAndStepOrder(eventUid, STEP_1.getOrder());
    }

    /**
     * This method increments the participant step to the target, destination or "to" step.
     *
     * @param eventParticipant  The event participant that the step will be incremented.
     * @param toStep            The target, destination or "to" step to advance the participant.
     */
    public void incrementParticipantToStep(EventParticipant eventParticipant, ParticipantStepEnum toStep) {

        ParticipantStep participantStep;

        switch ( toStep ) {
            case STEP_2 -> {             // Move from "Sending Invite" to "Determine Interest"

                // Get the next step ("Determine Interest") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_2.getOrder());

            }
            case STEP_3 -> {             // Move from "Determine Interest" to "Approve Deal Access"

                // Get the next step ("Approve Deal Access") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_3.getOrder());

            }
            case STEP_4 -> {             //  Move from "Approve Deal Access" to "Waiting for Deal to Launch"

                // Get the next step ("Waiting for Deal to Launch") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_4.getOrder());

            }
            case STEP_5 -> {             //  Lead launched the deal.  May be from "Waiting for Deal to Launch" or auto-advanced from "Approve Deal Access" action.

                participantStep = definitionService.getParticipantStepByOrder(STEP_5.getOrder());

            }
            case STEP_6 -> {             //  Move from "Provide Commitment Amount" to "Set Allocation Amount"

                // Get the next step ("Set Allocation Amount") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_6.getOrder());

            }
            case STEP_7 -> {             //  Move from "Set Allocation Amount" to "Participant is Allocated"

                // Get the next step ("Participant is Allocated") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_7.getOrder());

            }
            case STEP_8 -> {             //  Move from "Participant is Allocated" to "Deal Level Upload Draft Loan Documentation"

                // Get the next step ("Deal Level Upload Draft Loan Documentation") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_8.getOrder());

            }
            case STEP_9 -> {             //  Move from "Deal Level Upload Draft Loan Documentation" to "Waiting for the completed Participation Certificate document"

                // Get the next step ("Waiting for the completed Participation Certificate document") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_9.getOrder());

            }
            case STEP_10 -> {             //  Move from "Waiting for the completed Participation Certificate document" to "Confirm Participation Certificate is complete"

                // Get the next step ("Confirm Participation Certificate is complete") and assign it to the participant.
                participantStep = definitionService.getParticipantStepByOrder(STEP_10.getOrder());

            }
            default -> throw new IllegalStateException("Increment to step has not been implemented.");
        }

        // Assign the target or "to" step to the participant.
        eventParticipant.setStep(participantStep);

        eventParticipantDao.updateParticipantStep(eventParticipant.getId(), participantStep.getId(), eventParticipant.getUpdatedBy().getId());
    }

}