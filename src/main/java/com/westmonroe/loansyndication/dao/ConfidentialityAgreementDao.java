package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.ConfidentialityAgreementRowMapper;
import com.westmonroe.loansyndication.model.ConfidentialityAgreement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

import static com.westmonroe.loansyndication.querydef.ConfidentialityAgreementQueryDef.*;

@Repository
@Slf4j
public class ConfidentialityAgreementDao {

    private final JdbcTemplate jdbcTemplate;

    public ConfidentialityAgreementDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ConfidentialityAgreement saveConfidentialityAgreement(ConfidentialityAgreement confidentialityAgreement, Long userId) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_CONFIDENTIALITY_AGREEMENT, new String[] { "conf_agrmnt_id" });
            ps.setLong(index++, confidentialityAgreement.getInstitutionId());
            ps.setString(index++, confidentialityAgreement.getDescription());
            ps.setLong(index, userId);
            return ps;
        }, keyHolder);

        try {
            // Assign the unique id returned from the insert operation.
            confidentialityAgreement.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {
            log.error("Error retrieving unique id for Confidentiality Agreement.");
            throw new DatabaseException("Error retrieving unique id for Confidentiality Agreement.");
        }

        return confidentialityAgreement;
    }

    public ConfidentialityAgreement findConfidentialityAgreementByInstitutionyId(Long id) {
        String sql = SELECT_CONFIDENTIALITY_AGREEMENT + " WHERE INSTITUTION_ID =  ? ORDER BY CREATED_DATE DESC LIMIT 1";

        ConfidentialityAgreement confidentialityAgreement;

        try {
            confidentialityAgreement = jdbcTemplate.queryForObject(sql, new ConfidentialityAgreementRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Confidentiality agreement was not found for institution with that id. ( id = %s )", id));
            throw new DataNotFoundException("Confidentiality agreement was not found for institution with that id.");

        }

        return confidentialityAgreement;
    }

    public int deleteConfidentialityAgreementByInstitutionId(Long id) {
        String sql = DELETE_CONFIDENTIALITY_AGREEMENT + " WHERE INSTITUTION_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

}
