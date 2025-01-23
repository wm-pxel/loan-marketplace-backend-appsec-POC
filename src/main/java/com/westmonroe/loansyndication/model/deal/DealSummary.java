package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.Stage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Summary", description = "Model for a Loan Syndication deal summary.")
public class DealSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long id;

    @JsonProperty("id")
    private String uid;

    private String name;

    @ReadOnlyProperty
    private String relation;

    private Institution originator;

    private Stage stage;

    private BigDecimal dealAmount;

    private LocalDate projectedLaunchDate;

    private OffsetDateTime launchDate;

    private LocalDate commitmentDate;

    private LocalDate projectedCloseDate;

    private LocalDate effectiveDate;

    private OffsetDateTime closeDate;

    private String createdBy;

    @ReadOnlyProperty
    private String createdDate;

    private String active;

    private String declinedFlag;

    private String removedFlag;

    private ParticipantStep step;

    private String eventName;

}