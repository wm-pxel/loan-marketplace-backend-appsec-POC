package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.ConfidentialityAgreement;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ProviderData;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class InstitutionGQLController {

    private final InstitutionService institutionService;
    private final AuthorizationService authorizationService;

    public InstitutionGQLController(InstitutionService institutionService, AuthorizationService authorizationService) {
        this.institutionService = institutionService;
        this.authorizationService = authorizationService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('SUPER_ADM')")
    public List<Institution> allInstitutions() {
        return institutionService.getAllInstitutions();
    }

    @QueryMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public List<Institution> getParticipantsNotOnDeal(@Argument String dealUid) {

        // Get the list of institutions not on the specified deal.
        return institutionService.getParticipantsNotOnDeal(dealUid);
    }

    @QueryMapping
    @PreAuthorize("hasRole('MNG_PART_INST')")
    public List<Institution> getEventParticipantsNotOnDeal(@Argument String dealUid) {

        // Get the list of institutions not on the specified deal.
        return institutionService.getEventParticipantsNotOnDeal(dealUid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Institution getInstitutionByUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {

        // Get the institution first to verify it exists.
        Institution institution = institutionService.getInstitutionByUid(uid);

        // Perform the authorization for the current user.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, uid);

        return institution;
    }

    @MutationMapping
    @PreAuthorize("hasRole('SUPER_ADM')")
    public Institution createInstitution(@Argument @Valid Institution input) {
        return institutionService.save(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('SUPER_ADM')")
    public Institution createInstitutionAndConfidentialityAgreement(@Argument @Valid Institution input, @Argument String description, @AuthenticationPrincipal User currentUser) {
        Institution institution = institutionService.save(input);

        // create confidentiality agreement for the institution that was just created.
        institutionService.createConfidentialityAgreement(institution.getUid(), description, currentUser.getId());

        return institution;
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_INST', 'SUPER_ADM')")
    public Institution updateInstitution(@Argument Map<String, Object> input) {
        return institutionService.update(input);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_INST', 'SUPER_ADM')")
    public Institution updateInstitutionAndConfidentialityAgreement(@Argument Map<String, Object> input, @Argument String description, @AuthenticationPrincipal User currentUser) {
        // check if description (confidentiality agreement) is non-null and not empty
        if (description != null && !description.isEmpty()) {
            institutionService.createConfidentialityAgreement((String) input.get("uid"), description, currentUser.getId());
        }

        return institutionService.update(input);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Institution deleteInstitution(@Argument String institutionUid, @AuthenticationPrincipal User currentUser) {

        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, institutionUid);
        Institution institution = null;

        try {
            institution = institutionService.getInstitutionByUid(institutionUid);
        } catch ( DataNotFoundException e ) {
            // Throw specific error and message when institution not found.
            throw new DataNotFoundException("Institution could not be deleted because it does not exist.");
        }

        // Delete the institution.
        institutionService.deleteByUid(institutionUid);

        return institution;
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_INST', 'SUPER_ADM')")
    public ConfidentialityAgreement createConfidentialityAgreement(@Argument String institutionUid, @Argument String description, @AuthenticationPrincipal User currentUser) {
        return institutionService.createConfidentialityAgreement(institutionUid, description, currentUser.getId());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ConfidentialityAgreement getConfidentialityAgreementByInstitutionUid(@Argument String uid) {
        // Get the institution first to verify it exists.
        Institution institution = institutionService.getInstitutionByUid(uid);

        ConfidentialityAgreement confidentialityAgreement = institutionService.getConfidentialityAgreementByInstitutionId(institution.getId());

        return confidentialityAgreement;
    }

    @QueryMapping
    public ProviderData getProviderData(@Argument String email) {
        return institutionService.getProviderData(email);
    }

}