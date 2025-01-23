package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventLeadFacilityRowMapper implements RowMapper<EventLeadFacility> {

    @Override
    public EventLeadFacility mapRow(ResultSet rs, int rowNum) throws SQLException {

        EventLeadFacility eventLeadFacility = new EventLeadFacility();

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setEventExternalId(rs.getString("EVENT_EXTERNAL_UUID"));
        event.setName(rs.getString("EVENT_NAME"));
        eventLeadFacility.setEvent(event);

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
        eventLeadFacility.setEventDealFacility(eventDealFacility);

        if (rs.getObject("INVITATION_AMT") != null) {
            eventLeadFacility.setInvitationAmount(rs.getBigDecimal("INVITATION_AMT"));
        }

        if ( rs.getObject("COMMITMENT_AMT") != null ) {
            eventLeadFacility.setCommitmentAmount(rs.getBigDecimal("COMMITMENT_AMT"));
        }

        if ( rs.getObject("ALLOCATION_AMT") != null ) {
            eventLeadFacility.setAllocationAmount(rs.getBigDecimal("ALLOCATION_AMT"));
        }

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        eventLeadFacility.setCreatedBy(createdBy);
        eventLeadFacility.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        eventLeadFacility.setUpdatedBy(updatedBy);
        eventLeadFacility.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return eventLeadFacility;
    }

}