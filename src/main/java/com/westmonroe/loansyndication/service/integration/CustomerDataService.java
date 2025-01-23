package com.westmonroe.loansyndication.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.dao.InstitutionDao;
import com.westmonroe.loansyndication.dao.integration.CustomerDataDao;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.InvalidDataException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.*;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import com.westmonroe.loansyndication.model.integration.*;
import com.westmonroe.loansyndication.model.integration.mapper.*;
import com.westmonroe.loansyndication.service.DefinitionService;
import com.westmonroe.loansyndication.service.PicklistService;
import com.westmonroe.loansyndication.service.deal.*;
import com.westmonroe.loansyndication.service.event.EventDealFacilityService;
import com.westmonroe.loansyndication.service.event.EventLeadFacilityService;
import com.westmonroe.loansyndication.service.event.EventParticipantFacilityService;
import com.westmonroe.loansyndication.service.event.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_INTEGRATION;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.EventTypeEnum.ORIGINATION;

@Service
@Slf4j
public class CustomerDataService {

    @Value("${lamina.base-url}")
    private String baseUrl;

    private final CustomerDataDao customerDataDao;
    private final InstitutionDao institutionDao;
    private final DealService dealService;
    private final EventService eventService;
    private final DealMemberService dealMemberService;
    private final DealFacilityService dealFacilityService;
    private final DealCovenantService dealCovenantService;
    private final DealDocumentService dealDocumentService;
    private final DocumentBatchService documentBatchService;
    private final PicklistService picklistService;
    private final DefinitionService definitionService;
    private final ObjectMapper objectMapper;
    private final EventParticipantFacilityService eventParticipantFacilityService;
    private final EventDealFacilityService eventDealFacilityService;
    private final EventLeadFacilityService eventLeadFacilityService;

    public CustomerDataService(CustomerDataDao customerDataDao, InstitutionDao institutionDao, DealService dealService
            , EventService eventService, DealMemberService dealMemberService, DealFacilityService dealFacilityService
            , DealCovenantService dealCovenantService, DealDocumentService dealDocumentService
            , DocumentBatchService documentBatchService, PicklistService picklistService, DefinitionService definitionService
            , ObjectMapper objectMapper, EventParticipantFacilityService eventParticipantFacilityService
            , EventDealFacilityService eventDealFacilityService, EventLeadFacilityService eventLeadFacilityService) {
        this.customerDataDao = customerDataDao;
        this.institutionDao = institutionDao;
        this.dealService = dealService;
        this.eventService = eventService;
        this.dealMemberService = dealMemberService;
        this.dealFacilityService = dealFacilityService;
        this.dealCovenantService = dealCovenantService;
        this.dealDocumentService = dealDocumentService;
        this.documentBatchService = documentBatchService;
        this.picklistService = picklistService;
        this.definitionService = definitionService;
        this.objectMapper = objectMapper;
        this.eventParticipantFacilityService = eventParticipantFacilityService;
        this.eventDealFacilityService = eventDealFacilityService;
        this.eventLeadFacilityService = eventLeadFacilityService;
    }

    public CustomerData getCustomerDataByDealExternalId(String dealExternalId, User currentUser) {

        CustomerData customerData = customerDataDao.findByDealExternalId(dealExternalId);

        /*
         *  Create a refreshed deal and applicant object from current deal data.
         */
        Deal deal = dealService.getDealByExternalId(customerData.getPayload().getMarketplaceData().getDeal().getDealExternalId(), currentUser);
        DealMapper dealMapper = new DealMapper(picklistService, definitionService);
        DealDto dealDto = dealMapper.dealToDealDto(deal);
        ApplicantDto applicantDto = dealMapper.dealToApplicantDto(deal);

        // Add the base url for the user.  Storing this at the Marketplace Data level.
        customerData.getPayload().getMarketplaceData().setBaseUrl(baseUrl.concat("/deals/").concat(deal.getUid()));

        /*
         *  Create a refreshed event from current event data.
         */
        Event event = eventService.getOpenEventForDealUid(deal.getUid());
        EventMapper eventMapper = new EventMapper(definitionService);
        EventDto eventDto = eventMapper.eventToEventDto(event);

        /*
         *  Create a refreshed facility collection from current facilities data.
         */
        List<DealFacility> facilities = dealFacilityService.getFacilitiesForDeal(deal.getUid());
        List<DealFacilityDto> facilityDtos = new ArrayList<>();

        if ( !facilities.isEmpty() ) {

            DealFacilityMapper dealFacilityMapper = new DealFacilityMapper(picklistService);

            for ( DealFacility dealFacility : facilities ) {
                facilityDtos.add(dealFacilityMapper.dealFacilityToDealFacilityDto(dealFacility));
            }

        }

        /*
         *  Create a refreshed covenant collection from current covenant data.
         */
        List<DealCovenant> covenants = dealCovenantService.getCovenantsForDeal(deal.getUid());
        List<DealCovenantDto> covenantDtos = new ArrayList<>();

        if ( !covenants.isEmpty() ) {

            DealCovenantMapper dealCovenantMapper = new DealCovenantMapper();

            for ( DealCovenant dealCovenant : covenants ) {
                covenantDtos.add(dealCovenantMapper.dealCovenantToDealCovenantDto(dealCovenant));
            }

        }

        /*
         *  Create a refreshed participant facility collection from current participant facility data.
         */
        List<EventParticipantFacility> participantFacilities;

        // Get the view based on the user being a lead or participant.  Lead sees all and participant only sees theirs.
        if ( deal.getRelation().equals(ORIGINATOR.getDescription()) ) {
            participantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventId(event.getId());
        } else {
            participantFacilities = eventParticipantFacilityService.getEventParticipantFacilitiesByEventAndParticipantId(
                event.getId(), currentUser.getInstitution().getId()
            );
        }

        List<EventParticipantFacilityDto> participantFacilityDtos = new ArrayList<>();

        if ( !participantFacilities.isEmpty() ) {

            EventParticipantFacilityMapper eventParticipantFacilityMapper = new EventParticipantFacilityMapper();

            for ( EventParticipantFacility participantFacility : participantFacilities ) {
                participantFacilityDtos.add(eventParticipantFacilityMapper.eventParticipantFacilityToEventParticipantFacilityDto(participantFacility));
            }

        }

        /*
         *  Create a refreshed document collection from current document data.
         */
        List<DealDocument> documents = dealDocumentService.getDocumentsForDeal(deal.getUid());
        List<DealDocumentDto> documentDtos = new ArrayList<>();

        if ( !documents.isEmpty() ) {

            DealDocumentMapper dealDocumentMapper = new DealDocumentMapper(definitionService);

            for ( DealDocument dealDocument : documents ) {
                documentDtos.add(dealDocumentMapper.dealDocumentToDealDocumentDto(dealDocument));
            }

        }

        // Requirement is to send originator name back with payload for participants.
        customerData.getPayload().getMarketplaceData().setDeal(dealDto);
        customerData.getPayload().getMarketplaceData().setEvent(eventDto);
        customerData.getPayload().getMarketplaceData().setApplicants(Arrays.asList(applicantDto));
        customerData.getPayload().getMarketplaceData().setFacilities(facilityDtos);
        customerData.getPayload().getMarketplaceData().setCovenants(covenantDtos);
        customerData.getPayload().getMarketplaceData().setParticipantFacilities(participantFacilityDtos);
        customerData.getPayload().getMarketplaceData().setDocuments(documentDtos);

        return customerData;
    }

    @Transactional
    public CustomerData save(CustomerData data, User currentUser) {

        DealDto dealDto = data.getPayload().getMarketplaceData().getDeal();
        Institution originator = institutionDao.findByUid(dealDto.getOriginatorId());
        List<ApplicantDto> applicantDtos = data.getPayload().getMarketplaceData().getApplicants();

        if ( applicantDtos.size() > 1 ) {
            log.error(String.format("There was more than one applicant for the deal (deal external id = %s)", dealDto.getDealExternalId()));
            throw new InvalidDataException("There was more than one applicant for the deal.");
        }

        /*
         *  Map the deal integration data to the marketplace deal object.
         */
        DealMapper dealMapper = new DealMapper(picklistService, definitionService);
        Deal deal = dealMapper.dealDtoToDeal(dealDto, originator, applicantDtos.get(0));

        deal = dealService.save(deal, currentUser, SYSTEM_INTEGRATION);

        // Update the originator name for the response.
        dealDto.setOriginatorName(deal.getOriginator().getName());

        // Add the base url for the response.  Storing this at the Marketplace Data level.
        data.getPayload().getMarketplaceData().setBaseUrl(baseUrl.concat("/deals/").concat(deal.getUid()));

        /*
         *  Map the event integration data to the marketplace event object.
         */
        EventDto eventDto = data.getPayload().getMarketplaceData().getEvent();
        EventMapper eventMapper = new EventMapper(definitionService);
        Event event = eventMapper.eventDtoToEvent(eventDto, deal);

        eventService.save(event, currentUser, SYSTEM_INTEGRATION);

        /*
         *  Deal was created, so we will add the current user (the deal creator) to the deal team by default.
         */
        DealMember dealMember = new DealMember();
        dealMember.setDeal(deal);
        dealMember.setUser(currentUser);
        dealMember.setMemberTypeCode(ORIGINATOR.getCode());
        dealMember.setMemberTypeDesc(ORIGINATOR.getDescription());
        dealMember.setCreatedBy(currentUser);
        dealMemberService.save(dealMember, currentUser, SYSTEM_INTEGRATION, false);

        /*
         *  Map the facility integration data to the marketplace deal facility objects.
         */
        List<DealFacilityDto> facilityDtos = data.getPayload().getMarketplaceData().getFacilities();

        if ( facilityDtos != null ) {

            DealFacilityMapper dealFacilityMapper = new DealFacilityMapper(picklistService);

            for ( DealFacilityDto dealFacilityDto : facilityDtos ) {

                DealFacility dealFacility = dealFacilityMapper.dealFacilityDtoToDealFacility(dealFacilityDto, deal);
                dealFacilityService.save(dealFacility, currentUser);

            }

        }

        /*
         *  Create Event Deal Facilities and Event Lead Facilities.  NOTE: These records are only automatically created
         *  when the event type is ORIGINATION.
         */
        if ( event.getEventType().getName().equals(ORIGINATION.getName()) ) {

            // For ORIGINATION events, we will create event deal facility for each deal facility.
            eventDealFacilityService.createEventDealFacilitiesForEvent(event, currentUser);

            // Create the Event Lead Facility records for Event.  NOTE: Will have to add trigger when adding event deal facilities in Lamina.
            eventLeadFacilityService.createEventLeadFacilitiesForEvent(event, currentUser);

        }

        /*
         *  Map the covenant integration data to the marketplace deal covenant objects.
         */
        List<DealCovenantDto> covenantDtos = data.getPayload().getMarketplaceData().getCovenants();

        if ( covenantDtos != null ) {

            DealCovenantMapper dealCovenantMapper = new DealCovenantMapper();

            for ( DealCovenantDto dealCovenantDto : covenantDtos ) {

                DealCovenant dealCovenant = dealCovenantMapper.dealCovenantDtoToDealCovenant(dealCovenantDto, deal);
                dealCovenantService.save(dealCovenant, currentUser);

            }

        }

        /*
         *  Map the document integration data to the marketplace document batch objects and create the document batch.
         */
        List<DealDocumentDto> documentDtos = data.getPayload().getMarketplaceData().getDocuments();
        DocumentBatch batch = null;

        if ( documentDtos != null && !documentDtos.isEmpty() ) {

            DealDocumentMapper dealDocumentMapper = new DealDocumentMapper(definitionService);
            List<DocumentBatchDetail> details = new ArrayList<>();

            for ( DealDocumentDto dealDocumentDto : documentDtos ) {

                DocumentBatchDetail detail = dealDocumentMapper.dealDocumentDtoToDocumentBatchDetail(dealDocumentDto);
                details.add(detail);

            }

            batch = new DocumentBatch();
            batch.setDealExternalId(deal.getDealExternalId());
            batch.setDetails(details);
            batch.setTransferType("U");

            batch = documentBatchService.save(batch, currentUser);

            // Update the batch id in the payload, as this will trigger the AWS File Service Lambda
            data.getPayload().setBatchId(batch.getId());
        }

        // Save the original data submitted.
        customerDataDao.save(data.getPayload(), currentUser);

        return data;
    }

    @Transactional
    public Long update(Map<String, Object> customerMap, User currentUser) {

        // The batchId determines whether the File Service Lambda will be invoked in the controller.
        Long batchId = null;

        // Get the payload map, which is the base level for our data.
        Map<String, Object> payloadMap = (Map) customerMap.get("payload");

        Map<String, Object> dealMap = (Map) ((Map) payloadMap.get("marketplaceData")).get("deal");
        Map<String, Object> eventMap = (Map) ((Map) payloadMap.get("marketplaceData")).get("event");
        List<Map<String, Object>> applicantMaps = (List) ((Map) payloadMap.get("marketplaceData")).getOrDefault("applicants", Arrays.asList(new HashMap<String, Object>()));
        List<Map<String, Object>> covenantMaps = (List) ((Map) payloadMap.get("marketplaceData")).get("covenants");
        List<Map<String, Object>> facilityMaps = (List) ((Map) payloadMap.get("marketplaceData")).get("facilities");
        List<Map<String, Object>> documentMaps = (List) ((Map) payloadMap.get("marketplaceData")).get("documents");

        if ( applicantMaps.size() > 1 ) {
            log.error(String.format("There was more than one applicant for the deal (uid = %s)", dealMap.get("uid")));
            throw new InvalidDataException("There was more than one applicant for the deal.");
        }

        // Update the deal map's flat values for picklist options to objects before updating.
        DealMapper dealMapper = new DealMapper(picklistService, definitionService);
        dealMap = dealMapper.dealDtoMapToDealMap(dealMap, applicantMaps.get(0));

        // Update the deal using the methods created for GraphQL.
        Deal deal = dealService.update(dealMap, currentUser, SYSTEM_INTEGRATION);

        // Add the originator name to payload response.
        dealMap.put("originatorName", deal.getOriginator().getName());

        // If the event was supplied then update the event.  NOTE: Need to add check that it's not closed and belongs to the deal.
        Event event = null;
        if ( eventMap != null ) {

            EventMapper eventMapper = new EventMapper(definitionService);
            eventMap = eventMapper.eventDtoMapToEventMap(eventMap);
            event = eventService.update(eventMap, currentUser, SYSTEM_INTEGRATION);

        }

        /*
         *  Update or create the marketplace deal facilities based on the supplied integration facility map.
         */
        if ( facilityMaps != null && !facilityMaps.isEmpty() ) {

            DealFacilityMapper dealFacilityMapper = new DealFacilityMapper(picklistService);
            DealFacility dealFacility;
            DealFacilityDto dealFacilityDto;

            for ( Map<String, Object> facilityMap : facilityMaps ) {

                try {

                    dealFacility = dealFacilityService.getFacilityForExternalId(facilityMap.get("facilityExternalId").toString());

                    // The deal facility exists.  Run the facility through the mapper and perform the update.
                    facilityMap = dealFacilityMapper.facilityDtoMapToFacilityMap(facilityMap, dealFacility.getId(), deal.getUid());
                    dealFacilityService.update(facilityMap, currentUser, SYSTEM_INTEGRATION);

                } catch ( DataNotFoundException e ) {

                    // Deal facility doesn't exist.  Convert the facility map to a DTO and perform the save.
                    dealFacilityDto = objectMapper.convertValue(facilityMap, DealFacilityDto.class);
                    dealFacility = dealFacilityMapper.dealFacilityDtoToDealFacility(dealFacilityDto, deal);
                    dealFacilityService.save(dealFacility, currentUser);

                    // If an event was supplied then create the event deal facility and event lead facility record.
                    if ( event != null ) {

                        // Create the event deal facility record.
                        EventDealFacility eventDealFacility = eventDealFacilityService.save(new EventDealFacility(event, dealFacility), currentUser);

                        // Create the event lead facility record.
                        eventLeadFacilityService.createEventLeadFacilitiesForEventAndDealFacility(event, eventDealFacility, currentUser);

                        //TODO: Add Event Participant Facility records.

                    }
                }

            }

        }

        /*
         *  Update or create the marketplace covenants based on the supplied integration covenant map.
         */
        if ( covenantMaps != null && !covenantMaps.isEmpty() ) {

            DealCovenantMapper dealCovenantMapper = new DealCovenantMapper();
            DealCovenant dealCovenant;
            DealCovenantDto dealCovenantDto;

            for ( Map<String, Object> covenantMap : covenantMaps ) {

                try {

                    dealCovenant = dealCovenantService.getCovenantForExternalId(covenantMap.get("covenantExternalId").toString());

                    // The deal covenant exists, so add the deal uid perform the update.
                    covenantMap.put("id", dealCovenant.getId());                            // Need the unique id for the update.
                    covenantMap.put("deal", Map.of("uid", deal.getUid()));                    // Need the deal uid for the update.
                    dealCovenantService.update(covenantMap, currentUser);

                } catch ( DataNotFoundException e ) {

                    // Deal covenant doesn't exist.  Convert the covenant map to a DTO and perform the save.
                    dealCovenantDto = objectMapper.convertValue(covenantMap, DealCovenantDto.class);
                    dealCovenant = dealCovenantMapper.dealCovenantDtoToDealCovenant(dealCovenantDto, deal);
                    dealCovenantService.save(dealCovenant, currentUser);

                }

            }

        }

        /*
         *  Update or create the marketplace documents based on the supplied integration document map.
         */
        if ( documentMaps != null && !documentMaps.isEmpty() ) {

            DealDocumentMapper dealDocumentMapper = new DealDocumentMapper(definitionService);
            List<DocumentBatchDetail> details = new ArrayList<>();
            DealDocumentDto dealDocumentDto;

            for ( Map<String, Object> documentMap : documentMaps ) {

                dealDocumentDto = objectMapper.convertValue(documentMap, DealDocumentDto.class);
                DocumentBatchDetail detail = dealDocumentMapper.dealDocumentDtoToDocumentBatchDetail(dealDocumentDto);
                details.add(detail);

            }

            DocumentBatch batch = new DocumentBatch();
            batch.setDealExternalId(deal.getDealExternalId());
            batch.setDetails(details);
            batch.setTransferType("U");

            batch = documentBatchService.save(batch, currentUser);

            // Set the batchId, which will cause the AWS File Service Lambda to be invoked.
            batchId = batch.getId();
        }

        return batchId;
    }

    public int deleteForOriginatorId(Long originatorId) {
        return customerDataDao.deleteByOriginatorId(originatorId);
    }

    @Transactional
    public void delete(Map<String, Object> customerMap) {

        // Get the payload map, which is the base level for our data.
        Map<String, Object> payloadMap = (Map) customerMap.get("payload");

        List<Map<String, Object>> covenantMaps = (List) ((Map) payloadMap.get("marketplaceData")).get("covenants");
        List<Map<String, Object>> facilityMaps = (List) ((Map) payloadMap.get("marketplaceData")).get("facilities");

        if ( covenantMaps != null && !covenantMaps.isEmpty() ) {

            for ( Map<String, Object> covenantMap : covenantMaps ) {
                // The deal covenant exists, so delete it.
                dealCovenantService.deleteByExternalId(covenantMap.get("covenantExternalId").toString());
            }

        }

        if ( facilityMaps != null && !facilityMaps.isEmpty() ) {

            for ( Map<String, Object> facilityMap : facilityMaps ) {

                // Delete event lead facility records.
                eventLeadFacilityService.deleteAllByFacilityExternalId(facilityMap.get("facilityExternalId").toString());

                // Delete event deal facility records.
                eventDealFacilityService.deleteByFacilityExternalId(facilityMap.get("facilityExternalId").toString());

                // The deal facility exists, so delete it.
                dealFacilityService.deleteByExternalId(facilityMap.get("facilityExternalId").toString());

            }

        }

    }

}