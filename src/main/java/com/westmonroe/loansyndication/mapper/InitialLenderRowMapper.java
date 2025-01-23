package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.InitialLender;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InitialLenderRowMapper implements RowMapper<InitialLender> {

    @Override
    public InitialLender mapRow(ResultSet rs, int rowNum) throws SQLException {

        InitialLender lender = new InitialLender();
        lender.setId(rs.getLong("INITIAL_LENDER_ID"));
        lender.setLenderName(rs.getString("LENDER_NAME"));
        lender.setCreatedDate(rs.getString("CREATED_DATE"));
        lender.setUpdatedDate(rs.getString("UPDATED_DATE"));
        lender.setActive(rs.getString("ACTIVE_IND"));

        return lender;
    }

}