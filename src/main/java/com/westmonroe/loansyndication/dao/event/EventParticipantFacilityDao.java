package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.event.EventParticipantFacilityRowMapper;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventParticipantFacilityQueryDef.*;

@Repository
@Slf4j
public class EventParticipantFacilityDao {

    private final JdbcTemplate jdbcTemplate;

    public EventParticipantFacilityDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EventParticipantFacility findByEventParticipantFacility(EventParticipantFacility eventParticipantFacility) {
        return findByParticipantIdAndEventDealFacilityId(
                eventParticipantFacility.getEventParticipant().getId(),
                eventParticipantFacility.getEventDealFacility().getId()
        );
    }

    public EventParticipantFacility findByParticipantIdAndEventDealFacilityId(Long eventParticipantId, Long eventDealFacilityId) {

        String sql = SELECT_EVENT_PARTICIPANT_FACILITY + " WHERE EPF.EVENT_PARTICIPANT_ID = ? AND EPF.EVENT_DEAL_FACILITY_ID = ?";
        EventParticipantFacility eventParticipantFacility;

        try {
            eventParticipantFacility = jdbcTemplate.queryForObject(sql, new EventParticipantFacilityRowMapper(), eventParticipantId, eventDealFacilityId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal Participant Facility was not found for id. ( pid = %d, fid = %d )", eventParticipantId, eventDealFacilityId));
            throw new DataNotFoundException("Deal Participant Facility was not found for id.");

        }

        return eventParticipantFacility;
    }

    public List<EventParticipantFacility> findAllByEventId(Long dealId) {
        String sql = SELECT_EVENT_PARTICIPANT_FACILITY + " WHERE EP.EVENT_ID = ? "
                                                          + "AND EP.PARTICIPANT_ID IS NOT NULL "
                                                        + "ORDER BY EP.PARTICIPANT_ID, DF.DEAL_FACILITY_ID";
        return jdbcTemplate.query(sql, new EventParticipantFacilityRowMapper(), dealId);
    }

    public List<EventParticipantFacility> findAllByEventAndParticipantId(Long eventId, Long participantId) {
        String sql = SELECT_EVENT_PARTICIPANT_FACILITY + " WHERE EP.EVENT_ID = ? "
                                                         + "AND EP.PARTICIPANT_ID = ? "
                                                       + "ORDER BY EP.PARTICIPANT_ID, DF.DEAL_FACILITY_ID";
        return jdbcTemplate.query(sql, new EventParticipantFacilityRowMapper(), eventId, participantId);
    }

    public List<EventParticipantFacility> findAllByEventParticipantId(Long eventParticipantId) {
        String sql = SELECT_EVENT_PARTICIPANT_FACILITY + " WHERE EPF.EVENT_PARTICIPANT_ID = ? ORDER BY DF.DEAL_FACILITY_ID";
        return jdbcTemplate.query(sql, new EventParticipantFacilityRowMapper(), eventParticipantId);
    }

    public int save(EventParticipantFacility epf) {
        return jdbcTemplate.update(INSERT_EVENT_PARTICIPANT_FACILITY, epf.getEventParticipant().getId()
                , epf.getEventDealFacility().getId(), epf.getInvitationAmount(), epf.getCommitmentAmount()
                , epf.getAllocationAmount(), epf.getCreatedBy().getId(), epf.getCreatedBy().getId());
    }

    public void update(EventParticipantFacility epf) {
        jdbcTemplate.update(UPDATE_EVENT_PARTICIPANT_FACILITY, epf.getInvitationAmount(), epf.getCommitmentAmount(), epf.getAllocationAmount()
                , epf.getUpdatedBy().getId(), epf.getEventParticipant().getId(), epf.getEventDealFacility().getId());
    }

    /**
     * This method will delete all allocations from a event for the specified institution.  This is used when an
     * institution is deleted or declined after an allocation is set.
     *
     * @param   eventParticipantId
     * @return  The number of rows deleted.
     */
    public void deleteAllocationsByEventParticipantId(Long eventParticipantId) {
        jdbcTemplate.update(REMOVE_EVENT_PARTICIPANT_FACILITY_ALLOCATIONS, eventParticipantId);
    }

    /**
     * This method will delete a specific event participant facility for a event.
     *
     * @param   epf
     *
     * @return  The number of rows deleted.
     */
    public int delete(EventParticipantFacility epf) {
        return delete(epf.getEventParticipant().getId(), epf.getEventDealFacility().getId());
    }

    /**
     * This method will delete a specific event participant facility for a event.
     *
     * @param   participantId
     * @param   eventDealFacilityId
     *
     * @return  The number of rows deleted.
     */
    public int delete(Long participantId, Long eventDealFacilityId) {
        String sql = DELETE_EVENT_PARTICIPANT_FACILITY + " WHERE EVENT_PARTICIPANT_ID = ? AND EVENT_DEAL_FACILITY_ID = ?";
        return jdbcTemplate.update(sql, participantId, eventDealFacilityId);
    }

    /**
     * This method will delete all event participant facilities for the provided event participant id.
     *
     * @param   eventParticipantId
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventParticipantId(Long eventParticipantId) {
        String sql = DELETE_EVENT_PARTICIPANT_FACILITY + " WHERE EVENT_PARTICIPANT_ID =  ?";
        return jdbcTemplate.update(sql, eventParticipantId);
    }

    /**
     * This method will delete all participant facilities for an event.  This is used when an event is deleted.
     *
     * @param   eventId
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventId(Long eventId) {
        String sql = DELETE_EVENT_PARTICIPANT_FACILITY
                + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                                   + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                     + "ON EP.EVENT_ID = EI.EVENT_ID "
                                                  + "WHERE EI.EVENT_ID = ? )";
        return jdbcTemplate.update(sql, eventId);
    }

    /**
     * This method will delete all event participant facilities for a deal and step.  This is used when a event is closed.
     *
     * @param   eventUid
     * @param   order
     * @return  The number of rows deleted.
     */
    public int deleteAllByEventUidAndStepOrder(String eventUid, int order) {
        String sql = DELETE_EVENT_PARTICIPANT_FACILITY + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                                                                          + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                                                            + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN PARTICIPANT_STEP_DEF PSD "
                                                                                            + "ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID "
                                                                                         +" WHERE EI.EVENT_UUID = ? "
                                                                                           + "AND PSD.ORDER_NBR = ? )";
        return jdbcTemplate.update(sql, eventUid, order);
    }

    /**
     * This method will delete all event participant facilities from an event for the specified participant
     * (institution).  This is used when an institution is deleted.
     *
     * @param   participantId
     * @return  The number of rows deleted.
     */
    public int deleteAllByParticipantId(Long participantId) {
        String sql = DELETE_EVENT_PARTICIPANT_FACILITY
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
     * This method will delete all event participant facilities from an event for the specified institution.  This is
     * used when an institution is deleted.
     *
     * @param   institutionUid
     * @return  The number of rows deleted.
     */
    public int deleteAllByInstitutionUid(String institutionUid) {
        String sql = DELETE_EVENT_PARTICIPANT_FACILITY
                + " WHERE EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                                   + "FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI "
                                                     + "ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN DEAL_INFO DI "
                                                     + "ON EI.DEAL_ID = DI.DEAL_ID LEFT JOIN INSTITUTION_INFO II "
                                                     + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                  + "WHERE II.INSTITUTION_UUID = ? ) "
                    + "OR EVENT_PARTICIPANT_ID IN ( SELECT EP.EVENT_PARTICIPANT_ID "
                                                   + "FROM EVENT_PARTICIPANT EP LEFT JOIN INSTITUTION_INFO II "
                                                     + "ON EP.PARTICIPANT_ID = II.INSTITUTION_ID "
                                                  + "WHERE II.INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, institutionUid, institutionUid, institutionUid);
    }

}