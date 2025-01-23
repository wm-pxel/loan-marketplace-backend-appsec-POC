package com.westmonroe.loansyndication.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.ParticipantStep;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.validation.ValidInstitution;
import com.westmonroe.loansyndication.validation.ValidParticipantStep;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Event Participant", description = "Model for a Lamina event participant.")
public class EventParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    private Event event;

    @NotNull(message = "Participant cannot be null.")
    @ValidInstitution(message = "Participant must be a valid institution.")
    private Institution participant;

    @NotNull(message = "Participant step cannot be null.")
    @ValidParticipantStep(message = "Participant step must be a valid step.")
    private ParticipantStep step;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

    public EventParticipant(Long id, Long eventId) {
        this.id = id;
        this.event = new Event(eventId);
    }

    public EventParticipant(Long id, Event event, Institution participant) {
        this.id = id;
        this.event = event;
        this.participant = participant;
    }

}