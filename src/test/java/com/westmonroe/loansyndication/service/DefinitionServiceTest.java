package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.event.EventType;
import com.westmonroe.loansyndication.utils.ModelUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class DefinitionServiceTest {

    @Autowired
    DefinitionService definitionService;

    @Test
    void givenNoRolesInDatabase_whenGettingAll_thenReturnEmptySet() {

        List<Role> roles = definitionService.getAllRoles();
        assertThat(roles).hasSize(14);
    }

    @Test
    void givenNoRolesInDatabase_whenGettingRoleById_thenVerifyException() {

        assertThatThrownBy(() -> definitionService.getRoleById(99L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewRole_whenSavedAndGetRoleById_thenVerifySuccess() {

        String code = "TEST_ROLE_2";
        String name = "Test Role 2";
        String description = "Test Description 2";

        Role role = ModelUtil.createTestRole(2, code, name, description);
        definitionService.saveRole(role);

        Role savedRole = definitionService.getRoleById(role.getId());
        assertThat(savedRole.getCode()).isEqualTo(code);
        assertThat(savedRole.getName()).isEqualTo(name);
        assertThat(savedRole.getDescription()).isEqualTo(description);
    }

    @Test
    @Transactional
    void givenNewRole_whenUpdatedAndSaved_thenVerifyChange() {

        String code = "TEST_ROLE_3";
        String name = "Test Role 3";
        String description = "Test Description 3";

        Role role = ModelUtil.createTestRole(3, code, name, description);
        role = definitionService.saveRole(role);

        String updateName = "Test Role 4";
        String updatedDescription = "Test Description 4";

        // Update the role values and perform the update in the database.
        role.setName(updateName);
        role.setDescription(updatedDescription);
        definitionService.updateRole(role);

        Role updatedRole = definitionService.getRoleById(role.getId());
        assertThat(updatedRole.getName()).isEqualTo(updateName);
        assertThat(updatedRole.getDescription()).isEqualTo(updatedDescription);
    }

    @Test
    @Transactional
    void givenExistingRole_whenDeleted_thenVerifyRemoval() {

        // Verify the initial role count in the DB.
        List<Role> roles = definitionService.getAllRoles();
        assertThat(roles).hasSize(14);

        // Create and save a new role then verify count.
        Role role = ModelUtil.createTestRole(15, null, null, null);
        definitionService.saveRole(role);

        roles = definitionService.getAllRoles();
        assertThat(roles).hasSize(15);

        // Delete the role by uid and verify the removal.
        definitionService.deleteRole(role.getId());

        roles = definitionService.getAllRoles();
        assertThat(roles).hasSize(14);
    }

    @Test
    void givenNoRolesInDatabase_whenDeletingNonExistentRole_thenVerifyNoError() {

        assertThatNoException().isThrownBy(() -> { definitionService.deleteRole(99L); });
    }

    @Test
    void givenExistingEventTypes_whenGettingAllAndOne_thenVerifySizeAndType() {

        List<EventType> eventTypes = definitionService.getEventTypes();
        assertThat(eventTypes).hasSize(4);

        EventType eventType = definitionService.getEventTypeById(1L);
        assertThat(eventType)
            .isNotNull()
            .hasFieldOrPropertyWithValue("name", "Origination");
    }

}