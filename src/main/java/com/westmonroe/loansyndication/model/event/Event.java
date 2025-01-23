package com.westmonroe.loansyndication.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.Stage;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.validation.ValidEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Event", description = "Model for a Lamina deal event.")
public class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public Event(Long id) {
        this.id = id;
    }

    public Event(String uid) {
        this.uid = uid;
    }

    public Event(Long id, String uid) {
        this.id = id;
        this.uid = uid;
    }

    @JsonIgnore
    private Long id;

    @Size(max = 36, message = "Unique ID cannot be greater than 36 characters.")
    private String uid;

    @Size(max = 55, message = "Deal External ID cannot be greater than 55 characters.")
    private String eventExternalId;

    private Deal deal;

    @Size(min = 1, max = 80, message = "Name cannot be empty.")
    private String name;

    @ValidEventType
    private EventType eventType;

    @ReadOnlyProperty
    private Stage stage;

    private LocalDate projectedLaunchDate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime launchDate;

    private LocalDate commitmentDate;

    private LocalDate commentsDueByDate;

    private LocalDate effectiveDate;

    private LocalDate projectedCloseDate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime closeDate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal totalInvitationAmount;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal totalCommitmentAmount;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal totalAllocationAmount;

    @ReadOnlyProperty
    private OffsetDateTime leadInvitationDate;

    @ReadOnlyProperty
    private OffsetDateTime leadCommitmentDate;

    @ReadOnlyProperty
    private OffsetDateTime leadAllocationDate;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

}