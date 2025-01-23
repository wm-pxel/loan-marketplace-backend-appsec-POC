package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "MarketplaceData", description = "Model for the marketplace data.")
public class MarketplaceData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ReadOnlyProperty
    String baseUrl;

    @Valid
    @NotNull(message = "Deal cannot be null")
    DealDto deal;

    @Valid
    EventDto event;

    @Valid
    @NotNull(message = "Applicant cannot be null")
    List<ApplicantDto> applicants;

    @Valid
    List<DealCovenantDto> covenants;

    @Valid
    List<DealFacilityDto> facilities;

    @Valid
    List<EventParticipantFacilityDto> participantFacilities;

    @Valid
    List<DealDocumentDto> documents;

}