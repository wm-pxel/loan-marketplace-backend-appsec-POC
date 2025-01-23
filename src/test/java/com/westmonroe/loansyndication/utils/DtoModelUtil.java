package com.westmonroe.loansyndication.utils;

import com.westmonroe.loansyndication.model.integration.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DtoModelUtil {

    private DtoModelUtil() {
        throw new IllegalStateException("The class cannot be instantiated. It is a utility class.");
    }

    public static DealDto createTestDealDto(String dealExternalId, String originatorId, String name, String dealIndustry
            , String dealStructure, String description, BigDecimal dealAmount, Integer defaultProbability
            , BigDecimal currYearEbita) {

        DealDto deal = new DealDto();
        deal.setDealExternalId(dealExternalId);
        deal.setOriginatorId(originatorId);
        deal.setName(name);
        deal.setDealIndustry(dealIndustry);
        deal.setDealStructure(dealStructure);
        deal.setDealType("New");
        deal.setDescription(description);
        deal.setDealAmount(dealAmount);
        deal.setEffectiveDate(LocalDate.now());
        deal.setDefaultProbability(defaultProbability);
        deal.setCurrYearEbita(currYearEbita);

        return deal;
    }

    public static ApplicantDto createTestApplicantDto(String applicantExternalId, String borrowerDesc, String borrowerName
            , String cityName, String stateCode, String countyName, String farmCreditEligOption, String taxId
            , String industryCode, Integer businessAge) {

        ApplicantDto applicant = new ApplicantDto();
        applicant.setApplicantExternalId(applicantExternalId);
        applicant.setBorrowerDesc(borrowerDesc);
        applicant.setBorrowerName(borrowerName);
        applicant.setBorrowerCityName(cityName);
        applicant.setBorrowerStateCode(stateCode);
        applicant.setBorrowerCountyName(countyName);
        applicant.setFarmCreditElig(farmCreditEligOption);
        applicant.setTaxId(taxId);
        applicant.setBorrowerIndustry(industryCode);
        applicant.setBusinessAge(businessAge);

        return applicant;
    }

    public static DealCovenantDto createTestDealCovenantDto(String covenantExternalId, String entityName, String categoryName
            , String covenantType, String frequency, LocalDate nextEvalDate, LocalDate effectiveDate) {

        DealCovenantDto covenant = new DealCovenantDto();
        covenant.setCovenantExternalId(covenantExternalId);
        covenant.setEntityName(entityName);
        covenant.setCategoryName(categoryName);
        covenant.setCovenantType(covenantType);
        covenant.setFrequency(frequency);
        covenant.setNextEvalDate(nextEvalDate);
        covenant.setEffectiveDate(effectiveDate);

        return covenant;
    }

    public static DealFacilityDto createTestDealFacilityDto(String facilityExternalId, BigDecimal facilityAmount
                    , String facilityType, String collateral, Integer tenor, String pricing, String creditSpreadAdj
                    , String facilityPurpose, String purposeDetail, String dayCount, String guarInvFlag, String patronagePayingFlag
                    , String farmCreditType, Integer revolverUtil, String upfrontFees, String unusedFees
                    , String amortization, String lgdOption, String regulatoryLoanType) {

        DealFacilityDto facility = new DealFacilityDto();
        facility.setFacilityExternalId(facilityExternalId);
        facility.setFacilityAmount(facilityAmount);
        facility.setFacilityType(facilityType);
        facility.setCollateral(collateral);
        facility.setTenor(tenor);
        facility.setPricing(pricing);
        facility.setCreditSpreadAdj(creditSpreadAdj);
        facility.setFacilityPurpose(facilityPurpose);
        facility.setPurposeDetail(purposeDetail);
        facility.setDayCount(dayCount);
        facility.setGuarInvFlag(guarInvFlag);
        facility.setPatronagePayingFlag(patronagePayingFlag);
        facility.setFarmCreditType(farmCreditType);
        facility.setRevolverUtil(revolverUtil);
        facility.setUpfrontFees(upfrontFees);
        facility.setUnusedFees(unusedFees);
        facility.setAmortization(amortization);
        facility.setLgdOption(lgdOption);
        facility.setRegulatoryLoanType(regulatoryLoanType);


        return facility;
    }

    public static DealDocumentDto createTestDealDocumentDto(String documentExternalId, String url, String displayName
            , String extension, String category) {

        DealDocumentDto document = new DealDocumentDto();
        document.setDocumentExternalId(documentExternalId);
        document.setUrl(url);
        document.setDisplayName(displayName);
        document.setExtension(extension);
        document.setCategory(category);

        return document;
    }

}