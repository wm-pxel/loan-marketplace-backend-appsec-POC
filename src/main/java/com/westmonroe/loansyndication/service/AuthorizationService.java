package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.deal.DealDao;
import com.westmonroe.loansyndication.dao.event.EventDao;
import com.westmonroe.loansyndication.dao.event.EventParticipantDao;
import com.westmonroe.loansyndication.exception.AuthorizationException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.westmonroe.loansyndication.utils.Constants.*;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_3;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_3;

@Service
@Slf4j
public class AuthorizationService {

    private final DealDao dealDao;
    private final EventDao eventDao;
    private final EventParticipantDao eventParticipantDao;

    private static final String ERR_USER_NOT_AUTH_FOR_DEAL = "User %s is not authorized for deal %s";

    public AuthorizationService(DealDao dealDao, EventDao eventDao, EventParticipantDao eventParticipantDao) {
        this.dealDao = dealDao;
        this.eventDao = eventDao;
        this.eventParticipantDao = eventParticipantDao;
    }

    public void authorizeUserForInstitutionByInstitutionUid(User user, String institutionUid) {

        if ( user.getRoles().stream().anyMatch(r -> r.getCode().matches("(SUPER_ADM|APP_SERVICE)")) ) {

            // VALID: super admins are authorized for everything.

        } else {

            // Check if user's institution is the different from the one they're trying to access.
            if ( !user.getInstitution().getUid().equals(institutionUid) ) {

                log.error(String.format("User %s is not authorized for institution %s", user.getUid(), institutionUid));
                throw new AuthorizationException(ERR_UNAUTH_INSTITUTION_MEMBER);

            }

        }

    }

    public Deal authorizeUserForDealByDealUid(User user, String dealUid) {

        // Get the deal to perform authorization.
        Deal deal = dealDao.findByUid(dealUid, user);

        // Validate the user for the deal.
        validateUserForDeal(user, deal);

        return deal;
    }

    public DealEvent authorizeUserForDealEventByDealUid(User user, String dealUid){

        // Get the deal to perform authorization.
        DealEvent dealEvent = dealDao.findDealEventByUid(dealUid, user);

        // Validate the user for the deal.
        validateUserForDealEvent(user, dealEvent);

        return dealEvent;
    }

    public Deal authorizeUserForDealByDealExternalId(User user, String dealExternalId) {

        // Get the deal to perform authorization.
        Deal deal = dealDao.findByExternalId(dealExternalId, user);

        // Validate the user for the deal.
        validateUserForDeal(user, deal);

        return deal;
    }

    public DealEvent authorizeUserForDealEventByDealExternalId(User user, String dealExternalId){

        DealEvent dealEvent = dealDao.findDealEventByExternalId(dealExternalId, user);

        validateUserForDealEvent(user, dealEvent);

        return dealEvent;
    }

    public Event authorizeUserForDealByEventUid(User user, String eventUid) {

        // Get the event to verify it's existence.
        Event event = eventDao.findByUid(eventUid);

        // Get the deal that the event is under to perform authorization.
        Deal deal = dealDao.findById(event.getDeal().getId(), user);
        DealEvent dealEvent = dealDao.findDealEventByUid(event.getDeal().getUid(), user);

        // Validate the user for the deal (and event).
        validateUserForDealEvent(user, dealEvent);

        // Put the full deal object on the event.
        event.setDeal(deal);

        return event;
    }

    public void authorizeUserInOriginatingInstitution(User user, DealEvent dealEvent) {

        // Validate the user's institution is a participant on the deal.
        validateUserInOriginatingInstitution(user, dealEvent);

    }

    public void authorizeUserInParticipatingInstitution(User user, DealEvent dealEvent) {

        // Validate the user's institution is a participant on the deal.
        validateUserInParticipatingInstitution(user, dealEvent);

    }

    /**
     * This method is used to restrict participants from REST API and viewing/downloading files (per story). Requirement
     * is the following:
     * 1) The deal (event) has been launched.
     * 2) The participant has expressed interest.
     *
     * @param user
     * @param dealUid
     */
    public Deal authorizeUserForRestApisAndFileAccess(User user, String dealUid) {

        // if one exists, get the open event for the deal.
        Event event = eventDao.findLatestEventByDealUid(dealUid);

        // Check if the user is an originator, as they are authorized to view and download.
        if ( event.getDeal().getOriginator().getUid().equals(user.getInstitution().getUid()) ) {

            // User is an originator and they are authorized to view and/or download.

        } else {

            EventParticipant ep = eventParticipantDao.findByEventUidAndParticipantUid(event.getUid(), user.getInstitution().getUid());

            // Check whether deal has been launched and participant has confirmed interest.
            if ( event.getStage().getOrder() >= STAGE_3.getOrder() && ep != null && ep.getStep().getOrder() >= STEP_3.getOrder() ) {

                // All conditions are satisfied, so we can provide access.

            } else {

                // Criteria hasn't been satisfied, so we will not provide access.
                log.error(String.format(ERR_USER_NOT_AUTH_FOR_DEAL, user.getUid(), event.getDeal().getUid()));
                throw new AuthorizationException(ERR_UNAUTH_DEAL_MEMBER);

            }

        }

        return event.getDeal();
    }

    /**
     * This method verifies that the current user has access to the deal.  The rules are as follows:
     *
     * Deal Access Rules
     *  1. The user is apart of the Originating Institution AND
     *     a. The user has the ACCESS_ALL_INST_DEALS permission OR
     *     b. They have been added as a Deal Team member for the Deal
     *  2. The user is apart of a Participating Institution AND
     *     a. The user has the ACCESS_ALL_INST_DEALS permission OR
     *     b. They have been added as a Deal Team member for the Deal
     *
     * @param  user
     * @param  deal
     * @return Deal
     */
    private void validateUserForDeal(User user, Deal deal) {

        if ( user.getRoles().stream().anyMatch(r -> r.getCode().matches("(SUPER_ADM|APP_SERVICE)")) ) {

            // VALID: super admins are authorized for everything.

        } else {

            if ( deal.getMemberFlag().equals("Y") ) {

                // VALID: Users in the Deal Members table must be part of the originator or participant institution.

            } else if (( "Y".equals(deal.getOrigInstUserFlag()) || "Y".equals(deal.getPartInstUserFlag()) ) &&
                    user.getRoles().stream().anyMatch(r -> "ACCESS_ALL_INST_DEALS".equals(r.getCode()))) {

                // VALID: User is in originator or participant institution and has the ACCESS_ALL_INST_DEALS role.

            } else {

                log.error(String.format(ERR_USER_NOT_AUTH_FOR_DEAL, user.getUid(), deal.getUid()));
                throw new AuthorizationException(ERR_UNAUTH_DEAL_MEMBER);

            }

        }

    }

    /**
     * This method verifies that the current user has access to the dealEvent.  The rules are as follows:
     *
     * Deal Access Rules
     *  1. The user is a part of the Originating Institution AND
     *     a. The user has the ACCESS_ALL_INST_DEALS permission OR
     *     b. They have been added as a Deal Team member for the Deal
     *  2. The user is a part of a Participating Institution AND
     *     a. The user has the ACCESS_ALL_INST_DEALS permission OR
     *     b. They have been added as a Deal Team member for the Deal
     *
     * @param  user
     * @param  dealEvent
     * @return Deal
     */
    private void validateUserForDealEvent(User user, DealEvent dealEvent) {

        if ( user.getRoles().stream().anyMatch(r -> r.getCode().matches("(SUPER_ADM|APP_SERVICE)")) ) {

            // VALID: super admins are authorized for everything.

        } else {

            if (dealEvent.getMemberFlag().equals("Y")) {

                // VALID: Users in the Deal Members table must be part of the originator or participant institution.

            } else if (( "Y".equals(dealEvent.getOrigInstUserFlag()) || "Y".equals(dealEvent.getPartInstUserFlag()) ) &&
                    user.getRoles().stream().anyMatch(r -> "ACCESS_ALL_INST_DEALS".equals(r.getCode()))) {

                // VALID: User is in originator or participant institution and has the ACCESS_ALL_INST_DEALS role.

            } else {

                log.error(String.format(ERR_USER_NOT_AUTH_FOR_DEAL, user.getUid(), dealEvent.getUid()));
                throw new AuthorizationException(ERR_UNAUTH_DEAL_MEMBER);

            }

        }

    }

    private void validateUserInOriginatingInstitution(User user, DealEvent dealEvent) {

        if ( user.getRoles().stream().anyMatch(r -> r.getCode().matches("(SUPER_ADM|APP_SERVICE)")) ) {

            // VALID: super admins are authorized for everything.

        } else {

            if ( dealEvent.getRelation().equals(ORIGINATOR.getDescription()) ) {

                // VALID: The user's relation is a originator.  Meaning they are in the originating institution.

            } else {

                log.error(String.format(ERR_USER_NOT_AUTH_FOR_DEAL, user.getUid(), dealEvent.getUid()));
                throw new AuthorizationException(ERR_UNAUTH_DEAL_PARTICIPANT);

            }

        }

    }

    private void validateUserInParticipatingInstitution(User user, DealEvent dealEvent) {

        if ( user.getRoles().stream().anyMatch(r -> r.getCode().matches("(SUPER_ADM|APP_SERVICE)")) ) {

            // VALID: super admins are authorized for everything.

        } else {

            if ( dealEvent.getRelation().equals(PARTICIPANT.getDescription()) ) {

                // VALID: The user's relation is a participant.  Meaning they are a participant in at least one event.

            } else {

                log.error(String.format(ERR_USER_NOT_AUTH_FOR_DEAL, user.getUid(), dealEvent.getUid()));
                throw new AuthorizationException(ERR_UNAUTH_DEAL_PARTICIPANT);

            }

        }

    }

}