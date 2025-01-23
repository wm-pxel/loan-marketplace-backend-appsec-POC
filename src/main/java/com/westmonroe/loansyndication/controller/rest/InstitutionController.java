package com.westmonroe.loansyndication.controller.rest;

import com.westmonroe.loansyndication.exception.InvalidDataException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.InstitutionService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@CrossOrigin
@RequestMapping("/api")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final AuthorizationService authorizationService;

    public InstitutionController(InstitutionService institutionService, AuthorizationService authorizationService) {
        this.institutionService = institutionService;
        this.authorizationService = authorizationService;
    }

    @GetMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADM')")
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        return new ResponseEntity<>(institutionService.getAllInstitutions(), HttpStatus.OK);
    }

    @GetMapping(value = "/institutions/{institutionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Institution> getInstitutionById(@PathVariable String institutionId) {
        return new ResponseEntity<>(institutionService.getInstitutionByUid(institutionId), HttpStatus.OK);
    }

    @GetMapping(value = "/deals/{dealExternalId}/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('EDIT_DEAL_INFO','APP_SERVICE','SUPER_ADM')")
    public ResponseEntity<Institution> getInstitutionByDealExternalId(@PathVariable String dealExternalId) {
        return new ResponseEntity<>(institutionService.getInstitutionByDealExternalId(dealExternalId), HttpStatus.OK);
    }

    @PostMapping(value = "/institutions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Institution> saveInstitution(@RequestBody Institution institution) {
        institution = institutionService.save(institution);

        // Create the location header.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "/api/institutions/" + institution.getUid());

        return new ResponseEntity<>(institution, headers, HttpStatus.CREATED);
    }

    @PutMapping(value = "/institutions/{institutionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Institution> updateInstitution(@PathVariable String institutionId, @RequestBody Institution institution) {

        // Verify that the id was set or matches the value in the object.
        if ( institution.getUid() == null || institution.getUid().isEmpty() ) {
            throw new ValidationException("The id in the payload is missing.");
        } else if ( !institution.getUid().equals(institutionId) ) {
            throw new InvalidDataException("The id in the URL does not match the id in the payload.");
        }

        // User does not know the actual unique and private Institution ID, so we need to get it before updating.
        Institution savedInstitution = institutionService.getInstitutionByUid(institutionId);
        institution.setId(savedInstitution.getId());

        institutionService.update(institution);

        return new ResponseEntity<>(institution, HttpStatus.OK);
    }

    @DeleteMapping(value = "/institutions/{institutionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteInstitutionById(@PathVariable String institutionId, @AuthenticationPrincipal User currentUser) {

        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, institutionId);
        institutionService.deleteByUid(institutionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}