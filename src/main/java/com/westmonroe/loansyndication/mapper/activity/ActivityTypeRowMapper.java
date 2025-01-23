package com.westmonroe.loansyndication.mapper.activity;

import com.westmonroe.loansyndication.model.activity.ActivityCategory;
import com.westmonroe.loansyndication.model.activity.ActivityType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivityTypeRowMapper implements RowMapper<ActivityType> {

    @Override
    public ActivityType mapRow(ResultSet rs, int rowNum) throws SQLException {

        ActivityType activityType = new ActivityType();
        activityType.setId(rs.getLong("ACTIVITY_TYPE_ID"));
        activityType.setName(rs.getString("ACTIVITY_TYPE_NAME"));

        ActivityCategory activityCategory = new ActivityCategory();
        activityCategory.setId(rs.getLong("ACTIVITY_CATEGORY_ID"));
        activityCategory.setName(rs.getString("ACTIVITY_CATEGORY_NAME"));
        activityType.setCategory(activityCategory);

        return activityType;
    }

}