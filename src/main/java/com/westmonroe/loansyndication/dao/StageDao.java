package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.StageRowMapper;
import com.westmonroe.loansyndication.model.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.StageQueryDef.SELECT_STAGE;

@Repository
@Slf4j
public class StageDao {

    private final JdbcTemplate jdbcTemplate;

    public StageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Stage> findAll() {
        String sql = SELECT_STAGE + " ORDER BY ORDER_NBR";
        return jdbcTemplate.query(sql, new StageRowMapper());
    }

    public Stage findById(Long id) {
        String sql = SELECT_STAGE + " WHERE STAGE_ID = ?";
        Stage stage;

        try {
            stage = jdbcTemplate.queryForObject(sql, new StageRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Stage was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Stage was not found for id.");

        }

        return stage;
    }

    public Stage findByOrder(Integer order) {
        String sql = SELECT_STAGE + " WHERE ORDER_NBR = ?";
        Stage stage;

        try {
            stage = jdbcTemplate.queryForObject(sql, new StageRowMapper(), order);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Stage was not found for order. ( order = %d )", order));
            throw new DataNotFoundException("Stage was not found for order.");

        }

        return stage;
    }

}