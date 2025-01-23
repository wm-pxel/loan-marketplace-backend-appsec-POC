package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.event.EventRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventQueryDef.*;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_1;

@Repository
@Slf4j
public class EventDao {

    private final JdbcTemplate jdbcTemplate;

    public EventDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Event> findAllByDealId(Long dealId) {
        String sql = SELECT_EVENT + " WHERE EI.DEAL_ID = ? ORDER BY CREATED_DATE";
        return jdbcTemplate.query(sql, new EventRowMapper(), dealId);
    }

    public List<Event> findAllByDealUid(String dealUid) {
        String sql = SELECT_EVENT + " WHERE DI.DEAL_UUID = ? ORDER BY CREATED_DATE";
        return jdbcTemplate.query(sql, new EventRowMapper(), dealUid);
    }

    public Event findOpenEventByDealUid(String dealUid) {

        String sql = SELECT_EVENT + " WHERE EI.CLOSE_DATE IS NULL AND DI.DEAL_UUID = ? ORDER BY CREATED_DATE";
        Event event;

        try {
            event = jdbcTemplate.queryForObject(sql, new EventRowMapper(), dealUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("An open event was not found for the deal uid. ( uid = %s )", dealUid));
            throw new DataNotFoundException("An open event was not found for the deal.");

        }

        return event;
    }

    public Event findLatestEventByDealUid(String dealUid) {

        String sql = SELECT_EVENT + " WHERE DI.DEAL_UUID = ? ORDER BY CREATED_DATE LIMIT 1";
        Event event;

        try {
            event = jdbcTemplate.queryForObject(sql, new EventRowMapper(), dealUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("There are no events for the deal. ( uid = %s )", dealUid));
            throw new DataNotFoundException("There are no events for the deal.");

        }

        return event;
    }

    public Event findById(Long id) {

        String sql = SELECT_EVENT + " WHERE EI.EVENT_ID = ?";
        Event event;

        try {
            event = jdbcTemplate.queryForObject(sql, new EventRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Event was not found for id.");

        }

        return event;
    }

    public Event findByUid(String uid) {

        String sql = SELECT_EVENT + " WHERE EI.EVENT_UUID = ?";
        Event event;

        try {
            event = jdbcTemplate.queryForObject(sql, new EventRowMapper(), uid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event was not found for uid. ( uid = %s )", uid));
            throw new DataNotFoundException("Event was not found for uid.");

        }

        return event;
    }

    public Event findByExternalId(String externalId) {

        String sql = SELECT_EVENT + " WHERE EI.EVENT_EXTERNAL_UUID = ?";
        Event event;

        try {
            event = jdbcTemplate.queryForObject(sql, new EventRowMapper(), externalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event was not found for external id. ( id = %s )", externalId));
            throw new DataNotFoundException("Event was not found for external id.");

        }

        return event;
    }

    public Event save(Event event) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_EVENT, new String[] { "event_id" });
            ps.setString(index++, event.getUid());
            ps.setString(index++, event.getEventExternalId());
            ps.setLong(index++, event.getDeal().getId());
            ps.setString(index++, event.getName());
            ps.setLong(index++, event.getEventType().getId());
            ps.setLong(index++, STAGE_1.getOrder());                // The stage will always start at one for a new event.
            ps.setObject(index++, event.getProjectedLaunchDate());
            ps.setObject(index++, event.getCommitmentDate());
            ps.setObject(index++, event.getCommentsDueByDate());
            ps.setObject(index++, event.getEffectiveDate());
            ps.setObject(index++, event.getProjectedCloseDate());
            ps.setLong(index++, event.getCreatedBy().getId());
            ps.setLong(index, event.getCreatedBy().getId());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            event.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Event.");
            throw new DatabaseException("Error retrieving unique id for Event.");

        }

        return event;
    }

    public void update(Event event) {

        jdbcTemplate.update(UPDATE_EVENT, event.getName(), event.getEventType().getId(), event.getProjectedLaunchDate()
            , event.getCommitmentDate(), event.getCommentsDueByDate(), event.getEffectiveDate(), event.getProjectedCloseDate()
            , event.getUpdatedBy().getId(), event.getId());
    }

    public void updateEventStage(Long eventId, Long stageId, Long userId) {
        jdbcTemplate.update(UPDATE_EVENT_STAGE, stageId, userId, eventId);
    }

    public void updateLaunchDates(Long eventId, LocalDate commitmentDate, LocalDate projectedCloseDate, Long userId) {
        jdbcTemplate.update(UPDATE_EVENT_LAUNCH_DATES, commitmentDate, projectedCloseDate, userId, eventId);
    }

    public void updateCloseDates(Long eventId, LocalDate effectiveDate, Long userId) {
        jdbcTemplate.update(UPDATE_EVENT_CLOSE_DATES, effectiveDate, userId, eventId);
    }

    public void updateEventDates(Event event, User currentUser) {
        jdbcTemplate.update(UPDATE_EVENT_DATES, event.getProjectedLaunchDate(), event.getCommitmentDate()
                , event.getCommentsDueByDate(), event.getEffectiveDate(), event.getProjectedCloseDate()
                , currentUser.getId(), event.getId());
    }

    public void updateLeadInvitationDate(Long eventId, User currentUser) {
        jdbcTemplate.update(UPDATE_EVENT_LEAD_INVITATION_DATE, currentUser.getId(), eventId);
    }

    public void updateLeadCommitmentDate(Long eventId, User currentUser) {
        jdbcTemplate.update(UPDATE_EVENT_LEAD_COMMITMENT_DATE, currentUser.getId(), eventId);
    }

    public void updateLeadAllocationDate(Long eventId, User currentUser) {
        jdbcTemplate.update(UPDATE_EVENT_LEAD_ALLOCATION_DATE, currentUser.getId(), eventId);
    }

    public int delete(Event event) {
        return deleteById(event.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_EVENT + " WHERE EVENT_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteByUid(String uid) {
        String sql = DELETE_EVENT + " WHERE EVENT_UUID = ?";
        return jdbcTemplate.update(sql, uid);
    }

    public int deleteAllByParticipantId(Long participantId) {
        String sql = DELETE_EVENT + " WHERE DEAL_ID IN ( SELECT DEAL_ID "
                                                        + "FROM DEAL_INFO "
                                                       + "WHERE ORIGINATOR_ID = ? )";
        return jdbcTemplate.update(sql, participantId);
    }

}