package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.*;
import com.westmonroe.loansyndication.model.EmailNotification;
import com.westmonroe.loansyndication.model.RestResponse;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.AwsService;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.service.deal.DealDocumentService;
import com.westmonroe.loansyndication.service.deal.DealFacilityService;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.event.EventOriginationParticipantService;
import com.westmonroe.loansyndication.service.event.EventParticipantService;
import com.westmonroe.loansyndication.service.event.EventService;
import com.westmonroe.loansyndication.utils.ActivityTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.*;
import static com.westmonroe.loansyndication.utils.Constants.ERR_FILE_ALREADY_EXISTS;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.DocumentCategoryEnum.*;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.FILE_UPLOADED;

@Tag(name = "Documents", description = "Document management APIs")
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api")
public class DealController {
    private final EventService eventService;
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private final DealService dealService;
    private final DealDocumentService dealDocumentService;
    private final DealFacilityService dealFacilityService;
    private final EventParticipantService eventParticipantService;
    private final EventOriginationParticipantService eventOriginationParticipantService;
    private final AwsService awsService;
    private final AuthorizationService authorizationService;
    private final ActivityService activityService;
    private final EmailService emailService;

    public DealController(DealService dealService, DealDocumentService dealDocumentService, DealFacilityService dealFacilityService
            , EventParticipantService eventParticipantService, EventOriginationParticipantService eventOriginationParticipantService
            , AwsService awsService, AuthorizationService authorizationService, ActivityService activityService
            , EmailService emailService, EventService eventService) {
        this.dealService = dealService;
        this.dealDocumentService = dealDocumentService;
        this.dealFacilityService = dealFacilityService;
        this.eventParticipantService = eventParticipantService;
        this.eventOriginationParticipantService = eventOriginationParticipantService;
        this.awsService = awsService;
        this.authorizationService = authorizationService;
        this.activityService = activityService;
        this.emailService = emailService;
        this.eventService = eventService;
    }

    @Operation(hidden = true)
    @GetMapping(value = "/institutions/{institutionId}/deals", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Deal>> getInstitutionDeals(@PathVariable String institutionId, @AuthenticationPrincipal User currentUser) {

        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, institutionId);

        return new ResponseEntity<>(dealService.getAllDealsByInstitutionUid(institutionId, currentUser), HttpStatus.OK);
    }

    @Operation(
        summary = "Retrieve all documents for a Deal",
        description = "Get the list of documents uploaded for a Deal.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "The list of documents.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "404", description = "No documents were found for the Deal.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/deals/{dealId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ACCESS_ALL_INST_DEALS', 'SUPER_ADM')")
    public ResponseEntity<List<DealDocument>> getDocumentsForDeal(@PathVariable("dealId") String dealUid
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        return new ResponseEntity<>(dealDocumentService.getDocumentsForDeal(dealUid), HttpStatus.OK);
    }

    @Operation(
        summary = "Upload a document for a Deal",
        description = "Uploads a single document for a Deal.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully uploaded.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "500", description = "The document could not be uploaded.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/documents/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('MNG_DEAL_FILES', 'SUPER_ADM')")
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ResponseEntity<RestResponse> uploadDocumentForDeal(@PathVariable("dealId") String dealUid, @RequestParam("file") MultipartFile multipartFile
            , @RequestParam("category") String category, @RequestParam("description") String description
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        Deal deal = authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        String displayName;
        DealDocument document;

        synchronized (this) {

            // Make sure we're saving as a unique file name if a duplicate file name is uploaded.
            displayName = dealDocumentService.getUniqueFileName(dealUid, multipartFile.getOriginalFilename());

            // Save the document to the database.
            document = dealDocumentService.save(deal, category, displayName, multipartFile.getContentType()
                    , description, source, currentUser);

        }

        // Upload the deal document.  Exception will be generated if there's an issue.
        awsService.uploadDocument(deal, document, currentUser, multipartFile);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("dealDocument", document);
        activityService.createActivity(ActivityTypeEnum.FILE_UPLOADED, deal.getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        //TODO: templateData.put("isDealLaunched", deal.getStage().getOrder() >= 3);
        templateData.put("from", sendAddress);
        templateData.put("uploadedByInstitutionUid", currentUser.getInstitution().getUid());
        templateData.put("uploadedByInstitution", currentUser.getInstitution().getName());

        ObjectMapper objectMapper = new ObjectMapper();
        String templateDataJson = "";

        try {
            templateDataJson = objectMapper.writeValueAsString(templateData);
        } catch ( JsonProcessingException e ) {
            throw new ActivityCreationException("There was an error creating template data Json");
        }

        EmailNotification emailNotification = new EmailNotification();
        emailNotification.setEmailTypeCd(Long.toString(FILE_UPLOADED.getId()));
        emailNotification.setDeal(deal);
        emailNotification.setTemplateDataJson(templateDataJson);

        emailService.save(emailNotification);

        return new ResponseEntity<>(
                        new RestResponse("Document Upload"
                                        , HttpStatus.CREATED.value()
                                        , Instant.now().toString()
                                        , "File was successfully uploaded.")
                        , HttpStatus.CREATED);
    }

    @Operation(
        summary = "Upload the pricing grid for the Deal Facility",
        description = "Uploads a pricing grid document for the Deal Facility.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully uploaded.",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "400", description = "The Pricing Grid document has already been uploaded.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/facilities/{facilityId}/pricingGrids", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> uploadPricingGrid(@PathVariable("dealId") String dealUid
            , @PathVariable("facilityId") Long facilityId, @RequestParam("file") MultipartFile multipartFile
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        Deal deal = authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the originating institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, deal.getOriginator().getUid());

        DealFacility dealFacility = dealFacilityService.getFacilityForId(facilityId);

        // Verify that facility belongs to the deal.
        if ( !dealFacility.getDeal().getUid().equals(dealUid) ) {
            log.error("Facility does not belong to the deal.");
            throw new InvalidDataException("Facility does not belong to the deal.");
        }

        //Check if there exists a pricingGrid with this name
        try {
            DealDocument conflictingDocument = dealDocumentService.getDocumentByDealUidAndDisplayName(dealUid, multipartFile.getOriginalFilename());
            conflictingDocument.setDisplayName(dealDocumentService.getUniqueFileName(dealUid,multipartFile.getOriginalFilename()));
            dealDocumentService.update(conflictingDocument);
        } catch (DataNotFoundException e) {
        //Do nothing this means file name is unique
        }

        // Save the document to the database.
        DealDocument document = dealDocumentService.save(deal, PRICING_GRID.getName(), multipartFile.getOriginalFilename()
                , multipartFile.getContentType(), PRICING_GRID.getName(), source, currentUser);

        // Upload the pricing grid document.  Exception will be generated if there's an issue.
        awsService.uploadDocument(deal, document, currentUser, multipartFile);

        // Update the deal facility with the pricing grid.
        dealFacility.setPricingGrid(document);
        dealFacilityService.update(dealFacility, currentUser);

        /*
         *  Record the activity in the timeline
         */
        Map<String, Object> activityMap = new HashMap<>();
        Map<String, Object> dealFacilityMap = new HashMap<>();
        dealFacilityMap.put("wasPricingGridUploaded", true);
        activityMap.put("dealFacilityMap", dealFacilityMap);
        activityMap.put("newDealFacility", dealFacility);
        activityMap.put("oldDealFacility", dealFacility);
        activityService.createActivity(DEAL_INFO_UPDATED, dealFacility.getDeal().getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        // Upload
        return new ResponseEntity<>(
                new RestResponse("Document Upload"
                        , HttpStatus.CREATED.value()
                        , Instant.now().toString()
                        , "File was successfully uploaded.")
                , HttpStatus.CREATED);
    }

    @Operation(
        summary = "Delete the pricing grid for the Deal Facility",
        description = "Deletes the pricing grid document for the Deal Facility.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully deleted.",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "500", description = "There was an error deleting the document from the s3 bucket.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/deals/{dealId}/facilities/{facilityId}/pricingGrids", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> deletePricingGrid(@PathVariable("dealId") String dealUid
            , @PathVariable("facilityId") Long facilityId
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the originating institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, dealUid);

        DealFacility dealFacility = dealFacilityService.getFacilityForId(facilityId);

        // Verify that facility belongs to the deal.
        if ( !dealFacility.getDeal().getUid().equals(dealUid) ) {

            log.error("Facility does not belong to the deal.");
            throw new InvalidDataException("Facility does not belong to the deal.");

        }

        // Get the pricing grid document to be deleted.
        DealDocument document = dealFacility.getPricingGrid();

        // Update the deal facility with the pricing grid.  Has to be done first because of foreign key constraints.
        dealFacility.setPricingGrid(null);
        dealFacilityService.update(dealFacility, currentUser);

        /*
         *  Record the activity in the timeline
         */
        Map<String, Object> activityMap = new HashMap<>();
        Map<String, Object> dealFacilityMap = new HashMap<>();
        dealFacilityMap.put("wasPricingGridUploaded", true);
        activityMap.put("dealFacilityMap", dealFacilityMap);
        activityMap.put("newDealFacility", dealFacility);
        activityMap.put("oldDealFacility", dealFacility);
        activityService.createActivity(DEAL_INFO_UPDATED, dealFacility.getDeal().getId(), null, activityMap, currentUser, SYSTEM_MARKETPLACE);

        return new ResponseEntity<>(
                new RestResponse("Document Deleted"
                        , HttpStatus.OK.value()
                        , Instant.now().toString()
                        , "File was successfully deleted.")
                , HttpStatus.OK);
    }

    @Operation(
        summary = "Upload the commitment letter for the Deal Participant",
        description = "Uploads a single commitment letter for the Deal Participant.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully uploaded.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "400", description = "The Commitment Letter document has already been uploaded.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/participants/{participantId}/commitmentLetters", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> uploadCommitmentLetter(@PathVariable("dealId") String dealUid
            , @PathVariable("participantId") String participantUid, @RequestParam("file") MultipartFile multipartFile
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.  We are assuming that there is an open event.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the participating institution.
        authorizationService.authorizeUserInParticipatingInstitution(currentUser, dealEvent);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(
                dealEvent.getOpenEventUid(), currentUser.getInstitution().getUid()
        );

        // Check to see if the document has already been uploaded.
        if ( eop.getCommitmentLetter() != null ) {
            log.error(String.format(ERR_FILE_ALREADY_EXISTS, COMMIT_LTR.getName()));
            throw new FileUploadException(String.format(ERR_FILE_ALREADY_EXISTS, COMMIT_LTR.getName()));
        }

        // Save the deal document record before the upload.
        DealDocument document = dealDocumentService.save(dealEvent.toDeal(), COMMIT_LTR.getName(), multipartFile.getOriginalFilename()
                , multipartFile.getContentType(), COMMIT_LTR.getName(), source, currentUser);

        // Upload the commitment letter document.  Exception will be generated if there's an issue.
        awsService.uploadDocument(dealEvent.toDeal(), document, currentUser, multipartFile);

        // Update the deal participant with the commitment letter.
        eop.setCommitmentLetter(document);
        eventOriginationParticipantService.update(eop);     //TODO: Need to update the audit fields in event participant.

        // Upload
        return new ResponseEntity<>(
                new RestResponse("Document Upload"
                        , HttpStatus.CREATED.value()
                        , Instant.now().toString()
                        , "File was successfully uploaded.")
                , HttpStatus.CREATED);
    }

    @Operation(
        summary = "Delete the commitment letter for the Deal Facility",
        description = "Deletes the commitment letter document for the Deal Facility.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successfully deleted the commitment letter.",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "500", description = "There was an error deleting the document from the s3 bucket.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/deals/{dealId}/participants/{participantId}/commitmentLetters", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> deleteCommitmentLetter(@PathVariable("dealId") String dealUid
            , @PathVariable("participantId") String participantUid
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the participating institution.
        authorizationService.authorizeUserInParticipatingInstitution(currentUser, dealEvent);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(
                dealEvent.getOpenEventUid(), currentUser.getInstitution().getUid()
        );

        // Verify that participant belongs to the deal.
        if ( !eop.getEvent().getDeal().getUid().equals(dealUid) ) {

            log.error("Participant does not belong to the deal.");
            throw new InvalidDataException("Participant does not belong to the deal.");

        }

        // Get the commitment letter document to be deleted.
        DealDocument document = eop.getCommitmentLetter();

        // Update the deal participant with the commitment letter.  Has to be done first because of foreign key constraints.
        eop.setCommitmentLetter(null);
        eventOriginationParticipantService.update(eop);     //TODO: Need to update the audit fields in event participant.

        // Delete the document from the database.
        dealDocumentService.deleteById(document.getId());

        // Delete the document from the s3 bucket and the database.
        awsService.deleteDocument(document);

        return new ResponseEntity<>(
                new RestResponse("Document Deleted"
                        , HttpStatus.OK.value()
                        , Instant.now().toString()
                        , "File was successfully deleted.")
                , HttpStatus.OK);
    }

    @Operation(
        summary = "Upload the participant certificate",
        description = "Uploads a document that represents a participant certificate for the Deal Participant.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully uploaded.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "400", description = "The Participant Certificate document has already been uploaded.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/participants/{participantId}/participantCertificates", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> uploadParticipantCertificate(@PathVariable("dealId") String dealUid
            , @PathVariable("participantId") String participantUid, @RequestParam("file") MultipartFile multipartFile
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the originating institution.
        authorizationService.authorizeUserInOriginatingInstitution(currentUser, dealEvent);

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(
                dealEvent.getOpenEventUid(), participantUid
        );

        // Check to see if the document has already been uploaded.
        if ( eop.getParticipantCertificate() != null ) {
            log.error(String.format(ERR_FILE_ALREADY_EXISTS, PART_CERT.getName()));
            throw new FileUploadException(String.format(ERR_FILE_ALREADY_EXISTS, PART_CERT.getName()));
        }

        // Save the deal document record before the upload.
        DealDocument document = dealDocumentService.save(dealEvent.toDeal(), PART_CERT.getName(), multipartFile.getOriginalFilename()
                , multipartFile.getContentType(), PART_CERT.getName(), source, currentUser);

        // Upload the participant certificate document.  Exception will be generated if there's an issue.
        awsService.uploadDocument(dealEvent.toDeal(), document, currentUser, multipartFile);

        // Update the deal participant with the participant certificate.
        eop.setParticipantCertificate(document);
        eventOriginationParticipantService.update(eop);     //TODO: Need to update the audit fields in event participant.

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("externalPC", false, "eventOriginationParticipant", eop);
        activityService.createActivity(PART_CERT_SENT, dealEvent.getId(), eop.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        return new ResponseEntity<>(
                new RestResponse("Document Upload"
                        , HttpStatus.CREATED.value()
                        , Instant.now().toString()
                        , "File was successfully uploaded.")
                , HttpStatus.CREATED);
    }

    @Operation(
        summary = "Delete the participant certificate for the Deal Facility",
        description = "Deletes the participant certificate document for the Deal Facility.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully deleted.",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "500", description = "There was an error deleting the document from the s3 bucket.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/deals/{dealId}/participants/{participantId}/participantCertificates", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> deleteParticipantCertificate(@PathVariable("dealId") String dealUid
            , @PathVariable("participantId") String participantUid
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the originating institution.
        authorizationService.authorizeUserInOriginatingInstitution(currentUser, dealEvent);

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(
                dealEvent.getOpenEventUid(), participantUid
        );

        // Verify that participant belongs to the deal.
        if ( !eop.getEvent().getDeal().getUid().equals(dealUid) ) {

            log.error("Participant does not belong to the deal.");
            throw new InvalidDataException("Participant does not belong to the deal.");

        }

        // Get the participant certificate document to be deleted.
        DealDocument document = eop.getParticipantCertificate();

        // Update the deal participant with the participant certificate.  Has to be done first because of foreign key constraints.
        eop.setParticipantCertificate(null);
        eventOriginationParticipantService.update(eop);     //TODO: Need to update the audit fields in event participant.

        // Delete the document from the database.
        dealDocumentService.deleteById(document.getId());

        // Delete the document from the s3 bucket and the database.
        awsService.deleteDocument(document);

        return new ResponseEntity<>(
                new RestResponse("Document Deleted"
                        , HttpStatus.OK.value()
                        , Instant.now().toString()
                        , "File was successfully deleted.")
                , HttpStatus.OK);
    }

    @Operation(
        summary = "Upload the signed participant certificate",
        description = "Uploads a document that represents a signed participant certificate for the Deal Participant.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully uploaded.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "400", description = "The Signed Participant Certificate document has already been uploaded.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/participants/{participantId}/signedParticipantCertificates", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> uploadSignedParticipantCertificate(@PathVariable("dealId") String dealUid
            , @PathVariable("participantId") String participantUid, @RequestParam("file") MultipartFile multipartFile
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the participating institution.
        authorizationService.authorizeUserInParticipatingInstitution(currentUser, dealEvent);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(
                dealEvent.getOpenEventUid(), currentUser.getInstitution().getUid()
        );

        // Check to see if the document has already been uploaded.
        if ( eop.getSignedParticipantCertificate() != null ) {
            log.error(String.format(ERR_FILE_ALREADY_EXISTS, SIGNED_PART_CERT.getName()));
            throw new FileUploadException(String.format(ERR_FILE_ALREADY_EXISTS, SIGNED_PART_CERT.getName()));
        }

        // Save the deal document record before the upload.
        DealDocument document = dealDocumentService.save(dealEvent.toDeal(), SIGNED_PART_CERT.getName(), multipartFile.getOriginalFilename()
                , multipartFile.getContentType(), SIGNED_PART_CERT.getName(), source, currentUser);

        // Upload the signed participant certificate document.  Exception will be generated if there's an issue.
        awsService.uploadDocument(dealEvent.toDeal(), document, currentUser, multipartFile);

        // Update the deal participant with the signed participant certificate.
        eop.setSignedParticipantCertificate(document);
        eventOriginationParticipantService.update(eop);

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("externalPC", false);
        activityService.createActivity(SIGNED_PC_SENT, dealEvent.getId(), eop.getParticipant().getId()
                , activityMap, currentUser, SYSTEM_MARKETPLACE);

        return new ResponseEntity<>(
                new RestResponse("Document Upload"
                        , HttpStatus.CREATED.value()
                        , Instant.now().toString()
                        , "File was successfully uploaded.")
                , HttpStatus.CREATED);
    }

    @Operation(
        summary = "Delete the signed participant certificate for the Deal Facility",
        description = "Deletes the signed participant certificate document for the Deal Facility.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "File was successfully deleted.",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "500", description = "There was an error deleting the document from the s3 bucket.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/deals/{dealId}/participants/{participantId}/signedParticipantCertificates", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<RestResponse> deleteSignedParticipantCertificate(@PathVariable("dealId") String dealUid
            , @PathVariable("participantId") String participantUid
            , @RequestParam(value = "source", required = false, defaultValue = SYSTEM_MARKETPLACE) String source
            , @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the participating institution.
        authorizationService.authorizeUserInParticipatingInstitution(currentUser, dealEvent);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        EventOriginationParticipant eop = eventOriginationParticipantService.getEventOriginationParticipantByEventUidAndParticipantUid(
                dealEvent.getOpenEventUid(), currentUser.getInstitution().getUid()
        );

        // Verify that participant belongs to the deal.
        if ( !eop.getEvent().getDeal().getUid().equals(dealUid) ) {

            log.error("Participant does not belong to the deal.");
            throw new InvalidDataException("Participant does not belong to the deal.");

        }

        // Get the signed participant certificate document to be deleted.
        DealDocument document = eop.getSignedParticipantCertificate();

        // Update the deal participant with the signed participant certificate.  Has to be done first because of foreign key constraints.
        eop.setSignedParticipantCertificate(null);
        eventOriginationParticipantService.update(eop);

        // Delete the document from the database.
        dealDocumentService.deleteById(document.getId());

        // Delete the document from the s3 bucket and the database.
        awsService.deleteDocument(document);

        return new ResponseEntity<>(
                new RestResponse("Document Deleted"
                        , HttpStatus.OK.value()
                        , Instant.now().toString()
                        , "File was successfully deleted.")
                , HttpStatus.OK);
    }

    @Operation(
        summary = "Retrieve document information for a Deal",
        description = "Get document information for the specified Deal document.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Deal document information.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DealDocument.class))
            ),
            @ApiResponse(responseCode = "404", description = "Document were found for the specified ID.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/deals/{dealId}/documents/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('MNG_DEAL_FILES', 'SUPER_ADM')")
    public ResponseEntity<DealDocument> getDocumentForDocumentId(@PathVariable("dealId") String dealUid
            , @PathVariable("documentId") Long documentId, @AuthenticationPrincipal User currentUser) {

        // Verify user is authorized for the deal.
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealUid(currentUser, dealUid);

        // Verify that the user is a member of the participating institution.
        authorizationService.authorizeUserInParticipatingInstitution(currentUser, dealEvent);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        /*
         *  Make sure the document belongs to the deal.
         */
        DealDocument document = dealDocumentService.getValidDocumentForDeal(documentId, dealUid);

        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @Operation(
        summary = "View the specified document for a Deal",
        description = "View deal document for the specified Deal document id.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Deal document",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "404", description = "Document were found for the specified ID.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/deals/{dealId}/documents/{documentId}/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> viewDocumentForDocumentId(@PathVariable("dealId") String dealUid
            , @PathVariable("documentId") Long documentId, @AuthenticationPrincipal User currentUser) {

        // Verify the user can access the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);
        
        /*
         *  Make sure the document belongs to the deal.
         */
        DealDocument document = dealDocumentService.getValidDocumentForDeal(documentId, dealUid);

        if (!document.getCategory().getName().equals(PRICING_GRID.getName())) {
            // Excluding pricing grid docs, verify the user has permissions to view at the current deal stage and participant step.
            authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);
        }

        byte[] fileData;

        try {
            fileData = awsService.getDocumentContents(document);
        } catch (Exception e) {
            log.error("Unable to get document contents", e);
            throw new AwsS3Exception("There was an error retrieving the document contents.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getDocumentType()));

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }

    @Operation(
        summary = "Download the specified list of documents for a Deal",
        description = "Download the specified deal documents for the supplied Deal.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Deal document",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Document were found for the specified ID.")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/documents/download", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadDocumentList(@PathVariable("dealId") String dealUid
            , @RequestBody List<DealDocument> docList, @AuthenticationPrincipal User currentUser) throws IOException {

        // Verify the user can access the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        /*
         *  Make sure all documents in the list belong to the deal.
         */
        List<DealDocument> verifiedDocList = dealDocumentService.getValidDocumentsForDeal(docList, dealUid);

        // Get the zipped file contents.
        ByteArrayOutputStream baos = awsService.createZipFileForDocuments(verifiedDocList);

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "documents.zip");

        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    @Operation(
            summary = "Download all documents for the specified Deal",
            description = "Download all documents for the specified Deal.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Deal document",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "404", description = "Document were found for the specified ID.")
            }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealId}/documents/download/all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAllDocuments(@PathVariable("dealId") String dealUid
            , @AuthenticationPrincipal User currentUser) throws IOException {

        // Verify the user can access the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        /*
         *  Get all documents that belong to the deal.
         */
        List<DealDocument> documents = dealDocumentService.getDocumentsForDeal(dealUid);

        // Get the zipped file contents.
        ByteArrayOutputStream baos = awsService.createZipFileForDocuments(documents);

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "documents.zip");

        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    @DeleteMapping(value = "/deals/{dealId}/documents/{documentId}")
    @PreAuthorize("hasAnyRole('MNG_DEAL_FILES', 'SUPER_ADM')")
    public ResponseEntity<Void> deleteDocumentByDealUidAndDocumentId(@PathVariable("dealId") String dealUid
                , @PathVariable("documentId") Long documentId, @AuthenticationPrincipal User currentUser) {

        /*
         *  Make sure the document belongs to the deal.
         */
        DealDocument document = dealDocumentService.getValidDocumentForDeal(documentId, dealUid);

        // Verify the user can access the deal.
        authorizationService.authorizeUserForDealByDealUid(currentUser, dealUid);

        // Verify the user has permissions to view at the current deal stage and participant step.
        authorizationService.authorizeUserForRestApisAndFileAccess(currentUser, dealUid);

        // Delete the document from the s3 bucket and the database.
        awsService.deleteDocument(document);

        // Delete the document from the database.
        dealDocumentService.deleteById(document.getId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}