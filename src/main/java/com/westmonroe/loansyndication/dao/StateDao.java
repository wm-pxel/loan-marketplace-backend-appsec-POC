package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.StateRowMapper;
import com.westmonroe.loansyndication.model.State;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.StateQueryDef.SELECT_STATE;

@Repository
@Slf4j
public class StateDao {

    private final JdbcTemplate jdbcTemplate;

    public StateDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<State> findAll() {
        String sql = SELECT_STATE + " ORDER BY STATE_NAME";
        return jdbcTemplate.query(sql, new StateRowMapper());
    }

    public State findByCode(String code) {
        String sql = SELECT_STATE + " WHERE STATE_CD = ?";
        State state;

        try {
            state = jdbcTemplate.queryForObject(sql, new StateRowMapper(), code);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("State was not found for code. ( code = %s )", code));
            throw new DataNotFoundException("State was not found for code.");

        }

        return state;
    }

}