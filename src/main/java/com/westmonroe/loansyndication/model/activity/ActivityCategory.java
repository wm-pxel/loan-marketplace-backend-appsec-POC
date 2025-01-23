package com.westmonroe.loansyndication.model.activity;

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
@Schema(name = "Activity Category", description = "Model for a Lamina activity category.")
public class ActivityCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

}