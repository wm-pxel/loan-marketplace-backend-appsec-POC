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

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Event Lead Facility", description = "Model for a Lamina event lead facility.")
public class EventLeadFacility implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public EventLeadFacility(Event event, EventDealFacility eventDealFacility) {
        this.event = event;
        this.eventDealFacility = eventDealFacility;
    }

    @NotNull
    private Event event;

    @NotNull
    private EventDealFacility eventDealFacility;

    @Digits(integer=12, fraction=2, message = "Invitation amount must be in a valid currency format.")
    private BigDecimal invitationAmount;

    @Digits(integer=12, fraction=2, message = "Commitment amount must be in a valid currency format.")
    private BigDecimal commitmentAmount;

    @Digits(integer=12, fraction=2, message = "Allocation amount must be in a valid currency format.")
    private BigDecimal allocationAmount;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

}