package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.*;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.service.DefinitionService;
import com.westmonroe.loansyndication.service.InitialLenderService;
import com.westmonroe.loansyndication.service.PicklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class DefinitionGQLController {

    private final DefinitionService definitionService;
    private final InitialLenderService initialLenderService;
    private final PicklistService picklistService;

    public DefinitionGQLController(DefinitionService definitionService, InitialLenderService initialLenderService
                , PicklistService picklistService) {
        this.definitionService = definitionService;
        this.initialLenderService = initialLenderService;
        this.picklistService = picklistService;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<ParticipantStep> allParticipantSteps() {
        return definitionService.getParticipantSteps();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ParticipantStep getParticipantStepById(@Argument Long id) {
        return definitionService.getParticipantStepById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ParticipantStep getParticipantStepByOrder(@Argument Integer order) {
        return definitionService.getParticipantStepByOrder(order);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<NaicsCode> allNaicsCodes() {
        return definitionService.getAllNaicsCodes();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public NaicsCode getNaicsCodeByCode(@Argument String code) {
        return definitionService.getNaicsCodeByCode(code);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<NaicsCode> searchNaicsCodesByTitle(@Argument String title) {
        return definitionService.searchNaicsCodesByTitle(title);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<InitialLender> allInitialLenders() {
        return initialLenderService.getAllInitialLenders();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public InitialLender getInitialLenderById(@Argument Long lenderId) {
        return initialLenderService.getInitialLenderById(lenderId);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<InitialLender> searchInitialLendersByLender(@Argument String lenderName) {
        return initialLenderService.searchInitialLendersByLender(lenderName);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ACCESS_ALL_INST_DEALS')")
    public List<Role> allRoles() {
        return definitionService.getAllRoles();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Stage> allStages() {
        return definitionService.getStages();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<DocumentCategory> allDocumentCategories() {
        return definitionService.getDocumentCategories();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<PicklistCategory> allPicklistCategories() {
        return picklistService.getPicklistCategories();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<PicklistItem> getPicklistByCategoryId(@Argument Long categoryId) {
        return picklistService.getPicklistByCategoryId(categoryId);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<PicklistItem> getPicklistByCategoryName(@Argument String categoryName, @AuthenticationPrincipal User currentUser) {
        return picklistService.getPicklistByCategoryName(categoryName, currentUser);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<EventType> allEventTypes() {
        return definitionService.getEventTypes();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EventType getEventTypeById(@Argument Long id) {
        return definitionService.getEventTypeById(id);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public InitialLender createInitialLender(@Argument InitialLender input) {
        return initialLenderService.save(input);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public InitialLender updateInitialLender(@Argument Map<String, Object> input) {
        return initialLenderService.update(input);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public InitialLender deleteInitialLender(@Argument Long lenderId) {

        // Get the initial lender to make sure it exists.
        InitialLender lender = null;

        try {
            lender = initialLenderService.getInitialLenderById(lenderId);
        } catch ( DataNotFoundException e ) {
            // Throw specific error and message when initial lender not found.
            throw new DataNotFoundException("Initial Lender could not be deleted because it does not exist.");
        }

        // Delete the initial lender.
        initialLenderService.deleteById(lenderId);

        return lender;
    }

    @SchemaMapping
    @PreAuthorize("isAuthenticated()")
    public ParticipantStep step(EventParticipant eventParticipant) {
        return definitionService.getParticipantStepById(eventParticipant.getStep().getId());
    }

}