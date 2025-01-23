package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.State;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StateRowMapper implements RowMapper<State> {

    @Override
    public State mapRow(ResultSet rs, int rowNum) throws SQLException {

        State state = new State();
        state.setCode(rs.getString("STATE_CD"));
        state.setName(rs.getString("STATE_NAME"));

        return state;
    }

}