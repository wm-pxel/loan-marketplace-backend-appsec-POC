package com.westmonroe.loansyndication.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import com.westmonroe.loansyndication.validation.ValidInstitution;
import com.westmonroe.loansyndication.validation.ValidParticipantStep;
import com.westmonroe.loansyndication.validation.ValidUser;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_YN;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Event Participant Origination", description = "Model for a Lamina event participant origination.")
public class EventOriginationParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    private Event event;

    @ValidInstitution(message = "Participant must be a valid institution.")
    private Institution participant;

    @ValidParticipantStep(message = "Participant step must be a valid step.")
    private ParticipantStep step;

    @ValidUser(message = "Invite Recipient must be a valid user.")
    private User inviteRecipient;

    @ReadOnlyProperty
    private OffsetDateTime inviteDate;

    @ReadOnlyProperty
    private OffsetDateTime fullDealAccessDate;

    @Size(max = 3000, message = "Invite message cannot exceed 3,000 characters.")
    private String message;

    @Size(max = 3000, message = "Response cannot exceed 3,000 characters.")
    private String response;

    private DealDocument commitmentLetter;

    private DealDocument participantCertificate;

    private DealDocument signedParticipantCertificate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal totalInvitationAmount;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal totalCommitmentAmount;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal totalAllocationAmount;

    @Pattern(regexp = REGEX_YN, message = "The declined flag can only be Y or N.")
    private String declinedFlag;

    @Size(max = 3000, message = "Decline message cannot exceed 3,000 characters.")
    private String declinedMessage;

    @ReadOnlyProperty
    private String declinedDate;

    @Pattern(regexp = REGEX_YN, message = "The removed flag can only be Y or N.")
    private String removedFlag;

    @ReadOnlyProperty
    private String removedDate;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

    public EventOriginationParticipant(Long id, User inviteRecipient, String message, String response, String declinedMessage) {
        this.id = id;
        this.inviteRecipient = inviteRecipient;
        this.message = message;
        this.response = response;
        this.declinedMessage = declinedMessage;
    }

}