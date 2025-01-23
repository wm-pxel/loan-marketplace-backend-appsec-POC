package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.ValidationException;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.service.InstitutionService;
import com.westmonroe.loansyndication.service.UserService;
import com.westmonroe.loansyndication.utils.ModelUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.*;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class DealServiceTest {

    @Autowired
    InstitutionService institutionService;

    @Autowired
    DealService dealService;

    @Autowired
    UserService userService;

    @MockBean
    private SecurityContext securityContext;

    @Test
    void givenExistingDealsInDatabase_whenGettingAll_thenVerifySize() {

        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        List<Deal> deals = dealService.getAllDealsByInstitutionId(1L, currentUser);
        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).getName()).isEqualTo("Texas Dairy Farm");
    }

    @Test
    void givenExistingDealsInDatabase_whenRetrievingByDummyUid_thenVerifyException() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        assertThatThrownBy(() -> dealService.getDealByUid(TEST_DUMMY_UUID, currentUser))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void givenExistingDealInDatabase_whenRetrievingDealEventForUser_thenVerifyException() {

        // Create the current user (Lenor Anderson) object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Lenor is a participant in this deal.
        DealEvent dealEvent = dealService.getDealEventByUid(TEST_DEAL_UUID_1, currentUser);
        assertThat(dealEvent)
            .isNotNull()
            .hasFieldOrPropertyWithValue("relation", PARTICIPANT.getDescription())
            .hasFieldOrPropertyWithValue("viewType", VIEW_TYPE_SUMMARY)
            .hasFieldOrPropertyWithValue("memberTypeCode", PARTICIPANT.getCode())
            .hasFieldOrPropertyWithValue("partInstUserFlag", "Y");

        // Lenor is the originating institution for this deal.
        dealEvent = dealService.getDealEventByUid(TEST_DEAL_UUID_2, currentUser);
        assertThat(dealEvent)
            .isNotNull()
            .hasFieldOrPropertyWithValue("relation", ORIGINATOR.getDescription())
            .hasFieldOrPropertyWithValue("viewType", VIEW_TYPE_FULL)
            .hasFieldOrPropertyWithValue("memberTypeCode", ORIGINATOR.getCode())
            .hasFieldOrPropertyWithValue("partInstUserFlag", "N");
    }

    @Test
    void givenExistingDealsInDatabase_whenSavingDealWithExistingDealName_thenVerifyException() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        Deal deal = dealService.getDealByUid(TEST_DEAL_UUID_2, currentUser);

        Deal testDeal = ModelUtil.createTestDeal(1);
        testDeal.setOriginator(deal.getOriginator());
        testDeal.setDealExternalId(deal.getDealExternalId());
        testDeal.setName(deal.getName());

        // First exception should be the Deal External ID.
        assertThatThrownBy(() -> dealService.save(testDeal, currentUser, SYSTEM_MARKETPLACE))
                .hasMessage("The External Id value already exists and must be unique.");

        // Change the Deal External ID because we want to test the Deal Name.
        testDeal.setDealExternalId("XXXXXXX-XXXXXXXX-XXXXXXXX-XXXX");

        // Second exception should be Deal Name
        assertThatThrownBy(() -> dealService.save(testDeal, currentUser, SYSTEM_MARKETPLACE))
                .hasMessageContaining("The Deal Name value already exists and must be unique.");
    }

    @Test
    @Transactional
    void givenIncompleteDeal_whenSaving_thenVerifyException() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Simple test to make sure null deal is handled.
        assertThatThrownBy(() -> dealService.save(null, currentUser, SYSTEM_MARKETPLACE)).isInstanceOf(ValidationException.class);

        // Create a test institution and stage for the deal.
        Institution originator = institutionService.save(ModelUtil.createTestInstitution(1));

        Deal deal = ModelUtil.createTestDeal(7);

        // Deal is missing the originator, so an exception is thrown.
        assertThatThrownBy(() -> dealService.save(deal, currentUser, SYSTEM_MARKETPLACE)).isInstanceOf(ValidationException.class);
    }

    @Test
    @Transactional
    void givenNewDeal_whenSavedAndGetIdByUid_thenReturnId() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Create a test institution and stage for the deal.
        Institution institution = institutionService.save(ModelUtil.createTestInstitution(1));

        Deal deal = ModelUtil.createTestDeal(1);
        deal.setOriginator(institution);
        dealService.save(institution.getUid(), deal, currentUser, SYSTEM_MARKETPLACE);

        Deal savedDeal = dealService.getDealByUid(deal.getUid(), currentUser);
        assertThat(savedDeal.getId()).isEqualTo(deal.getId());
        assertThat(savedDeal.getOriginator().getUid()).isEqualTo(institution.getUid());
    }

    @Test
    void givenDealsInDatabase_whenRetrievingNonExistentDeals_thenVerifyException() {

        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        assertThatThrownBy(() -> dealService.getDealById(5L, currentUser))
                .isInstanceOf(DataNotFoundException.class);

        assertThatThrownBy(() -> dealService.getDealByUid(TEST_DUMMY_UUID, currentUser))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    @Transactional
    void givenNewDeal_whenSaved_thenVerify() {

        // Create a test institution and stage for the deal.
        Institution institution = institutionService.save(ModelUtil.createTestInstitution(3));

        // Create a user for the institution and a test token for the user.
        User currentUser = createUser(institution.getUid(), "Test", "User", "", "", "Y");

        // Verify there are no deals.
        List<Deal> deals = dealService.getAllDealsByInstitutionUid(institution.getUid(), currentUser);
        assertThat(deals).isEmpty();

        // Create and save a new deal.
        Deal deal = ModelUtil.createTestDeal(3);
        deal.setOriginator(institution);
        dealService.save(deal, currentUser, SYSTEM_MARKETPLACE);

        // Get all of the deals and verify that it was saved.
        deals = dealService.getAllDealsByInstitutionUid(institution.getUid(), currentUser);
        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).getId()).isNotNull();

        // Get the deal by the id and verify it matches the saved object.
        Deal savedDeal = dealService.getDealById(deal.getId(), currentUser);
        assertThat(deal.getName()).isEqualTo(savedDeal.getName());
        assertThat(deal.getOriginator().getUid()).isEqualTo(savedDeal.getOriginator().getUid());
        assertThat(deal.getActive()).isEqualTo(savedDeal.getActive());

        // Get the deal by the UUID and verify it matches the saved object.
        savedDeal = dealService.getDealByUid(deal.getUid(), currentUser);
        assertThat(deal.getName()).isEqualTo(savedDeal.getName());
        assertThat(deal.getActive()).isEqualTo(savedDeal.getActive());
    }

    @Test
    @Transactional
    void givenExistingDeal_whenUpdated_thenVerifyChange() {

        String newDealName = "New Test Deal Name";

        // Create the current user onject.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Create a test institution and stage for the deal.
        Institution institution = institutionService.save(ModelUtil.createTestInstitution(4));

        // Create and save a new deal.
        Deal deal = ModelUtil.createTestDeal(4);
        deal.setOriginator(institution);
        deal.setActive("Y");
        dealService.save(deal, currentUser, SYSTEM_MARKETPLACE);

        // Get the saved deal.  Right now deal and savedDeal should be the same.
        Deal savedDeal = dealService.getDealByUid(deal.getUid(), currentUser);
        assertThat(deal.getName()).isEqualTo(savedDeal.getName());
        assertThat(deal.getOriginator().getUid()).isEqualTo(savedDeal.getOriginator().getUid());
        assertThat(deal.getActive()).isEqualTo(savedDeal.getActive());

        // Update the deal before saving.
        deal.setName(newDealName);
        deal.setActive("N");
        dealService.update(deal, currentUser);

        Deal updatedDeal = dealService.getDealById(savedDeal.getId(), currentUser);
        assertThat(updatedDeal.getName()).isEqualTo(newDealName);
        assertThat(deal.getOriginator().getUid()).isEqualTo(savedDeal.getOriginator().getUid());
        assertThat(deal.getActive()).isNotEqualTo(savedDeal.getActive());
    }

    @Test
    @Transactional
    void givenExistingDeal_whenDeleted_thenVerifyRemoval() {

        // Create a test institution and stage for the deal.
        Institution institution = institutionService.save(ModelUtil.createTestInstitution(5));

        // Create a user for the institution and a test token for the user.
        User currentUser = createUser(institution.getUid(), "Test", "User", "", "", "Y");

        // Verify the initial deal count in the DB.
        assertThat(dealService.getAllDealsByInstitutionUid(institution.getUid(), currentUser)).isEmpty();

        // Create and save a new deal then verify count.
        Deal deal = ModelUtil.createTestDeal(5);
        deal.setOriginator(institution);
        dealService.save(deal, currentUser, SYSTEM_MARKETPLACE);
        assertThat(dealService.getAllDealsByInstitutionUid(institution.getUid(), currentUser)).hasSize(1);

        // Delete the deal by uid and verify the removal.
        dealService.deleteById(deal.getId());
        assertThat(dealService.getAllDealsByInstitutionUid(institution.getUid(), currentUser)).isEmpty();
    }

    @Test
    void givenExistingDeals_whenDeletingNonExistentDeal_thenVerifyNoError() {

        assertThatNoException().isThrownBy(() -> { dealService.deleteById(99L); });

        assertThatNoException().isThrownBy(() -> {
            dealService.deleteByUid(TEST_DEAL_UUID_2);
        });
    }

    private User createUser(String institutionUid, String firstName, String lastName, String email
            , String password, String active) {

        /*
         *  Create the test user.
         */
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setActive(active);

        Institution institution = new Institution();
        institution.setUid(institutionUid);

        user.setInstitution(institution);

        // Save the test user.
        userService.save(user);

        return user;
    }

}