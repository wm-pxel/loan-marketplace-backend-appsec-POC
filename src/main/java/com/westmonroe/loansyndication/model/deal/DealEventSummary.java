package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.event.Event;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Event Summary", description = "Model for a Loan Syndication deal event summary.")
public class DealEventSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long id;

    @JsonProperty("id")
    private String uid;

    private String name;

    @ReadOnlyProperty
    private String relation;

    private Institution originator;

    private BigDecimal dealAmount;

    private OffsetDateTime closeDate;

    private String createdBy;

    @ReadOnlyProperty
    private String createdDate;

    private String active;

    private Event event;

    private EventOriginationParticipant eventParticipant;

}