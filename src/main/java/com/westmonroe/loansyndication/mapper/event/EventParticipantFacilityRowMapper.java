package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.model.event.EventParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventParticipantFacilityRowMapper implements RowMapper<EventParticipantFacility> {

    @Override
    public EventParticipantFacility mapRow(ResultSet rs, int rowNum) throws SQLException {

        EventParticipantFacility eventParticipantFacility = new EventParticipantFacility();

        // Note: Full Deal Participant object is wired up through the GraphQL schema mapping.
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setId(rs.getLong("EVENT_PARTICIPANT_ID"));

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setEventExternalId(rs.getString("EVENT_EXTERNAL_UUID"));
        event.setName(rs.getString("EVENT_NAME"));
        eventParticipant.setEvent(event);

        Institution participant = new Institution();
        participant.setId(rs.getLong("PARTICIPANT_ID"));
        participant.setUid(rs.getString("INSTITUTION_UUID"));
        participant.setName(rs.getString("INSTITUTION_NAME"));
        eventParticipant.setParticipant(participant);

        eventParticipantFacility.setEventParticipant(eventParticipant);

        EventDealFacility eventDealFacility = new EventDealFacility();
        eventDealFacility.setId(rs.getLong("EVENT_DEAL_FACILITY_ID"));

        // Note: Full Deal Facility object is wired up through the GraphQL schema mapping.
        DealFacility dealFacility = new DealFacility();
        dealFacility.setId(rs.getLong("DEAL_FACILITY_ID"));
        dealFacility.setFacilityExternalId(rs.getString("FACILITY_EXTERNAL_UUID"));
        dealFacility.setFacilityName(rs.getString("FACILITY_NAME"));
        dealFacility.setFacilityAmount(rs.getBigDecimal("FACILITY_AMT"));

        PicklistItem facilityType = new PicklistItem(rs.getLong("FACILITY_TYPE_ID"), null, rs.getString("FACILITY_TYPE_NAME"), null);
        dealFacility.setFacilityType(facilityType);

        eventDealFacility.setDealFacility(dealFacility);
        eventParticipantFacility.setEventDealFacility(eventDealFacility);

        if ( rs.getObject("COMMITMENT_AMT") != null ) {
            eventParticipantFacility.setCommitmentAmount(rs.getBigDecimal("COMMITMENT_AMT"));
        }

        if ( rs.getObject("ALLOCATION_AMT") != null ) {
            eventParticipantFacility.setAllocationAmount(rs.getBigDecimal("ALLOCATION_AMT"));
        }

        if (rs.getObject("INVITATION_AMT") != null) {
            eventParticipantFacility.setInvitationAmount(rs.getBigDecimal("INVITATION_AMT"));
        }

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        eventParticipantFacility.setCreatedBy(createdBy);
        eventParticipantFacility.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        eventParticipantFacility.setUpdatedBy(updatedBy);
        eventParticipantFacility.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return eventParticipantFacility;
    }

}