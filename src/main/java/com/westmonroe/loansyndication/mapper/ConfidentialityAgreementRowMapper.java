package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.ConfidentialityAgreement;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfidentialityAgreementRowMapper implements RowMapper<ConfidentialityAgreement> {
    @Override
    public ConfidentialityAgreement mapRow(ResultSet rs, int rowNum) throws SQLException {
        ConfidentialityAgreement confidentialityAgreement = new ConfidentialityAgreement();
        confidentialityAgreement.setId(rs.getLong("CONF_AGRMNT_ID"));
        confidentialityAgreement.setDescription(rs.getString("CONF_AGRMNT_DESC"));
        confidentialityAgreement.setInstitutionId(rs.getLong("INSTITUTION_ID"));

        return confidentialityAgreement;
    }
}
