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
@Schema(name = "Picklist Item", description = "Model for a Lamina picklist item.")
public class PicklistItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private PicklistCategory category;
    private String option;
    private Integer order;

    public PicklistItem(Long id) {
        this.id = id;
    }

}