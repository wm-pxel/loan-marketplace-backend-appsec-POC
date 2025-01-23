package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.*;
import com.westmonroe.loansyndication.exception.*;
import com.westmonroe.loansyndication.model.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.westmonroe.loansyndication.utils.RoleDefEnum.RECV_ALL_INST_INVS;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final InstitutionDao institutionDao;
    private final DefinitionService definitionService;
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final Validator validator;
    private final EndUserAgreementDao endUserAgreementDao;
    private final UserEuaDao userEuaDao;
    private final RoleDao roleDao;

    public UserService(InstitutionDao institutionDao, DefinitionService definitionService, UserDao userDao,
                       UserRoleDao userRoleDao, EndUserAgreementDao endUserAgreementDao, UserEuaDao userEuaDao,
                       Validator validator, RoleDao roleDao) {
        this.institutionDao = institutionDao;
        this.definitionService = definitionService;
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.validator = validator;
        this.endUserAgreementDao = endUserAgreementDao;
        this.userEuaDao = userEuaDao;
        this.roleDao = roleDao;
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public List<User> getAllNonSystemUsers() {
        return userDao.findAllNonSystem();
    }

    public List<User> getUsersForInstitutionId(Long institutionId) {
        return userDao.findAllByInstitutionId(institutionId);
    }

    public List<User> getUsersForInstitutionUid(String institutionUid) {

        List<User> users = userDao.findAllNonSystemByInstitutionUid(institutionUid);

        // Get roles for each user.
        for ( User user : users ) {
            user.setRoles(userRoleDao.findRolesByUserId(user.getId()));
        }

        return users;
    }

    public List<User> getUsersAvailableForDealUid(String dealUid, String userUid) {

        User user = userDao.findByUid(userUid);

        return userDao.findAllNonSystemDealMemberUsersAvailableByDealUidAndInstitutionId(dealUid, user.getInstitution().getId());
    }

    public List<User> getUsersByInstitutionUidAndUserRoleId(String institutionUid, long roleId) {
        return userDao.findAllByInstitutionUidAndUserRoleId(institutionUid,roleId);
    }

    public List<User> getNonSystemUsersByInstitutionUidAndUserRoleId(String institutionUid, long roleId) {
        return userDao.findAllNonSystemByInstitutionUidAndUserRoleId(institutionUid,roleId);
    }

    public List<User> getDealMemberUsersByDealUid(String dealUid, String userUid) {

        User user = userDao.findByUid(userUid);

        return userDao.findAllNonSystemDealMemberUsers(dealUid, user.getInstitution().getId());
    }

    public Long getUserIdByInstitutionUidAndUserUid(String institutionUid, String userUid) {
        return userDao.findIdByInstitutionUidAndUserUid(institutionUid, userUid);
    }

    public User getUserById(Long id) {
        return getUserById(id, false);
    }

    public User getUserById(Long id, boolean fetchRoles) {

        User user = userDao.findById(id);

        // Only get roles if flag is true.
        if ( fetchRoles ) {
            user.setRoles(getRolesForUserId(user.getId()));
        }

        return user;
    }

    public User getUserByUid(String uid) {
        return getUserByUid(uid, false);
    }

    public User getUserByUid(String uid, boolean fetchRoles) {

        User user = userDao.findByUid(uid);

        // Only get roles if flag is true.
        if ( fetchRoles ) {
            user.setRoles(getRolesForUserId(user.getId()));
        }

        return user;
    }

    public User getUserByEmail(String email) {
        return getUserByEmail(email, false);
    }

    public User getUserByEmail(String email, boolean fetchRoles) {

        User user = userDao.findByEmail(email);

        // Only get roles if flag is true.
        if ( fetchRoles ) {
            user.setRoles(getRolesForUserId(user.getId()));
        }

        return user;
    }

    public List<Role> getRolesForUserId(Long userId) {
        return userRoleDao.findRolesByUserId(userId);
    }

    public List<Role> getRolesForUserUid(String userUid) {
        return userRoleDao.findRolesByUserUid(userUid);
    }

    public Map<User, List<Role>> getRolesForUsers(List<User> users) {

        List<UserRole> userRoles = userRoleDao.findRolesByUsers(users);
        Map<Long, List<UserRole>> userRolesMap = userRoles.stream().collect(Collectors.groupingBy(ur -> ur.getUser().getId()));
        return users.stream()
                .collect(Collectors.toMap(
                        user -> user,
                        user -> userRolesMap.getOrDefault(user.getId(), Collections.emptyList())
                                .stream()
                                .map(UserRole::getRole)
                                .toList()
                ));
    }

    public User save(User user) {

        // Create random UUID for the new user.
        user.setUid(UUID.randomUUID().toString());

        // Get the full institution object if only the institution uid is supplied. As expected from the GraphQL endpoint.
        if ( user.getInstitution() != null && user.getInstitution().getId() == null && user.getInstitution().getUid() != null ) {
            user.setInstitution(institutionDao.findByUid(user.getInstitution().getUid()));
        }

        // Save the user.
        try {
            user = userDao.save(user);
        } catch ( DuplicateKeyException dke ) {

            String fieldName = "email";

            if ( dke.getMessage().contains("user_uuid") ) {
                fieldName = "Unique Id";
            }

            log.error(dke.getMessage());
            throw new DataIntegrityException(String.format("The %s value already exists and must be unique.", fieldName));

        } catch ( Exception e ) {
            log.error(e.getMessage());
        }

        /*
         *  Add the roles, if they were supplied.
         */
        if ( user.getRoles() != null && !user.getRoles().isEmpty() ) {

            // Loop through all of the roles submitted with the user.
            for ( Role userRole : user.getRoles() ) {

                // Get the full role object.
                Role role = roleDao.findById(userRole.getId());

                // As a safeguard, make sure the role is visible (i.e. isn't a special application role)
                if ( role.getVisible().equals("Y") ) {
                    saveRoleForUser(user, role);
                }

            }

        }

        return getUserById(user.getId(), true);
    }

    /**
     * This method handles a special case for creating a user.  The user is created under the current user's institution
     * and with a invite status of "Invited".
     *
     * @param user
     * @param currentUser
     * @return The newly created user with their roles.
     */
    public User createInstitutionUserInvite(User user, User currentUser) {

        // If user is a super admin, find the institution using the UID provided
        // Otherwise, the current user can only create users in their institution, so set the new user's institution to the current user's.
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getCode().equals("SUPER_ADM"));
        if (isAdmin && user.getInstitution() != null) {
            user.setInstitution(institutionDao.findByUid(user.getInstitution().getUid()));
        }
        else {
            user.setInstitution(currentUser.getInstitution());
        }

        // Set the invite status to "Invited" and active to "Y" (as requested).
        user.setInviteStatus(new InviteStatus("I", "Invited"));
        user.setActive("Y");

        return save(user);
    }

    public void update(User user) {

        if ( user.getId() != null ) {
            userDao.updateById(user);
        } else if ( user.getUid() != null ) {
            userDao.updateByUid(user);
        } else {
            throw new OperationNotAllowedException("Cannot update the user without providing an id or uid.");
        }

    }

    /**
     * This method was created to support the GraphQL API.  This method will take a map of values and only update the
     * deal fields that were sent.
     *
     * @param  userMap
     * @return user
     */
    public User update(Map<String, Object> userMap) {

        if ( !userMap.containsKey("uid") ) {
            throw new MissingDataException("The user must contain the uid for an update.");
        }

        // Get the user by the uid.
        User user = userDao.findByUid((String) userMap.get("uid"));

        // Create collection for list of violations.
        Set<ConstraintViolation<User>> violations = new HashSet<>();

        /*
         * Check the fields in the map and update the deal object with the values from the map.
         * This will allow the user to only send the fields they want to update.
         */
        if ( userMap.containsKey("firstName") ) {
            user.setFirstName((String) userMap.get("firstName"));
        }

        if ( userMap.containsKey("lastName") ) {
            user.setLastName((String) userMap.get("lastName"));
        }

        if (userMap.containsKey("email") && !userMap.get("email").equals(user.getEmail())) {
            user.setEmail((String) userMap.get("email"));

            // Check to see if the email passes validations.
            violations.addAll(validator.validateProperty(user, "email"));
        }

        if ( userMap.containsKey("password") ) {
            user.setPassword((String) userMap.get("password"));
        }

        if ( userMap.containsKey("active") ) {
            user.setActive((String) userMap.get("active"));

            // Check to see if the active passes validations.
            violations.addAll(validator.validateProperty(user, "active"));
        }

        // Check whether we had any field validations that did not pass.
        if ( !violations.isEmpty() ) {
            log.error("Institution did not pass validations before update.");
            throw new ConstraintViolationException(violations);
        }

        // Update the user.
        userDao.updateById(user);

        return userDao.findById(user.getId());
    }

    public void deleteById(Long id) {

        // Delete all of the roles before deleting the user.
        // Throws an exception if user is only deal recipient
        deleteAllRolesForUser(id);

        // Delete the user.
        try {
            userDao.deleteById(id);
        } catch ( DataIntegrityViolationException e ) {
            // Make the user inactive.
            userDao.updateInactiveById(id);
        }
    }

    public void deleteAllByInstitutionId(Long institutionId) {

        // Get all users for the institution.
        List<User> users = userDao.findAllByInstitutionId(institutionId);

        // Loop through users and delete or make them inactive.
        for ( User user : users ) {
            deleteById(user.getId());
        }

    }

    @Transactional
    public void deleteByInstitutionUidAndUserUid(String institutionUid, String userUid) {

        User user = userDao.findByInstitutionUidAndUserUid(institutionUid, userUid);

        // Delete the roles first.
        deleteAllRolesForUser(user.getId());

        // Delete the user.
        userDao.deleteById(user.getId());
    }

    public void deleteByUid(String userUid) {
        deleteAllRolesForUser(userUid);
        userDao.deleteByUid(userUid);
    }

    /**
     * This method was created for GraphQL and REST controllers to add a role to a user.  All controllers must invoke
     * this method when adding a role for a user because of the validation implemented.  The validation only allows
     * admins the ability to add the SUPER_ADM or APP_SERVICE roles.
     *
     * @param userUid
     * @param roleId
     * @param currentUser
     *
     * @return  The user whom the role is being added.
     */
    public User saveRoleForUser(String userUid, Long roleId, User currentUser) {

        // Get the user and role objects.  Do not need addition checks, as the reads will throw exceptions if not found.
        User user = userDao.findByUid(userUid);
        Role role = definitionService.getRoleById(roleId);

        /*
         *  Need to perform a validation that only an admin can assign or delete invisible roles.  This currently includes
         *  SUPER_ADM and APP_SERVICE.
         */
        if ( role.getVisible().equals("N") && !currentUser.getRoles().stream().anyMatch(r -> r.getCode().equals("SUPER_ADM")) ) {
            throw new OperationNotAllowedException("Only system admins can assign or delete the selected role.");
        }

        return saveRoleForUser(user, role);
    }

    /**
     * This method adds a role to the provided user amd is provided for service functionality.  The list of roles will
     * not be returned with the user.
     *
     * @param user
     * @param role
     *
     * @return The user whom the role is being added.
     */
    public User saveRoleForUser(User user, Role role) {
        return saveRoleForUser(user, role, false);
    }

    /**
     * This method adds a role to the provided user amd is provided for service functionality.  The list of roles will
     * be returned with the user.
     *
     * @param user
     * @param role
     * @param fetchRoles
     *
     * @return The user whom the role is being added.
     */
    public User saveRoleForUser(User user, Role role, boolean fetchRoles) {

        userRoleDao.saveUserRole(user, role);

        return getUserById(user.getId(), fetchRoles);
    }

    public User deleteRoleForUser(String userUid, Long roleId, User currentUser) {

        // Get the user and role objects.  Do not need addition checks, as the reads will throw exceptions if not found.
        User user = userDao.findByUid(userUid);
        Role role = definitionService.getRoleById(roleId);

        /*
         *  Need to perform a validation that only an admin can assign or delete invisible roles.  This currently includes
         *  SUPER_ADM and APP_SERVICE.
         */
        if ( role.getVisible().equals("N") && !currentUser.getRoles().stream().anyMatch(r -> r.getCode().equals("SUPER_ADM")) ) {
            throw new OperationNotAllowedException("Only system admins can assign or delete the selected role.");
        }

        return deleteRoleForUser(user, role);
    }

    public User deleteRoleForUser(User user, Role role) {
        boolean canDeleteRole = true;

        if (role.getId() == RECV_ALL_INST_INVS.getId()) {
            List<User> invitationRecipients = getUsersByInstitutionUidAndUserRoleId(user.getInstitution().getUid(), RECV_ALL_INST_INVS.getId());
            canDeleteRole = invitationRecipients.size() > 1 || !invitationRecipients.get(0).getId().equals(user.getId());
        }

        if (canDeleteRole) {
            userRoleDao.deleteByUserAndRole(user, role);
        } else {
            throw new OperationNotAllowedException("There must be at least one user that is a deal recipient for this institution");
        }

        return userDao.findById(user.getId());
    }

    public void deleteAllRolesForUser(String userUid) {
        // Get the user object.  Do not need addition checks, as the reads will throw exceptions if not found.
        User user = getUserByUid(userUid);
        List<User> invitationRecipients = getUsersByInstitutionUidAndUserRoleId(user.getInstitution().getUid(), RECV_ALL_INST_INVS.getId());
        if (invitationRecipients.size() > 1 || !invitationRecipients.get(0).getId().equals(user.getId())){
            userRoleDao.deleteByUserUid(userUid);
        } else {
            throw new OperationNotAllowedException("There must be at least one user that is a deal recipient for this institution");
        }
    }

    public void deleteAllRolesForUser(Long userId) {
        // Get the user object.  Do not need addition checks, as the reads will throw exceptions if not found.
        User user = getUserById(userId);

        List<User> invitationRecipients = getUsersByInstitutionUidAndUserRoleId(user.getInstitution().getUid(), RECV_ALL_INST_INVS.getId());

        if ( invitationRecipients.size() > 1 || !invitationRecipients.get(0).getId().equals(user.getId()) ){
            userRoleDao.deleteByUserId(userId);
        } else {
            throw new OperationNotAllowedException("There must be at least one user that is a deal recipient for this institution");
        }
    }

    public EndUserAgreement getEndUserAgreementByUser(User user){
        Institution institution = institutionDao.findByUid(user.getInstitution().getUid());
        return endUserAgreementDao.findEndUserAgreementByBillingCode(institution.getBillingCode().getCode());
    }

    public EndUserAgreement getSignedEndUserAgreementByUser(User user){
        UserEua userEua = userEuaDao.findUserEuaByUserId(user.getId());
        return userEua.getEndUserAgreement();
    }

    public boolean agreeToEndUserAgreement(User user, Integer euaId){
        return userEuaDao.save(user, euaId);
    }

    @Override
    @Cacheable
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user;

        try {

            user = getUserByEmail(username, true);

            if ( "N".equals(user.getActive()) ) {
                throw new AuthorizationException("The user account has been deactivated.");
            }

        } catch ( DataNotFoundException e ) {
            throw new UsernameNotFoundException("The user was not found.");
        }

        return user;
    }
}