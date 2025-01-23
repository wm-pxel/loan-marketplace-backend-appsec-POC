package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.NaicsCodeRowMapper;
import com.westmonroe.loansyndication.model.NaicsCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class NaicsCodeDao {

    private static final String SELECT_NAICS_CODE = "SELECT NAICS_CD, TITLE_NAME FROM NAICS_DEF";

    private final JdbcTemplate jdbcTemplate;

    public NaicsCodeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NaicsCode> findAll() {
        String sql = SELECT_NAICS_CODE + " ORDER BY NAICS_CD";
        return jdbcTemplate.query(sql, new NaicsCodeRowMapper());
    }

    public NaicsCode findByCode(String code) {
        String sql = SELECT_NAICS_CODE + " WHERE NAICS_CD = ?";
        NaicsCode naicsCode;

        try {
            naicsCode = jdbcTemplate.queryForObject(sql, new NaicsCodeRowMapper(), code);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("NAICS Code was not found for code. ( code = %s )", code));
            throw new DataNotFoundException("NAICS Code was not found for code.");

        }

        return naicsCode;
    }

    public List<NaicsCode> searchByTitle(String text) {
        String sql = SELECT_NAICS_CODE + " WHERE LOWER(TITLE_NAME) LIKE ?";
        return jdbcTemplate.query(sql, new NaicsCodeRowMapper(), text.toLowerCase());
    }

}