package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.dao.InstitutionDao;
import com.westmonroe.loansyndication.dao.ParticipantStepDao;
import com.westmonroe.loansyndication.dao.UserDao;
import com.westmonroe.loansyndication.dao.event.EventOriginationParticipantDao;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipant;
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
public class EventOriginationParticipantService {

    private final EventOriginationParticipantDao eventOriginationParticipantDao;
    private final InstitutionDao institutionDao;
    private final ParticipantStepDao participantStepDao;
    private final UserDao userDao;
    private final EventParticipantService eventParticipantService;
    private final EventParticipantFacilityService eventParticipantFacilityService;
    private final DefinitionService definitionService;

    public EventOriginationParticipantService(EventOriginationParticipantDao eventOriginationParticipantDao, InstitutionDao institutionDao
            , ParticipantStepDao participantStepDao, UserDao userDao, EventParticipantService eventParticipantService
            , EventParticipantFacilityService eventParticipantFacilityService, DefinitionService definitionService) {
        this.eventOriginationParticipantDao = eventOriginationParticipantDao;
        this.institutionDao = institutionDao;
        this.participantStepDao = participantStepDao;
        this.userDao = userDao;
        this.eventParticipantService = eventParticipantService;
        this.eventParticipantFacilityService = eventParticipantFacilityService;
        this.definitionService = definitionService;
    }

    public EventOriginationParticipant getEventOriginationParticipantById(Long id) {
        return eventOriginationParticipantDao.findById(id);
    }

    public EventOriginationParticipant getEventOriginationParticipantByEventUidAndParticipantUid(String eventUid, String participantUid) {
        return eventOriginationParticipantDao.findByEventUidAndParticipantUid(eventUid, participantUid);
    }

    public List<EventOriginationParticipant> getEventOriginationParticipantsByEventUid(String eventUid) {
        return eventOriginationParticipantDao.findAllByEventUid(eventUid);
    }

    /**
     * This method performs the save operation for an event participant when the event type is Origination.
     *
     * @param eop           {@link EventOriginationParticipant}
     * @param currentUser   {@link User}
     *
     * @return {@link EventParticipant}
     */
    public EventOriginationParticipant save(EventOriginationParticipant eop, User currentUser) {

        EventParticipant eventParticipant = new EventParticipant(null, eop.getEvent(), eop.getParticipant()) ;

        // Get the full invite recipient if it was sent.
        if ( eop.getInviteRecipient() != null && eop.getInviteRecipient().getUid() != null ) {
            eop.setInviteRecipient(userDao.findByUid(eop.getInviteRecipient().getUid()));
        }

        /*
         *  First: Save the event participant record.  This is the same operation regardless of the event type.
         */
        try {


            // Save the event participant and set the unique id.
            eventParticipantService.save(eventParticipant, currentUser);
            eop.setId(eventParticipant.getId());
            eop.setCreatedBy(currentUser);

            // Save the event origination participant.
            eventOriginationParticipantDao.save(eop);

        } catch ( DuplicateKeyException dke ) {

            log.error("save(): Unique index or primary key violation saving event participant.", dke);
            throw new DataIntegrityException("A participant can only be added once for each deal.");

        }

        eop = eventOriginationParticipantDao.findById(eop.getId());
        eop.setParticipant(eventParticipant.getParticipant());

        // Return the full event participant object.
        return eop;
    }

    public void update(EventOriginationParticipant eop) {
        eventOriginationParticipantDao.update(eop);
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * event participant origination fields that were sent.
     *
     * @param  eventParticipantMap
     * @return eventOriginationParticipant
     */
    @Transactional
    public EventOriginationParticipant update(Map<String, Object> eventParticipantMap, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        if ( !eventParticipantMap.containsKey("id") ) {
            throw new MissingDataException("The event participant must contain the unique id for updates.");
        }

        // Get the event participant and event participant origination by the id.
        EventParticipant eventParticipant = eventParticipantService.getEventParticipantById(Long.valueOf(eventParticipantMap.get("id").toString()));
        EventOriginationParticipant eventOriginationParticipant = eventOriginationParticipantDao.findById(eventParticipant.getId());

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

        if ( eventParticipantMap.containsKey("inviteRecipient") ) {

            // If a primary user is provided with an institution then throw an error.
            if ( eventParticipant.getParticipant() == null ) {

                log.error("The participant must be supplied when a invite recipient is provided.");
                throw new MissingDataException("The participant must be supplied when a invite recipient user is provided.");

            }

            if ( eventParticipantMap.get("inviteRecipient") == null || ((Map) eventParticipantMap.get("inviteRecipient")).get("uid") == null ) {
                eventOriginationParticipant.setInviteRecipient(null);
            } else {

                eventOriginationParticipant.setInviteRecipient(userDao.findByUid(((Map) eventParticipantMap.get("inviteRecipient")).get("uid").toString()));

                // Verify that the primary user is part of the participating institution.
                if ( !eventOriginationParticipant.getInviteRecipient().getInstitution().getUid().equals(eventParticipant.getParticipant().getUid()) ) {

                    log.error("The participant must be supplied when a invite recipient user is provided.");
                    throw new DataIntegrityException("The invite recipient must be a part of the participating institution.");

                }
            }

        }

        if ( eventParticipantMap.containsKey("message") ) {
            eventOriginationParticipant.setMessage((String) eventParticipantMap.get("message"));
        }

        if ( eventParticipantMap.containsKey("response") ) {
            eventOriginationParticipant.setResponse((String) eventParticipantMap.get("response"));
        }

        if ( eventParticipantMap.containsKey("declinedMessage") ) {
            eventOriginationParticipant.setDeclinedMessage((String) eventParticipantMap.get("declinedMessage"));
        }

        try {

            // Update any of the fields in the event participant.
            eventParticipantService.update(eventParticipant, currentUser);

            // Updating the provided deal participant information.
            eventOriginationParticipantDao.update(eventOriginationParticipant);

            // Update the updated by fields.
            eventParticipantService.updateUpdatedBy(eventParticipant.getId(), currentUser.getId());

        } catch ( DuplicateKeyException dke ) {

            log.error("update(): Unique index or primary key violation updating event participant origination.", dke);
            throw new DataIntegrityException("A participant can only be added once for each event.");

        }

        // Return the full deal participant object.
        return eventOriginationParticipantDao.findById(eventOriginationParticipant.getId());
    }

    /**
     * This method increments the participant step to the target, destination or "to" step.
     *
     * @param eop     The event participant that the step will be incremented.
     * @param toStep  The target, destination or "to" step to advance the participant.
     */
    public void incrementParticipantToStep(EventOriginationParticipant eop, ParticipantStepEnum toStep) {

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
        eop.setStep(participantStep);

        eventParticipantService.updateParticipantStep(eop.getId(), participantStep.getId(), eop.getUpdatedBy().getId());
    }

    public void updateInviteDate(Long eventParticipantId, User currentUser) {
        eventOriginationParticipantDao.updateInviteDate(eventParticipantId);
        eventParticipantService.updateUpdatedBy(eventParticipantId, currentUser.getId());
    }

    public void updateFullDealAccessDate(Long eventParticipantId, User currentUser) {
        eventOriginationParticipantDao.updateFullDealAccessDate(eventParticipantId);
        eventParticipantService.updateUpdatedBy(eventParticipantId, currentUser.getId());
    }

    public void updateForDeclinedEvent(Long eventParticipantId, User currentUser) {
        eventOriginationParticipantDao.updateForDeclinedEvent(eventParticipantId);
        eventParticipantService.updateUpdatedBy(eventParticipantId, currentUser.getId());
    }

    public void updateForRemovedFromEvent(Long eventParticipantId, User currentUser) {
        eventOriginationParticipantDao.updateForRemovedFromEvent(eventParticipantId);
        eventParticipantService.updateUpdatedBy(eventParticipantId, currentUser.getId());
    }

    public int deleteDraftEventParticipantsOnEvent(String eventUid) {
        return eventOriginationParticipantDao.deleteAllByEventUidAndStepOrder(eventUid, STEP_1.getOrder());
    }

    public void delete(EventOriginationParticipant eop) {
        eventOriginationParticipantDao.deleteById(eop.getId());
    }

    public void deleteById(Long eventParticipantId) {
        eventOriginationParticipantDao.deleteById(eventParticipantId);
    }

    public void deleteByEventId(Long eventId) {
        eventOriginationParticipantDao.deleteAllByEventId(eventId);
    }

    public void deleteByParticipantId(Long participantId) {
        eventOriginationParticipantDao.deleteAllByParticipantId(participantId);
    }

    public void declineEventForParticipant(Long eventParticipantId, User currentUser) {
        eventParticipantFacilityService.deleteAllocationsForEventParticipantId(eventParticipantId);
        updateForDeclinedEvent(eventParticipantId, currentUser);
    }

    public void removedParticipantFromEvent(Long eventParticipantId, User currentUser) {
        eventParticipantFacilityService.deleteAllocationsForEventParticipantId(eventParticipantId);
        updateForRemovedFromEvent(eventParticipantId, currentUser);
    }

}