package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.event.EventParticipantRowMapper;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventParticipantQueryDef.*;

@Repository
@Slf4j
public class EventParticipantDao {

    private final JdbcTemplate jdbcTemplate;

    public EventParticipantDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EventParticipant findById(Long id) {

        String sql = SELECT_EVENT_PARTICIPANT + " WHERE EP.EVENT_PARTICIPANT_ID = ?";
        EventParticipant eventParticipant;

        try {
            eventParticipant = jdbcTemplate.queryForObject(sql, new EventParticipantRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event Participant was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Event Participant was not found for id.");

        }

        return eventParticipant;
    }

    public EventParticipant findByEventUidAndParticipantUid(String eventUid, String participantUid) {

        String sql = SELECT_EVENT_PARTICIPANT + " WHERE EI.EVENT_UUID = ? AND II.INSTITUTION_UUID = ?";
        EventParticipant eventParticipant;

        try {
            eventParticipant = jdbcTemplate.queryForObject(sql, new EventParticipantRowMapper(), eventUid, participantUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event Participant was not found for event and participant. ( event uid = %s, participant uid = %s )", eventUid, participantUid));
            throw new DataNotFoundException("Event Participant was not found for event and participant.");

        }

        return eventParticipant;
    }

    public List<EventParticipant> findAllByEventUid(String eventUid) {
        String sql = SELECT_EVENT_PARTICIPANT + " WHERE EI.EVENT_UUID = ? ORDER BY CREATED_DATE";
        return jdbcTemplate.query(sql, new EventParticipantRowMapper(), eventUid);
    }

    public Long save(Long eventId, Long participantId, Long stepId, Long createdById) {

        Long eventParticipantId = null;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_EVENT_PARTICIPANT, new String[] { "event_participant_id" });

            int index = 1;

            ps.setLong(index++, eventId);

            if ( participantId == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, participantId);
            }

            ps.setLong(index++, stepId);
            ps.setLong(index++, createdById);
            ps.setLong(index, createdById);
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            eventParticipantId = keyHolder.getKey().longValue();

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Event Participant.");
            throw new DatabaseException("Error retrieving unique id for Event Participant.");

        }

        return eventParticipantId;
    }

    public void update(EventParticipant dp) {
        update(dp.getId(), dp.getParticipant(), dp.getStep().getId(), dp.getUpdatedBy().getId());
    }

    public void update(Long eventParticipantId, Institution participant, Long stepId, Long updatedById) {

        Long participantId = participant == null ? null : participant.getId();

        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT, participantId, stepId, updatedById, eventParticipantId);
    }

    public void updateParticipantStep(Long eventParticipantId, Long stepId, Long updatedById) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_STEP, stepId, updatedById, eventParticipantId);
    }

    public void updateUpdatedBy(Long eventParticipantId, Long updatedById) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_UPDATED_BY, updatedById, eventParticipantId);
    }

    /**
     * This method will delete a specific event participant for a event.
     *
     * @param   ep
     * @return  The number of rows deleted.
     */
    public int delete(EventParticipant ep) {
        return deleteById(ep.getId());
    }

    /**
     * This method will delete a specific deal participant by the provided unigue id.
     *
     * @param   id
     * @return  The number of rows deleted.
     */
    public int deleteById(long id) {
        String sql = DELETE_EVENT_PARTICIPANT + " WHERE EVENT_PARTICIPANT_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * This method will delete all participants for an event.  This is used when an event is deleted.
     *
     * @param   eventId
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventId(Long eventId) {
        String sql = DELETE_EVENT_PARTICIPANT + " WHERE EVENT_ID = ?";
        return jdbcTemplate.update(sql, eventId);
    }

    /**
     * This method will delete all participants for an event.  This is used when an event is deleted.
     *
     * @param   eventUid
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventUid(String eventUid) {
        String sql = DELETE_EVENT_PARTICIPANT + " WHERE EVENT_ID = ( SELECT EVENT_ID FROM EVENT_INFO WHERE EVENT_UUID = ? )";
        return jdbcTemplate.update(sql, eventUid);
    }

    /**
     * This method will delete all event participants from the specified institution or event participants where the
     * deal event will be deleted.  This is used when an institution is deleted.
     *
     * @param   participantId
     * @return  The number of rows deleted.
     */
    public int deleteAllByParticipantId(Long participantId) {
        String sql = DELETE_EVENT_PARTICIPANT + " WHERE PARTICIPANT_ID = ? "
                                                  + "OR EVENT_ID IN ( SELECT EI.EVENT_ID "
                                                                     + "FROM EVENT_INFO EI LEFT JOIN DEAL_INFO DI "
                                                                       + "ON EI.DEAL_ID = DI.DEAL_ID "
                                                                    + "WHERE DI.ORIGINATOR_ID = ? )";
        return jdbcTemplate.update(sql, participantId, participantId);
    }

    /**
     * This method will delete all participants from an event for the specified institution.  This is used when an
     * institution is deleted.
     *
     * @param   participantUid
     * @return  The number of rows deleted.
     */
    public int deleteAllByParticipantUid(String participantUid) {
        String sql = DELETE_EVENT_PARTICIPANT + " WHERE PARTICIPANT_ID = ( SELECT INSTITUTION_ID FROM INSTITUTION_INFO WHERE INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, participantUid);
    }

    /**
     * This method will delete all participants from an event, where the specified institution was an originator.  This is
     * used when an institution is deleted.
     *
     * @param   originatorUid The uid of the institution that was the originator of the deal(s).
     * @return  The number of rows deleted.
     */
    public int deleteAllByDealOriginatorUid(String originatorUid) {

        String sql = DELETE_EVENT_PARTICIPANT + " WHERE EVENT_ID IN ( SELECT EI.EVENT_ID "
                                                                     + "FROM EVENT_INFO EI LEFT JOIN DEAL_INFO DI "
                                                                       + "ON EI.DEAL_ID = DI.DEAL_ID LEFT JOIN INSTITUTION_INFO II "
                                                                       + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                                    + "WHERE II.INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, originatorUid);
    }

    /**
     * This method will delete all participants from an event that have currently in a specified step.
     *
     * @param   eventUid
     * @param   order
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventUidAndStepOrder(String eventUid, int order){
        String sql = DELETE_EVENT_PARTICIPANT + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                                                                 + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                                                   + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN PARTICIPANT_STEP_DEF PSD "
                                                                                   + "ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID "
                                                                                +" WHERE EI.EVENT_UUID = ? "
                                                                                  + "AND PSD.ORDER_NBR = ? )";
        return jdbcTemplate.update(sql, eventUid, order);
    }

}