package com.westmonroe.loansyndication.mapper.event;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class EventOriginationParticipantRowMapper implements RowMapper<EventOriginationParticipant> {

    @Override
    public EventOriginationParticipant mapRow(ResultSet rs, int rowNum) throws SQLException {

        EventOriginationParticipant epo = new EventOriginationParticipant();

        epo.setId(rs.getLong("EVENT_PARTICIPANT_ID"));

        Event event = new Event();
        event.setId(rs.getLong("EVENT_ID"));
        event.setUid(rs.getString("EVENT_UUID"));
        event.setName(rs.getString("EVENT_NAME"));
        event.setLaunchDate(rs.getObject("LAUNCH_DATE", OffsetDateTime.class));

        EventType eventType = new EventType();
        eventType.setId(rs.getLong("EVENT_TYPE_ID"));
        eventType.setName(rs.getString("EVENT_TYPE_NAME"));
        event.setEventType(eventType);

        epo.setEvent(event);

        if ( rs.getObject("PARTICIPANT_ID") != null ) {

            Institution participant = new Institution();
            participant.setId(rs.getLong("PARTICIPANT_ID"));
            participant.setUid(rs.getString("INSTITUTION_UUID"));
            participant.setName(rs.getString("INSTITUTION_NAME"));
            epo.setParticipant(participant);

        }

        ParticipantStep step = new ParticipantStep();
        step.setId(rs.getLong("PARTICIPANT_STEP_ID"));
        step.setName(rs.getString("STEP_NAME"));
        step.setLeadViewStatus(rs.getString("ORIG_STATUS_DESC"));
        step.setParticipantStatus(rs.getString("PART_STATUS_DESC"));
        step.setOrder(rs.getInt("ORDER_NBR"));
        epo.setStep(step);

        if ( rs.getObject("INVITE_RECIPIENT_ID") != null ) {

            User inviteRecipient = new User();
            inviteRecipient.setId(rs.getLong("INVITE_RECIPIENT_ID"));
            inviteRecipient.setUid(rs.getString("INVITE_RECIPIENT_UUID"));
            inviteRecipient.setFirstName(rs.getString("INVITE_RECIPIENT_FIRST_NAME"));
            inviteRecipient.setLastName(rs.getString("INVITE_RECIPIENT_LAST_NAME"));
            inviteRecipient.setEmail(rs.getString("INVITE_RECIPIENT_EMAIL_ADDR"));
            inviteRecipient.setPassword(rs.getString("INVITE_RECIPIENT_PASSWORD_DESC"));
            inviteRecipient.setActive(rs.getString("INVITE_RECIPIENT_ACTIVE_IND"));
            Institution participant = new Institution();
            participant.setId(rs.getLong("INVITE_RECIPIENT_INSTITUTION_ID"));
            inviteRecipient.setInstitution(participant);
            epo.setInviteRecipient(inviteRecipient);

        }

        epo.setInviteDate(rs.getObject("INVITE_DATE", OffsetDateTime.class));
        epo.setFullDealAccessDate(rs.getObject("FULL_DEAL_ACCESS_DATE", OffsetDateTime.class));
        epo.setMessage(rs.getString("MESSAGE_DESC"));
        epo.setResponse(rs.getString("RESPONSE_DESC"));

        if ( rs.getObject("COMMITMENT_LETTER_ID") != null ) {

            DealDocument document = new DealDocument();
            document.setId(rs.getLong("COMMITMENT_LETTER_ID"));
            document.setDisplayName(rs.getString("CL_DISPLAY_NAME"));
            document.setDocumentName(rs.getString("CL_DOCUMENT_NAME"));
            document.setDocumentType(rs.getString("CL_DOCUMENT_TYPE"));
            document.setDescription(rs.getString("CL_DOCUMENT_DESC"));
            document.setSource(rs.getString("CL_SOURCE_CD"));
            epo.setCommitmentLetter(document);

        }

        if ( rs.getObject("PARTICIPANT_CERTIFICATE_ID") != null ) {

            DealDocument document = new DealDocument();
            document.setId(rs.getLong("PARTICIPANT_CERTIFICATE_ID"));
            document.setDisplayName(rs.getString("PC_DISPLAY_NAME"));
            document.setDocumentName(rs.getString("PC_DOCUMENT_NAME"));
            document.setDocumentType(rs.getString("PC_DOCUMENT_TYPE"));
            document.setDescription(rs.getString("PC_DOCUMENT_DESC"));
            document.setSource(rs.getString("PC_SOURCE_CD"));
            epo.setParticipantCertificate(document);

        }

        if ( rs.getObject("SIGNED_CERTIFICATE_ID") != null ) {

            DealDocument document = new DealDocument();
            document.setId(rs.getLong("SIGNED_CERTIFICATE_ID"));
            document.setDisplayName(rs.getString("SPC_DISPLAY_NAME"));
            document.setDocumentName(rs.getString("SPC_DOCUMENT_NAME"));
            document.setDocumentType(rs.getString("SPC_DOCUMENT_TYPE"));
            document.setDescription(rs.getString("SPC_DOCUMENT_DESC"));
            document.setSource(rs.getString("SPC_SOURCE_CD"));
            epo.setSignedParticipantCertificate(document);

        }

        epo.setTotalInvitationAmount(rs.getBigDecimal("TOTAL_INVITATION_AMT"));
        epo.setTotalCommitmentAmount(rs.getBigDecimal("TOTAL_COMMITMENT_AMT"));
        epo.setTotalAllocationAmount(rs.getBigDecimal("TOTAL_ALLOCATION_AMT"));
        epo.setDeclinedFlag(rs.getString("DECLINED_IND"));
        epo.setDeclinedMessage(rs.getString("DECLINED_DESC"));
        epo.setDeclinedDate(rs.getString("DECLINED_DATE"));
        epo.setRemovedFlag(rs.getString("REMOVED_IND"));
        epo.setRemovedDate(rs.getString("REMOVED_DATE"));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        epo.setCreatedBy(createdBy);

        epo.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        epo.setUpdatedBy(updatedBy);

        epo.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return epo;
    }

}