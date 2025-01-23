package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.utils.ModelUtil;
import com.westmonroe.loansyndication.utils.TestConstants;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static com.westmonroe.loansyndication.utils.RoleDefEnum.RECV_ALL_INST_INVS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@Testcontainers
class UserServiceTest {

    @Autowired
    InstitutionService institutionService;

    @Autowired
    UserService userService;

    @Autowired
    DefinitionService definitionService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenExistingUsersInDatabase_whenGettingAll_thenVerifySize() {

        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(16);
    }

    void givenExistingUsersInDatabase_whenGettingAllNonSystem_thenVerifySize() {

        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(12);
    }

    @Test
    void givenExistingUsersInDatabase_whenGettingIdByUid_thenVerifyException() {

        assertThatThrownBy(() -> userService.getUserIdByInstitutionUidAndUserUid(TestConstants.TEST_INSTITUTION_UUID_2, TestConstants.TEST_DUMMY_UUID))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewUser_whenSavedAndGetIdByUid_thenReturnId() {

        // Need to create an institution that he user can be associated.
        Institution institution = ModelUtil.createTestInstitution(1);
        institution = institutionService.save(institution);

        User user = ModelUtil.createTestUser(5);
        user.setInstitution(institution);
        userService.save(user);

        Long userId = userService.getUserIdByInstitutionUidAndUserUid(institution.getUid(), user.getUid());
        assertThat(user.getId()).isEqualTo(userId);
    }

    @Test
    void givenNoUsersInDatabase_whenRetrievingNonExistentUser_thenVerifyException() {

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(DataNotFoundException.class);

        assertThatThrownBy(() -> userService.getUserByUid(TestConstants.TEST_DUMMY_UUID))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewUser_whenSaved_thenVerify() {

        // Verify there are no users.
        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(16);

        // Create a test institution for associating users.
        Institution institution = ModelUtil.createTestInstitution(2);
        institutionService.save(institution);

        // Create and save a new user.
        User user = ModelUtil.createTestUser(1);
        user.setInstitution(institution);
        userService.save(user);

        // Get all of the users and verify that it was saved.
        users = userService.getAllUsers();
        assertThat(users).hasSize(17);

        // Get the user by the id and verify it matches the saved object.
        User savedUser = userService.getUserById(user.getId());
        assertThat(user.getUid()).isEqualTo(savedUser.getUid());
        assertThat(user.getFirstName()).isEqualTo(savedUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(savedUser.getLastName());
        assertThat(user.getActive()).isEqualTo(savedUser.getActive());

        // Get the user by the UUID and verify it matches the saved object.
        savedUser = userService.getUserByUid(user.getUid());
        assertThat(user.getId()).isEqualTo(savedUser.getId());
        assertThat(user.getFirstName()).isEqualTo(savedUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(savedUser.getLastName());
        assertThat(user.getActive()).isEqualTo(savedUser.getActive());
    }

    @Test
    @Transactional
    void givenNewUsers_whenSaved_thenVerifyCountByInstitution() {

        // Create a test institution for associating users.
        Institution institution = ModelUtil.createTestInstitution(6);
        institutionService.save(institution);

        // Create and save a new user.
        User user = ModelUtil.createTestUser(6);
        user.setInstitution(institution);
        userService.save(user);

        // Create another test user and save it.
        user = ModelUtil.createTestUser(7);
        user.setInstitution(institution);
        userService.save(user);

        // Create a third test user and save it.
        user = ModelUtil.createTestUser(8);
        user.setInstitution(institution);
        userService.save(user);

        // Get all of the users for the institution.
        List<User> users = userService.getUsersForInstitutionId(institution.getId());
        assertThat(users).hasSize(3);
    }

    @Test
    @Transactional
    void givenNewUserWithDealInvRecipRole_whenSaved() {
        // Create a test institution for associating users.
        Institution institution = ModelUtil.createTestInstitution(6);
        institutionService.save(institution);

        institution = institutionService.getInstitutionById(institution.getId());

        // Create and save a new user.
        User user = ModelUtil.createTestUser(6);
        user.setRoles(Arrays.asList(new Role(RECV_ALL_INST_INVS.getId())));
        user.setInstitution(institution);
        userService.save(user);

        List<User> users = userService.getUsersByInstitutionUidAndUserRoleId(institution.getUid(), RECV_ALL_INST_INVS.getId());
        assertThat(users).hasSize(1);

        user = ModelUtil.createTestUser(7);
        user.setInstitution(institution);
        userService.save(user);

        users = userService.getUsersByInstitutionUidAndUserRoleId(institution.getUid(), RECV_ALL_INST_INVS.getId());
        assertThat(users).hasSize(1);

        users = userService.getUsersForInstitutionId(institution.getId());
        assertThat(users).hasSize(2);
    }

    @Test
    @Transactional
    void givenExistingUser_whenUpdated_thenVerifyChange() {

        String newFirstName = "New Test First Name";
        String newLastName = "New Test Last Name";

        // Create a test institution for associating users.
        Institution institution = ModelUtil.createTestInstitution(3);
        institutionService.save(institution);

        // Create and save a new user.
        User user = ModelUtil.createTestUser(2);
        user.setInstitution(institution);
        userService.save(user);

        // Get the saved user.  Right now user and savedUser should be the same.
        User savedUser = userService.getUserByUid(user.getUid());
        assertThat(user.getUid()).isEqualTo(savedUser.getUid());
        assertThat(user.getFirstName()).isEqualTo(savedUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(savedUser.getLastName());

        // Update the new user values before saving.
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);
        userService.update(user);

        User updatedUser = userService.getUserById(savedUser.getId());
        assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
    }

    @Test
    @Transactional
    void givenExistingUser_whenDeleted_thenVerifyRemoval() {

        // Verify the initial user count in the DB.
        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(16);

        // Create a test institution for associating users.
        Institution institution = ModelUtil.createTestInstitution(4);
        institutionService.save(institution);

        // Create and save a new user then verify count.
        User user = ModelUtil.createTestUser(4);
        user.setInstitution(institution);
        userService.save(user);

        // Create additional user with DEAL_INV_RECIP permissions
        User recipientUser = ModelUtil.createTestUser(5);
        recipientUser.setInstitution(institution);
        userService.save(recipientUser);
        userService.saveRoleForUser(recipientUser, new Role(RECV_ALL_INST_INVS.getId()));

        users = userService.getAllUsers();
        assertThat(users).hasSize(18);

        // Delete the user by uid and verify the removal.
        userService.deleteById(user.getId());

        users = userService.getAllUsers();
        assertThat(users).hasSize(17);
    }

    @Test
    void givenNoUsersInDatabase_whenDeletingNonExistentUser_thenVerifyNoError() {
        assertThatThrownBy(() -> userService.deleteByUid(TestConstants.TEST_DUMMY_UUID)).isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewUser_whenInsertingAndDeletingRoles_thenVerify() {

        // Verify the initial user count in the DB.
        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(16);

        // Create a test institution for associating users.
        Institution institution = ModelUtil.createTestInstitution(4);
        institutionService.save(institution);

        // Create and save a new user then verify count.
        User user = ModelUtil.createTestUser(12);
        user.setInstitution(institution);
        userService.save(user);

        User recipientUser = ModelUtil.createTestUser(13);
        recipientUser.setInstitution(institution);
        userService.save(recipientUser);
        userService.saveRoleForUser(recipientUser, new Role(RECV_ALL_INST_INVS.getId()));

        users = userService.getAllUsers();
        assertThat(users).hasSize(18);

        // Add roles for testing.
        Role role1 = definitionService.saveRole(ModelUtil.createTestRole(1, null, null, null));
        Role role2 = definitionService.saveRole(ModelUtil.createTestRole(2, null, null, null));
        Role role3 = definitionService.saveRole(ModelUtil.createTestRole(3, null, null, null));

        // Add the roles to our test user.
        userService.saveRoleForUser(user, role1);
        userService.saveRoleForUser(user, role2);
        userService.saveRoleForUser(user, role3);

        // Get user and roles.
        User savedUser = userService.getUserById(user.getId(), true);
        assertThat(savedUser.getRoles()).hasSize(3);

        // Delete one role from the user.
        userService.deleteRoleForUser(savedUser, role2);

        // Get the updated user.
        User updatedUser = userService.getUserById(user.getId(), true);
        assertThat(updatedUser.getRoles()).hasSize(2);

        // Delete the user by uid and verify the removal.
        userService.deleteById(user.getId());

        users = userService.getAllUsers();
        assertThat(users).hasSize(17);
    }
}