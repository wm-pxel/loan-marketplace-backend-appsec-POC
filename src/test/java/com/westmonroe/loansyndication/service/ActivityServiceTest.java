package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.activity.Activity;
import com.westmonroe.loansyndication.model.activity.ActivityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.TEAM_MEMBER_ADDED;
import static com.westmonroe.loansyndication.utils.Constants.SYSTEM_MARKETPLACE;
import static com.westmonroe.loansyndication.utils.TestConstants.TEST_DEAL_UUID_1;
import static com.westmonroe.loansyndication.utils.TestConstants.TEST_USER_UUID_1;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Test
    void givenExistingActivityTypes_whenGettingAllActivityTypes_thenVerifyCount() {

        List<ActivityType> activityTypes = activityService.getActivityTypes();
        assertThat(activityTypes).hasSize(25);
    }

    @Test
    void givenUnknownActivityType_whenGettingActivityTypeById_thenVerifyException() {

        assertThatThrownBy(() -> activityService.getActivityTypeById(99L))
                .isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void givenNewMemberActivity_whenActivityCreated_thenVerify() throws ExecutionException, InterruptedException, TimeoutException {

        // Use "Texas Dairy Farm" as our test deal.
        Long dealId = 1L;
        String dealUid = TEST_DEAL_UUID_1;

        Map<String, Object> activityMap = Map.ofEntries(entry("teamMemberFullName", "Jane Halverson"));
        User currentUser = userService.getUserByUid(TEST_USER_UUID_1);

        CompletableFuture<Activity> future = activityService.createActivity(TEAM_MEMBER_ADDED, dealId, null, activityMap, currentUser, SYSTEM_MARKETPLACE);
        Activity activity = future.get(5, TimeUnit.SECONDS);

        List<Activity> activities = activityService.getActivitiesForDealUid(dealUid, currentUser);
        assertThat(activities).hasSize(2);
    }

}