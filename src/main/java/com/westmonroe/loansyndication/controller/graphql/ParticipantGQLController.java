package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Controller
@Slf4j
@Validated
public class ParticipantGQLController {

    private ParticipantService participantService;
    private AuthorizationService authorizationService;

    public ParticipantGQLController(ParticipantService participantService, AuthorizationService authorizationService) {
        this.participantService = participantService;
        this.authorizationService = authorizationService;
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant sendEventParticipantInvite(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.sendEventParticipantInvite(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventOriginationParticipant acceptEventParticipantInvite(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.acceptEventParticipantInvite(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant approveEventFullDealAccess(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.approveEventFullDealAccess(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public Event launchEvent(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {
        return participantService.launchEvent(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventOriginationParticipant sendEventCommitment(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.sendEventCommitment(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant sendEventAllocation(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.sendEventAllocation(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public Event confirmEventDraftLoanUploaded(@Argument String eventUid, @Argument String commentsDueByDate, @AuthenticationPrincipal User currentUser) {
        return participantService.confirmEventDraftLoanUploaded(eventUid, commentsDueByDate, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public Event notifyEventFinalLoanUploaded(@Argument String eventUid, @AuthenticationPrincipal User currentUser) {
        return participantService.notifyEventFinalLoanUploaded(eventUid, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant confirmEventLeadSentParticipantCertificate(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.confirmEventLeadSentParticipantCertificate(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventOriginationParticipant confirmEventParticipantSentParticipantCertificate(@Argument Long eventParticipantId
            , @AuthenticationPrincipal User currentUser) {
        return participantService.confirmEventParticipantSentParticipantCertificate(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public Event closeEvent(@Argument String eventUid, @Argument String effectiveDate, @AuthenticationPrincipal User currentUser) {
        return participantService.closeEvent(eventUid, effectiveDate, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public EventOriginationParticipant declineEvent(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.declineEvent(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Event setLeadInvitationDate(@Argument String eventUid, @AuthenticationPrincipal User currentUser) {
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);
        return participantService.setLeadInvitationDate(event.getId(), currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Event setLeadCommitmentDate(@Argument String eventUid, @AuthenticationPrincipal User currentUser) {
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);
        return participantService.setLeadCommitmentDate(event.getId(), currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Event setLeadAllocationDate(@Argument String eventUid, @AuthenticationPrincipal User currentUser) {
        Event event = authorizationService.authorizeUserForDealByEventUid(currentUser, eventUid);
        return participantService.setLeadAllocationDate(event.getId(), currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public EventOriginationParticipant removeParticipantFromEvent(@Argument Long eventParticipantId, @AuthenticationPrincipal User currentUser) {
        return participantService.removeParticipantFromEvent(eventParticipantId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public Event updateEventDates(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {
        return participantService.updateEventDates(input, currentUser);
    }

}