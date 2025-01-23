package com.westmonroe.loansyndication.controller.graphql;


import com.westmonroe.loansyndication.model.FeatureFlag;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.service.FeatureFlagService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@Validated
public class FeatureFlagGQLController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagGQLController(FeatureFlagService featureFlagService){
        this.featureFlagService = featureFlagService;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated() && hasRole('SUPER_ADM')")
    public List<FeatureFlag> getFeatureFlags(){
        return featureFlagService.getFeatureFlags();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated() && hasRole('SUPER_ADM')")
    public FeatureFlag getFeatureFlagById(@Argument Long id){
        return featureFlagService.getFeatureFlagForId(id);
    }


    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public FeatureFlag getFeatureFlagByFeatureName(@Argument String featureName){
        return featureFlagService.getFeatureFlagForName(featureName);
    }


    @MutationMapping
    @PreAuthorize("isAuthenticated() && hasRole('SUPER_ADM')")
    public FeatureFlag createFeatureFlag(@Argument @Valid FeatureFlag input, @AuthenticationPrincipal User currentUser){
        return featureFlagService.save(input, currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated() && hasRole('SUPER_ADM')")
    public FeatureFlag updateFeatureFlag(@Argument Map<String, Object> input, @AuthenticationPrincipal User currentUser){
        return featureFlagService.update(input, currentUser);
    }


    @MutationMapping
    @PreAuthorize("isAuthenticated() && hasRole('SUPER_ADM')")
    public FeatureFlag deleteFeatureFlag(@Argument Long id){
        FeatureFlag featureFlag = featureFlagService.getFeatureFlagForId(id);
        featureFlagService.deleteById(id);
        return featureFlag;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated() && hasRole('SUPER_ADM')")
    public List<FeatureFlag> deleteAllFeatureFlags(){
        List<FeatureFlag> featureFlags = featureFlagService.getFeatureFlags();
        featureFlagService.deleteAll();
        return featureFlags;
    }

}
