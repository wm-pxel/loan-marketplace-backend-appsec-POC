package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.Stage;
import com.westmonroe.loansyndication.model.deal.DealSummary;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DealSummaryRowMapper implements RowMapper<DealSummary> {

    @Override
    public DealSummary mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealSummary ds = new DealSummary();
        ds.setId(rs.getLong("DEAL_ID"));
        ds.setUid(rs.getString("DEAL_UUID"));
        ds.setName(rs.getString("DEAL_NAME"));
        ds.setRelation(rs.getString("RELATION_DESC"));
        ds.setDealAmount(rs.getBigDecimal("DEAL_AMT"));
        ds.setProjectedLaunchDate(rs.getObject("PROJ_LAUNCH_DATE", LocalDate.class));
        ds.setLaunchDate(rs.getObject("LAUNCH_DATE", OffsetDateTime.class));
        ds.setCommitmentDate(rs.getObject("COMMITMENT_DATE", LocalDate.class));
        ds.setProjectedCloseDate(rs.getObject("PROJ_CLOSE_DATE", LocalDate.class));
        ds.setEffectiveDate(rs.getObject("EFFECTIVE_DATE", LocalDate.class));
        ds.setCloseDate(rs.getObject("CLOSE_DATE", OffsetDateTime.class));
        ds.setActive(rs.getString("ACTIVE_IND"));
        ds.setDeclinedFlag(rs.getString("DECLINED_IND"));
        ds.setRemovedFlag(rs.getString("REMOVED_IND"));
        ds.setEventName(rs.getString("EVENT_NAME"));

        if ( rs.getObject("PARTICIPANT_STEP_ID") != null ) {
            ParticipantStep participantStep = new ParticipantStep();
            participantStep.setId(rs.getLong("PARTICIPANT_STEP_ID"));
            participantStep.setName(rs.getString("STEP_NAME"));
            ds.setStep(participantStep);
        }

        Institution institution = new Institution();
        institution.setId(rs.getLong("INSTITUTION_ID"));
        institution.setUid(rs.getString("INSTITUTION_UUID"));
        institution.setName(rs.getString("INSTITUTION_NAME"));
        ds.setOriginator(institution);

        Stage stage = new Stage();
        stage.setId(rs.getLong("STAGE_ID"));
        stage.setName(rs.getString("STAGE_NAME"));
        stage.setOrder(rs.getInt("ORDER_NBR"));
        ds.setStage(stage);

        return ds;
    }

}