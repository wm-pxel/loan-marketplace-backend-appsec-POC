package com.westmonroe.loansyndication.dao.deal;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.deal.DealEventRowMapper;
import com.westmonroe.loansyndication.mapper.deal.DealEventSummaryRowMapper;
import com.westmonroe.loansyndication.mapper.deal.DealRowMapper;
import com.westmonroe.loansyndication.mapper.deal.DealSummaryRowMapper;
import com.westmonroe.loansyndication.mapper.integration.DealDataRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealEvent;
import com.westmonroe.loansyndication.model.deal.DealEventSummary;
import com.westmonroe.loansyndication.model.deal.DealSummary;
import com.westmonroe.loansyndication.model.integration.DealData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.deal.DealQueryDef.*;
import static com.westmonroe.loansyndication.utils.DealStageEnum.STAGE_1;

@Repository
@Slf4j
public class DealDao {

    private final JdbcTemplate jdbcTemplate;

    public DealDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Deal> findAllByUser(User user) {
        String sql = SELECT_DEAL + " ORDER BY DEAL_NAME";
        return jdbcTemplate.query(sql, new DealRowMapper(), user.getInstitution().getId(), user.getInstitution().getId(), user.getId(), user.getId());
    }

    public List<DealSummary> findSummaryByInstitutionId(Long institutionId, Long userId) {
        String sql = SELECT_DEAL_SUMMARY + " ORDER BY DEAL_NAME";
        return jdbcTemplate.query(sql, new DealSummaryRowMapper(), institutionId, userId, userId, institutionId, userId, userId);
    }

    public List<DealEventSummary> findEventSummaryByInstitutionId(Long institutionId, Long userId) {
        String sql = SELECT_DEAL_EVENT_SUMMARY + " ORDER BY DEAL_NAME";
        return jdbcTemplate.query(sql, new DealEventSummaryRowMapper(), institutionId, userId, userId, institutionId, userId, userId);
    }

    public List<Deal> findAllByOriginatorId(Long originatorId, User user) {
        String sql = SELECT_DEAL + " WHERE DI.ORIGINATOR_ID = ? ORDER BY DEAL_NAME";
        return jdbcTemplate.query(sql, new DealRowMapper(),user.getInstitution().getId(), user.getInstitution().getId(), user.getId(), user.getId(), originatorId);
    }

    public List<Deal> findAllByOriginatorUid(String originatorUid, User user) {
        String sql = SELECT_DEAL + " WHERE II.INSTITUTION_UUID = ? ORDER BY DEAL_NAME";
        return jdbcTemplate.query(sql, new DealRowMapper(), user.getInstitution().getId(), user.getInstitution().getId(), user.getId(), user.getId(), originatorUid);
    }

    /**
     * This method gets all of the deals for the supplied participant (institution), where they're a participant. This
     * should be moved to a DealDataDao class but created here to reduce the number of unnecessary spring beans.
     *
     * @param   participantUid   The uid of the participating institution.
     * @return  List<DealData>
     */
    public List<DealData> findAllByParticipantUid(String participantUid) {
        String sql = SELECT_DEAL_ID + " WHERE DI.DEAL_ID IN ( SELECT EI.DEAL_ID "
                                                            + "FROM EVENT_INFO EI LEFT JOIN EVENT_PARTICIPANT EP "
                                                              + "ON EI.EVENT_ID = EP.EVENT_ID LEFT JOIN INSTITUTION_INFO II2 "
                                                              + "ON EP.PARTICIPANT_ID = II2.INSTITUTION_ID "
                                                           + "WHERE II2.INSTITUTION_UUID = ? ) "
                                     + "ORDER BY DEAL_NAME";
        return jdbcTemplate.query(sql, new DealDataRowMapper(), participantUid);
    }

    public Long findIdByUid(String dealUid) {

        String sql = "SELECT DI.DEAL_ID FROM DEAL_INFO DI WHERE DI.DEAL_UUID = ?";
        Long dealId;

        try {
            dealId = jdbcTemplate.queryForObject(sql, Long.class, dealUid);
        } catch (EmptyResultDataAccessException e) {

            log.error(String.format("Deal was not found for id ( id = %s ).", dealUid));
            throw new DataNotFoundException("Deal was not found for id.");

        }

        return dealId;
    }

    public Deal findById(Long id, User user) {

        String sql = SELECT_DEAL + " WHERE DI.DEAL_ID = ?";
        Deal deal;

        try {
            deal = jdbcTemplate.queryForObject(sql, new DealRowMapper(), user.getInstitution().getId(), user.getInstitution().getId(), user.getId(), user.getId(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Deal was not found for id.");

        }

        return deal;
    }

    public Deal findByUid(String uid, User user) {

        String sql = SELECT_DEAL + " WHERE DI.DEAL_UUID = ?";
        Deal deal;

        try {
            deal = jdbcTemplate.queryForObject(sql, new DealRowMapper(), user.getInstitution().getId(), user.getInstitution().getId(), user.getId(), user.getId(), uid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal was not found for uid. ( uid = %s )", uid));
            throw new DataNotFoundException("Deal was not found for uid.");

        }

        return deal;
    }

    public DealEvent findDealEventByUid(String uid, User user) {

        String sql = SELECT_DEAL_EVENT + " WHERE DI.DEAL_UUID = ?";
        DealEvent dealEvent;

        try {
            dealEvent = jdbcTemplate.queryForObject(sql, new DealEventRowMapper(), user.getInstitution().getId(), user.getInstitution().getId(), user.getId(), user.getId(), uid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal was not found for uid. ( uid = %s )", uid));
            throw new DataNotFoundException("Deal was not found for uid.");

        }

        return dealEvent;
    }

    public Deal findByExternalId(String externalId, User user) {

        String sql = SELECT_DEAL + " WHERE DI.DEAL_EXTERNAL_UUID = ?";
        Deal deal;

        try {
            deal = jdbcTemplate.queryForObject(sql, new DealRowMapper(), user.getInstitution().getId()
                    , user.getInstitution().getId(), user.getId(), user.getId(), externalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal was not found for external id. ( id = %s )", externalId));
            throw new DataNotFoundException("Deal was not found for external id.");

        }

        return deal;
    }

    public DealEvent findDealEventByExternalId(String externalId, User user){
        String sql = SELECT_DEAL_EVENT + " WHERE DI.DEAL_EXTERNAL_UUID = ?";
        DealEvent dealEvent;

        try {
            dealEvent = jdbcTemplate.queryForObject(sql, new DealEventRowMapper(), user.getInstitution().getId(), user.getInstitution().getId(), user.getId()
                    , user.getId(), externalId);
        } catch ( EmptyResultDataAccessException e ) {
            log.error(String.format("Deal was not found for external id. ( id = %s )", externalId));
            throw new DataNotFoundException("Deal was not found for external id.");
        }
        return dealEvent;
    }

    public Deal findByEventUid(String eventUid, User user) {

        String sql = SELECT_DEAL + " WHERE DI.DEAL_ID = ( SELECT DEAL_ID FROM EVENT_INFO WHERE EVENT_UUID = ? )";
        Deal deal;

        try {
            deal = jdbcTemplate.queryForObject(sql, new DealRowMapper(), user.getInstitution().getId()
                    , user.getInstitution().getId(), user.getId(), user.getId(), eventUid);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal was not found for event uid. ( id = %s )", eventUid));
            throw new DataNotFoundException("Deal was not found for event uid.");

        }

        return deal;
    }

    public Deal save(Deal d) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_DEAL, new String[] { "deal_id" });
            ps.setString(index++, d.getUid());
            ps.setString(index++, d.getDealExternalId());
            ps.setString(index++, d.getName());
            ps.setLong(index++, d.getDealIndustry().getId());
            ps.setLong(index++, d.getOriginator().getId());
            ps.setString(index++, d.getInitialLenderFlag());

            if ( d.getInitialLenderFlag() == null || d.getInitialLenderFlag().equals("N") || d.getInitialLender() == null ) {
                ps.setNull(index++, java.sql.Types.BIGINT);
            } else {
                ps.setLong(index++, d.getInitialLender().getId());
            }

            ps.setLong(index++, STAGE_1.getOrder());                // The stage will always start at one for a new deal.
            ps.setLong(index++, d.getDealStructure().getId());
            ps.setString(index++, d.getDealType());
            ps.setString(index++, d.getDescription());
            ps.setBigDecimal(index++, d.getDealAmount());
            ps.setString(index++, d.getApplicantExternalId());
            ps.setString(index++, d.getBorrowerDesc());
            ps.setString(index++, d.getBorrowerName());
            ps.setString(index++, d.getBorrowerCityName());
            ps.setString(index++, d.getBorrowerStateCode());
            ps.setString(index++, d.getBorrowerCountyName());

            if ( d.getFarmCreditElig() == null || d.getFarmCreditElig().getId() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, d.getFarmCreditElig().getId());
            }

            ps.setString(index++, d.getTaxId());

            if ( d.getBorrowerIndustry() == null ||  d.getBorrowerIndustry().getCode() == null ) {
                ps.setString(index++, null);
            } else {
                ps.setString(index++, d.getBorrowerIndustry().getCode());
            }

            if ( d.getBusinessAge() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setInt(index++, d.getBusinessAge());
            }

            if ( d.getDefaultProbability() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setInt(index++, d.getDefaultProbability());
            }

            if ( d.getCurrYearEbita() == null ) {
                ps.setNull(index++, Types.DOUBLE);
            } else {
                ps.setBigDecimal(index++, d.getCurrYearEbita());
            }

            ps.setLong(index++, d.getCreatedBy().getId());
            ps.setLong(index++, d.getCreatedBy().getId());
            ps.setString(index, d.getActive());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            d.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Deal.");
            throw new DatabaseException("Error retrieving unique id for Deal.");

        }

        return d;
    }

    public void update(Deal d) {

        Long initialLenderId = null;
        Long farmCreditEligId = d.getFarmCreditElig() == null ? null : d.getFarmCreditElig().getId();
        String borrowerIndustryCode = d.getBorrowerIndustry() == null ? null : d.getBorrowerIndustry().getCode();

        if ( d.getInitialLenderFlag() != null && d.getInitialLenderFlag().equals("Y") && d.getInitialLender() != null ) {
            initialLenderId = d.getInitialLender().getId();
        }

        jdbcTemplate.update(UPDATE_DEAL, d.getName(), d.getDealIndustry().getId(), d.getInitialLenderFlag(), initialLenderId
            , d.getDealStructure().getId(), d.getDealType(), d.getDescription(), d.getDealAmount(), d.getBorrowerDesc()
            , d.getBorrowerName(), d.getBorrowerCityName(), d.getBorrowerStateCode(), d.getBorrowerCountyName(), farmCreditEligId
            , d.getTaxId(), borrowerIndustryCode, d.getBusinessAge(), d.getDefaultProbability()
            , d.getCurrYearEbita(), d.getUpdatedBy().getId(), d.getActive(), d.getId());
    }

    /**
     * This method is used to increment the last facility number by one. The facility names are generated in the BE and
     * are given sequential names (Facility A, Facility B, ...). This helps reconcile that naming convention.
     *
     * @param dealId                The unique Deal ID.
     */
    public void updateLastFacilityNumber(Long dealId) {
        jdbcTemplate.update(UPDATE_LAST_FACILITY_NBR, dealId);
    }

    public int delete(Deal u) {
        return deleteById(u.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_DEAL + " WHERE DEAL_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteByUid(String uid) {
        String sql = DELETE_DEAL + " WHERE DEAL_UUID = ?";
        return jdbcTemplate.update(sql, uid);
    }

    public int deleteAllByOriginatorId(Long id) {
        String sql = DELETE_DEAL + " WHERE ORIGINATOR_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteAllByOriginatorUid(String institutionUid) {
        String sql = DELETE_DEAL + " WHERE ORIGINATOR_ID = ( SELECT INSTITUTION_ID "
                                                            + "FROM INSTITUTION_INFO "
                                                           + "WHERE INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, institutionUid);
    }

}