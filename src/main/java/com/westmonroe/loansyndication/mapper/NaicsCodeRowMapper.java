package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.NaicsCode;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NaicsCodeRowMapper implements RowMapper<NaicsCode> {

    @Override
    public NaicsCode mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaicsCode naics = new NaicsCode();
        naics.setCode(rs.getString("NAICS_CD"));
        naics.setTitle(rs.getString("TITLE_NAME"));

        return naics;
    }

}
