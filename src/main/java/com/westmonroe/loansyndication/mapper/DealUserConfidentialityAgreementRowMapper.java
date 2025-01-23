package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.ConfidentialityAgreement;
import com.westmonroe.loansyndication.model.DealUserConfidentialityAgreement;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class DealUserConfidentialityAgreementRowMapper implements RowMapper<DealUserConfidentialityAgreement> {

    @Override
    public DealUserConfidentialityAgreement mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealUserConfidentialityAgreement dealMemberConfidentialityAgreement = new DealUserConfidentialityAgreement();

        dealMemberConfidentialityAgreement.setDealId(rs.getLong("DEAL_ID"));
        dealMemberConfidentialityAgreement.setUserId(rs.getLong("USER_ID"));

        // set the confidentiality agreement on the deal user confidentiality agreement object
        ConfidentialityAgreement confidentialityAgreement = new ConfidentialityAgreement();
        confidentialityAgreement.setId(rs.getLong("CONF_AGRMNT_ID"));
        dealMemberConfidentialityAgreement.setConfidentialityAgreement(confidentialityAgreement);

        dealMemberConfidentialityAgreement.setAgreementDate(rs.getObject("AGREEMENT_DATE", OffsetDateTime.class));

        return dealMemberConfidentialityAgreement;
    }

}