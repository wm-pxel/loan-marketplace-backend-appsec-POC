package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.exception.AuthorizationException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealMember;
import com.westmonroe.loansyndication.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class DealMemberServiceTest {

    @Autowired
    private DealMemberService dealMemberService;

    @Autowired
    private UserService userService;

    @Test
    void givenExistingDealMembers_whenGettingMember_thenVerify() {

        DealMember member = dealMemberService.getDealMemberByDealUidAndUserUid(TEST_DEAL_UUID_2, TEST_USER_UUID_1);
        assertThat(member)
            .isNotNull()
            .hasFieldOrPropertyWithValue("memberTypeDesc", ORIGINATOR.getDescription());
    }

    @Test
    void givenExistingDealMembers_whenGettingInvalidMember_thenVerify() {

        assertThatThrownBy(() -> dealMemberService.getDealMemberByDealUidAndUserUid(TEST_DEAL_UUID_2, TEST_DUMMY_UUID))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void givenExistingDealMembers_whenGettingMembers_thenVerifySize() {

        // This is the method that SUPER_ADM users would use.
        List<DealMember> members = dealMemberService.getDealMembersByDealUid(TEST_DEAL_UUID_2);
        assertThat(members).hasSize(2);
    }

    @Test
    void givenExistingDealMembers_whenGettingMembersForDealAndInstitution_thenVerifySize() {

        // This is the method that normal users would use.
        List<DealMember> members = dealMemberService.getDealMembersByDealUidAndInstitutionUid(TEST_DEAL_UUID_2, TEST_INSTITUTION_UUID_2);
        assertThat(members).hasSize(2);
    }

    @Test
    void givenExistingDealMembers_whenGettingMembersForDealAndCurrentUser_thenVerifySize() {

        // Create the current user object.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        List<DealMember> members = dealMemberService.getDealMembersByDealUid(TEST_DEAL_UUID_2, currentUser);
        assertThat(members).hasSize(2);
    }

    @Test
    @Transactional
    void givenNewDealMembers_whenPerformingCrudOperations_thenVerify() {

        /*
         *  Test Deal: Texas Dairy Farm
         */

        /*
         *  Add an originator member to the deal.
         */
        String origUserUid = TEST_USER_UUID_2;                          // Annie Palinto

        // Get the creating user.
        User creatingOrigUser = userService.getUserByUid(origUserUid);

        // Create a new deal member object.
        DealMember origDealMember = new DealMember();
        origDealMember.setMemberTypeCode(ORIGINATOR.getCode());

        Deal deal = new Deal();
        deal.setUid(TEST_DEAL_UUID_1);
        origDealMember.setDeal(deal);

        User user = new User();
        user.setUid(origUserUid);
        origDealMember.setUser(user);

        // Add a new member to a deal.
        dealMemberService.save(origDealMember, creatingOrigUser, SYSTEM_MARKETPLACE, false);

        // Verify that the size increased by one.
        List<DealMember> members = dealMemberService.getDealMembersByDealUid(TEST_DEAL_UUID_1, creatingOrigUser);
        assertThat(members).hasSize(2);

        // Verify that the new member is an originator.
        DealMember member = dealMemberService.getDealMemberByDealUidAndUserUid(TEST_DEAL_UUID_1, origUserUid, creatingOrigUser, false);
        assertThat(member.getDeal().getUid()).isEqualTo(TEST_DEAL_UUID_1);
        assertThat(member.getUser().getUid()).isEqualTo(origUserUid);
        assertThat(member.getMemberTypeDesc()).isEqualTo(ORIGINATOR.getDescription());

        /*
         *  Add a participant member to the deal.
         */
        String partUserUid = TEST_USER_UUID_3;                          // Leon T. (Tim) Amerson

        // Get the creating user.
        User creatingPartUser = userService.getUserByUid(partUserUid);

        // Create a new deal member object.
        DealMember partDealMember = new DealMember();
        partDealMember.setMemberTypeCode(PARTICIPANT.getCode());

        partDealMember.setDeal(deal);

        user = new User();
        user.setUid(partUserUid);
        partDealMember.setUser(user);

        // Add a new member to a deal.
        dealMemberService.save(partDealMember, creatingPartUser, SYSTEM_MARKETPLACE, false);

        // Verify that the size increased by one.
        members = dealMemberService.getDealMembersByDealUid(TEST_DEAL_UUID_1, creatingPartUser);
        assertThat(members).hasSize(3);

        // Verify that the new member is an originator.
        member = dealMemberService.getDealMemberByDealUidAndUserUid(TEST_DEAL_UUID_1, partUserUid, creatingPartUser, false);
        assertThat(member.getDeal().getUid()).isEqualTo(TEST_DEAL_UUID_1);
        assertThat(member.getUser().getUid()).isEqualTo(partUserUid);
        assertThat(member.getMemberTypeDesc()).isEqualTo(PARTICIPANT.getDescription());

        /*
         *  Delete the deal member from the originating institution.
         */
        dealMemberService.delete(origDealMember, creatingOrigUser, SYSTEM_MARKETPLACE);

        // Verify that the size decreased by one.
        members = dealMemberService.getDealMembersByDealUid(TEST_DEAL_UUID_1, creatingOrigUser);
        assertThat(members).hasSize(1);

    }

    @Test
    void givenExistingDealMembers_whenSavingInvalidMember_thenVerifyException() {

        /*
         *  Create scenario where deal member and current user are in different institutions.
         */
        Deal deal = new Deal();
        deal.setUid("6f865256-e16e-441a-b495-bfb6ea856623");                                    // Texas Dairy Farm (Farm Credit Bank of Texas)
        User user = userService.getUserByUid("429a53d3-17af-4be1-bb82-44f48ae1e74e");           // Frank Bank (AgFirst Farm Credit Bank)
        User currentUser = userService.getUserByUid("16f0545d-f1ce-4c2d-be90-2d8bef9af8fe");    // Annie Palinto (Farm Credit Bank of Texas)

        DealMember dealMember = new DealMember(deal, user, ORIGINATOR.getCode(), ORIGINATOR.getDescription(), null, null, null);

        assertThatThrownBy(() -> dealMemberService.save(dealMember, currentUser, SYSTEM_MARKETPLACE, false))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @Transactional
    void givenExistingDealMembers_whenDeletingAllMembersForInstitution_thenVerifyException() {

        // Get the user object for a user in the test institution.
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        // Get the list of test institutions.  Should only be two in our test.
        List<DealMember> dealMembers = dealMemberService.getDealMembersByDealUidAndInstitutionUid(TEST_DEAL_UUID_2, TEST_INSTITUTION_UUID_2);
        assertThat(dealMembers).hasSize(2);

        // Delete the first member without generating an exception.
        dealMemberService.delete(dealMembers.get(0), currentUser, SYSTEM_MARKETPLACE);

        // Deleting the second and last for the institution should generate an exception.
        assertThatThrownBy(() -> dealMemberService.delete(dealMembers.get(1), currentUser, SYSTEM_MARKETPLACE))
                .isInstanceOf(OperationNotAllowedException.class);
    }

}