package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.Stage;
import com.westmonroe.loansyndication.model.deal.DealEventSummary;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DealEventSummaryRowMapper implements RowMapper<DealEventSummary> {

    @Override
    public DealEventSummary mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealEventSummary des = new DealEventSummary();
        des.setId(rs.getLong("DEAL_ID"));
        des.setUid(rs.getString("DEAL_UUID"));
        des.setName(rs.getString("DEAL_NAME"));
        des.setRelation(rs.getString("RELATION_DESC"));
        des.setDealAmount(rs.getBigDecimal("DEAL_AMT"));
        des.setCloseDate(rs.getObject("CLOSE_DATE", OffsetDateTime.class));
        des.setActive(rs.getString("ACTIVE_IND"));

        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        des.setOriginator(institution);

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setName(rs.getString("EVENT_NAME"));
        event.setProjectedLaunchDate(rs.getObject("PROJ_LAUNCH_DATE", LocalDate.class));
        event.setLaunchDate(rs.getObject("LAUNCH_DATE", OffsetDateTime.class));
        event.setCommitmentDate(rs.getObject("COMMITMENT_DATE", LocalDate.class));
        event.setCommentsDueByDate(rs.getObject("COMMENTS_DUE_DATE", LocalDate.class));
        event.setEffectiveDate(rs.getObject("EFFECTIVE_DATE", LocalDate.class));
        event.setProjectedCloseDate(rs.getObject("PROJ_CLOSE_DATE", LocalDate.class));

        Stage stage = new Stage();
        stage.setId(rs.getLong("STAGE_ID"));
        stage.setName(rs.getString("STAGE_NAME"));
        stage.setOrder(rs.getInt("ORDER_NBR"));
        event.setStage(stage);
        des.setEvent(event);

        EventOriginationParticipant eop = new EventOriginationParticipant();
        eop.setDeclinedFlag(rs.getString("DECLINED_IND"));
        eop.setRemovedFlag(rs.getString("REMOVED_IND"));

        if ( rs.getObject("PARTICIPANT_STEP_ID") != null ) {
            ParticipantStep participantStep = new ParticipantStep();
            participantStep.setId(rs.getLong("PARTICIPANT_STEP_ID"));
            participantStep.setName(rs.getString("STEP_NAME"));
            eop.setStep(participantStep);
        }
        des.setEventParticipant(eop);

        return des;
    }

}