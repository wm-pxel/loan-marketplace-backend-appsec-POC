package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.westmonroe.loansyndication.validation.ValidPicklistOptionName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.westmonroe.loansyndication.utils.Constants.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "DealFacilityDto", description = "Model for a Lamina deal facility DTO.")
public class DealFacilityDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Facility External ID cannot be empty or null.")
    @Size(max = 55, message = "Facility External ID cannot be greater than 55 characters.")
    private String facilityExternalId;

    @ReadOnlyProperty
    private String facilityName;

    @NotNull(message = "Facility amount is required.")
    private BigDecimal facilityAmount;

    @ValidPicklistOptionName(category = "Facility Type")
    private String facilityType;

    @ValidPicklistOptionName(category = "Collateral")
    private String collateral;

    @PositiveOrZero(message = "Tenor must be a positive number.")
    private Integer tenor;

    @Size(max = 500, message = "Pricing cannot exceed 500 characters.")
    private String pricing;

    @Size(max = 500, message = "CSA cannot exceed 500 characters.")
    private String creditSpreadAdj;

    @ValidPicklistOptionName(category = "Facility Purpose")
    private String facilityPurpose;

    @Size(max = 1500, message = "Purpose detail cannot exceed 1,500 characters.")
    private String purposeDetail;

    @ValidPicklistOptionName(category = "Day Count")
    private String dayCount;

    @ValidPicklistOptionName(category = "Regulatory Loan Type")
    private String regulatoryLoanType;

    @Pattern(regexp = REGEX_YN, message = "The Guarantors Involved flag can only be Y or N.")
    private String guarInvFlag;

    @Pattern(regexp = REGEX_YN, message = "The Patronage Paying flag can only be Y or N.")
    private String patronagePayingFlag;

    @Pattern(regexp = REGEX_FARM_CREDIT, message = "Farm Credit type can only be PCA or FLCA.")
    private String farmCreditType;

    @Range(min = 0, max = 100, message = "Revolver utilization must be between 0 and 100.")
    private Integer revolverUtil;

    @Size(max = 300, message = "Upfront fees cannot exceed 300 characters.")
    private String upfrontFees;

    @Size(max = 300, message = "Unused fees cannot exceed 300 characters.")
    private String unusedFees;

    @Size(max = 1500, message = "Amortization cannot exceed 1,500 characters.")
    private String amortization;

    private LocalDate maturityDate;

    private LocalDate renewalDate;

    @Pattern(regexp = REGEX_LGD, message = "LGD option must be A, B, C, D, E, or F.")
    private String lgdOption;
}