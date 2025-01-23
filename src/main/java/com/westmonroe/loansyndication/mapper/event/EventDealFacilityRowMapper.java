package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventDealFacility;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventDealFacilityRowMapper implements RowMapper<EventDealFacility> {

    @Override
    public EventDealFacility mapRow(ResultSet rs, int rowNum) throws SQLException {

        EventDealFacility def = new EventDealFacility();

        def.setId(rs.getLong("EVENT_DEAL_FACILITY_ID"));

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setEventExternalId(rs.getString("EVENT_EXTERNAL_UUID"));
        event.setName(rs.getString("EVENT_NAME"));
        def.setEvent(event);

        DealFacility facility = new DealFacility();

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));
        facility.setDeal(deal);

        facility.setId(rs.getLong("DEAL_FACILITY_ID"));
        facility.setFacilityExternalId(rs.getString("FACILITY_EXTERNAL_UUID"));
        facility.setFacilityName(rs.getString("FACILITY_NAME"));
        facility.setFacilityAmount(rs.getBigDecimal("FACILITY_AMT"));

        def.setDealFacility(facility);

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        def.setCreatedBy(createdBy);

        def.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        def.setUpdatedBy(updatedBy);

        def.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return def;
    }

}