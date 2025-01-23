package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.ConfidentialityAgreement;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.*;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.AwsService;
import com.westmonroe.loansyndication.service.DefinitionService;
import com.westmonroe.loansyndication.service.deal.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.FILE_REMOVED;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;

@Controller
@Slf4j
@Validated
public class DealGQLController {

    private final DealService dealService;
    private final DealMemberService dealMemberService;
    private final DealDocumentService dealDocumentService;
    private final DealCovenantService dealCovenantService;
    private final DealFacilityService dealFacilityService;
    private final AuthorizationService authorizationService;
    private final AwsService awsService;

    private final ActivityService activityService;
    private final DefinitionService definitionService;

    public DealGQLController(DealService dealService, DealMemberService dealMemberService, DealDocumentService dealDocumentService
            , DealCovenantService dealCovenantService, DealFacilityService dealFacilityService
            , AuthorizationService authorizationService, AwsService awsService, ActivityService activityService
            , DefinitionService definitionService) {
        this.dealService = dealService;
        this.dealMemberService = dealMemberService;
        this.dealDocumentService = dealDocumentService;
        this.dealCovenantService = dealCovenantService;
        this.dealFacilityService = dealFacilityService;
        this.authorizationService = authorizationService;
        this.awsService = awsService;
        this.activityService = activityService;
        this.definitionService = definitionService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('SUPER_ADM')")
    public List<Deal> allDeals(@AuthenticationPrincipal User currentUser) {
        return dealService.getDealsByUser(currentUser);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public DealMember getDealMemberByDealUidAndUserUid(@Argument String dealUid, @Argument String userUid
            , @AuthenticationPrincipal User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getCode().equals("SUPER_ADM"));

        return dealMemberService.getDealMemberByDealUidAndUserUid(dealUid, userUid, currentUser, isAdmin);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<DealMember> getDealMembersByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        List<DealMember> members;

        if (currentUser.getRoles().stream().anyMatch(r -> r.getCode().equals("SUPER_ADM")) )
            members = dealMemberService.getDealMembersByDealUid(uid);
        else
            members = dealMemberService.getDealMembersByDealUid(uid, currentUser);

        return members;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated() && !hasAnyRole('SUPER_ADM')")
    public List<DealSummary> getDealSummaryByUser(@AuthenticationPrincipal User currentUser) {
        return dealService.getSummaryByUser(currentUser);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated() && !hasAnyRole('SUPER_ADM')")
    public List<DealEventSummary> getDealEventSummaryByUser(@AuthenticationPrincipal User currentUser) {
        return dealService.getEventSummaryByUser(currentUser);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Deal getDealByUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        // Verify the user can access the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, uid);

        return authorizationService.authorizeUserForDealByDealUid(currentUser, uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public DealEvent getDealEventByUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        // Perform the authorization check and return.
        authorizationService.authorizeUserForDealEventByDealUid(currentUser, uid);

        return dealService.getDealEventByUid(uid, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Deal createDeal(@Argument @Valid Deal input, @AuthenticationPrincipal User currentUser) {
        return dealService.save(input, currentUser, SYSTEM_MARKETPLACE);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<DealCovenant> getDealCovenantsByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        authorizationService.authorizeUserForDealEventByDealUid(currentUser, uid);
        return dealCovenantService.getCovenantsForDeal(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public DealCovenant getDealCovenantById(@Argument Long id) {
        return dealCovenantService.getCovenantForId(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<DealFacility> getDealFacilitiesByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        authorizationService.authorizeUserForDealEventByDealUid(currentUser, uid);
        return dealFacilityService.getFacilitiesForDeal(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<DealFacility> getDealFacilitiesByEventUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        authorizationService.authorizeUserForDealByEventUid(currentUser, uid);
        return dealFacilityService.getFacilitiesForEvent(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public DealFacility getDealFacilityById(@Argument Long id) {
        return dealFacilityService.getFacilityForId(id);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Deal updateDeal(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {

        // Make sure this user has permissions to this deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, input.get("uid").toString());

        return dealService.update(input, currentUser, SYSTEM_MARKETPLACE);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Deal deleteDeal(@Argument String dealUid, @AuthenticationPrincipal User currentUser) {

        Deal deal = null;

        try {

            // Verify authorization for this deal.
            deal = authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        } catch ( DataNotFoundException e ) {
            // Throw specific error and message when deal not found.
            throw new DataNotFoundException("Deal could not be deleted because it does not exist.");
        }

        // Delete the deal.
        dealService.deleteById(deal.getId());

        return deal;
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_DEAL_MEMBERS', 'SUPER_ADM')")
    public DealMember createDealMember(@Argument DealMember input, @AuthenticationPrincipal User currentUser) {
        return dealMemberService.save(input, currentUser, SYSTEM_MARKETPLACE, false);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_DEAL_MEMBERS', 'SUPER_ADM')")
    public DealMembers createDealMembers(@Argument DealMembers input, @AuthenticationPrincipal User currentUser) {
        return dealMemberService.saveMemberList(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_DEAL_MEMBERS', 'SUPER_ADM')")
    public DealMember deleteDealMember(@Argument DealMember input, @AuthenticationPrincipal User currentUser) {

        // Get the deal member to see if it exists.
        DealMember dealMember = dealMemberService.getDealMemberByDealUidAndUserUid(input.getDeal().getUid(), input.getUser().getUid());

        // Delete the supplied deal member.
        dealMemberService.delete(input, currentUser, SYSTEM_MARKETPLACE);

        return dealMember;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public DealCovenant createDealCovenant(@Argument DealCovenant input, @AuthenticationPrincipal User currentUser) {
        return dealCovenantService.save(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public DealCovenant updateDealCovenant(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {
        return dealCovenantService.update(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public DealCovenant deleteDealCovenant(@Argument Long covenantId, @AuthenticationPrincipal User currentUser) {

        DealCovenant covenant;

        try {

            // Get the covenant to make sure it exists and to return it.
            covenant = dealCovenantService.getCovenantForId(covenantId);

        } catch ( DataNotFoundException e ) {

            // Throw specific error and message when deal covenant not found.
            throw new DataNotFoundException("Deal Covenant could not be deleted because it does not exist.");

        }

        // Verify authorization for this deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, covenant.getDeal().getUid());

        // Delete the covenant.
        dealCovenantService.deleteById(covenantId);

        return covenant;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public DealFacility createDealFacility(@Argument @Valid DealFacility input, @AuthenticationPrincipal User currentUser) {
        return dealFacilityService.save(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public DealFacility updateDealFacility(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {
        return dealFacilityService.update(input, currentUser, SYSTEM_MARKETPLACE);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public DealFacility deleteDealFacility(@Argument Long facilityId, @AuthenticationPrincipal User currentUser) {

        DealFacility facility;

        try {

            // Get the facility to make sure it exists and to return it.
            facility = dealFacilityService.getFacilityForId(facilityId);

        } catch ( DataNotFoundException e ) {

            // Throw specific error and message when deal facility not found.
            throw new DataNotFoundException("Deal Facility could not be deleted because it does not exist.");

        }

        // Verify authorization for this deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, facility.getDeal().getUid());

        // Delete the facility.
        dealFacilityService.deleteById(facilityId);

        return facility;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<DealDocument> getDealDocumentsByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        authorizationService.authorizeUserForDealEventByDealUid(currentUser, uid);
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, uid);

        return dealDocumentService.getDocumentsForDeal(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public DealDocument getDealDocumentById(@Argument Long id, @AuthenticationPrincipal User currentUser) {

        DealDocument document = dealDocumentService.getDocumentForId(id);

        // Verify that the user can access the document.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, document.getDeal().getUid());

        return document;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ConfidentialityAgreement getConfidentialityAgreementByDealAndUser(@Argument String dealUid, @AuthenticationPrincipal User currentUser) {
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        return definitionService.getConfidentialityAgreementByDealIdAndUserId(dealEvent.getId(), currentUser.getId());
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_DEAL_FILES', 'SUPER_ADM')")
    public DealDocument updateDealDocument(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser) {

        DealDocument document = dealDocumentService.getDocumentForId(Long.valueOf(input.get("id").toString()));

        // Verify that the user can update the document.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, document.getDeal().getUid());

        // Perform the document update
        return dealDocumentService.update(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_DEAL_FILES', 'SUPER_ADM')")
    @Transactional
    public DealDocument deleteDealDocument(@Argument Long documentId, @AuthenticationPrincipal User currentUser) {

        DealDocument document;

        try {

            // Get the document to make sure it exists and to return it.
            document = dealDocumentService.getDocumentForId(documentId);

        } catch ( DataNotFoundException e ) {

            // Throw specific error and message when deal facility not found.
            throw new DataNotFoundException("Deal Document could not be deleted because it does not exist.");

        }

        // Verify authorization for this deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, document.getDeal().getUid());
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, document.getDeal().getUid());

        // Delete the document.
        dealDocumentService.deleteById(documentId);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("dealDocument", document);
        activityService.createActivity(FILE_REMOVED, document.getDeal().getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        // Delete the document from the AWS S3 bucket.
        awsService.deleteDocument(document);

        return document;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean agreeToConfidentialityAgreement(@Argument String dealUid, @Argument Integer confidentialityAgreementId, @AuthenticationPrincipal User currentUser) {
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        return definitionService.agreeToConfidentialityAgreement(dealEvent.getId(), currentUser, confidentialityAgreementId);
    }

    @SchemaMapping
    public Deal deal(DealMember dealMember, @AuthenticationPrincipal User currentUser) {
        return dealService.getDealByUid(dealMember.getDeal().getUid(), currentUser);
    }

    @SchemaMapping
    public Deal deal(DealDocument dealDocument, @AuthenticationPrincipal User currentUser) {
        return dealService.getDealByUid(dealDocument.getDeal().getUid(), currentUser);
    }

}