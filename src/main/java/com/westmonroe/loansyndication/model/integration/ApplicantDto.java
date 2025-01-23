package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.westmonroe.loansyndication.validation.ValidNaicsCode;
import com.westmonroe.loansyndication.validation.ValidPicklistOptionName;
import com.westmonroe.loansyndication.validation.ValidState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApplicantDto", description = "Model for a Lamina applicant DTO.")
public class ApplicantDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Applicant External ID cannot be empty or null")
    @Size(max = 55, message = "Applicant External ID cannot be greater than 55 characters.")
    private String applicantExternalId;

    @Size(max = 5000, message = "Borrower description cannot exceed 5,000 characters.")
    private String borrowerDesc;

    @NotEmpty(message = "Borrower Name cannot be null or empty.")
    @Size(max=250, message = "Borrower Name cannot be exceed 250 characters.")
    private String borrowerName;

    @Size(max=50, message = "Borrower City Name cannot exceed 50 characters.")
    private String borrowerCityName;

    @ValidState(message = "Borrower State Code must be a valid state code.")
    private String borrowerStateCode;

    @Size(max=50, message = "Borrower County Name cannot exceed 50 characters.")
    private String borrowerCountyName;

    @ValidPicklistOptionName(category = "Farm Credit Eligibility")
    private String farmCreditElig;

    @NotEmpty(message = "Tax id cannot be null or empty.")
    @Size(min=1, max=15, message = "Tax id cannot exceed 15 characters.")
    private String taxId;

    @ValidNaicsCode(message = "The borrower industry must be a valid NAICS code.")
    private String borrowerIndustry;

    @PositiveOrZero(message = "Age of business must be a positive number.")
    private Integer businessAge;

}
