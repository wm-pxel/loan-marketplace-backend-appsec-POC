package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Member", description = "Model for a Lamina deal member.")
public class DealMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private Deal deal;
    private User user;
    private String memberTypeCode;
    private String memberTypeDesc;
    // TODO: Remove as part of LM-2493
    private EventOriginationParticipant eventOriginationParticipant;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

}