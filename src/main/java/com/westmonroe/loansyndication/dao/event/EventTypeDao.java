package com.westmonroe.loansyndication.dao.event;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.event.EventTypeRowMapper;
import com.westmonroe.loansyndication.model.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.event.EventTypeQueryDef.SELECT_EVENT_TYPE;

@Repository
@Slf4j
public class EventTypeDao {

    private final JdbcTemplate jdbcTemplate;

    public EventTypeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EventType> findAll() {
        String sql = SELECT_EVENT_TYPE + " ORDER BY EVENT_TYPE_ID";
        return jdbcTemplate.query(sql, new EventTypeRowMapper());
    }

    public EventType findById(Long id) {

        String sql = SELECT_EVENT_TYPE + " WHERE EVENT_TYPE_ID = ?";
        EventType eventType;

        try {
            eventType = jdbcTemplate.queryForObject(sql, new EventTypeRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event type was not found for id. ( id = %d )", id));
            throw new DataNotFoundException("Event type was not found for id.");

        }

        return eventType;
    }

    public EventType findByName(String name) {

        String sql = SELECT_EVENT_TYPE + " WHERE UPPER(EVENT_TYPE_NAME) = ?";
        EventType eventType;

        try {
            eventType = jdbcTemplate.queryForObject(sql, new EventTypeRowMapper(), name.toUpperCase());
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Event type was not found for name. ( name = %s )", name));
            throw new DataNotFoundException("Event type was not found for name.");

        }

        return eventType;
    }

}