package com.westmonroe.loansyndication.dao.deal;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.mapper.deal.DealCovenantRowMapper;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.deal.DealCovenantQueryDef.*;

@Repository
@Slf4j
public class DealCovenantDao {

    private final JdbcTemplate jdbcTemplate;

    public DealCovenantDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DealCovenant> findAll() {
        throw new OperationNotAllowedException("The findAll() method is not implemented.");
    }

    public DealCovenant findById(Long id) {
        String sql = SELECT_DEAL_COVENANT + " WHERE DC.DEAL_COVENANT_ID = ?";
        DealCovenant covenant;

        try {
            covenant = jdbcTemplate.queryForObject(sql, new DealCovenantRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal covenant was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Deal covenant was not found for id.");

        }

        return covenant;
    }

    public DealCovenant findByExternalId(String externalId) {
        String sql = SELECT_DEAL_COVENANT + " WHERE DC.COVENANT_EXTERNAL_UUID = ?";
        DealCovenant covenant;

        try {
            covenant = jdbcTemplate.queryForObject(sql, new DealCovenantRowMapper(), externalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Deal covenant was not found for external id. ( id = %s )", externalId));
            throw new DataNotFoundException("Deal covenant was not found for external id.");

        }

        return covenant;
    }

    public List<DealCovenant> findAllByDealUid(String dealUid) {
        String sql = SELECT_DEAL_COVENANT + " WHERE DC.DEAL_ID = ( SELECT DEAL_ID "
                                                                  + "FROM DEAL_INFO "
                                                                 + "WHERE DEAL_UUID = ? ) "
                                           + "ORDER BY ENTITY_NAME";
        return jdbcTemplate.query(sql, new DealCovenantRowMapper(), dealUid);
    }

    public DealCovenant save(DealCovenant covenant) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_DEAL_COVENANT, new String[] { "deal_covenant_id" });
            ps.setString(index++, covenant.getCovenantExternalId());
            ps.setLong(index++, covenant.getDeal().getId());
            ps.setString(index++, covenant.getEntityName());
            ps.setString(index++, covenant.getCategoryName());
            ps.setString(index++, covenant.getCovenantType());
            ps.setString(index++, covenant.getFrequency());
            ps.setObject(index++, covenant.getNextEvalDate());
            ps.setObject(index++, covenant.getEffectiveDate());
            ps.setLong(index++, covenant.getCreatedBy().getId());
            ps.setLong(index, covenant.getCreatedBy().getId());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            covenant.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Deal Covenant.");
            throw new DatabaseException("Error retrieving unique id for Deal Covenant.");

        }

        return covenant;
    }

    public void update(DealCovenant dc) {
        jdbcTemplate.update(UPDATE_DEAL_COVENANT, dc.getEntityName(), dc.getCategoryName(), dc.getCovenantType()
            , dc.getFrequency(), dc.getNextEvalDate(), dc.getEffectiveDate(), dc.getUpdatedBy().getId(), dc.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_DEAL_COVENANT + " WHERE DEAL_COVENANT_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int deleteByExternalId(String externalId) {
        String sql = DELETE_DEAL_COVENANT + " WHERE COVENANT_EXTERNAL_UUID = ?";
        return jdbcTemplate.update(sql, externalId);

    }

    public int deleteAllByDealId(Long dealId) {
        String sql = DELETE_DEAL_COVENANT + " WHERE DEAL_ID = ?";
        return jdbcTemplate.update(sql, dealId);
    }

    public int deleteAllByDealUid(String dealUid) {
        String sql = DELETE_DEAL_COVENANT + " WHERE DEAL_ID = ( SELECT DEAL_ID "
                                                               + "FROM DEAL_INFO "
                                                              + "WHERE DEAL_UUID = ? )";
        return jdbcTemplate.update(sql, dealUid);
    }

    public int deleteAllByDealOriginatorId(Long originatorId) {
        String sql = DELETE_DEAL_COVENANT + " WHERE DEAL_ID IN ( SELECT DEAL_ID "
                                                                + "FROM DEAL_INFO "
                                                               + "WHERE ORIGINATOR_ID = ? )";
        return jdbcTemplate.update(sql, originatorId);
    }

    public int deleteAllByDealOriginatorUid(String originatorUid) {
        String sql = DELETE_DEAL_COVENANT + " WHERE DEAL_ID IN ( SELECT DI.DEAL_ID "
                                                                + "FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II "
                                                                  + "ON DI.ORIGINATOR_ID = II.INSTITUTION_ID "
                                                               + "WHERE II.INSTITUTION_UUID = ? )";
        return jdbcTemplate.update(sql, originatorUid);
    }

}