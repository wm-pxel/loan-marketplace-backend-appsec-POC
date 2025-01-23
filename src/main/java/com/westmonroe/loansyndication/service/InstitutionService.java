package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.ConfidentialityAgreementDao;
import com.westmonroe.loansyndication.dao.InstitutionDao;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.ConfidentialityAgreement;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ProviderData;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.service.integration.CustomerDataService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class InstitutionService {

    private final InstitutionDao institutionDao;
    private final DealService dealService;
    private final CustomerDataService customerDataService;
    private final AwsService awsService;
    private final UserService userService;
    private final ConfidentialityAgreementDao confidentialityAgreementDao;
    private final Validator validator;

    public InstitutionService(InstitutionDao institutionDao, DealService dealService, CustomerDataService customerDataService
            , UserService userService, ConfidentialityAgreementDao confidentialityAgreementDao, AwsService awsService, Validator validator) {
        this.institutionDao = institutionDao;
        this.dealService = dealService;
        this.customerDataService = customerDataService;
        this.userService = userService;
        this.confidentialityAgreementDao = confidentialityAgreementDao;
        this.awsService = awsService;
        this.validator = validator;
    }

    public List<Institution> getAllInstitutions() {
        return institutionDao.findAll();
    }

    public List<Institution> getParticipantsNotOnDeal(String dealUid) {
        return institutionDao.findAllNotOnDeal(dealUid);
    }

    public List<Institution> getEventParticipantsNotOnDeal(String dealUid) {
        return institutionDao.findEventParticipantsNotOnDeal(dealUid);
    }

    public Institution getInstitutionById(Long id) {
        return institutionDao.findById(id);
    }

    public Institution getInstitutionByUid(String uid) {
        return institutionDao.findByUid(uid);
    }

    public Institution getInstitutionByName(String name) {
        return institutionDao.findByName(name);
    }

    public Institution getInstitutionByDealExternalId(String dealExternalId) {
        return institutionDao.findByDealExternalId(dealExternalId);
    }

    public ConfidentialityAgreement getConfidentialityAgreementByInstitutionId(Long id ) {
        return confidentialityAgreementDao.findConfidentialityAgreementByInstitutionyId(id);
    }

    public ConfidentialityAgreement createConfidentialityAgreement(String institutionUid, String description, Long userId) {
        Institution institution = institutionDao.findByUid(institutionUid);

        ConfidentialityAgreement confidentialityAgreement = new ConfidentialityAgreement();
        confidentialityAgreement.setInstitutionId(institution.getId());
        confidentialityAgreement.setDescription(description);

        return confidentialityAgreementDao.saveConfidentialityAgreement(confidentialityAgreement, userId);
    }

    public Institution save(Institution institution) {

        // Create random UUID for new institutions.
        institution.setUid(UUID.randomUUID().toString());

        // Save the institution.
        institution = institutionDao.save(institution);

        return institutionDao.findById(institution.getId());
    }

    public Institution update(Institution institution) {

        institutionDao.update(institution);
        return institutionDao.findById(institution.getId());
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * institution fields that were sent.
     *
     * @param  institutionMap
     * @return institution
     */
    public Institution update(Map<String, Object> institutionMap) {

        if ( !institutionMap.containsKey("uid") ) {
            throw new MissingDataException("The institution must contain the uid for an update.");
        }

        // Get the institution by the uid.
        Institution institution = institutionDao.findByUid((String) institutionMap.get("uid"));

        // Create collection for list of violations.
        Set<ConstraintViolation<Institution>> violations = new HashSet<>();

        /*
         * Check the fields in the map and update the institution object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */

        // look to see if name changed; if so then apply validation
        if ( institutionMap.containsKey("name") && !institutionMap.get("name").equals(institution.getName()) ) {
            institution.setName((String) institutionMap.get("name"));

            // Check to see if the new name passes validations.
            violations.addAll(validator.validateProperty(institution, "name"));
        }

        if ( institutionMap.containsKey("brandName") ) {
            institution.setBrandName((String) institutionMap.get("brandName"));
        }

        if ( institutionMap.containsKey("active") ) {
            institution.setActive((String) institutionMap.get("active"));

            // Check to see if the active passes validations.
            violations.addAll(validator.validateProperty(institution, "active"));
        }

        if ( institutionMap.containsKey("permissionSet") ) {
            institution.setPermissionSet((String) institutionMap.get("permissionSet"));

            // Check to see if the permission set passes validations.
            violations.addAll(validator.validateProperty(institution, "permissionSet"));
        }

        // Check whether we had any field validations that did not pass.
        if ( !violations.isEmpty() ) {
            log.error("Institution did not pass validations before update.");
            throw new ConstraintViolationException(violations);
        }

        // Update the institution.
        institutionDao.update(institution);

        return institutionDao.findById(institution.getId());
    }

    /**
     * This method will delete an institution and all associated records in other tables related to the specified
     * institution.  This method is transactional so that if any of the deletes fail, the entire transaction will be
     * rolled back.
     *
     * @param   id    The id of the institution to delete.
     */
    public void deleteById(Long id) {

        // Delete all deals related records where this institution was the originator or participant.
        dealService.deleteAllByInstitutionId(id);

        // Delete all customer data for this institution.
        customerDataService.deleteForOriginatorId(id);

        confidentialityAgreementDao.deleteConfidentialityAgreementByInstitutionId(id);

        // Delete all of the users.
        userService.deleteAllByInstitutionId(id);

        // Delete the institution.
        institutionDao.deleteById(id);
    }

    /**
     * This method will delete an institution and all associated records in other tables related to the specified
     * institution.  This method is transactional so that if any of the deletes fail, the entire transaction will be
     * rolled back.  The try/catch block assures that the operation is idempotent.
     *
     * Added the delete of the deal and associated data that the institution was the originator.
     *
     * @param   uid    The uid of the institution to delete.
     */
    public void deleteByUid(String uid) {

        Institution institution = institutionDao.findByUid(uid);

        try {
            deleteById(institution.getId());
        } catch ( RuntimeException e ) {
            // There was an exception, so make the institution inactive.
            institution.setActive("N");
            institutionDao.update(institution);
        }

    }

    public ProviderData getProviderData(String email) {
        try {
            Boolean ssoIndicator = userService.getUserByEmail(email).getInstitution().getSsoFlag().equals("Y");
            if (ssoIndicator) {
                return new ProviderData(ssoIndicator, awsService.getIdentityProviderForEmail(email));
            }
            return new ProviderData(false, null);
        } catch (Exception e) {
            return new ProviderData(false, null);
        }
    }
}