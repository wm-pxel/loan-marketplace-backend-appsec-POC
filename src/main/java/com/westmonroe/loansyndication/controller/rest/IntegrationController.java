package com.westmonroe.loansyndication.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.ActivityCreationException;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.EmailNotification;
import com.westmonroe.loansyndication.model.RestResponse;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.integration.*;
import com.westmonroe.loansyndication.model.integration.mapper.DealDocumentMapper;
import com.westmonroe.loansyndication.service.*;
import com.westmonroe.loansyndication.service.deal.DealDocumentService;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.integration.CustomerDataService;
import com.westmonroe.loansyndication.service.integration.DocumentBatchService;
import com.westmonroe.loansyndication.utils.ActivityTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_INTEGRATION;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.FILE_UPLOADED;
import static com.westmonroe.loansyndication.utils.MapUtils.getNodeValue;

@Tag(name = "Integration", description = "Integration APIs")
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/api/ext")
public class IntegrationController {
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private final CustomerDataService customerDataService;
    private final DocumentBatchService documentBatchService;
    private final DealService dealService;
    private final DealDocumentService dealDocumentService;
    private final DefinitionService definitionService;
    private final AuthorizationService authorizationService;
    private final EmailService emailService;
    private final AwsService awsService;
    private final ActivityService activityService;

    public IntegrationController(CustomerDataService customerDataService, DocumentBatchService documentBatchService
            , DealService dealService, DealDocumentService dealDocumentService, DefinitionService definitionService
            , AuthorizationService authorizationService, AwsService awsService, EmailService emailService
            , ActivityService activityService) {

        this.customerDataService = customerDataService;
        this.documentBatchService = documentBatchService;
        this.dealService = dealService;
        this.dealDocumentService = dealDocumentService;
        this.definitionService = definitionService;
        this.authorizationService = authorizationService;
        this.awsService = awsService;
        this.activityService = activityService;
        this.emailService = emailService;
    }

    @Operation(
        summary = "Retrieve a Customer Data record by Id",
        description = "Get a Customer Data record by providing its unique identifier.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "The Customer Data",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerData.class))
            ),
            @ApiResponse(responseCode = "404", description = "Customer Data was not found")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/data/{dealExternalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerData> getCustomerDataByDealExternalId(@PathVariable("dealExternalId") String dealExternalId
            , @AuthenticationPrincipal User currentUser) {

       // Get the deal first to verify that it exists.
        CustomerData customerData = customerDataService.getCustomerDataByDealExternalId(dealExternalId, currentUser);

        // Perform authorization for this customer data record.
        // TODO: Need to refine the permissions.
        // authorizationService.authorizeUserForDeal(principal, dealUid);

        return new ResponseEntity<>(customerData, HttpStatus.OK);
    }

    @Operation(
        summary = "Create a Customer Data record",
        description = "Create a Customer Data record.",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "Successfully created the record",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerData.class))
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerData> saveCustomerData(@Valid @RequestBody CustomerData customerData
            , BindingResult errors, @AuthenticationPrincipal User currentUser) {

        if ( errors.hasErrors() ) {
            throw new ValidationException(errors.getFieldErrors());
        }

        DealDto dealDto = customerData.getPayload().getMarketplaceData().getDeal();

        // Make sure the user has permissions to create this deal (i.e. they are authorized to the institution).
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, dealDto.getOriginatorId());

        customerData = customerDataService.save(customerData, currentUser);

        // If a batchId was set then we will invoke the AWS File Service Lambda to start processing the documents.
        if ( customerData.getPayload().getBatchId() != null ) {
            awsService.loadDealDocuments(dealDto.getOriginatorId(), customerData.getPayload().getBatchId());
        }

        // Create the location header.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", String.format("/api/ext/data/%s", customerData.getPayload().getMarketplaceData().getDeal().getDealExternalId()));

        return new ResponseEntity<>(customerData, headers, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update a Customer Data record",
            description = "Update a Customer Data record.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Successfully updated the record",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerData.class))
                    )
            }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateCustomerData(@Valid @RequestBody Map<String, Object> customerMap
            , BindingResult errors, @AuthenticationPrincipal User currentUser) {

        if ( errors.hasErrors() ) {
            throw new ValidationException(errors.getFieldErrors());
        }

        // Get the originator id from the customer data map.
        String originatorId = (String) getNodeValue(customerMap, Arrays.asList("payload", "marketplaceData", "deal", "originatorId"));

        // Make sure the user has permissions to update this deal (i.e. they are authorized to the institution).
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, originatorId);

        Long batchId = customerDataService.update(customerMap, currentUser);

        // If a batchId was set then we will invoke the AWS File Service Lambda to start processing the documents.
        if ( batchId != null ) {
            awsService.loadDealDocuments(originatorId, batchId);
        }

        return new ResponseEntity<>(customerMap, HttpStatus.OK);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerData> deleteCustomerData(@RequestBody Map<String, Object> customerMap
            , @AuthenticationPrincipal User currentUser) {

        // Get the originator id from the customer data map.
        String originatorId = (String) getNodeValue(customerMap, Arrays.asList("payload", "marketplaceData", "deal", "originatorId"));

        // Make sure the user has permissions to update this deal (i.e. they are authorized to the institution).
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, originatorId);

        customerDataService.delete(customerMap);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Retrieve the deals for the supplied participant.",
        description = "Get the list of deals for the provided participating institution.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "The list of deals",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DealData.class)))
            ),
            @ApiResponse(responseCode = "404", description = "The participant was not found")
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/participants/{participantId}/deals", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DealData>> getDealListByParticipantId(@PathVariable("participantId") String participantId
            , @AuthenticationPrincipal User currentUser) {

        // Verify that user is authorized (is a member) for the participating institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, participantId);

        // Get the list of deals where the user's institution is a participant.
        List<DealData> deals = dealService.getDealDataListByParticipantUid(currentUser.getInstitution().getUid());

        return new ResponseEntity<>(deals, HttpStatus.OK);
    }

    @Operation(
        summary = "Retrieve a Document Batch",
        description = "Retrieve a Document Batch record for the specified document batch id.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successfully retrieved the document batch record.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentBatch.class))
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/data/documents/{batchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentBatch> getDocumentBatch(@PathVariable Long batchId, @AuthenticationPrincipal User currentUser) {

        // Perform the save for the batch and detail.
        DocumentBatch documentBatch = documentBatchService.getDocumentBatchForId(batchId);

        // Make sure the user has permissions to create this deal (i.e. they are authorized to the institution).
        authorizationService.authorizeUserForDealEventByDealExternalId(currentUser, documentBatch.getDealExternalId());

        return new ResponseEntity<>(documentBatch, HttpStatus.OK);
    }

    @Operation(
        summary = "Create a Document Batch",
        description = "Create Document records for the specified deal that will be loaded into or downloaded from Lamina via automated process.",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "Successfully created the document records",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentBatch.class))
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/data/documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentBatch> saveDocumentBatch(@Valid @RequestBody DocumentBatch documentBatch
            , BindingResult errors, @AuthenticationPrincipal User currentUser) {

        if ( errors.hasErrors() ) {
            throw new ValidationException(errors.getFieldErrors());
        }

        // Make sure the user has permissions to create this deal (i.e. they are authorized to the institution).
        DealEvent dealEvent = authorizationService.authorizeUserForDealEventByDealExternalId(currentUser, documentBatch.getDealExternalId());

        // Perform the save for the batch and detail.
        documentBatch = documentBatchService.save(documentBatch, currentUser);

        // Invoke the AWS File Service Lambda to start processing the documents.
        switch(documentBatch.getTransferType()) {
            case "U" -> awsService.loadDealDocuments(dealEvent.getOriginator().getUid(), documentBatch.getId());
            case "D" -> awsService.distributeDealDocuments(currentUser.getInstitution().getUid(), documentBatch.getId());
            default -> throw new IllegalStateException("Invalid transfer type");
        }

        // Create the location header.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", String.format("/api/ext/data/documents/%s", documentBatch.getId()));

        return new ResponseEntity<>(documentBatch, headers, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Start Processing a Document Batch",
        description = "This endpoint is called when starting to process a batch of documents.  The batch start date is assigned with the current date and time, if it hadn't already been set.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successfully set the document batch start date",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
            )
        },
        hidden = true
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping(value = "/data/documents/{batchId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_SERVICE')")
    public ResponseEntity<RestResponse> startProcessingDocumentBatch(@PathVariable Long batchId) {

        // Set the document batch start date to the current date time.
        documentBatchService.updateDocumentBatchProcessStartDate(batchId);

        return new ResponseEntity<>(new RestResponse("Document Batch", HttpStatus.OK.value(), Instant.now().toString()
                , "Document Batch processing was successfully started."), HttpStatus.OK);
    }

    @Operation(
        summary = "Complete Processing a Document Batch",
        description = "This endpoint is called when the processing of a batch of documents is complete.  The batch end date is assigned with the current date and time, if it hadn't already been set.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successfully set the document batch processing end date",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
            )
        },
        hidden = true
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping(value = "/data/documents/{batchId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_SERVICE')")
    public ResponseEntity<RestResponse> completeProcessingDocumentBatch(@PathVariable Long batchId) {

        // Set the document batch end date to the current date time.
        documentBatchService.updateDocumentBatchProcessEndDate(batchId);

        return new ResponseEntity<>(new RestResponse("Document Batch", HttpStatus.OK.value(), Instant.now().toString()
                , "Document Batch processing was successfully completed."), HttpStatus.OK);
    }

    @Operation(
        summary = "Start Processing a Batch File",
        description = "This endpoint is called when starting to process a file from a batch of documents.  The file start date is assigned with the current date and time, if it hadn't already been set.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successfully set the batch file start date",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
            )
        },
        hidden = true
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping(value = "/data/documents/{batchId}/details/{detailId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_SERVICE')")
    public ResponseEntity<RestResponse> startProcessingBatchFile(@PathVariable Long batchId, @PathVariable Long detailId) {

        // Set the batch file start date to the current date time.
        documentBatchService.updateDocumentBatchDetailProcessStartDate(batchId, detailId);

        return new ResponseEntity<>(new RestResponse("Document Batch Detail", HttpStatus.OK.value(), Instant.now().toString()
                , "Batch file processing was successfully started."), HttpStatus.OK);
    }

    @Operation(
        summary = "Complete Processing a Batch File",
        description = "This endpoint is called when the processing of a batch file is complete.  The batch file end date is assigned with the current date and time, if it hadn't already been set.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Successfully set the batch file processing end date",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
            )
        },
        hidden = true
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping(value = "/data/documents/{batchId}/details/{detailId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_SERVICE')")
    public ResponseEntity<RestResponse> completeProcessingBatchFile(@PathVariable Long batchId, @PathVariable Long detailId) {

        // Set the batch file end date to the current date time.
        documentBatchService.updateDocumentBatchDetailProcessEndDate(batchId, detailId);

        return new ResponseEntity<>(new RestResponse("Document Batch Detail", HttpStatus.OK.value(), Instant.now().toString()
                , "Batch file processing was successfully completed."), HttpStatus.OK);
    }

    @Operation(
        summary = "Get a document record for a Deal",
        description = "Get document record for the specified Deal by the Deal External ID.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Document was successfully received.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DealDocumentDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "The document could not be retrieved.")
        },
        hidden = true
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/deals/{dealExternalId}/documents/{documentExternalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_SERVICE')")
    public ResponseEntity<DealDocumentDto> getDocumentForDeal(@PathVariable("dealExternalId") String dealExternalId
            , @PathVariable("documentExternalId") String documentExternalId, @AuthenticationPrincipal User currentUser) {

        // Get the deal by the dealExternalId.
        DealDocument document = dealDocumentService.getDocumentForExternalId(documentExternalId);

        // Verify that the deal document belongs to this deal external id.
        if ( !dealExternalId.equals(document.getDeal().getDealExternalId()) ) {
            log.error(String.format("Document doesn't exist for deal external id. ( doc id = %s )", documentExternalId));
            throw new DataIntegrityException("Document doesn't exist for deal external id.");
        }

        // Convert the deal document to the integration document (DocumentDto).
        DealDocumentMapper documentMapper = new DealDocumentMapper(definitionService);
        DealDocumentDto documentDto = documentMapper.dealDocumentToDealDocumentDto(document);

        return new ResponseEntity<>(documentDto, HttpStatus.OK);
    }

    @Operation(
        summary = "Create a document record for a Deal",
        description = "Create document record for the specified Deal.  This endpoint was created for the file processing service.",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "Document record was successfully created.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DealDocumentDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "The document could not be created.")
        },
        hidden = true
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/deals/{dealExternalId}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('APP_SERVICE')")
    public ResponseEntity<DealDocumentDto> createDocumentForDeal(@PathVariable("dealExternalId") String dealExternalId
            , @RequestBody DealDocumentDto documentDto, @AuthenticationPrincipal User currentUser) {

        // Get the deal by the dealExternalId.
        Deal deal = dealService.getDealByExternalId(dealExternalId, currentUser);

        // Map the integration document fields/values to a DealDocument.
        DealDocumentMapper documentMapper = new DealDocumentMapper(definitionService);
        DealDocument document = documentMapper.dealDocumentDtoToDealDocument(documentDto);

        // Add necessary values for saving the deal.
        document.setDeal(deal);
        document.setSource(SYSTEM_INTEGRATION);

        // Save the document to the database.  Note: We will use the user from the DTO as the created by.
        document = dealDocumentService.save(document, document.getCreatedBy());

        return new ResponseEntity<>(documentDto, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Create a timeline event when a document batch is processed",
        description = "nCino file upload timeline event",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "Successfully created timeline event",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/data/{dealExternalId}/activity", produces = MediaType.APPLICATION_JSON_VALUE)
    //     @PreAuthorize("hasRole('APP_SERVICE')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestResponse> createFileUploadActivity(@PathVariable("dealExternalId") String dealExternalId, @AuthenticationPrincipal User currentUser,
                                                                 @RequestBody Map<String, Object> payload) {
        // Get the deal by the dealExternalId.
        Deal deal = dealService.getDealByExternalId(dealExternalId, currentUser);

        String documentExternalId = (String) payload.get("documentExternalId");
        DealDocument document = dealDocumentService.getDocumentForExternalId(documentExternalId);
        Map<String, Object> activityMap = Map.of("dealDocument", document);

        activityService.createActivity(ActivityTypeEnum.FILE_UPLOADED, deal.getId(), null,
                activityMap, document.getCreatedBy(), SYSTEM_INTEGRATION);

        return new ResponseEntity<>(new RestResponse("Timeline Activity", HttpStatus.OK.value(), Instant.now().toString()
                , "Timeline activity was successfully created."), HttpStatus.OK);
    }

    @Operation(
        summary = "Send a notification email when a document batch is processed",
        description = "nCino file upload notification email",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "Successfully sent email",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
            )
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/data/{dealExternalId}/notify", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestResponse> sendNotificationEmail(@PathVariable("dealExternalId") String dealExternalId, @AuthenticationPrincipal User currentUser) {

        // Get the deal by the dealExternalId.
        Deal deal = dealService.getDealByExternalId(dealExternalId, currentUser);
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        //TODO: templateData.put("isDealLaunched", deal.getStage().getOrder() >= 3);

        ObjectMapper objectMapper = new ObjectMapper();
        String templateDataJson = "";

        try {
            templateDataJson =  objectMapper.writeValueAsString(templateData);
        } catch ( JsonProcessingException e ) {
            throw new ActivityCreationException("There was an error creating template data Json");
        }

        EmailNotification emailNotification = new EmailNotification();
        emailNotification.setEmailTypeCd(Long.toString(FILE_UPLOADED.getId()));
        emailNotification.setDeal(deal);
        emailNotification.setTemplateDataJson(templateDataJson);

        emailService.save(emailNotification);

        return new ResponseEntity<>(new RestResponse("Email", HttpStatus.OK.value(), Instant.now().toString()
                , "Notification email was successfully sent."), HttpStatus.OK);

    }

}