package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.InitialLenderRowMapper;
import com.westmonroe.loansyndication.model.InitialLender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.InitialLenderQueryDef.*;

@Repository
@Slf4j
public class InitialLenderDao {

    private final JdbcTemplate jdbcTemplate;

    public InitialLenderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InitialLender> findAll() {
        String sql = SELECT_INITIAL_LENDER + " ORDER BY LENDER_NAME";
        return jdbcTemplate.query(sql, new InitialLenderRowMapper());
    }

    public List<InitialLender> searchByLenderName(String lenderName) {
        String sql = SELECT_INITIAL_LENDER + " WHERE LENDER_NAME LIKE ? ORDER BY LENDER_NAME";
        return jdbcTemplate.query(sql, new InitialLenderRowMapper(), lenderName);
    }

    public InitialLender findById(Long id) {
        String sql = SELECT_INITIAL_LENDER + " WHERE INITIAL_LENDER_ID = ?";
        InitialLender lender;

        try {
            lender = jdbcTemplate.queryForObject(sql, new InitialLenderRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Initial Lender was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Initial Lender was not found for id.");

        }

        return lender;
    }

    public InitialLender save(InitialLender lender) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_INITIAL_LENDER, new String[] { "initial_lender_id" });
            ps.setString(1, lender.getLenderName());
            ps.setString(2, lender.getActive());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            lender.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Initial Lender.");
            throw new DatabaseException("Error retrieving unique id for Initial Lender.");

        }

        return lender;
    }

    public void update(InitialLender i) {
        jdbcTemplate.update(UPDATE_INITIAL_LENDER, i.getLenderName(), i.getActive(), i.getId());
    }

    public int delete(InitialLender u) {
        return deleteById(u.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_INITIAL_LENDER + " WHERE INITIAL_LENDER_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

}