package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.dao.deal.DealDao;
import com.westmonroe.loansyndication.dao.deal.DealFacilityDao;
import com.westmonroe.loansyndication.exception.InvalidDataException;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.DEAL_INFO_UPDATED;
import static com.westmonroe.loansyndication.utils.DealFacilityTypeEnum.*;

@Service
@Slf4j
public class DealFacilityService {
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private final DealFacilityDao dealFacilityDao;
    private final DealDao dealDao;
    private final ActivityService activityService;
    private final EmailService emailService;
    private final Validator validator;

    public DealFacilityService(DealFacilityDao dealFacilityDao, DealDao dealDao, ActivityService activityService, EmailService emailService, Validator validator) {
        this.dealFacilityDao = dealFacilityDao;
        this.dealDao = dealDao;
        this.activityService = activityService;
        this.emailService = emailService;
        this.validator = validator;
    }

    private void validateDealFacility(DealFacility facility) {

        if (facility.getAmortization() != null && facility.getFacilityType().getId() == REVOLVER.getId()) {
            log.error("save(): Amortization must be null for this facility type");
            throw new ValidationException("Amortization must be null for this facility type");
        }

        // Non-null revolver util is only valid for these three facility types
        if (facility.getRevolverUtil() != null && facility.getFacilityType().getId() != REVOLVER.getId()
                && facility.getFacilityType().getId() != REVOLVING_TERM_LOAN.getId()
                && facility.getFacilityType().getId() != DELAYED_DRAW_TERM_LOAN.getId()
        ){
            log.error("save(): Revolver Util must be null for this facility type");
            throw new ValidationException("Revolver Util must be null for this facility type");
        }

        if (facility.getRenewalDate() != null && facility.getFacilityType().getId() != REVOLVER.getId()
                && facility.getFacilityType().getId() != REVOLVING_TERM_LOAN.getId()
                && facility.getFacilityType().getId() != DELAYED_DRAW_TERM_LOAN.getId()
        ){
            log.error("save(): Renewal Date must be null for this facility type");
            throw new ValidationException("Renewal Date must be null for this facility type");
        }
    }

    public DealFacility getFacilityForId(Long facilityId) {
        return dealFacilityDao.findById(facilityId);
    }

    public DealFacility getFacilityForExternalId(String externalId) {
        return dealFacilityDao.findByExternalId(externalId);
    }

    public List<DealFacility> getFacilitiesForDeal(String dealUid) {
        return dealFacilityDao.findAllByDealUid(dealUid);
    }

    public List<DealFacility> getFacilitiesForEvent(String eventUid) {
        return dealFacilityDao.findAllByEventUid(eventUid);
    }

    public DealFacility save(DealFacility facility, User currentUser) {

        //TODO: This should be removed after integrations are complete.
        // If the facility external id is empty then set it.
        if ( facility.getFacilityExternalId() == null ) {
            facility.setFacilityExternalId(UUID.randomUUID().toString());
        }

        validateDealFacility(facility);

        // Get full deal object for this facility.
        Deal deal = dealDao.findByUid(facility.getDeal().getUid(), currentUser);

        // Set the deal object on the facility, as it has the needed unique id.
        facility.setDeal(deal);

        // Set the facility name and the created by user to the deal.
        facility.setFacilityName("Facility " + (char)(deal.getLastFacilityNumber() + 65));
        facility.setCreatedBy(currentUser);

        // Save the facility.  Facility will be returned with the unique id.
        dealFacilityDao.save(facility);

        // Increment the last facility number on the deal.
        dealDao.updateLastFacilityNumber(deal.getId());

        return dealFacilityDao.findById(facility.getId());
    }

    public DealFacility update(DealFacility facility, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.

        validateDealFacility(facility);
        // Set the current user to the updating user.
        facility.setUpdatedBy(currentUser);

        // NOTE: This only updates the status of the deal participant.
        dealFacilityDao.update(facility);

        // Return the full deal participant object.
        return dealFacilityDao.findById(facility.getId());
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * deal facility fields that were sent.
     *
     * @param  facilityMap
     * @return dealFacility
     */
    public DealFacility update(Map<String, Object> facilityMap, User currentUser, String systemSource) {

        //TODO: Verify that user has update permissions for this deal.

        if ( !( facilityMap.containsKey("deal") && ((Map) facilityMap.get("deal")).containsKey("uid") ) ) {
            throw new MissingDataException("The deal facility must contain the deal uid for an update.");
        }

        if ( !facilityMap.containsKey("id") ) {
            throw new MissingDataException("The deal facility must contain the unique id for an update.");
        }

        // Get the deal facility by the unique id.
        DealFacility dealFacility = dealFacilityDao.findById(Long.valueOf(facilityMap.get("id").toString()));

        // Verify that the facility belongs to the deal.
        if ( !dealFacility.getDeal().getUid().equals(((Map) facilityMap.get("deal")).get("uid").toString()) ) {
            log.error("Facility id does not belong to the supplied deal.");
            throw new InvalidDataException("The facility does not belong to the deal.");
        }

        // Add the "old" deal facility to the activity map before any of the deal facility fields change.
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("dealFacilityMap", facilityMap);
        activityMap.put("oldDealFacility", SerializationUtils.clone(dealFacility));

        /*
         * Check the fields in the map and update the deal facility object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( facilityMap.containsKey("facilityAmount") ) {
            dealFacility.setFacilityAmount(BigDecimal.valueOf(Double.valueOf(facilityMap.get("facilityAmount").toString())));
        }

        if ( facilityMap.containsKey("facilityType") ) {
            if ( facilityMap.get("facilityType") == null || ((Map) facilityMap.get("facilityType")).get("id") == null ) {
                dealFacility.setFacilityType(null);
            } else {
                dealFacility.setFacilityType(new PicklistItem(Long.valueOf(((Map) facilityMap.get("facilityType")).get("id").toString())));
            }
        }

        if ( facilityMap.containsKey("tenor") ) {
            if ( facilityMap.get("tenor") == null ) {
                dealFacility.setTenor(null);
            } else {
                dealFacility.setTenor(Integer.valueOf(facilityMap.get("tenor").toString()));
            }
        }

        if ( facilityMap.containsKey("pricing") ) {
            dealFacility.setPricing((String) facilityMap.get("pricing"));
        }

        if ( facilityMap.containsKey("creditSpreadAdj") ) {
            dealFacility.setCreditSpreadAdj((String) facilityMap.get("creditSpreadAdj"));
        }

        if ( facilityMap.containsKey("facilityPurpose") ) {
            if ( facilityMap.get("facilityPurpose") == null || ((Map) facilityMap.get("facilityPurpose")).get("id") == null ) {
                dealFacility.setFacilityPurpose(null);
            } else {
                dealFacility.setFacilityPurpose(new PicklistItem(Long.valueOf(((Map) facilityMap.get("facilityPurpose")).get("id").toString())));
            }
        }
        
        if ( facilityMap.containsKey("collateral") ) {
            if ( ((Map) facilityMap.get("collateral")).get("id") == null ) {
                dealFacility.setCollateral(null);
            } else {
                dealFacility.setCollateral(new PicklistItem(Long.valueOf(((Map) facilityMap.get("collateral")).get("id").toString())));
            }
        }

        if ( facilityMap.containsKey("purposeDetail") ) {
            dealFacility.setPurposeDetail((String) facilityMap.get("purposeDetail"));
        }

        if ( facilityMap.containsKey("dayCount") ) {
            if ( ((Map) facilityMap.get("dayCount")).get("id") == null ) {
                dealFacility.setDayCount(null);
            } else {
                dealFacility.setDayCount(new PicklistItem(Long.valueOf(((Map) facilityMap.get("dayCount")).get("id").toString())));
            }
        }

        if ( facilityMap.containsKey("regulatoryLoanType") ) {
            if ( ((Map) facilityMap.get("regulatoryLoanType")).get("id") == null ) {
                dealFacility.setRegulatoryLoanType(null);
            } else {
                dealFacility.setRegulatoryLoanType(new PicklistItem(Long.valueOf(((Map) facilityMap.get("regulatoryLoanType")).get("id").toString())));
            }
        }

        if ( facilityMap.containsKey("guarInvFlag") ) {
            dealFacility.setGuarInvFlag((String) facilityMap.get("guarInvFlag"));
        }

        if ( facilityMap.containsKey("patronagePayingFlag") ) {
            dealFacility.setPatronagePayingFlag((String) facilityMap.get("patronagePayingFlag"));
        }

        if ( facilityMap.containsKey("farmCreditType") ) {
            dealFacility.setFarmCreditType((String) facilityMap.get("farmCreditType"));
        }

        if ( facilityMap.containsKey("revolverUtil") ) {
            if ( facilityMap.get("revolverUtil") == null ) {
                dealFacility.setRevolverUtil(null);
            } else {
                dealFacility.setRevolverUtil(Integer.valueOf(facilityMap.get("revolverUtil").toString()));
            }
        }

        if ( facilityMap.containsKey("upfrontFees") ) {
            dealFacility.setUpfrontFees((String) facilityMap.get("upfrontFees"));
        }

        if ( facilityMap.containsKey("unusedFees") ) {
            dealFacility.setUnusedFees((String) facilityMap.get("unusedFees"));
        }

        if ( facilityMap.containsKey("amortization") ) {
            dealFacility.setAmortization((String) facilityMap.get("amortization"));
        }

        if ( facilityMap.containsKey("maturityDate") ) {
            if ( facilityMap.get("maturityDate") == null ) {
                dealFacility.setMaturityDate(null);
            } else {
                dealFacility.setMaturityDate(LocalDate.parse(facilityMap.get("maturityDate").toString()));
            }
        }

        if ( facilityMap.containsKey("renewalDate") ) {
            if ( facilityMap.get("renewalDate") == null ) {
                dealFacility.setRenewalDate(null);
            } else {
                dealFacility.setRenewalDate(LocalDate.parse(facilityMap.get("renewalDate").toString()));
            }
        }

        if ( facilityMap.containsKey("lgdOption") ) {
            dealFacility.setLgdOption((String) facilityMap.get("lgdOption"));
        }

        // Add the updated by user to the deal.
        dealFacility.setUpdatedBy(currentUser);

        // Validate the deal facility that was just assembled
        Set<ConstraintViolation<DealFacility>> violations = validator.validate(dealFacility);
        if ( !violations.isEmpty() ) {
            throw new ConstraintViolationException(violations);
        }

        // Update the deal facility.
        dealFacilityDao.update(dealFacility);

        DealFacility updatedDealFacility = dealFacilityDao.findById(dealFacility.getId());

        /*
         *  Record the activity in the timeline
         */
        activityMap.put("newDealFacility", updatedDealFacility);
        activityService.createActivity(DEAL_INFO_UPDATED, dealFacility.getDeal().getId(), null, activityMap, currentUser, systemSource);

        Deal deal = dealDao.findById(dealFacility.getDeal().getId(), currentUser); // temporary workaround to get the full deal object...
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", deal.getName());
        templateData.put("dealUid", deal.getUid());
        templateData.put("from", sendAddress);
        templateData.put("institutionUid", currentUser.getInstitution().getUid());
        templateData.put("institutionName", currentUser.getInstitution().getName());
        templateData.put("category", "Facility");
        //TODO: templateData.put("isDealLaunched", deal.getStage().getOrder() >= 3);
        templateData.put("facilityMap", facilityMap);

        emailService.sendEmail(EmailTypeEnum.DEAL_INFO_UPDATED, deal, templateData);
        // Return the full deal facility object.
        return updatedDealFacility;
    }

    public int deleteById(Long id) {
        //TODO: Verify that user has update permissions for this deal.
        return dealFacilityDao.deleteById(id);
    }

    public int deleteByExternalId(String facilityExternalId) {
        //TODO: Verify that user has update permissions for this deal.
        return dealFacilityDao.deleteByExternalId(facilityExternalId);
    }

    public int deleteAllByDealUid(String dealUid) {
        return dealFacilityDao.deleteAllByDealUid(dealUid);
    }

}