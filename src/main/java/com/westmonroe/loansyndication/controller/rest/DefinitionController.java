package com.westmonroe.loansyndication.controller.rest;

import com.westmonroe.loansyndication.exception.InvalidDataException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.service.DefinitionService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@CrossOrigin
@RequestMapping("/api")
public class DefinitionController {

    private final DefinitionService definitionService;

    public DefinitionController(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Role>> getAllRoles() {
        return new ResponseEntity<>(definitionService.getAllRoles(), HttpStatus.OK);
    }

    @GetMapping(value = "/roles/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Role> getRoleById(@PathVariable Long roleId) {
        return new ResponseEntity<>(definitionService.getRoleById(roleId), HttpStatus.OK);
    }

    @PostMapping(value = "/roles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        definitionService.saveRole(role);

        // Create the location header.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "/api/roles/" + role.getId());

        return new ResponseEntity<>(role, headers, HttpStatus.CREATED);
    }

    @PutMapping(value = "/roles/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Role> updateRole(@PathVariable Long roleId, @RequestBody Role role) {

        // Verify that the id was set or matches the value in the object.
        if ( role.getId() == null ) {
            throw new ValidationException("The id in the payload is missing.");
        } else if ( !role.getId().equals(roleId) ) {
            throw new InvalidDataException("The id in the URL does not match the id in the payload.");
        }

        definitionService.updateRole(role);

        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @DeleteMapping(value = "/roles/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADM')")
    public ResponseEntity<Void> deleteRoleById(@PathVariable Long roleId) {
        definitionService.deleteRole(roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}