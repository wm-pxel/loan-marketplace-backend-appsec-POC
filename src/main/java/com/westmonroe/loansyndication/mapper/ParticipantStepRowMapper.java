package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.ParticipantStep;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ParticipantStepRowMapper implements RowMapper<ParticipantStep> {

    @Override
    public ParticipantStep mapRow(ResultSet rs, int rowNum) throws SQLException {

        ParticipantStep step = new ParticipantStep();
        step.setId(rs.getLong("PARTICIPANT_STEP_ID"));
        step.setName(rs.getString("STEP_NAME"));
        step.setLeadViewStatus(rs.getString("ORIG_STATUS_DESC"));
        step.setParticipantStatus(rs.getString("PART_STATUS_DESC"));
        step.setOrder(rs.getInt("ORDER_NBR"));

        return step;
    }

}