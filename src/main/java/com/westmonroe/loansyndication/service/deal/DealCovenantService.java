package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.dao.deal.DealCovenantDao;
import com.westmonroe.loansyndication.dao.deal.DealDao;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DealCovenantService {

    private final DealCovenantDao dealCovenantDao;
    private final DealDao dealDao;

    public DealCovenantService(DealCovenantDao dealCovenantDao, DealDao dealDao) {
        this.dealCovenantDao = dealCovenantDao;
        this.dealDao = dealDao;
    }

    public DealCovenant getCovenantForId(Long covenantId) {
        return dealCovenantDao.findById(covenantId);
    }

    public DealCovenant getCovenantForExternalId(String externalId) {
        return dealCovenantDao.findByExternalId(externalId);
    }

    public List<DealCovenant> getCovenantsForDeal(String dealUid) {
        return dealCovenantDao.findAllByDealUid(dealUid);
    }

    public DealCovenant save(DealCovenant covenant, User currentUser) {

        //TODO: This should be removed after integrations are complete.
        // If the covenant external id is empty then set it.
        if ( covenant.getCovenantExternalId() == null ) {
            covenant.setCovenantExternalId(UUID.randomUUID().toString());
        }

        // Add the created by user to the deal.
        covenant.setCreatedBy(currentUser);

        // Get full deal object for this covenant.
        Deal deal = dealDao.findByUid(covenant.getDeal().getUid(), currentUser);

        // Set the deal object on the covenant, as it has the needed unique id.
        covenant.setDeal(deal);

        // Save the covenant.  Covenant will be returned with the unique id.
        dealCovenantDao.save(covenant);

        return dealCovenantDao.findById(covenant.getId());
    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * deal covenant fields that were sent.
     *
     * @param  covenantMap
     * @return dealCovenant
     */
    public DealCovenant update(Map<String, Object> covenantMap, User currentUser) {

        //TODO: Verify that user has update permissions for this deal.
        //TODO: Verify that the covenant belongs to the deal.

        if ( !( covenantMap.containsKey("deal") && ((Map) covenantMap.get("deal")).containsKey("uid") ) ) {
            throw new MissingDataException("The deal covenant must contain the deal uid for an update.");
        }

        if ( !covenantMap.containsKey("id") ) {
            throw new MissingDataException("The deal covenant must contain the unique id for an update.");
        }

        // Get the deal covenant by the unique id.
        DealCovenant dealCovenant = dealCovenantDao.findById(Long.valueOf(covenantMap.get("id").toString()));

        /*
         * Check the fields in the map and update the institution object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( covenantMap.containsKey("entityName") ) {
            dealCovenant.setEntityName((String) covenantMap.get("entityName"));
        }

        if ( covenantMap.containsKey("categoryName") ) {
            dealCovenant.setCategoryName((String) covenantMap.get("categoryName"));
        }

        if ( covenantMap.containsKey("covenantType") ) {
            dealCovenant.setCovenantType((String) covenantMap.get("covenantType"));
        }

        if ( covenantMap.containsKey("frequency") ) {
            dealCovenant.setFrequency((String) covenantMap.get("frequency"));
        }

        if ( covenantMap.containsKey("nextEvalDate") ) {
            if ( covenantMap.get("nextEvalDate") == null ) {
                dealCovenant.setNextEvalDate(null);
            } else {
                dealCovenant.setNextEvalDate(LocalDate.parse(covenantMap.get("nextEvalDate").toString()));
            }
        }

        if ( covenantMap.containsKey("effectiveDate") ) {
            if ( covenantMap.get("effectiveDate") == null ) {
                dealCovenant.setEffectiveDate(null);
            } else {
                dealCovenant.setEffectiveDate(LocalDate.parse(covenantMap.get("effectiveDate").toString()));
            }
        }

        // Add the updated by user to the deal.
        dealCovenant.setUpdatedBy(currentUser);

        // Update the deal covenant.
        dealCovenantDao.update(dealCovenant);

        // Return the full deal participant object.
        return dealCovenant;
    }


    public int deleteById(Long id) {
        //TODO: Verify that user has update permissions for this deal.
        return dealCovenantDao.deleteById(id);
    }

    public int deleteByExternalId(String covenantExternalIid) {
        //TODO: Verify that user has update permissions for this deal.
        return dealCovenantDao.deleteByExternalId(covenantExternalIid);
    }

    public int deleteAllByDealUid(String dealUid) {
        return dealCovenantDao.deleteAllByDealUid(dealUid);
    }

}