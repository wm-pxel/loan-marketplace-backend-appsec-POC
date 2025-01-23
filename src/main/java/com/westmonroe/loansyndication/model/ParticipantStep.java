package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Participant Step", description = "Model for a Lamina participant step.")
public class ParticipantStep implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String leadViewStatus;

    private String participantStatus;

    private Integer order;

}