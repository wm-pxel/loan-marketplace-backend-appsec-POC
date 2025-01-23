package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.AwsCognitoException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ProviderData;
import com.westmonroe.loansyndication.utils.ModelUtil;
import com.westmonroe.loansyndication.utils.TestConstants;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@Testcontainers
class InstitutionServiceTest {

    @Autowired
    InstitutionService institutionService;

    @MockBean
    private AwsService awsService;

    @BeforeEach
    void setUp(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void givenExistingInstitutionsInDatabase_whenGettingAll_thenVerifySize() {

        List<Institution> institutions = institutionService.getAllInstitutions();
        assertThat(institutions).hasSize(4);
    }

    @Test
    void  givenDealExternalId_whenGettingInstitution_thenVerify() {

        // Deal External Id for Kentucky Processing Plant
        Institution institution = institutionService.getInstitutionByDealExternalId("b86517b4-0693-4ec6-b880-06de4c0507f3");

        assertThat(institution)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", 2L)
            .hasFieldOrPropertyWithValue("name", "AgFirst Farm Credit Bank")
            .hasFieldOrPropertyWithValue("owner", "Leon T. (Tim) Amerson")
            .hasFieldOrPropertyWithValue("dealCount", 4L)
            .hasFieldOrPropertyWithValue("memberCount", 5L);
    }

    @Test
    void givenExistingInstitutionsInDatabase_whenRetrievingByDummyUid_thenVerifyException() {

        assertThatThrownBy(() -> institutionService.getInstitutionByUid(TestConstants.TEST_DUMMY_UUID))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewInstitution_whenSavedAndGetIdByUid_thenReturnId() {

        Institution institution = ModelUtil.createTestInstitution(5);
        institutionService.save(institution);

        Institution savedInstitution = institutionService.getInstitutionByUid(institution.getUid());
        assertThat(institution.getId()).isEqualTo(savedInstitution.getId());
    }

    @Test
    void givenNoInstitutionsInDatabase_whenRetrievingNonExistentInstitution_thenVerifyException() {

        assertThatThrownBy(() -> institutionService.getInstitutionById(99L))
                .isInstanceOf(DataNotFoundException.class);

        assertThatThrownBy(() -> institutionService.getInstitutionByUid(TestConstants.TEST_DUMMY_UUID))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewInstitution_whenSaved_thenVerify() {

        // Verify there are four initial institutions.
        List<Institution> institutions = institutionService.getAllInstitutions();
        assertThat(institutions).hasSize(4);

        // Create and save a new institution.
        Institution institution = ModelUtil.createTestInstitution(1);
        institutionService.save(institution);

        // Get all of the institutions and verify that it was saved.
        institutions = institutionService.getAllInstitutions();
        assertThat(institutions).hasSize(5);
    }

    @Test
    @Transactional
    void givenExistingInstitution_whenUpdated_thenVerifyChange() {

        String newInstitutionName = "New Test Institution Name";

        // Create and save a new institution.
        Institution institution = ModelUtil.createTestInstitution(2);
        institutionService.save(institution);

        // Get the saved institution.  Right now institution and savedInstitution should be the same.
        Institution savedInstitution = institutionService.getInstitutionByUid(institution.getUid());
        assertThat(institution.getId()).isEqualTo(savedInstitution.getId());
        assertThat(institution.getName()).isEqualTo(savedInstitution.getName());
        assertThat(institution.getBrandName()).isEqualTo(savedInstitution.getBrandName());

        // Update the institution before saving.
        institution.setName(newInstitutionName);
        institutionService.update(institution);

        Institution updatedInstitution = institutionService.getInstitutionById(savedInstitution.getId());
        assertThat(updatedInstitution.getName()).isEqualTo(newInstitutionName);
    }

    @Test
    @Transactional
    void givenExistingInstitution_whenDeleted_thenVerifyRemoval() {

        // Verify the initial institution count in the DB.
        List<Institution> institutions = institutionService.getAllInstitutions();
        assertThat(institutions).hasSize(4);

        // Create and save a new institution then verify count.
        Institution institution = ModelUtil.createTestInstitution(4);
        institutionService.save(institution);

        institutions = institutionService.getAllInstitutions();
        assertThat(institutions).hasSize(5);

        // Delete the institution and verify the removal.
        institutionService.deleteById(institution.getId());

        institutions = institutionService.getAllInstitutions();
        assertThat(institutions).hasSize(4);
    }

    @Test
    void givenExistingInstitutionsInDatabase_whenDeletingNonExistentInstitution_thenVerifyNoError() {
        assertThatThrownBy(() -> institutionService.deleteByUid(TestConstants.TEST_DUMMY_UUID))
            .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void givenAnInvalidEmail_whenRetrievingProviderData_thenShouldReturnFalseSsoIndAndNullProviderName() {
        ProviderData providerData = institutionService.getProviderData("user-does-not-exist@example.com");
        assertThat(providerData.getSsoIndicator()).isFalse();
        assertThat(providerData.getProviderName()).isNull();
    }

    @Test
    void givenAValidEmailAndValidIdp_whenRetrievingProviderData_thenShouldReturnFalseSsoIndAndValidProviderName() {
        when(awsService.getIdentityProviderForEmail("lm.amie.pala@outlook.com")).thenReturn("TEST_PROVIDER_NAME");

        ProviderData providerData = institutionService.getProviderData("lm.amie.pala@outlook.com");

        assertThat(providerData.getSsoIndicator()).isTrue();
        assertThat(providerData.getProviderName()).isEqualTo("TEST_PROVIDER_NAME");
    }

    @Test
    void givenAValidEmailAndInvalidIdp_whenRetrievingProviderData_thenShouldReturnFalseSsoIndAndValidProviderName() {
        doThrow(new AwsCognitoException("Error retrieving idp for identifier"))
                .when(awsService).getIdentityProviderForEmail("lm.amie.pala@outlook.com");
        ProviderData providerData = institutionService.getProviderData("lm.amie.pala@outlook.com");

        assertThat(providerData.getSsoIndicator()).isFalse();
        assertThat(providerData.getProviderName()).isNull();
    }
}