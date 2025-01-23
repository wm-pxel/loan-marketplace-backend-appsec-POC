package com.westmonroe.loansyndication.service.deal;

import com.westmonroe.loansyndication.dao.UserDao;
import com.westmonroe.loansyndication.dao.deal.DealDao;
import com.westmonroe.loansyndication.dao.deal.DealMemberDao;
import com.westmonroe.loansyndication.exception.AuthorizationException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DuplicateDataException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealMember;
import com.westmonroe.loansyndication.model.deal.DealMembers;
import com.westmonroe.loansyndication.service.ActivityService;
import com.westmonroe.loansyndication.service.EmailService;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.TEAM_MEMBER_ADDED;
import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.TEAM_MEMBER_REMOVED;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.UNKNOWN;

@Service
@Slf4j
public class DealMemberService {
    @Value("${lamina.email-service.send-address}")
    private String sendAddress;


    private final DealMemberDao dealMemberDao;
    private final DealDao dealDao;
    private final UserDao userDao;
    private final ActivityService activityService;
    private final EmailService emailService;

    public DealMemberService(DealMemberDao dealMemberDao, DealDao dealDao, UserDao userDao, ActivityService activityService, EmailService emailService) {
        this.dealMemberDao = dealMemberDao;
        this.dealDao = dealDao;
        this.userDao = userDao;
        this.activityService = activityService;
        this.emailService = emailService;
    }

    private DealMember getDealMember(String dealUid, String userUid, User createdBy) {

        DealMember dealMember = new DealMember();

        // Get the components of the deal member and return the full object.
        dealMember.setDeal(dealDao.findByUid(dealUid, createdBy));
        dealMember.setUser(userDao.findByUid(userUid));

        // Set the deal member type code and description, along with the createdBy user.
        dealMember.setMemberTypeCode(dealMember.getDeal().getRelation().substring(0, 1));
        dealMember.setMemberTypeDesc(dealMember.getDeal().getRelation());
        dealMember.setCreatedBy(createdBy);

        return dealMember;
    }

    public DealMember getDealMemberByDealUidAndUserUid(String dealUid, String userUid) {
        return dealMemberDao.findByDealUidAndUserUid(dealUid, userUid);
    }

    public DealMember getDealMemberByDealUidAndUserUid(String dealUid, String userUid, User currentUser, boolean isAdmin) {

        // Get the full deal member details.
        DealMember dealMember = getDealMember(dealUid, userUid, currentUser);

        // If the current user is NOT and a "SUPER_ADM" then verify that the current user can still access the deal member.
        if ( !isAdmin ) {
            verifyDealMemberAccess(dealMember, currentUser);
        }

        return getDealMemberByDealUidAndUserUid(dealUid, userUid);
    }

    public List<DealMember> getDealMembersByDealUid(String dealUid) {
        return dealMemberDao.findAllByDealUid(dealUid);
    }

    public List<DealMember> getDealMembersByDealUid(String dealUid, User currentUser) {
        return dealMemberDao.findAllByDealUidAndInstitutionUid(dealUid, currentUser.getInstitution().getUid());
    }

    public List<DealMember> getDealMembersByDealUidAndInstitutionUid(String dealUid, String institutionUid) {
        return dealMemberDao.findAllByDealUidAndInstitutionUid(dealUid, institutionUid);
    }

    /**
     * This method saves a member to the Deal.  Members are stored in a separate table and are explicitly provided
     * access to the Deal, regardless of role.
     *
     * There is a special case where the lead (who is not a user in the same institution) can initiate the save.  This
     * occurs when the lead is inviting the participant and specifies a primary user.  The primary user will automatically
     * be added as a deal member, hence the leadInitiated flag.
     *
     * @param dealMember    DealMember object that contains the Deal, Participant and primary user.
     * @param currentUser   The User object of the current user (i.e. user that is initiating the save).
     * @param source        The source system where the save was initiated.
     * @param leadInitiated This flag is to indicate the special case where the lead is adding a primary user.
     *
     * @return
     */
    public DealMember save(DealMember dealMember, User currentUser, String source, boolean leadInitiated) {

        /*
         *  Check if the member was already added to the deal.
         */
        try {

            // Get the deal member record.
            DealMember existingDealMember = dealMemberDao.findByDealUidAndUserUid(dealMember.getDeal().getUid(), dealMember.getUser().getUid());

            // If an exception wasn't thrown on the previous line then the member is already a deal member.
            throw new DuplicateDataException("The deal member already exists.");

        } catch ( DataNotFoundException e ) {
            // We expect the member to NOT be a deal member, so there is no need to take any further action.
        }

        // Perform validation when the lead didn't initiate the save.  Note: Can only happen in Marketplace.
        if ( source.equals(SYSTEM_MARKETPLACE) && !leadInitiated ) {

            // Get the full deal and user objects, as we need the unique generated ids for the save.
            dealMember = getDealMember(dealMember.getDeal().getUid(), dealMember.getUser().getUid(), currentUser);

            // Verify that we can save this member.
            verifyDealMemberSave(dealMember, currentUser);

        }

        // Save the deal member.
        dealMemberDao.save(dealMember);

        Map<String, Object> activityMap = Map.of("teamMemberFullName", dealMember.getUser().getFullName());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("dealName", dealMember.getDeal().getName());
        templateData.put("recipient", dealMember);
        templateData.put("addedByName", currentUser.getFullName());
        templateData.put("from", sendAddress);
        templateData.put("dealUid", dealMember.getDeal().getUid());
        templateData.put("leadInstitutionUid", dealDao.findByUid(dealMember.getDeal().getUid(), currentUser).getOriginator().getUid());
        templateData.put("addedByInstitutionUid", currentUser.getInstitution().getUid());
        templateData.put("addedByInstitution", currentUser.getInstitution().getName());

        // Record the activity in the timeline.
        if ( source.equals(SYSTEM_MARKETPLACE) && leadInitiated ) { // comes from lamina
            activityService.createActivity(TEAM_MEMBER_ADDED, dealMember.getDeal().getId()
                    , dealMember.getUser().getInstitution().getId(), activityMap, currentUser, source);
        } else { // comes from integration
            activityService.createActivity(TEAM_MEMBER_ADDED, dealMember.getDeal().getId()
                    , null, activityMap, currentUser, source);
        }

        // Send an email to the team member who was added to deal team
        if ( source.equals(SYSTEM_MARKETPLACE) && !leadInitiated ) { // comes from lamina but not lead initiated
            emailService.sendEmail(EmailTypeEnum.TEAM_MEMBER_ADDED, dealMember.getDeal(), templateData);
        }

        // Call the find method again to update and return the created date.
        return dealMemberDao.findByDealUidAndUserUid(dealMember.getDeal().getUid(), dealMember.getUser().getUid());
    }

    @Transactional
    public DealMembers saveMemberList(DealMembers dealMembers, User currentUser) {

        DealMember dealMember;
        String dealUid = dealMembers.getDeal().getUid();

        // Loop through the list of users and save each one.
        for ( User user : dealMembers.getUsers() ) {

            // Get the full deal and user objects, as we need the relation .
            dealMember = getDealMember(dealUid, user.getUid(), currentUser);

            // Verify that we can save this member.
            verifyDealMemberSave(dealMember, currentUser);

            // Save the deal participant user.
            dealMemberDao.save(dealMember);
        }

        return dealMembers;
    }

    /**
     * This method will verify that the user can save deal member.  This is based on the following two rules:
     *
     *   1) The current user can only save members who are in their own institution.
     *   2) The deal member's institution must be the same as the originator or participant.
     *
     * If the rules are violated then an exception is thrown.
     *
     * @param dealMember
     * @param currentUser
     */
    private void verifyDealMemberSave(DealMember dealMember, User currentUser) {

        if ( !dealMember.getUser().getInstitution().getId().equals(currentUser.getInstitution().getId()) ) {
            throw new AuthorizationException("Users can only add members from their own institution.");
        } else if ( dealMember.getDeal().getRelation().equals(UNKNOWN.getDescription()) ) {
            throw new OperationNotAllowedException("The deal member's institution must be the same as the originator or participant.");
        }

    }

    /**
     * This method will verify that the user can access the deal member.  This is based on the following rule, the
     * current user can only access members who are in their own institution.
     *
     * If the rule is violated then an exception is thrown.
     *
     * @param dealMember
     * @param currentUser
     */
    private void verifyDealMemberAccess(DealMember dealMember, User currentUser) {

        if ( !dealMember.getUser().getInstitution().getUid().equals(currentUser.getInstitution().getUid()) ) {
            throw new AuthorizationException("Users can only access members from their own institution.");
        }

    }

    /**
     * This method will delete a specific user from the deal member table.
     *
     * @param   dealMember
     * @return  The number of rows deleted.
     */
    @Transactional
    public int delete(DealMember dealMember, User currentUser, String source) {

        // Get the full deal member object.
        dealMember = dealMemberDao.findByDealUidAndUserUid(dealMember.getDeal().getUid(), dealMember.getUser().getUid());

        // The current user can only delete members who are in their own institution.
        if ( !dealMember.getUser().getInstitution().getId().equals(currentUser.getInstitution().getId()) ) {
            throw new AuthorizationException("Users can only delete members from their own institution.");
        }

        /*
         *  We are not allowed to remove all members for an institution.  Throw an exception if this is the last member.
         */
        List<DealMember> dealMembers = dealMemberDao.findAllByDealUidAndInstitutionUid(dealMember.getDeal().getUid(), currentUser.getInstitution().getUid());

        if ( dealMembers.size() == 1 ) {

            String message = "Error deleting member. Institutions are required to have at least one deal member.";
            log.error(message.concat(String.format(" (deal uid = %s, institution uid = %s)", dealMember.getDeal().getUid(), currentUser.getInstitution().getUid())));
            throw new OperationNotAllowedException(message);

        }

        /*
         *  Record the activity in the timeline.
         */
        Map<String, Object> activityMap = Map.of("teamMemberFullName", dealMember.getUser().getFullName());
        activityService.createActivity(TEAM_MEMBER_REMOVED, dealMember.getDeal().getId(), null, activityMap, currentUser, source);

        // Delete the deal member.  The unique ids are needed and used for the delete operation.
        return dealMemberDao.delete(dealMember);
    }

}