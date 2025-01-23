package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.event.EventType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventTypeRowMapper implements RowMapper<EventType> {

    @Override
    public EventType mapRow(ResultSet rs, int rowNum) throws SQLException {

        EventType eventType = new EventType();
        eventType.setId(rs.getLong("EVENT_TYPE_ID"));
        eventType.setName(rs.getString("EVENT_TYPE_NAME"));

        return eventType;
    }

}