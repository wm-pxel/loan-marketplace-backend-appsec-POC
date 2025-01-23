package com.westmonroe.loansyndication.controller.graphql;

import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.activity.Activity;
import com.westmonroe.loansyndication.model.activity.ActivityType;
import com.westmonroe.loansyndication.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
public class ActivityGQLController {

    private final ActivityService activityService;

    public ActivityGQLController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('SUPER_ADM')")
    public List<ActivityType> allActivityTypes() {
        return activityService.getActivityTypes();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Activity> getActivitiesByDealUid(@Argument String uid, @AuthenticationPrincipal User currentUser) {
        return activityService.getActivitiesForDealUid(uid, currentUser);
    }

}