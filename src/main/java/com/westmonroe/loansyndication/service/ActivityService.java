package com.westmonroe.loansyndication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.dao.activity.ActivityDao;
import com.westmonroe.loansyndication.dao.activity.ActivityTypeDao;
import com.westmonroe.loansyndication.dao.event.EventOriginationParticipantDao;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.activity.*;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.service.deal.DealService;
import com.westmonroe.loansyndication.utils.ActivityTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.DEAL_INFO_UPDATED;
import static com.westmonroe.loansyndication.utils.Constants.VIEW_TYPE_NO_ACCESS;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;

@Service
@Slf4j
public class ActivityService {

    private final ActivityTypeDao activityTypeDao;
    private final ActivityDao activityDao;
    private final DealService dealService;
    private final EventOriginationParticipantDao eventOriginationParticipantDao;
    private ObjectMapper mapper = new ObjectMapper();

    public ActivityService(ActivityTypeDao activityTypeDao, ActivityDao activityDao, @Lazy DealService dealService
            , EventOriginationParticipantDao eventOriginationParticipantDao) {
        this.activityTypeDao = activityTypeDao;
        this.activityDao = activityDao;
        this.eventOriginationParticipantDao = eventOriginationParticipantDao;
        this.dealService = dealService;
    }

    public List<ActivityType> getActivityTypes() {
        return activityTypeDao.findAll();
    }

    public ActivityType getActivityTypeById(Long activityTypeId) {
        return activityTypeDao.findById(activityTypeId);
    }

    private ActivityFormat getActivityFormatter(ActivityTypeEnum activityType) {

        ActivityFormat activityFormat;

        switch(activityType) {
            case TEAM_MEMBER_ADDED -> activityFormat = new TeamMemberAddedActivity();
            case TEAM_MEMBER_REMOVED -> activityFormat = new TeamMemberRemovedActivity();
            case DEAL_CREATED -> activityFormat = new DealCreatedActivity();
            case DEAL_INFO_UPDATED -> activityFormat = new DealInfoUpdatedActivity();
            case FILE_UPLOADED -> activityFormat = new FileUploadedActivity();
            case FILE_REMOVED -> activityFormat = new FileRemovedActivity();
            case INVITE_SENT -> activityFormat = new InviteSentActivity();
            case DEAL_INTEREST -> activityFormat = new DealInterestActivity();
            case DEAL_LAUNCHED -> activityFormat = new DealLaunchActivity();
            case COMMITMENTS_SENT -> activityFormat = new CommitmentsSentActivity();
            case ALLOCATIONS_SENT -> activityFormat = new AllocationsSentActivity();
            case DEAL_DECLINED -> activityFormat = new DealDeclinedActivity();
            case PARTICIPANT_REMOVED -> activityFormat = new ParticipantRemovedActivity();
            case DEAL_DATES_UPDATED -> activityFormat = new DealDatesUpdatedActivity();
            case PART_CERT_SENT -> activityFormat = new ParticipationCertificateSentActivity();
            case SIGNED_PC_SENT -> activityFormat = new SignedParticipantCertificateSentActivity();
            case DRAFT_LOAN_DOCS_UPLOADED -> activityFormat = new DraftLoanDocsUploadedActivity();
            case FINAL_LOAN_DOCS_UPLOADED -> activityFormat = new FinalLoanDocsUploadedActivity();
            case CLOSING_MEMO_UPLOADED -> activityFormat = new ClosingMemoUploadedActivity();
            case DEAL_CLOSED -> activityFormat = new DealClosedActivity();
            case INVITE_AMOUNT_SET -> activityFormat = new InviteAmountSetActivity();
            case COMMITMENT_AMOUNT_SET -> activityFormat = new CommitmentAmountSetActivity();
            case ALLOCATION_AMOUNT_SET -> activityFormat = new AllocationAmountSetActivity();
            default -> throw new IllegalStateException("Activity type is not defined.");
        }

        return activityFormat;
    }

    @Async
    public CompletableFuture<Activity> createActivity(ActivityTypeEnum activityType, Long dealId, Long participantId
            , Map<String, Object> activityMap, User currentUser, String source) {

        Activity activity = new Activity();

        Deal deal = new Deal();
        deal.setId(dealId);
        activity.setDeal(deal);

        // Add the participant if it's not null.
        if ( participantId != null ) {
            activity.setParticipant(new Institution(participantId));
        }

        ActivityType type = activityTypeDao.findByName(activityType.getName());
        activity.setActivityType(type);

        ActivityFormat activityFormat = getActivityFormatter(activityType);
        activity.setJson(activityFormat.getJson(activityMap));

        activity.setSource(source);
        activity.setCreatedBy(currentUser);

        activityDao.save(activity);

        return CompletableFuture.completedFuture(activity);
    }

    public List<Activity> getActivitiesForDealUid(String dealUid, User currentUser) {

        DealEvent dealEvent  = dealService.getDealEventByUid(dealUid, currentUser);

        List<Activity> activities;

        if ( dealEvent.getRelation().equals(ORIGINATOR.getDescription()) ) {
            activities = activityDao.findAllForOriginator(dealUid, currentUser.getInstitution().getId());

            // Put the full to summary data.
            activities.parallelStream().forEach(a -> {
                if ( a.getActivityType().getId().equals(DEAL_INFO_UPDATED.getId()) ) {
                    moveFulltoSummaryData(a);
                }
            });
        } else {
            EventOriginationParticipant eventOriginationParticipant = eventOriginationParticipantDao.findByEventUidAndParticipantUid(dealEvent.getEvent().getUid(), currentUser.getInstitution().getUid());
            activities = activityDao.findAllForParticipant(dealUid, currentUser.getInstitution().getId());

            // Filter the activity based on criteria defined in Confluence.
            activities = participantFilter(activities, dealEvent, eventOriginationParticipant);
        }

        return activities;
    }

    private List<Activity> participantFilter(List<Activity> activities, DealEvent dealEvent, EventOriginationParticipant eventOriginationParticipant) {

        activities.removeIf(a -> {
            switch ( ActivityTypeEnum.valueOfId(a.getActivityType().getId()) ) {
                case DEAL_INFO_UPDATED -> {

                   if ( eventOriginationParticipant.getInviteDate() == null ||
                      ( eventOriginationParticipant.getInviteDate() != null && a.getCreatedDate().isBefore(eventOriginationParticipant.getInviteDate()))) {

                       return true;

                   } else if (( eventOriginationParticipant.getFullDealAccessDate() != null && a.getCreatedDate().isAfter(eventOriginationParticipant.getFullDealAccessDate())) &&
                              ( eventOriginationParticipant.getEvent().getLaunchDate() != null && a.getCreatedDate().isAfter(eventOriginationParticipant.getEvent().getLaunchDate()))) {

                       // Show Full View - Move full fields to the summary level.
                       moveFulltoSummaryData(a);
                       return false;

                   } else {

                       // Show Summary View - Remove the full node.
                       try {

                           Map<String, Object> jsonMap = mapper.readValue(a.getJson(), Map.class);
                           jsonMap.remove("full");

                           // Handle the case where only "full" fields where updated.
                           if ( jsonMap.isEmpty() || (jsonMap.size() == 1 && jsonMap.containsKey("facilityId")) ) {
                               return true;                                     // No summary fields were updated, so remove activity.
                           } else {
                               a.setJson(mapper.writeValueAsString(jsonMap));   // Summary fields were updated. Convert JsonMap to String.
                           }

                       } catch ( JsonProcessingException e ) {
                           throw new RuntimeException(e);
                       }

                       return false;
                   }

                }
                case FILE_UPLOADED, FILE_RENAMED, FILE_REMOVED -> {

                    if ( eventOriginationParticipant.getInviteDate() == null || a.getCreatedDate().isBefore(eventOriginationParticipant.getInviteDate()) ) {
                        return true;
                    } else if (( eventOriginationParticipant.getEvent().getLaunchDate() != null && a.getCreatedDate().isAfter(eventOriginationParticipant.getEvent().getLaunchDate()) ) &&
                               ( eventOriginationParticipant.getFullDealAccessDate() != null && a.getCreatedDate().isAfter(eventOriginationParticipant.getFullDealAccessDate()) )) {
                        return false;
                    } else {
                        return true;
                    }
                }
                case DEAL_DECLINED, PARTICIPANT_REMOVED -> {
                    return true;
                }
                default -> {
                    if ( dealEvent.getViewType().equals(VIEW_TYPE_NO_ACCESS) ) {
                        return true;
                    }
                }
            }

            return false;
        });

        return activities;
    }

    private void moveFulltoSummaryData(Activity activity) {

        try {

            Map<String, Object> jsonMap = mapper.readValue(activity.getJson(), Map.class);
            jsonMap.putAll((Map) jsonMap.get("full"));
            jsonMap.remove("full");
            activity.setJson(mapper.writeValueAsString(jsonMap));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public int deleteActivitiesByDealId(Long dealId) {
        return activityDao.deleteActivitiesByDealId(dealId);
    }

    public int deleteActivitiesByDealUid(String dealUid) {
        return activityDao.deleteActivitiesByDealUid(dealUid);
    }

    public int deleteActivitiesByInstitutionId(Long dealId) {
        return activityDao.deleteActivitiesByInstitutionId(dealId);
    }


    public int deleteActivitiesByInstitutionUid(String dealUid) {
        return activityDao.deleteActivitiesByInstitutionUid(dealUid);
    }
}