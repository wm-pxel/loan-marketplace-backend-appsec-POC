package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Activity", description = "Model for a Lamina activity.")
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Deal deal;

    // Represents the open event, if it exists.
    private Event event;

    private Institution participant;

    private ActivityType activityType;

    private String json;

    private String source;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private OffsetDateTime createdDate;

}