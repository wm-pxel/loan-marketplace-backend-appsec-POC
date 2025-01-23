package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.event.EventLeadFacilityRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventLeadFacilityQueryDef.*;

@Repository
@Slf4j
public class EventLeadFacilityDao {

    private final JdbcTemplate jdbcTemplate;

    public EventLeadFacilityDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EventLeadFacility findByEventLeadFacility(EventLeadFacility eventLeadFacility) {
        return findByEventIdAndEventDealFacilityId(
                eventLeadFacility.getEvent().getId(),
                eventLeadFacility.getEventDealFacility().getId()
        );
    }

    public EventLeadFacility findByEventIdAndEventDealFacilityId(Long eventId, Long eventDealFacilityId) {

        String sql = SELECT_EVENT_LEAD_FACILITY + " WHERE ELF.EVENT_ID = ? AND ELF.EVENT_DEAL_FACILITY_ID = ?";
        EventLeadFacility eventLeadFacility;

        try {
            eventLeadFacility = jdbcTemplate.queryForObject(sql, new EventLeadFacilityRowMapper(), eventId, eventDealFacilityId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error("Event Lead Facility was not found for id. ( eid = {}, edfid = {} )", eventId, eventDealFacilityId);
            throw new DataNotFoundException("Event Lead Facility was not found for id.");

        }

        return eventLeadFacility;
    }

    public List<EventLeadFacility> findAllByEventId(Long eventId) {
        String sql = SELECT_EVENT_LEAD_FACILITY + " WHERE ELF.EVENT_ID = ? "
                                                 + "ORDER BY DF.FACILITY_NAME";
        return jdbcTemplate.query(sql, new EventLeadFacilityRowMapper(), eventId);
    }

    public List<EventLeadFacility> findAllByEventUid(String eventUid) {
        String sql = SELECT_EVENT_LEAD_FACILITY + " WHERE EI.EVENT_UUID = ? "
                                                 + "ORDER BY DF.FACILITY_NAME";
        return jdbcTemplate.query(sql, new EventLeadFacilityRowMapper(), eventUid);
    }

    public int save(EventLeadFacility elf, User currentUser) {
        return jdbcTemplate.update(INSERT_EVENT_LEAD_FACILITY, elf.getEvent().getId()
                , elf.getEventDealFacility().getId(), elf.getInvitationAmount(), elf.getCommitmentAmount()
                , elf.getAllocationAmount(), currentUser.getId(), currentUser.getId());
    }

    public void update(EventLeadFacility elf) {
        jdbcTemplate.update(UPDATE_EVENT_LEAD_FACILITY, elf.getInvitationAmount(), elf.getCommitmentAmount(), elf.getAllocationAmount()
                , elf.getUpdatedBy().getId(), elf.getEvent().getId(), elf.getEventDealFacility().getId());
    }

    public void updateAllocation(BigDecimal allocationAmount, Long updatedById, String eventUid, Long eventDealFacilityId) {
        jdbcTemplate.update(UPDATE_EVENT_LEAD_FACILITY_ALLOCATION, allocationAmount, updatedById, eventUid, eventDealFacilityId);
    }

    /**
     * This method will delete all lead facilities from a event for the specified event id.  This is used when an
     * event is deleted.
     *
     * @param   eventId
     * @return  The number of rows deleted.
     */
    public void deleteAllByEventId(Long eventId) {
        String sql = DELETE_EVENT_LEAD_FACILITY + " WHERE EVENT_ID = ?";
        jdbcTemplate.update(sql, eventId);
    }

    /**
     * This method will delete a specific event lead facility for a event.
     *
     * @param   elf
     *
     * @return  The number of rows deleted.
     */
    public int delete(EventLeadFacility elf) {
        return delete(elf.getEvent().getId(), elf.getEventDealFacility().getId());
    }

    /**
     * This method will delete a specific event lead facility for a event.
     *
     * @param   eventId
     * @param   eventDealFacilityId
     *
     * @return  The number of rows deleted.
     */
    public int delete(Long eventId, Long eventDealFacilityId) {
        String sql = DELETE_EVENT_LEAD_FACILITY + " WHERE EVENT_ID = ? AND EVENT_DEAL_FACILITY_ID = ?";
        return jdbcTemplate.update(sql, eventId, eventDealFacilityId);
    }

    /**
     * This method will delete all of the event lead facility records for the supplied facility external id.
     *
     * @param dealFacilityExternalId
     * @return
     */
    public int deleteAllByFacilityExternalId(String dealFacilityExternalId) {
        String sql = DELETE_EVENT_LEAD_FACILITY + " WHERE EVENT_DEAL_FACILITY_ID IN ( SELECT EDF.EVENT_DEAL_FACILITY_ID "
                                                                                    + "FROM EVENT_DEAL_FACILITY EDF LEFT JOIN DEAL_FACILITY DF "
                                                                                      + "ON EDF.DEAL_FACILITY_ID = DF.DEAL_FACILITY_ID "
                                                                                   + "WHERE DF.FACILITY_EXTERNAL_UUID = ? )";
        return jdbcTemplate.update(sql, dealFacilityExternalId);
    }

}