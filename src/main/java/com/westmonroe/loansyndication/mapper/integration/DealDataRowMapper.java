package com.westmonroe.loansyndication.mapper.integration;

import com.westmonroe.loansyndication.model.integration.DealData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DealDataRowMapper implements RowMapper<DealData> {

    @Override
    public DealData mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealData dsd = new DealData();
        dsd.setUid(rs.getString("DEAL_UUID"));
        dsd.setName(rs.getString("DEAL_NAME"));
        dsd.setDealExternalId(rs.getString("DEAL_EXTERNAL_UUID"));
        dsd.setOriginatorName(rs.getString("INSTITUTION_NAME"));

        return dsd;
    }

}