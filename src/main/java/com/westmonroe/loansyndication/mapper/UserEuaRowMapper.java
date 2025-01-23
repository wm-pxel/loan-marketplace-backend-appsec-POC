package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.BillingCode;
import com.westmonroe.loansyndication.model.EndUserAgreement;
import com.westmonroe.loansyndication.model.UserEua;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class UserEuaRowMapper implements RowMapper<UserEua> {

    @Override
    public UserEua mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEua userEua = new UserEua();

        userEua.setUserId(rs.getLong("USER_ID"));
        userEua.setAgreementDate(rs.getObject("AGREEMENT_DATE", OffsetDateTime.class));

        EndUserAgreement endUserAgreement = new EndUserAgreement();
        endUserAgreement.setId(rs.getLong("EUA_ID"));
        endUserAgreement.setContent(rs.getString("EUA_CONTENT"));
        endUserAgreement.setCreatedDate(rs.getString("CREATED_DATE"));

        BillingCode billingCode = new BillingCode();
        billingCode.setCode(rs.getString("BILLING_CD"));
        billingCode.setDescription(rs.getString("BILLING_DESC"));

        endUserAgreement.setBillingCode(billingCode);
        userEua.setEndUserAgreement(endUserAgreement);

        return userEua;
    }
}
