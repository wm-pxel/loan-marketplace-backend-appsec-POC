package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.westmonroe.loansyndication.validation.ValidPicklistOptionName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_DEAL_TYPE;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "DealDto", description = "Model for a Lamina deal DTO.")
public class DealDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Deal External ID cannot be empty or null")
    @Size(max = 55, message = "Deal External ID cannot be greater than 55 characters.")
    private String dealExternalId;

    @NotNull(message = "Originator id data cannot be null")
    private String originatorId;

    @ReadOnlyProperty
    private String originatorName;

    @Size(max = 80, message = "Name cannot cannot exceed 80 characters.")
    private String name;

    @ValidPicklistOptionName(category = "Deal Industry")
    private String dealIndustry;

    @ValidPicklistOptionName(category = "Deal Structure")
    private String dealStructure;

    @NotNull(message = "Deal type cannot be null.")
    @Pattern(regexp = REGEX_DEAL_TYPE, message = "Deal type must be New, Renewal or Modification.")
    private String dealType;

    @Size(max = 5000, message = "Deal description cannot exceed 5,000 characters.")
    private String description;

    @Digits(integer=12, fraction=2, message = "Total Amount must be in valid numeric format.")
    private BigDecimal dealAmount;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate projectedLaunchDate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime launchDate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate commitmentDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate commentsDueByDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate projectedCloseDate;

    @ReadOnlyProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private OffsetDateTime closeDate;

    private Integer defaultProbability;

    @Digits(integer=12, fraction=2, message = "Curr Year Ebita must be valid decimal number.")
    private BigDecimal currYearEbita;

}