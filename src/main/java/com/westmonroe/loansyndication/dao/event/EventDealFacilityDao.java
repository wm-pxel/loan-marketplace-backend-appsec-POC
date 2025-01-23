package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.event.EventDealFacilityRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventDealFacilityQueryDef.*;

@Repository
@Slf4j
public class EventDealFacilityDao {

    private final JdbcTemplate jdbcTemplate;

    public EventDealFacilityDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EventDealFacility findById(Long id) {
        String sql = SELECT_EVENT_DEAL_FACILITY + " WHERE EDF.EVENT_DEAL_FACILITY_ID = ?";
        EventDealFacility eventDealFacility;

        try {
            eventDealFacility = jdbcTemplate.queryForObject(sql, new EventDealFacilityRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event deal facility was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Event deal facility was not found for id.");

        }

        return eventDealFacility;
    }

    public List<EventDealFacility> findAllByEventId(Long eventId) {
        String sql = SELECT_EVENT_DEAL_FACILITY + " WHERE EDF.EVENT_ID = ? ORDER BY CREATED_DATE";
        return jdbcTemplate.query(sql, new EventDealFacilityRowMapper(), eventId);
    }

    public List<EventDealFacility> findAllByEventUid(String eventUid) {
        String sql = SELECT_EVENT_DEAL_FACILITY + " WHERE EI.EVENT_UUID = ? ORDER BY CREATED_DATE";
        return jdbcTemplate.query(sql, new EventDealFacilityRowMapper(), eventUid);
    }

    public EventDealFacility save(EventDealFacility edf) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_EVENT_DEAL_FACILITY, new String[] { "event_deal_facility_id" });
            ps.setLong(index++, edf.getEvent().getId());
            ps.setLong(index++, edf.getDealFacility().getId());
            ps.setLong(index++, edf.getCreatedBy().getId());
            ps.setLong(index, edf.getCreatedBy().getId());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            edf.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Event Deal Facility.");
            throw new DatabaseException("Error retrieving unique id for Event Deal Facility.");

        }

        return edf;
    }

    public void insertEventDealFacilitiesForEvent(Event event, User currentUser) {
        jdbcTemplate.query(INSERT_EVENT_DEAL_FACILITIES_FOR_EVENT, new EventDealFacilityRowMapper(), event.getId()
                , currentUser.getId(), currentUser.getId(), event.getDeal().getId());
    }

    public int delete(EventDealFacility eventDealFacility) {
        return deleteById(eventDealFacility.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_EVENT_DEAL_FACILITY + " WHERE EVENT_DEAL_FACILITY_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteAllByEventId(Long eventId) {
        String sql = DELETE_EVENT_DEAL_FACILITY + " WHERE EVENT_ID = ?";
        return jdbcTemplate.update(sql, eventId);
    }

    public int deleteAllByParticipantId(Long participantId) {
        String sql = DELETE_EVENT_DEAL_FACILITY + " WHERE EVENT_ID IN ( SELECT DI.DEAL_ID "
                                                                       + "FROM EVENT_INFO EI LEFT JOIN DEAL_INFO DI "
                                                                         + "ON EI.DEAL_ID = DI.DEAL_ID LEFT JOIN INSTITUTION_INFO II "
                                                                         + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                                      + "WHERE II.INSTITUTION_ID = ? )";
        return jdbcTemplate.update(sql, participantId);
    }

    public int deleteAllByFacilityExternalId(String dealFacilityExternalId) {
        String sql = DELETE_EVENT_DEAL_FACILITY + " WHERE DEAL_FACILITY_ID IN ( SELECT DEAL_FACILITY_ID "
                                                                               + "FROM DEAL_FACILITY "
                                                                              + "WHERE FACILITY_EXTERNAL_UUID = ? )";
        return jdbcTemplate.update(sql, dealFacilityExternalId);
    }

}