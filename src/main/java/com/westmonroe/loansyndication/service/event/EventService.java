package com.westmonroe.loansyndication.service.event;

import com.westmonroe.loansyndication.dao.StageDao;
import com.westmonroe.loansyndication.dao.deal.DealDao;
import com.westmonroe.loansyndication.dao.event.EventDao;
import com.westmonroe.loansyndication.dao.event.EventDealFacilityDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantFacilityDao;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.Stage;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.utils.DealStageEnum;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.*;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.DealStageEnum.*;

@Service
@Slf4j
public class EventService {

    private final DealDao dealDao;
    private final EventDao eventDao;
    private final EventParticipantDao eventParticipantDao;
    private final EventLeadFacilityService eventLeadFacilityService;
    private final EventParticipantFacilityDao eventParticipantFacilityDao;
    private final EventOriginationParticipantService eventOriginationParticipantService;
    private final EventDealFacilityDao eventDealFacilityDao;
    private final StageDao stageDao;
    private final ActivityService activityService;
    private final EmailService emailService;
    private final AuthorizationService authorizationService;
    private final EventDealFacilityService eventDealFacilityService;

    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    public EventService(DealDao dealDao, EventDao eventDao, EventParticipantDao eventParticipantDao
            , EventOriginationParticipantService eventOriginationParticipantService, EventParticipantFacilityDao eventParticipantFacilityDao
            , EventLeadFacilityService eventLeadFacilityService, EventDealFacilityDao eventDealFacilityDao, StageDao stageDao
            , ActivityService activityService, EmailService emailService, AuthorizationService authorizationService, EventDealFacilityService eventDealFacilityService) {
        this.dealDao = dealDao;
        this.eventDao = eventDao;
        this.eventParticipantDao = eventParticipantDao;
        this.eventLeadFacilityService = eventLeadFacilityService;
        this.eventParticipantFacilityDao = eventParticipantFacilityDao;
        this.eventOriginationParticipantService = eventOriginationParticipantService;
        this.eventDealFacilityDao = eventDealFacilityDao;
        this.stageDao = stageDao;
        this.activityService = activityService;
        this.emailService = emailService;
        this.authorizationService = authorizationService;
        this.eventDealFacilityService = eventDealFacilityService;
    }

    public List<Event> getEventsByDealId(Long dealId) {
        return eventDao.findAllByDealId(dealId);
    }

    public Event getEventById(Long id) {
        return eventDao.findById(id);
    }

    public Event getEventByUid(String uid) {
        return eventDao.findByUid(uid);
    }

    public Event getEventByExternalId(String externalId) {
        return eventDao.findByExternalId(externalId);
    }

    /**
     * There can only be one open event for a deal, so this method will get that open event.  If all events are closed
     * or there are no events for the deal then a null value is returned.
     *
     * @param   dealUid     A valid and unique deal uid.
     * @return  Event       An open event or null if all events are closed.
     */
    public Event getOpenEventForDealUid(String dealUid) {
        Event event;

        try {
            event = eventDao.findOpenEventByDealUid(dealUid);
        } catch ( DataNotFoundException e ) {
            event = null;
        }

        return event;
    }

    /**
     * This method saves the Event and assumes that the user added the deal uid value to the deal
     * object in the event.  The deal uid is required because the event must be associated with a Deal.
     *
     * @param event
     *
     * @return The saved event object.
     */
    public Event save(Event event, User currentUser, String source) {

        /*
         *  Verify that we have everything necessary before saving the deal.
         */
        if ( event == null ) {

            log.error("save(): Cannot save a null Event.");
            throw new ValidationException("Cannot save a null Event.");

        } else if ( event.getDeal() == null || event.getDeal().getUid() == null ) {

            log.error("save(): The deal uid is null.");
            throw new ValidationException("The deal uid was not supplied.");

        } else if ( getOpenEventForDealUid(event.getDeal().getUid()) != null ) {

            log.error("save(): The event cannot be created when an event is still open.");
            throw new ValidationException("The event cannot be created when an event is still open.");

        }

        // Get the deal object and assign to event.
        Deal deal = authorizationService.authorizeUserForDealByDealUid(currentUser, event.getDeal().getUid());
        event.setDeal(deal);

        // Generate a random UUID for the new event.
        event.setUid(UUID.randomUUID().toString());

        //TODO: Remove this code when integrations are complete.
        // If the event external id is null then add it.
        if ( event.getEventExternalId() == null ) {
            event.setEventExternalId(event.getUid());
        }

        // Add the created by user to the event.
        event.setCreatedBy(currentUser);

        try {
            eventDao.save(event);
        } catch ( DuplicateKeyException dke ) {

            log.error(dke.getMessage());

            String fieldName = "Event Uid";

            if ( dke.getMessage().contains("event_external_uuid") ) {
                fieldName = "External Id";
            } else if ( dke.getMessage().contains("event_name") ) {
                fieldName = "Event Name";
            }

            throw new DataIntegrityException(String.format("The %s value already exists and must be unique.", fieldName));

        } catch ( Exception e ) {
            log.error(e.getMessage());
        }

        Map<String, Object> activityMap = Map.of();
        activityService.createActivity(DEAL_CREATED, deal.getId(), null, activityMap, currentUser, source);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("recipient", currentUser); // should only be the current user that receives the email, right?
        templateData.put("from", "noreply@app.laminafs.com");
        templateData.put("createdByInstitutionUid", currentUser.getInstitution().getUid());
        templateData.put("createdByInstitution", currentUser.getInstitution().getName());

        emailService.sendEmail(EmailTypeEnum.DEAL_CREATED, deal, templateData);

        return eventDao.findById(event.getId());
    }

    public Event update(Event event, User currentUser) {

        // Add the updated by user to the event.
        event.setUpdatedBy(currentUser);

        // Update the event.
        eventDao.update(event);

        return eventDao.findByUid(event.getUid());
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * event fields that were sent.
     *
     * @param  eventMap
     * @return event
     */
    public Event update(Map<String, Object> eventMap, User currentUser, String systemSource) {

        if ( !(eventMap.containsKey("uid") || eventMap.containsKey("eventExternalId")) ) {
            throw new MissingDataException("The event must contain the uid or externalDealId for an update.");
        }

        /*
         *  Get the event by the uid or dealExternalId.
         */
        Event event;
        if ( eventMap.containsKey("uid") ) {
            event = eventDao.findByUid(eventMap.get("uid").toString());
        } else {
            event = eventDao.findByExternalId(eventMap.get("eventExternalId").toString());
        }

        // Add the "old" deal to the activity map before any of the event information changes.  This is our snapshot in time.
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("eventMap", eventMap);
        activityMap.put("oldEvent", SerializationUtils.clone(event));

        // Add the updated by user to the event.
        event.setUpdatedBy(currentUser);

        /*
         * Check the fields in the map and update the deal object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( eventMap.containsKey("name") ) {
            event.setName((String) eventMap.get("name"));
        }

        if ( eventMap.containsKey("eventType") ) {
            event.setEventType(new EventType(Long.valueOf(((Map) eventMap.get("eventType")).get("id").toString())));
        }

        if ( eventMap.containsKey("projectedLaunchDate") ) {
            if ( eventMap.get("projectedLaunchDate") == null ) {
                event.setProjectedLaunchDate(null);
            } else {
                event.setProjectedLaunchDate(LocalDate.parse(eventMap.get("projectedLaunchDate").toString()));
            }
        }

        if ( eventMap.containsKey("commitmentDate") ) {
            if ( eventMap.get("commitmentDate") == null ) {
                event.setCommitmentDate(null);
            } else {
                event.setCommitmentDate(LocalDate.parse(eventMap.get("commitmentDate").toString()));
            }
        }

        if ( eventMap.containsKey("commentsDueByDate") ) {
            if ( eventMap.get("commentsDueByDate") == null ) {
                event.setCommentsDueByDate(null);
            } else {
                event.setCommentsDueByDate(LocalDate.parse(eventMap.get("commentsDueByDate").toString()));
            }
        }

        if ( eventMap.containsKey("effectiveDate") ) {
            if ( eventMap.get("effectiveDate") == null ) {
                event.setEffectiveDate(null);
            } else {
                event.setEffectiveDate(LocalDate.parse(eventMap.get("effectiveDate").toString()));
            }
        }

        if ( eventMap.containsKey("projectedCloseDate") ) {
            if ( eventMap.get("projectedCloseDate") == null ) {
                event.setProjectedCloseDate(null);
            } else {
                event.setProjectedCloseDate(LocalDate.parse(eventMap.get("projectedCloseDate").toString()));
            }
        }

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", event.getDeal().getName());
        templateData.put("dealUid", event.getDeal().getUid());
        templateData.put("from", "noreply@app.laminafs.com");
        templateData.put("institutionUid", currentUser.getInstitution().getUid());
        templateData.put("institutionName", currentUser.getInstitution().getName());
        templateData.put("category", "Financial Metrics");
        templateData.put("isDealLaunched", event.getStage().getOrder() >= 3);
        templateData.put("eventMap", eventMap);

        emailService.sendEmail(EmailTypeEnum.DEAL_INFO_UPDATED, event.getDeal(), templateData);

        // Update the event.
        eventDao.update(event);

        Deal updatedDeal = dealDao.findById(event.getDeal().getId(), currentUser);

        /*
         *  Record the activity in the timeline.
         */
        activityMap.put("newDeal", updatedDeal);        // Add the resulting deal from the update.
        activityService.createActivity(DEAL_INFO_UPDATED, event.getDeal().getId(), null, activityMap, currentUser, systemSource);

        return eventDao.findById(event.getId());
    }

    public Event updateLaunchDates(Event event, Map<String, Object> eventMap, User currentUser) {

        // Convert the dates
        LocalDate commitmentDate = null;
        LocalDate projectedCloseDate = null;

        Map<String, Object> activityMap = new HashMap<>();
        Map<String, Object> eventDateMap = new HashMap<>();
        Event oldEvent = SerializationUtils.clone(event);
        activityMap.put("oldEvent", oldEvent);

        if ( eventMap.containsKey("commitmentDate") ) {
            if ( eventMap.get("commitmentDate") != null ) {
                commitmentDate = LocalDate.parse(eventMap.get("commitmentDate").toString());
                eventMap.put("commitmentDate", commitmentDate);
            }
        }

        if ( eventMap.containsKey("projectedCloseDate") ) {
            if ( eventMap.get("projectedCloseDate") != null ) {
                projectedCloseDate = LocalDate.parse(eventMap.get("projectedCloseDate").toString());
                eventMap.put("projectedCloseDate", projectedCloseDate);
            }
        }

        // Set the launch date on the deal to the current date and time.
        eventDao.updateLaunchDates(event.getId(), commitmentDate, projectedCloseDate, currentUser.getId());

        eventDateMap.put("launchDate", LocalDate.now());
        activityMap.put("eventDateMap", eventDateMap);
        activityService.createActivity(DEAL_DATES_UPDATED, event.getDeal().getId(), null, activityMap
                , currentUser, SYSTEM_MARKETPLACE);

        return eventDao.findById(event.getId());
    }

    public Event updateCloseDates(Event event, LocalDate effectiveDate, User currentUser) {

        // Set the close date on the event to the current date and time.
        eventDao.updateCloseDates(event.getId(), effectiveDate, currentUser.getId());

        return eventDao.findById(event.getId());
    }

    public Event updateEventDates(Map<String, Object> eventDateMap, User currentUser) {

        if ( !eventDateMap.containsKey("uid") ) {
            throw new MissingDataException("The event must contain the uid to update dates.");
        }

        // Get the event by the uid.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventDateMap.get("uid").toString());
        Deal deal = event.getDeal();

        // Add the "old" event to the activity map before any of the event information changes.  This is our snapshot in time.
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("eventDateMap", eventDateMap);
        Event oldEvent = SerializationUtils.clone(event);
        activityMap.put("oldEvent", oldEvent);

        //Create templateData for email notification
        Map<String, Object> templateData = new HashMap<>();
        ArrayList<Map<String, String>> eventDatesArray = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        templateData.put("eventName", event.getName());
        templateData.put("eventUid", event.getUid());
        templateData.put("from", sendAddress);
        templateData.put("leadInstitution", deal.getOriginator().getName());
        templateData.put("leadInstitutionUid", deal.getOriginator().getUid());

        boolean didEventDatesChange = false;

        /*
         * Check the fields in the map and update the event object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( eventDateMap.containsKey("projectedLaunchDate") ) {
            if ( eventDateMap.get("projectedLaunchDate") == null ) {
                event.setProjectedLaunchDate(null);
                if (oldEvent.getProjectedLaunchDate() != null) {
                    eventDatesArray.add(Map.of ("eventDateField", "Projected Launch Date", "newEventDate", "--"));
                    didEventDatesChange = true;
                }

            } else { //
                LocalDate newDate = LocalDate.parse(eventDateMap.get("projectedLaunchDate").toString());
                event.setProjectedLaunchDate(newDate);
                if (oldEvent.getProjectedLaunchDate() == null || !oldEvent.getProjectedLaunchDate().equals(newDate)) {
                    eventDatesArray.add(Map.of ("eventDateField", "Projected Launch Date", "newEventDate", newDate.format(formatter)));
                    didEventDatesChange = true;
                }
            }
        }

        if ( eventDateMap.containsKey("commitmentDate") ) {
            if ( eventDateMap.get("commitmentDate") == null ) {
                event.setCommitmentDate(null);
                if (oldEvent.getCommitmentDate() != null) {
                    eventDatesArray.add(Map.of ("eventDateField", "Commitment Date", "newEventDate", "--"));
                    didEventDatesChange = true;
                }
            } else {
                LocalDate newDate = LocalDate.parse(eventDateMap.get("commitmentDate").toString());
                event.setCommitmentDate(newDate);
                if (oldEvent.getCommitmentDate() == null || !oldEvent.getCommitmentDate().equals(newDate)) {
                    eventDatesArray.add(Map.of ("eventDateField", "Commitment Date", "newEventDate", newDate.format(formatter)));
                    didEventDatesChange = true;
                }
            }
        }

        if ( eventDateMap.containsKey("commentsDueByDate") ) {
            if ( eventDateMap.get("commentsDueByDate") == null ) {
                event.setCommentsDueByDate(null);
                if (oldEvent.getCommentsDueByDate() != null) {
                    eventDatesArray.add(Map.of ("eventDateField", "Comments Due By Date", "newEventDate", "--"));
                    didEventDatesChange = true;
                }
            } else {
                LocalDate newDate = LocalDate.parse(eventDateMap.get("commentsDueByDate").toString());
                event.setCommentsDueByDate(newDate);
                if (oldEvent.getCommentsDueByDate() == null || !oldEvent.getCommentsDueByDate().equals(newDate)) {
                    eventDatesArray.add(Map.of ("eventDateField", "Comments Due By Date", "newEventDate", newDate.format(formatter)));
                    didEventDatesChange = true;
                }
            }
        }

        if ( eventDateMap.containsKey("projectedCloseDate") ) {
            if ( eventDateMap.get("projectedCloseDate") == null ) {
                event.setProjectedCloseDate(null);
                if (oldEvent.getProjectedCloseDate() != null) {
                    eventDatesArray.add(Map.of ("eventDateField", "Projected Close Date", "newEventDate", "--"));
                    didEventDatesChange = true;
                }
            } else {
                LocalDate newDate = LocalDate.parse(eventDateMap.get("projectedCloseDate").toString());
                event.setProjectedCloseDate(newDate);
                if (oldEvent.getProjectedCloseDate() == null || !oldEvent.getProjectedCloseDate().equals(newDate)) {
                    eventDatesArray.add(Map.of ("eventDateField", "Projected Close Date", "newEventDate", newDate.format(formatter)));
                    didEventDatesChange = true;
                }
            }
        }

        if ( eventDateMap.containsKey("effectiveDate") ) {
            if ( eventDateMap.get("effectiveDate") == null ) {
                event.setEffectiveDate(null);
                if (oldEvent.getEffectiveDate() != null) {
                    eventDatesArray.add(Map.of ("eventDateField", "Effective Date", "newEventDate", "--"));
                    didEventDatesChange = true;
                }
            } else {
                LocalDate newDate = LocalDate.parse(eventDateMap.get("effectiveDate").toString());
                event.setEffectiveDate(newDate);
                if (oldEvent.getEffectiveDate() == null || !oldEvent.getEffectiveDate().equals(newDate)) {
                    eventDatesArray.add(Map.of ("eventDateField", "Effective Date", "newEventDate", newDate.format(formatter)));
                    didEventDatesChange = true;
                }
            }
        }

        // Set the launch date on the event to the current date and time.
        eventDao.updateEventDates(event, currentUser);
        templateData.put("eventDates", eventDatesArray);

        /*
         *  Record the activity in the timeline.
         */
        activityMap.put("newEvent", eventDao.findById(event.getId()));        // Add the resulting deal from the update.

        // Only create an activity if at least one of the deal dates changes
        if ( didEventDatesChange ) {

            if ( eventDatesArray.size() == 1 ) {
                templateData.put("changedDate", eventDatesArray.get(0).get("eventDateField"));
            }

            activityService.createActivity(DEAL_DATES_UPDATED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);
            emailService.sendEmail(EmailTypeEnum.DEAL_DATES_UPDATED, deal, templateData);

        }

        return eventDao.findById(deal.getId());
    }

    public Event updateLeadInvitationDate(Long eventId, User currentUser) {

        // Timestamp the lead invitation date.
        eventDao.updateLeadInvitationDate(eventId, currentUser);

        Event event = eventDao.findById(eventId);
        activityService.createActivity(INVITE_AMOUNT_SET, event.getDeal().getId(), null, null, currentUser, SYSTEM_MARKETPLACE);

        // Return the full event object.
        return event;
    }

    public Event updateLeadCommitmentDate(Long eventId, User currentUser) {

        // Timestamp the lead commitment date.
        eventDao.updateLeadCommitmentDate(eventId, currentUser);

        Event event = eventDao.findById(eventId);

        activityService.createActivity(COMMITMENT_AMOUNT_SET, event.getDeal().getId(), null, null, currentUser, SYSTEM_MARKETPLACE);

        // Return the full event object.
        return event;
    }

    public Event updateLeadAllocationDate(Long eventId, User currentUser) {

        // Timestamp the lead allocation date.
        eventDao.updateLeadAllocationDate(eventId, currentUser);

        Event event = eventDao.findById(eventId);
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("facilities", eventLeadFacilityService.getEventLeadFacilitiesByEventId(eventId));

        activityService.createActivity(ALLOCATION_AMOUNT_SET, event.getDeal().getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        // Return the full event object.
        return eventDao.findById(eventId);
    }

    @Transactional
    public void deleteById(Long id) {

        // Delete all event participant origination.
        eventOriginationParticipantService.deleteByEventId(id);

        // Delete all event lead facilities.
        eventLeadFacilityService.deleteAllForEventId(id);

        // Delete all event participant facilities.
        eventParticipantFacilityDao.deleteAllByEventId(id);

        // Delete all event deal facilities.
        eventDealFacilityDao.deleteAllByEventId(id);

        // Delete all event participants.
        eventParticipantDao.deleteAllByEventId(id);

        // Delete the event.
        eventDao.deleteById(id);
    }

    /**
     * Deletes event and event related records by uid.  The try/catch block assures that the operation is idempotent.
     *
     * @param   uid     An event uid.
     * @return  Number of records deleted.
     */
    @Transactional
    public void deleteByUid(String uid) {

        try {
            Event event = eventDao.findByUid(uid);
            deleteById(event.getId());
        } catch ( DataNotFoundException e ) {
            // Do nothing
        }

    }

    @Transactional
    public void deleteByInstitutionId(Long id) {

        // Delete all event participant origination.
        eventOriginationParticipantService.deleteByParticipantId(id);

        // Delete all event participant facilities.
        eventParticipantFacilityDao.deleteAllByParticipantId(id);

        // Delete all event deal facilities.
        eventDealFacilityDao.deleteAllByParticipantId(id);

        // Delete all event participants.
        eventParticipantDao.deleteAllByParticipantId(id);

        // Delete the event.
        eventDao.deleteAllByParticipantId(id);
    }

    /**
     * This method increments the event stage to the target, destination or "to" stage.
     *
     * @param eventUid      The UUID of the event that the stage will be incremented.
     * @param toStage       The target, destination or "to" stage to advance the deal.
     * @param currentUser   The current user, which was retrieved from the principal object.
     */
    public Event incrementEventToStage(String eventUid, DealStageEnum toStage, User currentUser) {

        Event event = eventDao.findByUid(eventUid);
        Stage stage = null;

        switch ( toStage ) {
            case STAGE_2 -> {       // Move the stage from "New Deal Created" to "Pre-Launch".

                if ( event.getStage().getOrder() == STAGE_1.getOrder() ) {
                    stage = stageDao.findByOrder(STAGE_2.getOrder());
                }

            }
            case STAGE_3 -> {       // Move the stage from "Pre-Launch" to "Launched".

                // Skipped

            }
            case STAGE_4 -> {       // Move the stage from "Pre-Launch" to "Awaiting Draft Loan Documents".

                if ( event.getStage().getOrder() == STAGE_2.getOrder() ) {
                    stage = stageDao.findByOrder(STAGE_4.getOrder());
                }

            }
            case STAGE_5 -> {       // Move the stage from "Awaiting Draft Loan Documents" to "Draft Loan Documents Complete".

                if ( event.getStage().getOrder() == STAGE_4.getOrder() ) {
                    stage = stageDao.findByOrder(STAGE_5.getOrder());
                }

            }
            case STAGE_6 -> {       // Move the stage from "Upload Final Loan Documentation" to "Loan Documentation Complete".

                // Skipped

            }
            case STAGE_7 -> {       // Move the stage 5 or 6 to "Upload Closing Memo".

                // Skipped

            }
            case STAGE_8 -> {       // Move the stage from "Draft Loan Documents Complete" to "Awaiting Closing".

                if ( event.getStage().getOrder() == STAGE_5.getOrder() ) {
                    stage = stageDao.findByOrder(STAGE_8.getOrder());
                }

            }
            case STAGE_9 -> {       // Move the stage from "Awaiting Closing" to "Event Closed".

                if ( event.getStage().getOrder() == STAGE_8.getOrder() ) {
                    stage = stageDao.findByOrder(STAGE_9.getOrder());
                }

            }
            default -> throw new IllegalStateException("Increment to stage has not been implemented.");
        }

        // If stage was assigned then update the event with the new stage.
        if ( stage != null ) {
            event.setStage(stage);
            eventDao.updateEventStage(event.getId(), stage.getId(), currentUser.getId());
        }

        return event;
    }

}