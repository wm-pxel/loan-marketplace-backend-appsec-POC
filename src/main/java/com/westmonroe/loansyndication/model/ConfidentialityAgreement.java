package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfidentialityAgreement implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long id;

    private String description;

    private Long institutionId;

    public ConfidentialityAgreement(Long id) {
        this.id = id;
    }

}
