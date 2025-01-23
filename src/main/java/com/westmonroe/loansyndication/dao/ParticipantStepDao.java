package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.ParticipantStepRowMapper;
import com.westmonroe.loansyndication.model.ParticipantStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.ParticipantStepQueryDef.SELECT_PARTICIPANT_STEP;

@Repository
@Slf4j
public class ParticipantStepDao {

    private final JdbcTemplate jdbcTemplate;

    public ParticipantStepDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ParticipantStep> findAll() {
        String sql = SELECT_PARTICIPANT_STEP + " ORDER BY ORDER_NBR";
        return jdbcTemplate.query(sql, new ParticipantStepRowMapper());
    }

    public ParticipantStep findById(Long id) {
        String sql = SELECT_PARTICIPANT_STEP + " WHERE PARTICIPANT_STEP_ID = ?";
        ParticipantStep participantStep;

        try {
            participantStep = jdbcTemplate.queryForObject(sql, new ParticipantStepRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Participant Step was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Participant Step was not found for id.");

        }

        return participantStep;
    }

    public ParticipantStep findByName(String name) {
        String sql = SELECT_PARTICIPANT_STEP + " WHERE UPPER(STEP_NAME) = ?";
        ParticipantStep participantStep;

        try {
            participantStep = jdbcTemplate.queryForObject(sql, new ParticipantStepRowMapper(), name.toUpperCase());
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Participant Step was not found for name. ( name = %s )", name));
            throw new DataNotFoundException("Participant Step was not found for name.");

        }

        return participantStep;
    }

    public ParticipantStep findByOrder(Integer order) {
        String sql = SELECT_PARTICIPANT_STEP + " WHERE ORDER_NBR = ?";
        ParticipantStep participantStep;

        try {
            participantStep = jdbcTemplate.queryForObject(sql, new ParticipantStepRowMapper(), order);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Participant Step was not found for order number. ( order = %d )", order));
            throw new DataNotFoundException("Participant Step was not found for order number.");

        }

        return participantStep;
    }

}