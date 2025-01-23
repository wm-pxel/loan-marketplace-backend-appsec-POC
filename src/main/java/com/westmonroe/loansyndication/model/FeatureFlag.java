package com.westmonroe.loansyndication.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_YN;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Feature Flag", description = "Model for a Lamina feature flags")
public class FeatureFlag implements Serializable {
    private Long id;

    @NotEmpty
    private String featureName;

    @NotEmpty
    private String description;

    @NotEmpty
    @Pattern(regexp = REGEX_YN, message = "The isEnabled flag must be Y or N.")
    private String isEnabled;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;
}
