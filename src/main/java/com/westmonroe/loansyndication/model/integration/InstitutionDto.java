package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_UUID;
import static com.westmonroe.loansyndication.utils.Constants.REGEX_YN;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstitutionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "The institution uid cannot be null.")
    @Pattern(regexp = REGEX_UUID, message = "The id must be in valid UUID format.")
    private String uid;

    @NotNull(message = "The institution name cannot be null.")
    @Size(min = 5, message = "The institution name must be at least 5 characters")
    @Size(max = 80, message = "The institution name must be no more than 80 characters")
    private String name;

    @Pattern(regexp = REGEX_YN, message = "The active flag can only be Y or N.")
    private String active;

    public InstitutionDto(String uid) {
        this.uid = uid;
    }

}