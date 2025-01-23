package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.BillingCode;
import com.westmonroe.loansyndication.model.EndUserAgreement;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EndUserAgreementRowMapper implements RowMapper<EndUserAgreement> {

    @Override
    public EndUserAgreement mapRow(ResultSet rs, int rowNum) throws SQLException {
        EndUserAgreement endUserAgreement = new EndUserAgreement();
        endUserAgreement.setId(rs.getLong("EUA_ID"));
        endUserAgreement.setContent(rs.getString("EUA_CONTENT"));
        endUserAgreement.setCreatedDate(rs.getString("CREATED_DATE"));

        BillingCode billingCode = new BillingCode();
        billingCode.setCode(rs.getString("BILLING_CD"));
        billingCode.setDescription(rs.getString("BILLING_DESC"));

        endUserAgreement.setBillingCode(billingCode);

        return endUserAgreement;
    }

}
