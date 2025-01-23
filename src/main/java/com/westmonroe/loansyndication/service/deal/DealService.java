package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.dao.InstitutionDao;
import com.westmonroe.loansyndication.dao.StageDao;
import com.westmonroe.loansyndication.dao.deal.*;
import com.westmonroe.loansyndication.dao.event.EventDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantDao;
import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.deal.DealEventSummary;
import com.westmonroe.loansyndication.model.deal.DealSummary;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.model.integration.DealData;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.service.event.EventService;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.DEAL_CREATED;
import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.DEAL_INFO_UPDATED;

@Service
@Slf4j
public class DealService {

    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private final EventDao eventDao;
    private final EventParticipantDao eventParticipantDao;
    private final DealDao dealDao;
    private final DealMemberDao dealMemberDao;
    private final DealCovenantDao dealCovenantDao;
    private final DealFacilityDao dealFacilityDao;
    private final DealDocumentDao dealDocumentDao;
    private final InstitutionDao institutionDao;
    private final StageDao stageDao;
    private final ActivityService activityService;
    private final EmailService emailService;
    private final EventService eventService;

    public DealService(DealDao dealDao, DealMemberDao dealMemberDao, DealCovenantDao dealCovenantDao
            , DealFacilityDao dealFacilityDao, DealDocumentDao dealDocumentDao, InstitutionDao institutionDao
            , StageDao stageDao, ActivityService activityService, EmailService emailService, EventService eventService
            , EventDao eventDao, EventParticipantDao eventParticipantDao) {
        this.dealDao = dealDao;
        this.dealMemberDao = dealMemberDao;
        this.dealCovenantDao = dealCovenantDao;
        this.dealFacilityDao = dealFacilityDao;
        this.dealDocumentDao = dealDocumentDao;
        this.institutionDao = institutionDao;
        this.stageDao = stageDao;
        this.activityService = activityService;
        this.emailService = emailService;
        this.eventService = eventService;
        this.eventDao = eventDao;
        this.eventParticipantDao = eventParticipantDao;
    }

    public List<Deal> getDealsByUser(User currentUser) {
        return dealDao.findAllByUser(currentUser);
    }

    public List<DealSummary> getSummaryByUser(User currentUser) {
        return dealDao.findSummaryByInstitutionId(currentUser.getInstitution().getId(), currentUser.getId());
    }

    public List<DealEventSummary> getEventSummaryByUser(User currentUser) {
        return dealDao.findEventSummaryByInstitutionId(currentUser.getInstitution().getId(), currentUser.getId());
    }

    public List<Deal> getAllDealsByInstitutionId(Long institutionId, User currentUser) {
        return dealDao.findAllByOriginatorId(institutionId, currentUser);
    }

    public List<Deal> getAllDealsByInstitutionUid(String institutionUid, User currentUser) {
        return dealDao.findAllByOriginatorUid(institutionUid, currentUser);
    }

    public List<DealData> getDealDataListByParticipantUid(String participantUid) {
        return dealDao.findAllByParticipantUid(participantUid);
    }

    public Deal getDealById(Long id, User currentUser) {
        return dealDao.findById(id, currentUser);
    }

    public Deal getDealByUid(String uid, User currentUser) {
        return dealDao.findByUid(uid, currentUser);
    }

    public DealEvent getDealEventByUid(String uid, User currentUser) {

        DealEvent dealEvent = dealDao.findDealEventByUid(uid, currentUser);

        // Get the open event.  If none are open then it will remain null.
        List<Event> events = eventDao.findAllByDealUid(uid);
        Event event = events.get(0);
        dealEvent.setEvent(event);

        // If there was an event then get the event participant for the current user's institution.
        if ( dealEvent.getOrigInstUserFlag().equals("N") && event != null ) {

            try {
                EventParticipant eventParticipant = eventParticipantDao.findByEventUidAndParticipantUid(event.getUid(), currentUser.getInstitution().getUid());
                dealEvent.setEventParticipant(eventParticipant);
            } catch ( Exception e ) {
                // Do nothing ... event participant will remain null.
            }

        }

        return dealEvent;
    }

    public Deal getDealByExternalId(String externalId, User currentUser) {
        return dealDao.findByExternalId(externalId, currentUser);
    }

    public Long getDealIdByDealUid(String dealUid) {
        return dealDao.findIdByUid(dealUid);
    }

    /**
     * This method saves the Deal.  The Institution for the supplied uid is retrieved and added to the deal object.  The
     * institution is required because a Deal must be associated with an Institution.
     *
     * @param institutionUid
     * @param deal
     *
     * @return The saved deal object.
     */
    public Deal save(String institutionUid, Deal deal, User currentUser, String source) {

        // Get the institution object and assign to deal.
        Institution institution = institutionDao.findByUid(institutionUid);
        deal.setOriginator(institution);

        return save(deal, currentUser, source);
    }

    /**
     * This method saves the Deal and assumes that the user added the originator (institution) object to the deal
     * object.  The originator is required because a Deal must be associated with an originator (institution).
     *
     * @param deal
     *
     * @return The saved deal object.
     */
    public Deal save(Deal deal, User currentUser, String source) {
        /*
         *  Verify that we have everything necessary before saving the deal.
         */
        if ( deal == null ) {

            log.error("save(): Cannot save a null Deal.");
            throw new ValidationException("Cannot save a null Deal.");

        } else if ( deal.getInitialLenderFlag().equals("Y") && ( deal.getInitialLender() == null || deal.getInitialLender().getId() == null ) ) {

            log.error("save(): The initial lender flag was \"Y\" and the initial lender is null.");
            throw new ValidationException("The initial lender flag was \"Y\" but the initial lender id was not supplied.");

        } else if ( deal.getOriginator() == null || deal.getOriginator().getUid() == null ) {

            log.error("save(): The originator or originator uid is null.");
            throw new ValidationException("The originator or originator id was not supplied.");

        }

        //TODO: Remove this code when integrations are complete.
        // If the deal external id is null then add it.
        if ( deal.getDealExternalId() == null ) {
            deal.setDealExternalId(UUID.randomUUID().toString());
        }

        //TODO: Remove this code when integrations are complete.
        // If the applicant external id is null then add it.
        if ( deal.getApplicantExternalId() == null ) {
            deal.setApplicantExternalId(UUID.randomUUID().toString());
        }

        // Add the created by user to the deal.
        deal.setCreatedBy(currentUser);

        // We allow the initial lender to be blank if the initial lender flag is "N".
        if ( deal.getInitialLenderFlag().equals("N") ) {
            deal.setInitialLender(new InitialLender());
        }

        // Get the originator (institution) for this deal and set the id in the deal.
        Institution originator = institutionDao.findByUid(deal.getOriginator().getUid());
        deal.getOriginator().setId(originator.getId());

        // Generate a random UUID for the new deal and save it.
        deal.setUid(UUID.randomUUID().toString());

        try {
            dealDao.save(deal);
        } catch ( DuplicateKeyException dke ) {

            log.error(dke.getMessage());

            String fieldName = "Deal Uid";

            if ( dke.getMessage().contains("deal_external_uuid") ) {
                fieldName = "External Id";
            } else if ( dke.getMessage().contains("deal_name") ) {
                fieldName = "Deal Name";
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
        templateData.put("from", sendAddress);
        templateData.put("createdByInstitutionUid", currentUser.getInstitution().getUid());
        templateData.put("createdByInstitution", currentUser.getInstitution().getName());

        emailService.sendEmail(EmailTypeEnum.DEAL_CREATED, deal, templateData);


        return dealDao.findById(deal.getId(), currentUser);
    }

    public Deal update(Deal deal, User currentUser) {

        // Add the updated by user to the deal.
        deal.setUpdatedBy(currentUser);

        // Update the deal.
        dealDao.update(deal);

        return dealDao.findByUid(deal.getUid(), currentUser);
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * deal fields that were sent.
     *
     * @param  dealMap
     * @return deal
     */
    public Deal update(Map<String, Object> dealMap, User currentUser, String systemSource) {

        if ( !(dealMap.containsKey("uid") || dealMap.containsKey("dealExternalId")) ) {
            throw new MissingDataException("The deal must contain the uid or externalDealId for an update.");
        }

        /*
         *  Get the deal by the uid or dealExternalId.
         */
        Deal deal;
        if ( dealMap.containsKey("uid") ) {
            deal = dealDao.findByUid(dealMap.get("uid").toString(), currentUser);
        } else {
            deal = dealDao.findByExternalId(dealMap.get("dealExternalId").toString(), currentUser);
        }

        // Add the "old" deal to the activity map before any of the deal information changes.  This is our snapshot in time.
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("dealMap", dealMap);
        activityMap.put("oldDeal", SerializationUtils.clone(deal));

        // Add the updated by user to the deal.
        deal.setUpdatedBy(currentUser);

        /*
         * Check the fields in the map and update the deal object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( dealMap.containsKey("name") ) {
            deal.setName((String) dealMap.get("name"));
        }

        if ( dealMap.containsKey("dealIndustry") && ((Map) dealMap.get("dealIndustry")).containsKey("id") ) {
            deal.getDealIndustry().setId(Long.valueOf(((Map) dealMap.get("dealIndustry")).get("id").toString()));
        }

        if ( dealMap.containsKey("initialLenderFlag") ) {
            deal.setInitialLenderFlag((String) dealMap.get("initialLenderFlag"));
        }

        if ( dealMap.containsKey("initialLender") ) {
            if ( dealMap.get("initialLender") == null || ((Map) dealMap.get("initialLender")).get("id") == null ) {
                deal.setInitialLender(null);
            } else {
                deal.setInitialLender(new InitialLender(Long.valueOf(((Map) dealMap.get("initialLender")).get("id").toString()), null, null, null, null));
            }
        }

        if ( dealMap.containsKey("dealStructure") && ((Map) dealMap.get("dealStructure")).containsKey("id") ) {
            deal.getDealStructure().setId(Long.valueOf(((Map) dealMap.get("dealStructure")).get("id").toString()));
        }

        if ( dealMap.containsKey("dealType") ) {
            deal.setDealType((String) dealMap.get("dealType"));
        }

        if ( dealMap.containsKey("description") ) {
            deal.setDescription((String) dealMap.get("description"));
        }

        if ( dealMap.containsKey("dealAmount") ) {
            deal.setDealAmount(BigDecimal.valueOf(Double.valueOf(dealMap.get("dealAmount").toString())));
        }

        if ( dealMap.containsKey("borrowerDesc") ) {
            deal.setBorrowerDesc((String) dealMap.get("borrowerDesc"));
        }

        if ( dealMap.containsKey("borrowerName") ) {
            deal.setBorrowerName((String) dealMap.get("borrowerName"));
        }

        if ( dealMap.containsKey("borrowerCityName") ) {
            deal.setBorrowerCityName((String) dealMap.get("borrowerCityName"));
        }

        if ( dealMap.containsKey("borrowerStateCode") ) {
            deal.setBorrowerStateCode((String) dealMap.get("borrowerStateCode"));
        }

        if ( dealMap.containsKey("borrowerCountyName") ) {
            deal.setBorrowerCountyName((String) dealMap.get("borrowerCountyName"));
        }

        if ( dealMap.containsKey("farmCreditElig") ) {
            if ( dealMap.get("farmCreditElig") == null || ((Map) dealMap.get("farmCreditElig")).get("id") == null ) {
                deal.setFarmCreditElig(null);
            } else {
                deal.setFarmCreditElig(new PicklistItem(Long.valueOf(((Map) dealMap.get("farmCreditElig")).get("id").toString())));
            }
        }

        if ( dealMap.containsKey("taxId") ) {
            deal.setTaxId((String) dealMap.get("taxId"));
        }

        if ( dealMap.containsKey("borrowerIndustry") ) {
            if ( dealMap.get("borrowerIndustry") == null || ((Map) dealMap.get("borrowerIndustry")).get("code") == null ) {
                deal.setBorrowerIndustry(null);
            } else {
                deal.setBorrowerIndustry(new NaicsCode(((Map) dealMap.get("borrowerIndustry")).get("code").toString(), null));
            }
        }

        if ( dealMap.containsKey("businessAge") ) {
            if ( dealMap.get("businessAge") == null ) {
                deal.setBusinessAge(null);
            } else {
                deal.setBusinessAge(Integer.valueOf(dealMap.get("businessAge").toString()));
            }
        }
        /*
            Financial Metrics
         */
        if ( dealMap.containsKey("defaultProbability") ) {
            if ( dealMap.get("defaultProbability") == null ) {
                deal.setDefaultProbability(null);
            } else {
                deal.setDefaultProbability(Integer.valueOf(dealMap.get("defaultProbability").toString()));
            }
        }

        if ( dealMap.containsKey("currYearEbita") ) {
            if ( dealMap.get("currYearEbita") == null ) {
                deal.setCurrYearEbita(null);
            } else {
                deal.setCurrYearEbita(BigDecimal.valueOf(Double.valueOf(dealMap.get("currYearEbita").toString())));
            }
        }

        if ( dealMap.containsKey("active") ) {
            deal.setActive((String) dealMap.get("active"));
        }

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("institutionUid", currentUser.getInstitution().getUid());
        templateData.put("institutionName", currentUser.getInstitution().getName());
        templateData.put("category", "Financial Metrics");
        //TODO: templateData.put("isDealLaunched", deal.getStage().getOrder() >= 3);
        templateData.put("dealMap", dealMap);

        emailService.sendEmail(EmailTypeEnum.DEAL_INFO_UPDATED, deal, templateData);

        // Update the deal.
        dealDao.update(deal);

        Deal updatedDeal = dealDao.findById(deal.getId(), currentUser);

        /*
         *  Record the activity in the timeline.
         */
        activityMap.put("newDeal", updatedDeal);        // Add the resulting deal from the update.
        activityService.createActivity(DEAL_INFO_UPDATED, deal.getId(), null, activityMap, currentUser, systemSource);


        return updatedDeal;
    }

    @Transactional
    public int deleteById(Long id) {

        /*
         *  Delete all of the events and event data for this deal.
         */
        List<Event> events = eventService.getEventsByDealId(id);

        for ( Event event : events ) {
            eventService.deleteById(event.getId());
        }

        // Delete all deal members.
        dealMemberDao.deleteAllByDealId(id);

        // Delete all deal covenants.
        dealCovenantDao.deleteAllByDealId(id);

        // Delete all deal facilities.
        dealFacilityDao.deleteAllByDealId(id);

        // Delete all deal documents.
        dealDocumentDao.deleteAllByDealId(id);

        // Delete all activities associated with deal.
        activityService.deleteActivitiesByDealId(id);

        // Delete the deal.
        return dealDao.deleteById(id);
    }

    @Transactional
    public int deleteByUid(String uid) {
        Long dealId = getDealIdByDealUid(uid);
        return deleteById(dealId);
    }

    @Transactional
    public void deleteAllByInstitutionId(Long id) {

        // Delete all of the event related records for the institution.
        eventService.deleteByInstitutionId(id);

        // Delete all deal members where this institution was a deal originator or user's institution.
        dealMemberDao.deleteAllByInstitutionId(id);

        // Delete all deal covenants where this institution is the deal originator.
        dealCovenantDao.deleteAllByDealOriginatorId(id);

        // Delete all deal facilities where this institution is the deal originator.
        dealFacilityDao.deleteAllByDealOriginatorId(id);

        // Delete all deal documents where this institution is the deal originator.
        dealDocumentDao.deleteAllByInstitutionId(id);

        // Delete all deal activities where this institution is the deal originator.
        activityService.deleteActivitiesByInstitutionId(id);

        // Delete all of the deals where the specified institution is the originator.
        dealDao.deleteAllByOriginatorId(id);
    }

}