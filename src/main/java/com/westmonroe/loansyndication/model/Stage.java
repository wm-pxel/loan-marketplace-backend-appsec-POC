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
@Schema(name = "Deal Stage", description = "Model for a Lamina deal stage.")
public class Stage implements Serializable {

    private static final long serialVersionUID = 1L;

    public Stage(Long id) {
        this.id = id;
    }

    private Long id;
    private String name;
    private String title;
    private String subtitle;
    private Integer order;

}