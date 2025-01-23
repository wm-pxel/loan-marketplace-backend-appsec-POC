package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "EventParticipantFacilityDTO", description = "Model for a Lamina event participant facility DTO.")
public class EventParticipantFacilityDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Participant ID cannot be empty or null.")
    private String participantId;

    private String participantName;

    @NotEmpty(message = "Facility external ID cannot be empty or null.")
    @Size(max = 55, message = "Facility External ID cannot be greater than 55 characters.")
    private String facilityExternalId;

    private BigDecimal commitmentAmount;

    private BigDecimal allocationAmount;

    private BigDecimal invitationAmount;

}
