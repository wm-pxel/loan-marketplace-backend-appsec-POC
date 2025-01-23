package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.FeatureFlagDao;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.FeatureFlag;
import com.westmonroe.loansyndication.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FeatureFlagService {

    private final FeatureFlagDao featureFlagDao;
    private Validator validator;

    public FeatureFlagService(FeatureFlagDao featureFlagDao, Validator validator){
        this.featureFlagDao = featureFlagDao;
        this.validator = validator;
    }

    public FeatureFlag getFeatureFlagForId(Long featureFlagId){
        return featureFlagDao.findById(featureFlagId);
    }

    public FeatureFlag getFeatureFlagForName(String featureName){
        return featureFlagDao.findByName(featureName);
    }

    public List<FeatureFlag> getFeatureFlags(){
        return featureFlagDao.findAll();
    }


    public FeatureFlag save(FeatureFlag featureFlag, User currentUser){
        //TODO: Verify user has permissions to create feature flag
        featureFlag.setCreatedBy(currentUser);
        featureFlag.setUpdatedBy(currentUser);

        featureFlagDao.save(featureFlag);
        return featureFlagDao.findById(featureFlag.getId());
    }

    public FeatureFlag update(Map<String, Object> featureFlagMap, User currentUser){
        //TODO: Verify user has permissions to update feature flag
        if ( !featureFlagMap.containsKey("id") ) {
            throw new MissingDataException("The featureFlag must contain the unique id for an update.");
        }

        FeatureFlag featureFlag = featureFlagDao.findById(Long.valueOf(featureFlagMap.get("id").toString()));
        Set<ConstraintViolation<FeatureFlag>> violations = new HashSet<>();

        if (featureFlagMap.containsKey("featureName")) {
            featureFlag.setFeatureName((String) featureFlagMap.get("featureName"));


            violations.addAll(validator.validateProperty(featureFlag, "featureName"));
        }

        if (featureFlagMap.containsKey("isEnabled") && !featureFlag.getIsEnabled().equals(featureFlagMap.get("isEnabled")) ) {
            featureFlag.setIsEnabled((String) featureFlagMap.get("isEnabled"));

            violations.addAll(validator.validateProperty(featureFlag, "isEnabled"));
        }

        featureFlag.setUpdatedBy(currentUser);

        // Check whether we had any field validations that did not pass.
        if ( !violations.isEmpty() ) {
            throw new ConstraintViolationException(violations);
        }

        featureFlagDao.update(featureFlag);

        return featureFlagDao.findById(featureFlag.getId());
    }

    public void deleteById(Long id){
        featureFlagDao.deleteById(id);
    }

    public void deleteAll(){
        featureFlagDao.deleteAll();
    }
}
