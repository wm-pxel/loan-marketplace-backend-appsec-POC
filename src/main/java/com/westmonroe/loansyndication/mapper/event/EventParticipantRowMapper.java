package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.model.event.EventType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventParticipantRowMapper implements RowMapper<EventParticipant> {

    @Override
    public EventParticipant mapRow(ResultSet rs, int rowNum) throws SQLException {

        EventParticipant eventParticipant = new EventParticipant();

        eventParticipant.setId(rs.getLong("EVENT_PARTICIPANT_ID"));

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setName(rs.getString("EVENT_NAME"));

        EventType eventType = new EventType();
        eventType.setId(rs.getLong("EVENT_TYPE_ID"));
        eventType.setName(rs.getString("EVENT_TYPE_NAME"));
        event.setEventType(eventType);

        eventParticipant.setEvent(event);

        if ( rs.getObject("PARTICIPANT_ID") != null ) {

            Institution participant = new Institution();
            participant.setId(rs.getLong("PARTICIPANT_ID"));
            participant.setUid(rs.getString("INSTITUTION_UUID"));
            participant.setName(rs.getString("INSTITUTION_NAME"));
            eventParticipant.setParticipant(participant);

        }

        ParticipantStep step = new ParticipantStep();
        step.setId(rs.getLong("PARTICIPANT_STEP_ID"));
        step.setName(rs.getString("STEP_NAME"));
        step.setLeadViewStatus(rs.getString("ORIG_STATUS_DESC"));
        step.setParticipantStatus(rs.getString("PART_STATUS_DESC"));
        step.setOrder(rs.getInt("ORDER_NBR"));
        eventParticipant.setStep(step);

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        eventParticipant.setCreatedBy(createdBy);

        eventParticipant.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        eventParticipant.setUpdatedBy(updatedBy);

        eventParticipant.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return eventParticipant;
    }

}