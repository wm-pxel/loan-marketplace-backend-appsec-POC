package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.validation.ValidPicklistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
@Schema(name = "Deal Facility", description = "Model for a Lamina deal facility.")
public class DealFacility implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 55, message = "Facility External ID cannot be greater than 55 characters.")
    private String facilityExternalId;

    private Deal deal;

    @ReadOnlyProperty
    private String facilityName;

    @ReadOnlyProperty
    private BigDecimal facilityAmount;

    @ValidPicklistItem(category = "Facility Type")
    private PicklistItem facilityType;

    @ValidPicklistItem(category = "Collateral")
    private PicklistItem collateral;

    @PositiveOrZero(message = "Tenor must be a positive number.")
    private Integer tenor;

    private DealDocument pricingGrid;

    @Size(max = 500, message = "Pricing cannot exceed 500 characters.")
    private String pricing;

    @Size(max = 500, message = "CSA cannot exceed 500 characters.")
    private String creditSpreadAdj;

    @ValidPicklistItem(category = "Facility Purpose")
    private PicklistItem facilityPurpose;

    @Size(max = 1500, message = "Purpose detail cannot exceed 1,500 characters.")
    private String purposeDetail;

    @ValidPicklistItem(category = "Day Count")
    private PicklistItem dayCount;

    @ValidPicklistItem(category = "Regulatory Loan Type")
    private PicklistItem regulatoryLoanType;

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

    @Pattern(regexp = REGEX_LGD, message = "LGD option must be A, B, C, D, E, or F.")
    private String lgdOption;

    private LocalDate maturityDate;

    private LocalDate renewalDate;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

}