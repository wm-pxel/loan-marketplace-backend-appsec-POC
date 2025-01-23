package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.EndUserAgreementRowMapper;
import com.westmonroe.loansyndication.model.EndUserAgreement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import static com.westmonroe.loansyndication.querydef.EndUserAgreementQueryDef.SELECT_END_USER_AGREEMENT;

@Repository
@Slf4j
public class EndUserAgreementDao {
    private final JdbcTemplate jdbcTemplate;

    public EndUserAgreementDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EndUserAgreement findEndUserAgreementByBillingCode(String billingCode){
        String sql = SELECT_END_USER_AGREEMENT + " WHERE EUA.BILLING_CD =  ? ORDER BY EUA.CREATED_DATE DESC LIMIT 1";
        EndUserAgreement endUserAgreement;

        try {
            endUserAgreement = jdbcTemplate.queryForObject(sql, new EndUserAgreementRowMapper(), billingCode);
        } catch ( EmptyResultDataAccessException e ) {
            log.error(String.format("End user agreement was not found for billing code. ( billing_cd = %s )", billingCode));
            throw new DataNotFoundException("End user agreement was not found for billing code.");
        }

        return endUserAgreement;
    }
}
