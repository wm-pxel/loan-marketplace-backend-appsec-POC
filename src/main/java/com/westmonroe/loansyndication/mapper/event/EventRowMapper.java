package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.Stage;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {

        Event event = new Event();

        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setEventExternalId(rs.getString("EVENT_EXTERNAL_UUID"));
        event.setName(rs.getString("EVENT_NAME"));

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setDealExternalId(rs.getString("DEAL_EXTERNAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));

        Institution originator = new Institution();
        originator.setId(rs.getLong("INSTITUTION_ID"));
        originator.setUid(rs.getString("INSTITUTION_UUID"));
        originator.setName(rs.getString("INSTITUTION_NAME"));
        deal.setOriginator(originator);

        event.setDeal(deal);

        EventType eventType = new EventType();
        eventType.setId(rs.getLong("EVENT_TYPE_ID"));
        eventType.setName(rs.getString("EVENT_TYPE_NAME"));
        event.setEventType(eventType);

        Stage stage = new Stage();
        stage.setId(rs.getLong("STAGE_ID"));
        stage.setName(rs.getString("STAGE_NAME"));
        stage.setOrder(rs.getInt("ORDER_NBR"));
        event.setStage(stage);

        event.setProjectedLaunchDate(rs.getObject("PROJ_LAUNCH_DATE", LocalDate.class));
        event.setLaunchDate(rs.getObject("LAUNCH_DATE", OffsetDateTime.class));
        event.setCommitmentDate(rs.getObject("COMMITMENT_DATE", LocalDate.class));
        event.setCommentsDueByDate(rs.getObject("COMMENTS_DUE_DATE", LocalDate.class));
        event.setEffectiveDate(rs.getObject("EFFECTIVE_DATE", LocalDate.class));
        event.setProjectedCloseDate(rs.getObject("PROJ_CLOSE_DATE", LocalDate.class));
        event.setCloseDate(rs.getObject("CLOSE_DATE", OffsetDateTime.class));
        event.setLeadInvitationDate(rs.getObject("LEAD_INVITATION_DATE", OffsetDateTime.class));
        event.setLeadCommitmentDate(rs.getObject("LEAD_COMMITMENT_DATE", OffsetDateTime.class));
        event.setLeadAllocationDate(rs.getObject("LEAD_ALLOCATION_DATE", OffsetDateTime.class));
        event.setTotalInvitationAmount(rs.getBigDecimal("TOTAL_INVITATION_AMT"));
        event.setTotalCommitmentAmount(rs.getBigDecimal("TOTAL_COMMITMENT_AMT"));
        event.setTotalAllocationAmount(rs.getBigDecimal("TOTAL_ALLOCATION_AMT"));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        event.setCreatedBy(createdBy);

        event.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        event.setUpdatedBy(updatedBy);

        event.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return event;
    }

}