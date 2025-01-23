package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.DealUserConfidentialityAgreementRowMapper;
import com.westmonroe.loansyndication.model.DealUserConfidentialityAgreement;
import com.westmonroe.loansyndication.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.DealUserConfidentialityAgreementQueryDef.INSERT_DEAL_USER_CONF_AGRMNT;
import static com.westmonroe.loansyndication.querydef.DealUserConfidentialityAgreementQueryDef.SELECT_DEAL_USER_CONF_AGRMNT;

@Repository
@Slf4j
public class DealUserConfidentialityAgreementDao {

    private final JdbcTemplate jdbcTemplate;

    public DealUserConfidentialityAgreementDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DealUserConfidentialityAgreement> findDealUserConfidentialityAgreementsByDealId(Long dealId) {
        String sql = SELECT_DEAL_USER_CONF_AGRMNT + " WHERE DEAL_ID = ? ";
        return jdbcTemplate.query(sql, new DealUserConfidentialityAgreementRowMapper(), dealId);
    }

    public DealUserConfidentialityAgreement findDealUserConfidentialityAgreementByDealIdAndUserId(Long dealId, Long userId) {
        String sql = SELECT_DEAL_USER_CONF_AGRMNT + " WHERE DEAL_ID = ? AND USER_ID = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new DealUserConfidentialityAgreementRowMapper(), dealId, userId);
        } catch ( EmptyResultDataAccessException e ) {
            log.error(String.format("Deal user confidentiality agreement was not found for deal id ( id = %s) and user id ( id = %s ) combination", dealId, userId));
            throw new DataNotFoundException("Deal user confidentiality agreement was not found for deal id and user id combination.");
        }

    }

    public boolean save (Long dealId, User user, Integer confidentialityAgreementId) {

        try {
            int rowsAffected = jdbcTemplate.update(INSERT_DEAL_USER_CONF_AGRMNT, dealId, user.getId(), confidentialityAgreementId);
            return rowsAffected > 0;
        } catch (DuplicateKeyException pke) {
            log.error("Deal User Confidentiality Agreement could not be saved because of DuplicateKeyException. " +
                      "(user id = {}, deal id = {}, confidentiality agreement id = {})",
                      user.getId(), dealId, confidentialityAgreementId);
            throw new DataIntegrityException("Deal User Confidentiality Agreement could not be saved because it already exists.");
        }
    }
}