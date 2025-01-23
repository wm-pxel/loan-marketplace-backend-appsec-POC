package com.westmonroe.loansyndication.dao.activity;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.activity.ActivityTypeRowMapper;
import com.westmonroe.loansyndication.model.activity.ActivityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.activity.ActivityTypeQueryDef.SELECT_ACTIVITY_TYPE;

@Repository
@Slf4j
public class ActivityTypeDao {

    private final JdbcTemplate jdbcTemplate;

    public ActivityTypeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ActivityType> findAll() {
        String sql = SELECT_ACTIVITY_TYPE + " ORDER BY ACTIVITY_TYPE_NAME";
        return jdbcTemplate.query(sql, new ActivityTypeRowMapper());
    }

    public ActivityType findById(Long id) {
        String sql = SELECT_ACTIVITY_TYPE + " WHERE ACTIVITY_TYPE_ID = ?";
        ActivityType activityType;

        try {
            activityType = jdbcTemplate.queryForObject(sql, new ActivityTypeRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Activity type was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Activity type was not found for id.");

        }

        return activityType;
    }

    public ActivityType findByName(String name) {

        String sql = SELECT_ACTIVITY_TYPE + " WHERE ACTIVITY_TYPE_NAME = ?";
        ActivityType activityType;

        try {
            activityType = jdbcTemplate.queryForObject(sql, new ActivityTypeRowMapper(), name);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Activity type was not found for name. ( name = %s )", name));
            throw new DataNotFoundException("Activity type was not found for name.");

        }

        return activityType;
    }

}