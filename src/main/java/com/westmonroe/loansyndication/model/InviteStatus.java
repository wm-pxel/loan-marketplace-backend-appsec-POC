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
@Schema(name = "Invite Status", description = "Model for a Lamina invite status.")
public class InviteStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    public InviteStatus(String code) {
        this.code = code;
    }

    private String code;
    private String description;

}