package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Covenant", description = "Model for a Lamina deal covenant.")
public class DealCovenant implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 55, message = "Covenant External ID cannot be greater than 55 characters.")
    private String covenantExternalId;

    private Deal deal;

    @NotEmpty(message = "Entity name cannot be empty or null.")
    @Size(max = 100, message = "Entity name cannot be greater than 100 characters.")
    private String entityName;

    @NotEmpty(message = "Category name cannot be empty or null.")
    @Size(max = 100, message = "Category name cannot be greater than 100 characters.")
    private String categoryName;

    @Size(max = 100, message = "Covenant type cannot be greater than 100 characters.")
    private String covenantType;

    @Size(max = 20, message = "Frequency cannot be greater than 20 characters.")
    private String frequency;

    private LocalDate nextEvalDate;

    private LocalDate effectiveDate;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

}