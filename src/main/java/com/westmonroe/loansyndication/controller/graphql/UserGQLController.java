package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.EndUserAgreement;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealMember;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.EmailTypeEnum.INSTITUTION_MEMBER_ADDED;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.USER_ACTIVATED;
import static com.westmonroe.loansyndication.utils.RoleDefEnum.RECV_ALL_INST_INVS;

@Controller
@Slf4j
public class UserGQLController {
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;

    private final UserService userService;
    private final EmailService emailService;

    public UserGQLController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Role> getRolesByUserUid(@Argument String uid) {
        return userService.getRolesForUserUid(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return currentUser;
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<User> getUsersByInstitutionUid(@Argument String uid) {
        return userService.getUsersForInstitutionUid(uid);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<User> getDealMemberUsersAvailableByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        return userService.getUsersAvailableForDealUid(uid, currentUser.getUid());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<User> getDealMemberUsersByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        return userService.getDealMemberUsersByDealUid(uid, currentUser.getUid());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User getUserByUid(@Argument String uid) {
        return userService.getUserByUid(uid);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public User createUser(@Argument @Valid User input) {

        User user = userService.save(input);

        // It's possible to save a user without an institution, so check whether an institution was supplied before adding a default role.
        if ( user.getInstitution() != null ) {

            List<User> inviteRecipients = userService.getUsersByInstitutionUidAndUserRoleId(user.getInstitution().getUid(), RECV_ALL_INST_INVS.getId());

            // Adding a default role for the user.  NOTE: This may not be the best place for this logic.  Probably
            // better in the controller to keep the service cleaner and eliminate the dependency.
            if ( inviteRecipients.isEmpty() ) {
                userService.saveRoleForUser(user, new Role(RECV_ALL_INST_INVS.getId()));
            }
        }

        return user;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public User updateUser(@Argument Map<String, Object> input) {

        User user = userService.getUserByUid((String) input.get("uid"));
        User updatedUser = userService.update(input);

        if (!user.getActive().equals((String) input.get("active"))) {
            emailService.sendEmail(USER_ACTIVATED, null, new HashMap<String, Object>() {{
                put("updatedUser", updatedUser);
                put("user", user);
                put("institutionName", user.getInstitution().getName());
                put("from", sendAddress);
            }});
        }

        return updatedUser;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public User deleteUser(@Argument String userUid) {

        User user = null;

        try {
            user = userService.getUserByUid(userUid);
        } catch ( DataNotFoundException e ) {
            // Throw specific error and message when user not found.
            throw new DataNotFoundException("User could not be deleted because it does not exist.");
        }

        // Delete the user.
        userService.deleteByUid(userUid);

        return user;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public User createInstitutionUserInvite(@Argument @Valid User input, @AuthenticationPrincipal User currentUser) {

        User user = userService.createInstitutionUserInvite(input, currentUser);

        // It's possible to save a user without an institution, so check whether an institution was supplied before adding a default role.
        if ( user.getInstitution() != null ) {

            List<User> inviteRecipients = userService.getUsersByInstitutionUidAndUserRoleId(user.getInstitution().getUid(), RECV_ALL_INST_INVS.getId());

            // Adding a default role for the user.  NOTE: This may not be the best place for this logic.  Probably
            // better in the controller to keep the service cleaner and eliminate the dependency.
            if ( inviteRecipients.isEmpty() ) {
                userService.saveRoleForUser(user, new Role(RECV_ALL_INST_INVS.getId()));
            }

            emailService.sendEmail(INSTITUTION_MEMBER_ADDED, null, new HashMap<>() {{
                put("user", user);
                put("participantInstitution", user.getInstitution().getName());
                put("isInstitutionEmail", "Y");
                put("from", sendAddress);
            }});
        }

        return user;
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_INST_USR', 'SUPER_ADM')")
    public User addRoleToUser(@Argument String userUid, @Argument Long roleId, @AuthenticationPrincipal User currentUser) {
        return userService.saveRoleForUser(userUid, roleId, currentUser);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('MNG_INST_USR', 'SUPER_ADM')")
    public User deleteRoleFromUser(@Argument String userUid, @Argument Long roleId, @AuthenticationPrincipal User currentUser) {
        return userService.deleteRoleForUser(userUid, roleId, currentUser);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EndUserAgreement getEndUserAgreement(@AuthenticationPrincipal User currentUser) {
        return userService.getEndUserAgreementByUser(currentUser);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public EndUserAgreement getSignedEndUserAgreement(@AuthenticationPrincipal User currentUser) {
        return userService.getSignedEndUserAgreementByUser(currentUser);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean agreeToEndUserAgreement(@Argument Integer euaId, @AuthenticationPrincipal User currentUser) {
        return userService.agreeToEndUserAgreement(currentUser, euaId);
    }

    @SchemaMapping
    public User user(DealMember dealMember) {
        return userService.getUserByUid(dealMember.getUser().getUid());
    }

    @BatchMapping(field = "roles", typeName = "User")
    public Map<User, List<Role>> roles(List<User> users) {
        return userService.getRolesForUsers(users);
    }

}