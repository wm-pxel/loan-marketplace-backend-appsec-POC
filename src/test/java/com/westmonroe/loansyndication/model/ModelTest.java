package com.westmonroe.loansyndication.model;

import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ModelTest {

    @Autowired
    private Validator validator;

    @Test
    void givenDeal_whenValidatingObject_thenVerify() {

        Deal deal = new Deal();
        Set<ConstraintViolation<Deal>> violations;

        /*
         *  Set valid values on the deal.
         */
        deal.setDealIndustry(new PicklistItem(16L, null, "Farm Credit", 99));
        deal.setOriginator(new Institution(1L, "Test Institution"));
        deal.setDealStructure(new PicklistItem(4L, null, "Participation", 99));
        deal.setDealType("New");
        deal.setInitialLender(new InitialLender(1L, "Test Lender", null, null, "Y"));
        deal.setBusinessAge(10);
        deal.setDefaultProbability(7);
        deal.setFarmCreditElig(new PicklistItem(1L, null, "Association Eligible", 99));
        deal.setBorrowerName("Test Borrower Name");
        deal.setBorrowerStateCode("IL");
        deal.setTaxId("123-45-6789");

        violations = validator.validate(deal);

        assertThat(violations).isEmpty();

        /*
         *  Create deal with invalid values.
         */
        deal.setDealIndustry(new PicklistItem(15L, null, "Test Deal Industry", 99));
        deal.setDealType("Existing");
        deal.setFarmCreditElig(new PicklistItem(4L, null, "Test Farm Credit Eligibility", 99));
        deal.setBorrowerName(null);
        deal.setBorrowerStateCode("XX");
        deal.setBorrowerIndustry(new NaicsCode("9875", ""));
        deal.setTaxId(null);

        violations = validator.validate(deal);

        assertThat(violations).hasSize(7);
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("dealIndustry") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("dealType") && v.getMessage().equals("Deal type must be New, Renewal or Modification.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("farmCreditElig") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("borrowerName") && v.getMessage().equals("Borrower Name cannot be empty.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("borrowerStateCode") && v.getMessage().equals("Borrower State Code must be a valid state code.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("taxId") && v.getMessage().equals("Tax id cannot be null or empty.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("borrowerIndustry") && v.getMessage().equals("The borrower industry must be a valid NAICS code.")
        )).isTrue();
    }

    @Test
    void givenInstitution_whenIgnoringFieldsForValidation_thenVerifyFieldsArentValidated() {

        Institution institution = new Institution();
        institution.setName("Test Institution");
        institution.setActive("X");

        Set<ConstraintViolation<Institution>> violations = new HashSet<>();

        violations.addAll(validator.validateProperty(institution, "name"));
        violations.addAll(validator.validateProperty(institution, "active"));

        assertThat(violations).hasSize(1);
    }

    @Test
    void givenDealFacility_whenValidatingObject_thenVerify() {

        DealFacility dealFacility = new DealFacility();
        Set<ConstraintViolation<DealFacility>> violations;

        /*
         *  Set valid values on the deal facility.
         */
        dealFacility.setFacilityAmount(BigDecimal.valueOf(11000000.11));
        dealFacility.setFacilityType(new PicklistItem(8L, null, "Revolving Term Loan", 99));
        dealFacility.setCollateral(new PicklistItem(26L, null, "Secured", 99));
        dealFacility.setFacilityPurpose(new PicklistItem(11L, null, "Existing Business Expansion", 99));
        dealFacility.setDayCount(new PicklistItem(19L, null, "Actual/360", 99));
        dealFacility.setRegulatoryLoanType(new PicklistItem(32L, null, "Agribusiness - Loans to Cooperatives", 99));
        dealFacility.setTenor(10);
        dealFacility.setPricing("SOFR + 140.0bps");
        dealFacility.setCreditSpreadAdj("Test CSA");
        dealFacility.setPurposeDetail("This is paragraph text.");
        dealFacility.setGuarInvFlag("Y");
        dealFacility.setPatronagePayingFlag("Y");
        dealFacility.setFarmCreditType("FLCA");
        dealFacility.setUpfrontFees("free text field");
        dealFacility.setUnusedFees("free text field");
        dealFacility.setAmortization("free text field");
        dealFacility.setLgdOption("A");

        violations = validator.validate(dealFacility);

        assertThat(violations).isEmpty();

        /*
         *  Create deal facility with invalid values.
         */
        dealFacility.setFacilityType(new PicklistItem(1L, null, "Revolving Term Loan", 99));
        dealFacility.setCollateral(new PicklistItem(8L, null, "Secured", 99));
        dealFacility.setFacilityPurpose(new PicklistItem(20L, null, "Existing Business Expansion", 99));
        dealFacility.setDayCount(new PicklistItem(28L, null, "Actual/360", 99));
        dealFacility.setRegulatoryLoanType(new PicklistItem(28L, null, "Agribusiness - Loans to Cooperatives", 99));
        dealFacility.setTenor(-10);
        dealFacility.setGuarInvFlag("X");
        dealFacility.setPatronagePayingFlag("X");
        dealFacility.setFarmCreditType("AAAA");
        dealFacility.setUpfrontFees(StringUtils.repeat("*", 301));
        dealFacility.setUnusedFees(StringUtils.repeat("*", 301));
        dealFacility.setPricing(StringUtils.repeat("*", 501));
        dealFacility.setCreditSpreadAdj(StringUtils.repeat("*", 501));
        dealFacility.setAmortization(StringUtils.repeat("*", 1501));
        dealFacility.setPurposeDetail(StringUtils.repeat("*", 1501));
        dealFacility.setLgdOption("M");

        violations = validator.validate(dealFacility);

        assertThat(violations).hasSize(16);
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("facilityType") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("collateral") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("facilityPurpose") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("dayCount") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("regulatoryLoanType") && v.getMessage().contains("Invalid picklist item")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("tenor") && v.getMessage().equals("Tenor must be a positive number.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("guarInvFlag") && v.getMessage().contains("The Guarantors Involved flag can only be Y or N.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("patronagePayingFlag") && v.getMessage().contains("The Patronage Paying flag can only be Y or N.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("farmCreditType") && v.getMessage().equals("Farm Credit type can only be PCA or FLCA.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("upfrontFees") && v.getMessage().equals("Upfront fees cannot exceed 300 characters.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("unusedFees") && v.getMessage().equals("Unused fees cannot exceed 300 characters.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("pricing") && v.getMessage().equals("Pricing cannot exceed 500 characters.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("creditSpreadAdj") && v.getMessage().equals("CSA cannot exceed 500 characters.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("amortization") && v.getMessage().equals("Amortization cannot exceed 1,500 characters.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("purposeDetail") && v.getMessage().equals("Purpose detail cannot exceed 1,500 characters.")
        )).isTrue();
        assertThat(violations.stream().anyMatch(v ->
            v.getPropertyPath().toString().equals("lgdOption") && v.getMessage().equals("LGD option must be A, B, C, D, E, or F.")
        )).isTrue();
    }
}