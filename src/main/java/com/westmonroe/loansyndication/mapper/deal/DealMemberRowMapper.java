package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealMember;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class DealMemberRowMapper implements RowMapper<DealMember> {

    @Override
    public DealMember mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealMember dm = new DealMember();

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));

        Institution originator = new Institution();
        originator.setId(rs.getLong("ORIGINATOR_ID"));
        originator.setUid(rs.getString("ORIG_INSTITUTION_UUID"));
        originator.setName(rs.getString("ORIG_INSTITUTION_NAME"));
        deal.setOriginator(originator);

        dm.setDeal(deal);

        User user = new User();
        user.setId(rs.getLong("USER_ID"));
        user.setUid(rs.getString("USER_UUID"));
        user.setFirstName(rs.getString("FIRST_NAME"));
        user.setLastName(rs.getString("LAST_NAME"));
        user.setEmail(rs.getString("EMAIL_ADDR"));
        user.setPassword(rs.getString("PASSWORD_DESC"));
        user.setActive(rs.getString("ACTIVE_IND"));
        user.setCreatedDate(rs.getString("CREATED_DATE"));

        Institution institution = new Institution();
        institution.setId(rs.getLong("USER_INSTITUTION_ID"));
        institution.setUid(rs.getString("USER_INSTITUTION_UUID"));
        institution.setName(rs.getString("USER_INSTITUTION_NAME"));
        user.setInstitution(institution);

        dm.setUser(user);
        dm.setMemberTypeCode(rs.getString("MEMBER_TYPE_CD"));
        dm.setMemberTypeDesc(rs.getString("MEMBER_TYPE_DESC"));

        // TODO: remove this "if" statement as part of LM-2493
        if ( rs.getObject("EVENT_PARTICIPANT_ID") != null ) {

            EventOriginationParticipant eventOriginationParticipant = new EventOriginationParticipant();

            eventOriginationParticipant.setId(rs.getLong("EVENT_PARTICIPANT_ID"));
            eventOriginationParticipant.setInviteDate(rs.getObject("INVITE_DATE", OffsetDateTime.class));
            eventOriginationParticipant.setFullDealAccessDate(rs.getObject("FULL_DEAL_ACCESS_DATE", OffsetDateTime.class));

            ParticipantStep step = new ParticipantStep();
            step.setId(rs.getLong("PARTICIPANT_STEP_ID"));
            step.setName(rs.getString("STEP_NAME"));
            step.setOrder(rs.getInt("ORDER_NBR"));
            eventOriginationParticipant.setStep(step);

            eventOriginationParticipant.setRemovedFlag(rs.getString("REMOVED_IND"));
            eventOriginationParticipant.setDeclinedFlag(rs.getString("DECLINED_IND"));

            dm.setEventOriginationParticipant(eventOriginationParticipant);

        }

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        dm.setCreatedBy(createdBy);

        dm.setCreatedDate(rs.getString("CREATED_DATE"));

        return dm;
    }

}