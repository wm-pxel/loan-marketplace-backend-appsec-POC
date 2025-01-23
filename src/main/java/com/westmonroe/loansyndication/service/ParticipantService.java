package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.DuplicateDataException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealMember;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import com.westmonroe.loansyndication.service.deal.DealMemberService;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.event.*;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.*;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.DealStageEnum.*;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.*;
import static com.westmonroe.loansyndication.utils.RoleDefEnum.RECV_ALL_INST_INVS;
import static java.util.Map.entry;

@Service
@Slf4j
public class ParticipantService {

    private final EventDealFacilityService eventDealFacilityService;
    private final EventLeadFacilityService eventLeadFacilityService;
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private final AuthorizationService authorizationService;
    private final DealService dealService;
    private final EventService eventService;
    private final EventParticipantService eventParticipantService;
    private final EventOriginationParticipantService eventOriginationParticipantService;
    private final EventParticipantFacilityService eventParticipantFacilityService;
    private final ActivityService activityService;
    private final EmailService emailService;
    private final UserService userService;
    private final Validator validator;

    private DealMemberService dealMemberService;

    public ParticipantService(AuthorizationService authorizationService, DealService dealService, EventService eventService
            , EventParticipantService eventParticipantService, EventOriginationParticipantService eventOriginationParticipantService
            , EventParticipantFacilityService eventParticipantFacilityService, ActivityService activityService
            , EmailService emailService, UserService userService, Validator validator, DealMemberService dealMemberService, EventDealFacilityService eventDealFacilityService, EventLeadFacilityService eventLeadFacilityService) {
        this.authorizationService = authorizationService;
        this.dealService = dealService;
        this.eventService = eventService;
        this.eventParticipantService = eventParticipantService;
        this.eventOriginationParticipantService = eventOriginationParticipantService;
        this.eventParticipantFacilityService = eventParticipantFacilityService;
        this.activityService = activityService;
        this.emailService = emailService;
        this.userService = userService;
        this.validator = validator;
        this.dealMemberService =dealMemberService;
        this.eventDealFacilityService = eventDealFacilityService;
        this.eventLeadFacilityService = eventLeadFacilityService;
    }

    private EventOriginationParticipant preEventStepSetup(Long eventParticipantId, User currentUser) {

        // Get the Event Origination Participant object.
        EventOriginationParticipant eventOriginationParticipant = eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);

        // Verify authorization for this deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventOriginationParticipant.getEvent().getUid());

        // Assign the full event object to the event participant for efficiency (saves an additional call in the flow).
        eventOriginationParticipant.setEvent(event);

        // Set the updated by user to the current user.
        eventOriginationParticipant.setUpdatedBy(currentUser);

        return eventOriginationParticipant;
    }

    @Transactional
    public EventOriginationParticipant sendEventParticipantInvite(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the originator.

        // Perform validations before moving participant to the next step.
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_1.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 2 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // Validate the event participant object before sending the invitation.
        Set<ConstraintViolation<EventOriginationParticipant>> violations = validator.validate(eventOriginationParticipant);
        if ( !violations.isEmpty() ) {
            throw new ConstraintViolationException(violations);
        }

        // Move the participant to the next step.
        eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_2);

        // Timestamp the invite date for the event participant.
        eventOriginationParticipantService.updateInviteDate(eventParticipantId, currentUser);

        /*
         *  Stages advance independently from steps.  Only allow increment when stage is less than 2.
         */
        if ( eventOriginationParticipant.getEvent().getStage().getOrder() < STAGE_2.getOrder() ) {

            // Increment the Event Stage
            eventService.incrementEventToStage(eventOriginationParticipant.getEvent().getUid(), STAGE_2, currentUser);

        }

        /*
         * Check if deal Participant has a primary contact
         * if not add DEAL_INV_RECIPS as Deal Member
         */
        DealMember dealMember = new DealMember();
        dealMember.setDeal(deal);
        dealMember.setMemberTypeCode(PARTICIPANT.getCode());
        dealMember.setMemberTypeDesc(PARTICIPANT.getDescription());
        dealMember.setCreatedBy(currentUser);

        if ( eventOriginationParticipant.getInviteRecipient() == null ) {
            List<User> recipientUsers = userService.getNonSystemUsersByInstitutionUidAndUserRoleId(eventOriginationParticipant.getParticipant().getUid(), RECV_ALL_INST_INVS.getId());
            dealMember.setUser(recipientUsers.get(0));
        } else {
            dealMember.setUser(eventOriginationParticipant.getInviteRecipient());
        }

        /*
         *  Record the activity in the timeline.
         */
        List<EventParticipantFacility> eventParticipantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventParticipantId(eventOriginationParticipant.getId());

        Map<String, Object> activityMap = Map.of("eventOriginationParticipant", eventOriginationParticipant, "eventParticipantFacilities", eventParticipantFacilities);
        activityService.createActivity(INVITE_SENT, deal.getId(), eventOriginationParticipant.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        try {
            dealMemberService.save(dealMember, currentUser, SYSTEM_MARKETPLACE, true);
        } catch (DuplicateDataException dde ) {
            // The member is already a deal member, so no need to throw an exception.  Just log for visibility.
            log.error(String.format("Lead added %s as a team member but the record already existed."
                    , eventOriginationParticipant.getInviteRecipient().getFullName()));
        }

        Institution participantInstitution = eventOriginationParticipant.getParticipant();
        User inviteRecipient = eventOriginationParticipant.getInviteRecipient();

        List<User> recipientParticipantUsers = userService.getNonSystemUsersByInstitutionUidAndUserRoleId(participantInstitution.getUid(), RECV_ALL_INST_INVS.getId());
        if ( inviteRecipient != null ) {
            boolean isPrimaryUserInRecipients = recipientParticipantUsers.stream().anyMatch(user->user.getUid().equals(inviteRecipient.getUid()));
            if (!isPrimaryUserInRecipients) recipientParticipantUsers.add(inviteRecipient);
        }

        if ( !recipientParticipantUsers.isEmpty() ) {

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("dealName", deal.getName());
            templateData.put("recipients", recipientParticipantUsers); // will want a fallback if they don't provide a primary contact
            templateData.put("from", sendAddress);
            templateData.put("dealUid", deal.getUid());
            templateData.put("leadInstitution", currentUser.getInstitution().getName());
            templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
            templateData.put("participantId", eventOriginationParticipant.getId());

            emailService.sendEmail(EmailTypeEnum.INVITE_SENT, deal, templateData);
        }

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
        eop.setEvent(eventService.getEventById(eventOriginationParticipant.getEvent().getId()));

        return eop;
    }

    public EventOriginationParticipant acceptEventParticipantInvite(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Event event = eventOriginationParticipant.getEvent();
        Deal deal = event.getDeal();

        //TODO: Validate that this is the participant.

        /*
         *  Perform validations before moving participant to the next step.
         */
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_2.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 4 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // If event has been launched and interest has been sent move the participant to step 5, otherwise move to step 4.
        if ( event.getStage().getOrder() >= STAGE_3.getOrder() ) {
            eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_5);
        } else {
            eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_4);
        }

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("eventParticipantId", eventOriginationParticipant.getId());
        activityService.createActivity(DEAL_INTEREST, deal.getId(), eventOriginationParticipant.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("from", sendAddress);
        templateData.put("dealUid", deal.getUid());
        templateData.put("leadInstitution", deal.getOriginator().getName());
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("participantUid", eventOriginationParticipant.getParticipant().getUid());
        templateData.put("participantId", eventOriginationParticipant.getId());

        emailService.sendEmail(EmailTypeEnum.DEAL_INTEREST, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
    }

    public EventOriginationParticipant approveEventFullDealAccess(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving participant to the next step.
         */
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_4.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 5 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // Move the participant to the next step ("Full Deal Access Provided").
        eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_5);

        // Timestamp the full deal access date for the deal participant.
        eventOriginationParticipantService.updateFullDealAccessDate(eventParticipantId, currentUser);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
    }

    @Transactional
    public Event launchEvent(Map<String, Object> eventMap, User currentUser) {

        // Verify authorization for this event.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventMap.get("uid").toString());
        Deal deal = event.getDeal();    // This is to make the following code referencing the deal much cleaner.

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving deal to the next stage.  Will ignore if stage is already in the future.
         */
        if ( event.getStage().getOrder() < STAGE_3.getOrder() && event.getStage().getOrder() != STAGE_2.getOrder() ) {

            String message = String.format("Event stage cannot be launched from stage %d.", event.getStage().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // Increment the Deal Stage to "Awaiting Draft Loan Documents"
        eventService.incrementEventToStage(event.getUid(), STAGE_4, currentUser);

        /*
         *  Update the deal dates.  Will only update the launch, commitment, and projected close dates on the deal.
         */
        event = eventService.updateLaunchDates(event, eventMap, currentUser);

        List<EventOriginationParticipant> participants = eventOriginationParticipantService.getEventOriginationParticipantsByEventUid(event.getUid());

        /*
         *  Event is launched, so move all of the participants to the step 5 "Full Deal Access Provided".
         */
        for ( EventOriginationParticipant participant : participants ) {
            if ( participant.getStep().getOrder() == STEP_4.getOrder() ) {
                eventOriginationParticipantService.incrementParticipantToStep(participant, STEP_5);
            }
        }

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("deal", deal, "event", event);
        activityService.createActivity(DEAL_LAUNCHED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("from", sendAddress);
        templateData.put("dealUid", deal.getUid());
        templateData.put("leadInstitution", currentUser.getInstitution().getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        templateData.put("commitmentDate", event.getCommitmentDate().format(formatter));
        templateData.put("projectedCloseDate", event.getProjectedCloseDate().format(formatter));

        emailService.sendEmail(EmailTypeEnum.DEAL_LAUNCHED, deal, templateData);

        return eventService.getEventById(event.getId());
    }

    public EventOriginationParticipant sendEventCommitment(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the participant.

        // Perform validations before moving participant to the next step.
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_5.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 6 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // Move the participant to the next step.
        eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_6);

        /*
         *  Record the activity in the timeline.
         */
        List<EventParticipantFacility> eventParticipantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventParticipantId(eventOriginationParticipant.getId());
        Map<String, Object> activityMap = Map.of("eventParticipant", eventOriginationParticipant, "eventParticipantFacility", eventParticipantFacilities);
        activityService.createActivity(COMMITMENTS_SENT, deal.getId(), eventOriginationParticipant.getParticipant().getId(), activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("participantInstitutionUid", eventOriginationParticipant.getParticipant().getUid());
        templateData.put("participantId", eventOriginationParticipant.getId());

        emailService.sendEmail(EmailTypeEnum.COMMITMENTS_SENT, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
    }

    @Transactional
    public EventOriginationParticipant sendEventAllocation(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Event event = eventOriginationParticipant.getEvent();
        Deal deal = event.getDeal();

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving event to the next stage.  Will ignore if the stage is in the future.
         */
        if ( event.getStage().getOrder() < STAGE_4.getOrder() && event.getStage().getOrder() != STAGE_3.getOrder() ) {

            String message = String.format("Lead cannot send allocation from stage %d.", event.getStage().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        /*
         *  Perform validations before moving participant to the next step.
         */
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_6.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 8 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // Skip step 7 ("Allocated") and auto-advance to step 8 ("Awaiting Draft Loan Documentation Upload").
        eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_8);

        /*
         *  Record the activity in the timeline.
         */
        List<EventParticipantFacility> eventParticipantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventParticipantId(eventOriginationParticipant.getId());

        Map<String, Object> activityMap = Map.of("eventParticipant", eventOriginationParticipant, "eventParticipantFacility", eventParticipantFacilities);
        activityService.createActivity(ALLOCATIONS_SENT, deal.getId(), eventOriginationParticipant.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("participantInstitutionUid", eventOriginationParticipant.getParticipant().getUid());
        templateData.put("leadInstitution", deal.getOriginator().getName());
        templateData.put("participantId", eventOriginationParticipant.getId());

        emailService.sendEmail(EmailTypeEnum.ALLOCATIONS_SENT, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
    }

    @Transactional
    public Event confirmEventDraftLoanUploaded(String eventUid, String commentsDueByDate, User currentUser) {

        // Verify authorization for this deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);
        Deal deal = event.getDeal();
        Event oldEvent = SerializationUtils.clone(event);
        Map<String, Object> eventDateMap = new HashMap<>();

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving deal to the next stage.  Will ignore if stage is in the future.
         */
        if ( event.getStage().getOrder() < STAGE_5.getOrder() && event.getStage().getOrder() != STAGE_4.getOrder() ) {

            String message = String.format("Event stage cannot be advanced from stage %d.", event.getStage().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        event = eventService.incrementEventToStage(event.getUid(), STAGE_5, currentUser);
        LocalDate commentsDueByDateObject = LocalDate.parse(commentsDueByDate);

        event.setCommentsDueByDate(commentsDueByDateObject);
        eventDateMap.put("commentsDueByDate", commentsDueByDateObject);
        eventService.update(event, currentUser);

        Map<String, Object> dateActivityMap = new HashMap<>();
        dateActivityMap.put("oldEvent", oldEvent);
        dateActivityMap.put("eventDateMap", eventDateMap);

        if ( oldEvent.getCommentsDueByDate() == null || !oldEvent.getCommentsDueByDate().equals(commentsDueByDateObject) ) {
            activityService.createActivity(DEAL_DATES_UPDATED, deal.getId(), null, dateActivityMap, currentUser, SYSTEM_MARKETPLACE);
        }

        /*
         *  Record the Draft Loan Docs Uploaded activity in the timeline.
         *  TODO: Send the correct file link once the upload is implemented.
         */
        Map<String, Object> activityMap = Map.of("documentLink", "file");
        activityService.createActivity(DRAFT_LOAN_DOCS_UPLOADED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        Map<String, Object> draftLoanTemplateData = new HashMap<>();
        draftLoanTemplateData.put("dealName", deal.getName());
        draftLoanTemplateData.put("dealUid", deal.getUid());
        draftLoanTemplateData.put("from", sendAddress);
        draftLoanTemplateData.put("leadInstitution", currentUser.getInstitution().getName());
        draftLoanTemplateData.put("commentsDueByDate", event.getCommentsDueByDate().format(formatter));
        emailService.sendEmail(EmailTypeEnum.DRAFT_LOAN_DOCS_UPLOADED, deal, draftLoanTemplateData);

        return eventService.getEventById(event.getId());
    }

    public Event notifyEventFinalLoanUploaded(String eventUid, User currentUser) {

        // Verify authorization for this deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);
        Deal deal = event.getDeal();

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving deal to the next stage.  Will ignore if the stage is in the future.
         */
        if ( event.getStage().getOrder() != STAGE_5.getOrder() ) {

            String message = String.format("Event stage cannot be advanced from stage %d.", event.getStage().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        eventService.incrementEventToStage(eventUid, STAGE_8, currentUser);    // Otherwise, increment the Deal Stage to "Loan Documentation Complete"

        /*
         *  Record the Final Loan Docs Uploaded activity in the timeline.
         *  TODO: Send the correct file link once the upload is implemented.
         */
        Map<String, Object> activityMap = Map.of("documentLink", "file");
        activityService.createActivity(FINAL_LOAN_DOCS_UPLOADED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        /*
         *  TODO: build out template data for the email.
         */
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("leadInstitution", currentUser.getInstitution().getName());
        emailService.sendEmail(EmailTypeEnum.FINAL_LOAN_DOCS_UPLOADED, deal, templateData);

        return eventService.getEventByUid(eventUid);
    }

    public EventOriginationParticipant confirmEventLeadSentParticipantCertificate(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving participant to the next step.
         */
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_8.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 9 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_9);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("externalPC", true, "eventParticipant", eventOriginationParticipant);
        activityService.createActivity(PART_CERT_SENT, deal.getId(), eventOriginationParticipant.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("leadInstitution", deal.getOriginator().getName());
        templateData.put("participantId", eventOriginationParticipant.getId());
        templateData.put("leadInstitution", deal.getOriginator().getName());
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("participantInstitutionUid", eventOriginationParticipant.getParticipant().getUid());


        emailService.sendEmail(EmailTypeEnum.PART_CERT_SENT, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
    }

    @Transactional
    public EventOriginationParticipant confirmEventParticipantSentParticipantCertificate(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the participant.

        /*
         *  Perform validations before moving participant to the next step.
         */
        if ( eventOriginationParticipant.getStep().getOrder() != STEP_9.getOrder() ) {

            String message = String.format("Participant cannot be moved to step 10 from step %d.", eventOriginationParticipant.getStep().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        eventOriginationParticipantService.incrementParticipantToStep(eventOriginationParticipant, STEP_10);

        Map<String, Object> activityMap = Map.ofEntries(
            entry("externalPC", true),
            entry("participantId", eventOriginationParticipant.getId())
        );
        activityService.createActivity(SIGNED_PC_SENT, deal.getId(), eventOriginationParticipant.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("leadInstitution", deal.getOriginator().getName());
        templateData.put("participantInstitutionUid", eventOriginationParticipant.getParticipant().getUid());
        templateData.put("participantId", eventOriginationParticipant.getId());

        emailService.sendEmail(EmailTypeEnum.SIGNED_PC_SENT, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventOriginationParticipant.getId());
    }

    public Event closeEvent(String eventUid, String effectiveDate, User currentUser) {

        // Verify authorization for this deal.
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);
        Event oldEvent =  SerializationUtils.clone(event);
        Map<String, Object> eventDateMap = new HashMap<>();

        //TODO: Validate this is the originator.

        /*
         *  Perform validations before moving event to the next stage.  Will ignore if the event is already closed.
         */
        if ( event.getStage().getOrder() < STAGE_9.getOrder() && event.getStage().getOrder() != STAGE_8.getOrder() ) {

            String message = String.format("Event stage cannot be advanced from stage %d.", event.getStage().getOrder());
            log.error(message);
            throw new OperationNotAllowedException(message);

        }

        // Increment the Deal Stage to "Deal Origination Closed"
        eventService.incrementEventToStage(eventUid, STAGE_9, currentUser);

        event.setEffectiveDate(LocalDate.parse(effectiveDate));
        eventDateMap.put("effectiveDate", LocalDate.parse(effectiveDate));

        /*
         *  Remove all draft Deal Participants and their Facilities on the event.
         */
        eventParticipantFacilityService.deleteDraftEventParticipantFacilitiesOnEvent(event.getUid());
        eventOriginationParticipantService.deleteDraftEventParticipantsOnEvent(event.getUid());
        eventParticipantService.deleteDraftEventParticipantsOnEvent(event.getUid());

        /*
         *  Close the event and update the effective date on the event.
         */
        eventService.updateCloseDates(event, LocalDate.parse(effectiveDate), currentUser);
        eventDateMap.put("closeDate", LocalDate.now());

        Map<String, Object> dateActivityMap = new HashMap<>();
        dateActivityMap.put("oldEvent", oldEvent);
        dateActivityMap.put("eventDateMap", eventDateMap);
        activityService.createActivity(DEAL_DATES_UPDATED, event.getId(), null, dateActivityMap, currentUser, SYSTEM_MARKETPLACE);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("event", event);
        activityService.createActivity(DEAL_CLOSED, event.getDeal().getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        // Create templateData for email notification
        Map<String, Object> templateData = new HashMap<>();
        ArrayList<Map<String, String>> eventDatesArray = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        templateData.put("eventName", event.getName());
        templateData.put("eventUid", event.getUid());
        templateData.put("from", sendAddress);
        templateData.put("leadInstitution", event.getDeal().getOriginator().getName());
        templateData.put("leadInstitutionUid", event.getDeal().getOriginator().getUid());

        /*
         *  Send email notification
         */
        eventDatesArray.add(Map.of ("eventDateField", "Effective Date", "newDealDate", LocalDate.parse(effectiveDate).format(formatter)));
        eventDatesArray.add(Map.of ("eventDateField", "Close Date", "newDealDate", LocalDate.now().format(formatter)));
        templateData.put("eventDates", eventDatesArray);
        emailService.sendEmail(EmailTypeEnum.DEAL_DATES_UPDATED, event.getDeal(), templateData);

        return eventService.getEventByUid(eventUid);
    }

    public EventOriginationParticipant declineEvent(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the participant.

        // Update the decline event flag on the event participant record.
        eventOriginationParticipantService.declineEventForParticipant(eventParticipantId, currentUser);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("eventParticipant", eventOriginationParticipant);
        activityService.createActivity(DEAL_DECLINED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("participantId", eventOriginationParticipant.getId());
        templateData.put("participantInstitutionUid", eventOriginationParticipant.getParticipant().getUid());
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("leadInstitution", currentUser.getInstitution().getName());

        emailService.sendEmail(EmailTypeEnum.DEAL_DECLINED, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);
    }

    public EventOriginationParticipant removeParticipantFromEvent(Long eventParticipantId, User currentUser) {

        EventOriginationParticipant eventOriginationParticipant = preEventStepSetup(eventParticipantId, currentUser);
        Deal deal = eventOriginationParticipant.getEvent().getDeal();

        //TODO: Validate this is the participant.

        // Update the removed from the deal flag on the event participant record.
        eventOriginationParticipantService.removedParticipantFromEvent(eventParticipantId, currentUser);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("eventParticipant", eventOriginationParticipant);
        activityService.createActivity(PARTICIPANT_REMOVED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("participantId", eventOriginationParticipant.getId());
        templateData.put("participantInstitutionUid", eventOriginationParticipant.getParticipant().getUid());
        templateData.put("participantInstitution", eventOriginationParticipant.getParticipant().getName());
        templateData.put("leadInstitution", currentUser.getInstitution().getName());

        emailService.sendEmail(EmailTypeEnum.PARTICIPANT_REMOVED, deal, templateData);

        return eventOriginationParticipantService.getEventOriginationParticipantById(eventParticipantId);
    }

    public Event setLeadInvitationDate(Long eventId, User currentUser) {

        // Set the current date and time to the lead invitation date.
        return eventService.updateLeadInvitationDate(eventId, currentUser);
    }

    public Event setLeadCommitmentDate(Long eventId, User currentUser) {

        // Set the current date and time to the lead commitment date.
        return eventService.updateLeadCommitmentDate(eventId, currentUser);
    }

    public Event setLeadAllocationDate(Long eventId, User currentUser) {

        // Set the current date and time to the lead allocation date.
        return eventService.updateLeadAllocationDate(eventId, currentUser);
    }

    /**
     * This method calls out to the {@link EventService} method that updates the event dates.  The only dates updated are
     * the five possible dates in the dialog during the participant flow.
     *
     * @param eventDateMap  A Map of the dates to be changed and their new values.
     * @param currentUser   The user submitting the change to the dates.
     *
     * @return {@link Event} The full Event object with the updated dates.
     */
    public Event updateEventDates(Map<String, Object> eventDateMap, User currentUser) {
        return eventService.updateEventDates(eventDateMap, currentUser);
    }

}