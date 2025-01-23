package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "DealCovenantDto", description = "Model for a Lamina deal covenant DTO.")
public class DealCovenantDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Covenant External ID cannot be empty or null.")
    @Size(max = 55, message = "Covenant External ID cannot be greater than 55 characters.")
    private String covenantExternalId;

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

}