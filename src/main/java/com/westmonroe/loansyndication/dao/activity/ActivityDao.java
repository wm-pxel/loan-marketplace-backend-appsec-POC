package com.westmonroe.loansyndication.dao.activity;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.activity.ActivityRowMapper;
import com.westmonroe.loansyndication.model.activity.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.activity.ActivityQueryDef.*;
import static com.westmonroe.loansyndication.utils.ActivityTypeEnum.*;

@Repository
@Slf4j
public class ActivityDao {

    private final JdbcTemplate jdbcTemplate;

    public ActivityDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Activity> findAllForOriginator(String dealUid, Long originatorId) {
        StringBuilder sql = new StringBuilder(SELECT_ACTIVITY)
                                .append(" WHERE DI.DEAL_UUID = ? ")
                                    .append("AND (")
                                        .append("( AI.ACTIVITY_TYPE_ID IN (")
                                            .append(TEAM_MEMBER_ADDED.getId())
                                            .append(", ")
                                            .append(TEAM_MEMBER_REMOVED.getId())
                                        .append(") AND UIC.INSTITUTION_ID = ? AND AI.PARTICIPANT_ID IS NULL )")
                                        .append(" OR ")
                                        .append("AI.ACTIVITY_TYPE_ID > ").append(TEAM_MEMBER_REMOVED.getId())
                                    .append(") ")
                                .append("ORDER BY AI.CREATED_DATE DESC");
        return jdbcTemplate.query(sql.toString(), new ActivityRowMapper(), dealUid, originatorId);
    }

    public List<Activity> findAllForParticipant(String dealUid, Long participantId) {
        StringBuilder sql = new StringBuilder(SELECT_ACTIVITY)
                                .append(" WHERE DI.DEAL_UUID = ? ")
                                    .append("AND (")
                                        .append("( AI.ACTIVITY_TYPE_ID IN (")
                                            .append(TEAM_MEMBER_ADDED.getId())
                                            .append(", ")
                                            .append(TEAM_MEMBER_REMOVED.getId())
                                        .append(") AND UIC.INSTITUTION_ID = ? OR AI.PARTICIPANT_ID = ? )")
                                        .append(" OR ")
                                        .append("( AI.ACTIVITY_TYPE_ID IN ( ")
                                            .append(DEAL_CREATED.getId()).append(", ")
                                            .append(DEAL_INFO_UPDATED.getId()).append(", ")
                                            .append(FILE_UPLOADED.getId()).append(", ")
                                            .append(FILE_RENAMED.getId()).append(", ")
                                            .append(FILE_REMOVED.getId()).append(", ")
                                            .append(DEAL_LAUNCHED.getId()).append(", ")
                                            .append(DEAL_DATES_UPDATED.getId()).append(", ")
                                            .append(FINAL_LOAN_DOCS_UPLOADED.getId()).append(", ")
                                            .append(DRAFT_LOAN_DOCS_UPLOADED.getId()).append(", ")
                                            .append(CLOSING_MEMO_UPLOADED.getId()).append(", ")
                                            .append(DEAL_CLOSED.getId()).append(") ")
                                        .append(")")
                                        .append(" OR ")
                                        .append("( AI.ACTIVITY_TYPE_ID > 2 AND PII.INSTITUTION_ID = ? )")
                                    .append(") ")
                                .append("ORDER BY AI.CREATED_DATE DESC");
        return jdbcTemplate.query(sql.toString(), new ActivityRowMapper(), dealUid, participantId, participantId, participantId);
    }

    public Activity findById(Long activityId) {

        String sql = SELECT_ACTIVITY + " WHERE AI.ACTIVITY_ID = ?";
        Activity activity;

        try {
            activity = jdbcTemplate.queryForObject(sql, new ActivityRowMapper(), activityId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Activity was not found for id. ( id = %s )", activityId));
            throw new DataNotFoundException("Activity was not found for id.");

        }

        return activity;
    }

    public Activity save(Activity activity) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(INSERT_ACTIVITY, new String[] { "activity_id" });
            int index = 1;

            ps.setLong(index++, activity.getDeal().getId());

            if ( activity.getParticipant() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, activity.getParticipant().getId());
            }

            ps.setLong(index++, activity.getActivityType().getId());
            ps.setString(index++, activity.getJson());
            ps.setString(index++, activity.getSource());
            ps.setLong(index, activity.getCreatedBy().getId());
            return ps;

        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            activity.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for activity.");
            throw new DatabaseException("Error retrieving unique id for activity.");

        }

        return activity;
    }

    public int deleteActivitiesByDealId(Long dealId) {
        String sql = DELETE_ACTIVITY + " WHERE DEAL_ID = ?";
        return jdbcTemplate.update(sql, dealId);
    }

    public int deleteActivitiesByDealUid(String dealUid) {
        String sql = DELETE_ACTIVITY + " WHERE DEAL_ID IN (SELECT DEAL_ID FROM DEAL_INFO WHERE DEAL_UUID = ?)";
        return jdbcTemplate.update(sql, dealUid);
    }

    public int deleteActivitiesByInstitutionId(Long institutionId) {
        String sql = DELETE_ACTIVITY + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                + "WHERE II.INSTITUTION_ID = ? ) OR PARTICIPANT_ID = ?";
        return jdbcTemplate.update(sql, institutionId, institutionId);
    }

    public int deleteActivitiesByInstitutionUid(String institutionUid) {
        String sql = DELETE_ACTIVITY + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                + "WHERE II.INSTITUTION_UUID = ? )"
                + " OR PARTICIPANT_ID IN (SELECT INSTITUTION_ID FROM INSTITUTION_INFO WHERE INSTITUTION_UUID = ?)";

        return jdbcTemplate.update(sql, institutionUid, institutionUid);
    }

}