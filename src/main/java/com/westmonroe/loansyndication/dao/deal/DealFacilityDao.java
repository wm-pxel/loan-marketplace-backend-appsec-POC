package com.westmonroe.loansyndication.dao.deal;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.mapper.deal.DealFacilityRowMapper;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.deal.DealFacilityQueryDef.*;

@Repository
@Slf4j
public class DealFacilityDao {

    private final JdbcTemplate jdbcTemplate;

    public DealFacilityDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DealFacility> findAll() {
        throw new OperationNotAllowedException("The findAll() method is not implemented.");
    }

    public DealFacility findById(Long id) {
        String sql = SELECT_DEAL_FACILITY + " WHERE DF.DEAL_FACILITY_ID = ?";
        DealFacility facility;

        try {
            facility = jdbcTemplate.queryForObject(sql, new DealFacilityRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal facility was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Deal facility was not found for id.");

        }

        return facility;
    }

    public DealFacility findByExternalId(String externalId) {
        String sql = SELECT_DEAL_FACILITY + " WHERE DF.FACILITY_EXTERNAL_UUID = ?";
        DealFacility facility;

        try {
            facility = jdbcTemplate.queryForObject(sql, new DealFacilityRowMapper(), externalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal facility was not found for external id. ( id = %s )", externalId));
            throw new DataNotFoundException("Deal facility was not found for external id.");

        }

        return facility;
    }

    public List<DealFacility> findAllByDealId(Long dealId) {
        String sql = SELECT_DEAL_FACILITY + " WHERE DF.DEAL_ID =  ? ORDER BY DEAL_FACILITY_ID";
        return jdbcTemplate.query(sql, new DealFacilityRowMapper(), dealId);
    }

    public List<DealFacility> findAllByDealUid(String dealUid) {
        String sql = SELECT_DEAL_FACILITY + " WHERE DF.DEAL_ID = ( SELECT DEAL_ID "
                                                                  + "FROM DEAL_INFO "
                                                                 + "WHERE DEAL_UUID = ? ) "
                                                                 + "ORDER BY DEAL_FACILITY_ID";
        return jdbcTemplate.query(sql, new DealFacilityRowMapper(), dealUid);
    }

    public List<DealFacility> findAllByEventId(Long eventId) {
        String sql = SELECT_DEAL_FACILITY + " WHERE DF.DEAL_FACILITY_ID IN ( SELECT DEAL_FACILITY_ID "
                                                                            + "FROM EVENT_DEAL_FACILITY "
                                                                           + "WHERE EVENT_ID = ? ) "
                                           + "ORDER BY FACILITY_NAME";
        return jdbcTemplate.query(sql, new DealFacilityRowMapper(), eventId);
    }

    public List<DealFacility> findAllByEventUid(String eventUid) {
        String sql = SELECT_DEAL_FACILITY + " WHERE DF.DEAL_FACILITY_ID IN ( SELECT EDF.DEAL_FACILITY_ID "
                                                                            + "FROM EVENT_DEAL_FACILITY EDF LEFT JOIN EVENT_INFO EI "
                                                                              + "ON EDF.EVENT_ID = EI.EVENT_ID "
                                                                           + "WHERE EI.EVENT_UUID = ? ) "
                                            + "ORDER BY FACILITY_NAME";
        return jdbcTemplate.query(sql, new DealFacilityRowMapper(), eventUid);
    }

    public DealFacility save(DealFacility facility) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_DEAL_FACILITY, new String[] { "deal_facility_id" });
            ps.setString(index++, facility.getFacilityExternalId());
            ps.setLong(index++, facility.getDeal().getId());
            ps.setString(index++, facility.getFacilityName());
            ps.setBigDecimal(index++, facility.getFacilityAmount());

            if ( facility.getFacilityType() == null || facility.getFacilityType().getId() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, facility.getFacilityType().getId());
            }

            if ( facility.getTenor() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setInt(index++, facility.getTenor());
            }

            if ( facility.getCollateral() == null || facility.getCollateral().getId() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, facility.getCollateral().getId());
            }

            ps.setString(index++, facility.getPricing());
            ps.setString(index++, facility.getCreditSpreadAdj());

            if ( facility.getFacilityPurpose() == null || facility.getFacilityPurpose().getId() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, facility.getFacilityPurpose().getId());
            }

            ps.setString(index++, facility.getPurposeDetail());

            if ( facility.getDayCount() == null || facility.getDayCount().getId() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setLong(index++, facility.getDayCount().getId());
            }

            ps.setString(index++, facility.getGuarInvFlag());
            ps.setString(index++, facility.getPatronagePayingFlag());
            ps.setString(index++, facility.getFarmCreditType());

            if ( facility.getRevolverUtil() == null ) {
                ps.setNull(index++, Types.INTEGER);
            } else {
                ps.setInt(index++, facility.getRevolverUtil());
            }

            ps.setString(index++, facility.getUpfrontFees());
            ps.setString(index++, facility.getUnusedFees());
            ps.setString(index++, facility.getAmortization());
            ps.setObject(index++, facility.getMaturityDate());
            ps.setObject(index++, facility.getRenewalDate());

            ps.setLong(index++, facility.getCreatedBy().getId());
            ps.setLong(index++, facility.getCreatedBy().getId());
            ps.setString(index++, facility.getLgdOption());

            if ( facility.getRegulatoryLoanType() == null || facility.getRegulatoryLoanType().getId() == null ) {
                ps.setNull(index, Types.INTEGER);
            } else {
                ps.setLong(index, facility.getRegulatoryLoanType().getId());
            }

            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            facility.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Deal Facility.");
            throw new DatabaseException("Error retrieving unique id for Deal Facility.");

        }

        return facility;
    }

    public void update(DealFacility df) {

        Long facilityTypeId = df.getFacilityType() == null ? null : df.getFacilityType().getId();
        Long pricingGridId = df.getPricingGrid() == null ? null : df.getPricingGrid().getId();
        Long facilityPurposeId = df.getFacilityPurpose() == null ? null : df.getFacilityPurpose().getId();
        Long dayCountId = df.getDayCount() == null ? null : df.getDayCount().getId();
        Long collateralId = df.getCollateral() == null ? null : df.getCollateral().getId();
        Long regulatoryLoanTypeId = df.getRegulatoryLoanType() == null ? null : df.getRegulatoryLoanType().getId();

        jdbcTemplate.update(UPDATE_DEAL_FACILITY, df.getFacilityAmount(), facilityTypeId, df.getTenor(), collateralId
                , pricingGridId, df.getPricing(), df.getCreditSpreadAdj(), facilityPurposeId, df.getPurposeDetail()
                , dayCountId, df.getGuarInvFlag(), df.getPatronagePayingFlag(), df.getFarmCreditType(), df.getRevolverUtil(), df.getUpfrontFees()
                , df.getUnusedFees(), df.getAmortization(), df.getMaturityDate(), df.getRenewalDate(), df.getLgdOption(), regulatoryLoanTypeId
                , df.getUpdatedBy().getId(), df.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_DEAL_FACILITY + " WHERE DEAL_FACILITY_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteByExternalId(String externalId) {
        String sql = DELETE_DEAL_FACILITY + " WHERE FACILITY_EXTERNAL_UUID = ?";
        return jdbcTemplate.update(sql, externalId);
    }

    public int deleteAllByDealId(Long dealId) {
        String sql = DELETE_DEAL_FACILITY + " WHERE DEAL_ID = ?";
        return jdbcTemplate.update(sql, dealId);
    }

    public int deleteAllByDealUid(String dealUid) {
        String sql = DELETE_DEAL_FACILITY + " WHERE DEAL_ID = ( SELECT DEAL_ID "
                                                               + "FROM DEAL_INFO "
                                                              + "WHERE DEAL_UUID = ? )";
        return jdbcTemplate.update(sql, dealUid);
    }

    public int deleteAllByDealOriginatorId(Long originatorId) {
        String sql = DELETE_DEAL_FACILITY + " WHERE DEAL_ID IN ( SELECT DEAL_ID "
                                                                + "FROM DEAL_INFO "
                                                               + "WHERE ORIGINATOR_ID = ? )";
        return jdbcTemplate.update(sql, originatorId);
    }

    public int deleteAllByDealOriginatorUid(String originatorUid) {
        String sql = DELETE_DEAL_FACILITY + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                                                                + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II "
                                                                  + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                               + "WHERE II.INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, originatorUid);
    }

}