package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.event.EventOriginationParticipantRowMapper;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventOriginationParticipantQueryDef.*;

@Repository
@Slf4j
public class EventOriginationParticipantDao {

    private final JdbcTemplate jdbcTemplate;

    public EventOriginationParticipantDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EventOriginationParticipant findById(Long id) {

        String sql = SELECT_EVENT_PARTICIPANT_ORIG + " WHERE EP.EVENT_PARTICIPANT_ID = ?";
        EventOriginationParticipant eop;

        try {
            eop = jdbcTemplate.queryForObject(sql, new EventOriginationParticipantRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event Participant Origination was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Event Participant Origination was not found for id.");

        }

        return eop;
    }

    public EventOriginationParticipant findByEventUidAndParticipantUid(String eventUid, String participantUid) {

        String sql = SELECT_EVENT_PARTICIPANT_ORIG + " WHERE EI.EVENT_UUID = ? AND II.INSTITUTION_UUID = ?";
        EventOriginationParticipant eventOriginationParticipant;

        try {
            eventOriginationParticipant = jdbcTemplate.queryForObject(sql, new EventOriginationParticipantRowMapper(), eventUid, participantUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event Participant was not found for event and participant. ( event uid = %s, participant uid = %s )", eventUid, participantUid));
            throw new DataNotFoundException("Event Participant was not found for event and participant.");

        }

        return eventOriginationParticipant;
    }

    public List<EventOriginationParticipant> findAllByEventUid(String eventUid) {
        String sql = SELECT_EVENT_PARTICIPANT_ORIG + " WHERE EI.EVENT_UUID = ? ORDER BY CREATED_DATE";
        return jdbcTemplate.query(sql, new EventOriginationParticipantRowMapper(), eventUid);
    }

    public void save(EventOriginationParticipant eop) {

        Long inviteRecipientId = eop.getInviteRecipient() == null ? null : eop.getInviteRecipient().getId();

        jdbcTemplate.update(INSERT_EVENT_PARTICIPANT_ORIG, eop.getId(), inviteRecipientId, eop.getMessage()
                , eop.getResponse(), eop.getDeclinedMessage());
    }

    public void update(EventOriginationParticipant eop) {

        Long inviteRecipientId = eop.getInviteRecipient() == null ? null : eop.getInviteRecipient().getId();

        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_ORIG, inviteRecipientId, eop.getMessage(), eop.getResponse()
                , eop.getDeclinedMessage(), eop.getId());
    }

    public void updateInviteDate(Long eventParticipantId) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_ORIG_INVITE_DATE, eventParticipantId);
    }

    public void updateFullDealAccessDate(Long eventParticipantId) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_ORIG_FULL_DEAL_ACCESS_DATE, eventParticipantId);
    }

    public void updateForDeclinedEvent(Long eventParticipantId) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_ORIG_DECLINED_EVENT, eventParticipantId);
    }

    public void updateForRemovedFromEvent(Long eventParticipantId) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_ORIG_REMOVE_FROM_EVENT, eventParticipantId);
    }

    /**
     * This method will delete a specific event participant origination by the provided unique id.
     *
     * @param   id
     * @return  The number of rows deleted.
     */
    public int deleteById(Long id) {
        String sql = DELETE_EVENT_PARTICIPANT_ORIG + " WHERE EVENT_PARTICIPANT_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * This method will delete all participants for an event.  This is used when an event is deleted.
     *
     * @param   eventId
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventId(Long eventId) {
        String sql = DELETE_EVENT_PARTICIPANT_ORIG + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EVENT_PARTICIPANT_ID "
                                                                                      + "FROM EVENT_PARTICIPANT "
                                                                                     + "WHERE EVENT_ID = ? )";
        return jdbcTemplate.update(sql, eventId);
    }

    /**
     * This method will delete all participants from a deal for the specified institution.  This is used when an
     * institution is deleted.
     *
     * @param   participantId
     * @return  The number of rows deleted.
     */
    public int deleteAllByParticipantId(Long participantId) {
        String sql = DELETE_EVENT_PARTICIPANT_ORIG
            + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EVENT_PARTICIPANT_ID "
                                              + "FROM EVENT_PARTICIPANT "
                                             + "WHERE PARTICIPANT_ID = ? ) "
                + "OR EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                               + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                 + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN DEAL_INFO DI "
                                                 + "ON EI.DEAL_ID = DI.DEAL_ID "
                                              + "WHERE DI.ORIGINATOR_ID = ? )";
        return jdbcTemplate.update(sql, participantId, participantId);
    }

    /**
     * This method will delete all participants from a event, where the specified institution was an originator.  This is
     * used when an institution is deleted.
     *
     * @param   originatorUid The uid of the institution that was the originator of the deal(s).
     * @return  The number of rows deleted.
     */
    public int deleteAllByDealOriginatorUid(String originatorUid) {

        String sql = DELETE_EVENT_PARTICIPANT_ORIG + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                                                                   + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II "
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
        String sql = DELETE_EVENT_PARTICIPANT_ORIG + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                                                                      + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                                                        + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN PARTICIPANT_STEP_DEF PSD "
                                                                                        + "ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID "
                                                                                     + "WHERE EI.EVENT_UUID = ? "
                                                                                       + "AND PSD.ORDER_NBR = ? )";
        return jdbcTemplate.update(sql, eventUid, order);
    }

}