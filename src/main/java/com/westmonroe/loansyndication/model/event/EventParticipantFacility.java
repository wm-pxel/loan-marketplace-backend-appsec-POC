package com.westmonroe.loansyndication.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Event Participant Facility", description = "Model for a Lamina event participant facility.")
public class EventParticipantFacility {

    @NotNull
    private EventParticipant eventParticipant;

    @NotNull
    private EventDealFacility eventDealFacility;

    @Digits(integer=12, fraction=2, message = "Commitment amount must be in a valid currency format.")
    private BigDecimal commitmentAmount;

    @Digits(integer=12, fraction=2, message = "Allocation amount must be in a valid currency format.")
    private BigDecimal allocationAmount;

    @Digits(integer=12, fraction=2, message = "Invitation amount must be in a valid currency format.")
    private BigDecimal invitationAmount;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

}