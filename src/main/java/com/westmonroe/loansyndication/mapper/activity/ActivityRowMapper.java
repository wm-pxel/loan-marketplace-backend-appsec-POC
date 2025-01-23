package com.westmonroe.loansyndication.mapper.activity;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.activity.Activity;
import com.westmonroe.loansyndication.model.activity.ActivityCategory;
import com.westmonroe.loansyndication.model.activity.ActivityType;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

@Slf4j
public class ActivityRowMapper implements RowMapper<Activity> {

    @Override
    public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {

        Activity activity = new Activity();

        activity.setId(rs.getLong("ACTIVITY_ID"));

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));

        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        deal.setOriginator(institution);

        activity.setDeal(deal);

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setName(rs.getString("EVENT_NAME"));
        event.setLaunchDate(rs.getObject("LAUNCH_DATE", OffsetDateTime.class));
        activity.setEvent(event);

        Institution participant = new Institution();
        participant.setId(rs.getLong("PART_INSTITUTION_ID"));
        participant.setUid(rs.getString("PART_INSTITUTION_UUID"));
        participant.setName(rs.getString("PART_INSTITUTION_NAME"));
        activity.setParticipant(participant);

        ActivityType activityType = new ActivityType();
        activityType.setId(rs.getLong("ACTIVITY_TYPE_ID"));
        activityType.setName(rs.getString("ACTIVITY_TYPE_NAME"));

        ActivityCategory activityCategory = new ActivityCategory();
        activityCategory.setId(rs.getLong("ACTIVITY_CATEGORY_ID"));
        activityCategory.setName(rs.getString("ACTIVITY_CATEGORY_NAME"));
        activityType.setCategory(activityCategory);

        activity.setActivityType(activityType);

        if ( rs.getObject("ACTIVITY_JSON") != null ) {
            activity.setJson(rs.getString("ACTIVITY_JSON"));
        }

        activity.setSource(rs.getString("SOURCE_CD"));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        activity.setCreatedBy(createdBy);

        Institution createdByInstitution = new Institution();
        createdByInstitution.setId(rs.getLong("CREATED_BY_INSTITUTION_ID"));
        createdByInstitution.setUid(rs.getString("CREATED_BY_INSTITUTION_UUID"));
        createdByInstitution.setName(rs.getString("CREATED_BY_INSTITUTION_NAME"));
        activity.getCreatedBy().setInstitution(createdByInstitution);

        activity.setCreatedDate(rs.getObject("CREATED_DATE", OffsetDateTime.class));

        return activity;
    }

}