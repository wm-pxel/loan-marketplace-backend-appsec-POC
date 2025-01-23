package com.westmonroe.loansyndication.controller.rest;

import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.InstitutionDto;
import com.westmonroe.loansyndication.model.integration.RoleDto;
import com.westmonroe.loansyndication.model.integration.UserDto;
import com.westmonroe.loansyndication.service.AuthorizationService;
import com.westmonroe.loansyndication.service.integration.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.ERR_UNAUTH_INSTITUTION_MEMBER;

@Tag(name = "User Management", description = "User Management APIs")
@RestController
@CrossOrigin
@RequestMapping("/api/ext")
public class UserManagementController {

    private final AuthorizationService authorizationService;
    private final UserManagementService userManagementService;

    public UserManagementController(AuthorizationService authorizationService, UserManagementService userManagementService) {
        this.authorizationService = authorizationService;
        this.userManagementService = userManagementService;
    }

    @Operation(
        summary = "Retrieve list of roles in Lamina.",
        description = "The list of roles for users in the Lamina application..",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "The list of roles",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleDto.class)))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleDto>> getRoles(@AuthenticationPrincipal User currentUser) {
        return new ResponseEntity<>(userManagementService.getRoles(), HttpStatus.OK);
    }

    @Operation(
        summary = "Retrieve list of users for an institution.",
        description = "The list of users for the supplied institution.  The requesting user account is verified before access to institutions.",
        responses = {
            @ApiResponse(responseCode = "200",
                    description = "The list of users",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/institutions/{institutionUid}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDto>> getUsers(@PathVariable String institutionUid, @AuthenticationPrincipal User currentUser) {
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, institutionUid);
        return new ResponseEntity<>(userManagementService.getUsersForInstitutionUid(institutionUid), HttpStatus.OK);
    }

    @Operation(
        summary = "Retrieve user information",
        description = "The user information for the supplied unique identifier.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "User information",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/users/{userUid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUser(@PathVariable String userUid, @AuthenticationPrincipal User currentUser) {

        // Get the user to allow authorization of the current user and institution.  This also verifies that the user exists.
        UserDto userDto = userManagementService.getUserByUid(userUid, true);

        // Authorize the current user has access to the requested user's institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, userDto.getInstitution().getUid());

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @Operation(
        summary = "Create a user",
        description = "Create a user for the supplied institution.",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "User created",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/institutions/{institutionUid}/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> saveUser(@PathVariable String institutionUid, @Valid @RequestBody UserDto userDto
            , BindingResult errors, @AuthenticationPrincipal User currentUser) {

        if ( errors.hasErrors() ) {
            throw new ValidationException(errors.getFieldErrors());
        }

        // Authorize the user for this institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, institutionUid);

        userDto.setInstitution(new InstitutionDto(institutionUid));
        userDto = userManagementService.save(userDto);

        // Create the location header.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", String.format("/api/ext/users/%s", userDto.getUid()));

        return new ResponseEntity<>(userDto, headers, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Update a user",
        description = "Update user information.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "User updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping(value = "/users/{userUid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateUser(@PathVariable String userUid, @Valid @RequestBody UserDto userDto
            , BindingResult errors, @AuthenticationPrincipal User currentUser) {

        // Get the user to allow authorization of the current user and institution.  This also verifies that the user exists.
        UserDto savedUserDto = userManagementService.getUserByUid(userUid);

        List<FieldError> fieldErrors;

        // Remove the validation for unique email, if the email is the same.
        if ( userDto.getEmail().equals(savedUserDto.getEmail()) ) {
            fieldErrors = errors.getFieldErrors().stream().filter(fe -> !fe.getDefaultMessage().contains("unique")).toList();
        } else {
            fieldErrors = errors.getFieldErrors();
        }

        // If there were more validation exceptions
        if ( fieldErrors.size() > 0 ) {
            throw new ValidationException(fieldErrors);
        }

        // Authorize the user for this institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, userDto.getInstitution().getUid());

        // Case 1: Verify that the uid in the payload was set or not empty.
        if ( userDto.getUid() == null || userDto.getUid().isEmpty() ) {
            throw new ValidationException("The uid in the payload is missing.");

            // Case 2: Verify that the uid in the path and payload match.
        } else if ( !userUid.equals(userDto.getUid()) ) {
            throw new ValidationException("The uids in the url and payload do not match.");

        // Case 3: Check if the institution in the payload was set.
        } else if ( userDto.getInstitution() == null || userDto.getInstitution().getUid() == null ) {
            throw new OperationNotAllowedException("The institution in the payload was not assigned.");
        }

        userManagementService.update(userDto);

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @Operation(
        summary = "Delete a user",
        description = "Delete a user.  If user cannot be deleted then the user is disabled.",
        responses = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/users/{userUid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUser(@PathVariable String userUid, @AuthenticationPrincipal User currentUser) {

        // Get the user to allow authorization of the current user and institution.  This also verifies that the user exists.
        UserDto userDto = userManagementService.getUserByUid(userUid);

        // Authorize the user for this institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, userDto.getInstitution().getUid());

        // Delete the user, if an exception is generated then the user will be made inactive.
        userManagementService.delete(userDto);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Retrieve list of roles for a user.",
        description = "The list of roles for the supplied user.  The requesting user account is verified before access to user.",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "The list of roles",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleDto.class)))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/users/{userUid}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleDto>> getAllRolesForUser(@PathVariable String userUid, @AuthenticationPrincipal User currentUser) {

        // Get the user to allow authorization of the current user and institution.  This also verifies that the user exists.
        UserDto userDto = userManagementService.getUserByUid(userUid, true);

        // Authorize the current user has access to the requested user's institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, userDto.getInstitution().getUid());

        return new ResponseEntity<>(userDto.getRoles(), HttpStatus.OK);
    }

    @Operation(
        summary = "Add a role to a user",
        description = "Add a role to the user.",
        responses = {
            @ApiResponse(responseCode = "201",
                description = "Role added to User",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/users/{userUid}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> addRoleForUser(@PathVariable String userUid, @RequestBody RoleDto roleDto, @AuthenticationPrincipal User currentUser) {

        // Get the user to allow authorization of the current user and institution.  This also verifies that the user exists.
        UserDto userDto = userManagementService.getUserByUid(userUid);

        // Authorize the current user has access to the requested user's institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, userDto.getInstitution().getUid());

        // TODO: Need to make sure this is a role that can be assigned (i.e. they can't assign super admin)

        return new ResponseEntity<>(userManagementService.saveRoleForUser(userDto, roleDto, currentUser, true), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Remove a role from a user",
        description = "Remove a role from a user.",
        responses = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "401", description = ERR_UNAUTH_INSTITUTION_MEMBER)
        }
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping(value = "/users/{userUid}/roles/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> deleteRoleForUser(@PathVariable String userUid, @PathVariable Long roleId, @AuthenticationPrincipal User currentUser) {

        // Get the user to allow authorization of the current user and institution.  This also verifies that the user exists.
        UserDto userDto = userManagementService.getUserByUid(userUid);

        // Authorize the current user has access to the requested user's institution.
        authorizationService.authorizeUserForInstitutionByInstitutionUid(currentUser, userDto.getInstitution().getUid());

        // TODO: Need to make sure this is a role that can be deleted (i.e. they can't delete super admin from user)

        // Delete the role from the user.
        userManagementService.deleteRoleForUser(userUid, roleId, currentUser);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}