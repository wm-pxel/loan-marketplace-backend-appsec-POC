package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.NaicsCode;
import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.integration.ApplicantDto;
import com.westmonroe.loansyndication.model.integration.DealDto;
import com.westmonroe.loansyndication.service.DefinitionService;
import com.westmonroe.loansyndication.service.PicklistService;

import java.util.Map;

import static java.util.Map.entry;

public class DealMapper {

    private final PicklistService picklistService;
    private final DefinitionService definitionService;

    public DealMapper(PicklistService picklistService, DefinitionService definitionService) {
        this.picklistService = picklistService;
        this.definitionService = definitionService;
    }

    private Map<String, Object> createPicklistItemMap(PicklistItem picklistItem) {
        return Map.ofEntries(
                entry("id", picklistItem.getId()),
                entry("option", picklistItem.getOption()),
                entry("order", picklistItem.getOrder())
            );
    }

    public Deal dealDtoToDeal(DealDto dealDto, Institution originator, ApplicantDto applicantDto) {

        Deal deal = new Deal();

        deal.setDealExternalId(dealDto.getDealExternalId());
        deal.setName(dealDto.getName());

        PicklistItem dealIndustry = picklistService.getPicklistForCategoryAndOption(
            "Deal Industry", dealDto.getDealIndustry()
        );
        deal.setDealIndustry(dealIndustry);

        deal.setOriginator(originator);
        deal.setInitialLenderFlag("N");      // We will not get initial lender, so hard-code this to "N".

        PicklistItem dealStructure = picklistService.getPicklistForCategoryAndOption(
            "Deal Structure", dealDto.getDealStructure()
        );
        deal.setDealStructure(dealStructure);

        deal.setDealType(dealDto.getDealType());
        deal.setDescription(dealDto.getDescription());
        deal.setDealAmount(dealDto.getDealAmount());

        /*
         *  Applicant data to borrower fields on the deal.
         */
        deal.setApplicantExternalId(applicantDto.getApplicantExternalId());
        deal.setBorrowerDesc(applicantDto.getBorrowerDesc());
        deal.setBorrowerName(applicantDto.getBorrowerName());
        deal.setBorrowerCityName(applicantDto.getBorrowerCityName());
        deal.setBorrowerStateCode(applicantDto.getBorrowerStateCode());
        deal.setBorrowerCountyName(applicantDto.getBorrowerCountyName());

        PicklistItem farmCreditElig = picklistService.getPicklistForCategoryAndOption(
            "Farm Credit Eligibility", applicantDto.getFarmCreditElig()
        );
        deal.setFarmCreditElig(farmCreditElig);

        deal.setTaxId(applicantDto.getTaxId());
        deal.setBorrowerIndustry(new NaicsCode(applicantDto.getBorrowerIndustry(), null));
        deal.setBusinessAge(applicantDto.getBusinessAge());

        deal.setDefaultProbability(dealDto.getDefaultProbability());
        deal.setCurrYearEbita(dealDto.getCurrYearEbita());
        deal.setActive("Y");

        return deal;
    }

    public DealDto dealToDealDto(Deal deal) {

        DealDto dealDto = new DealDto();

        dealDto.setDealExternalId(deal.getDealExternalId());
        dealDto.setOriginatorId(deal.getOriginator().getUid());
        dealDto.setOriginatorName(deal.getOriginator().getName());
        dealDto.setName(deal.getName());
        dealDto.setDealIndustry(deal.getDealIndustry().getOption());
        dealDto.setDealStructure(deal.getDealStructure().getOption());
        dealDto.setDealType(deal.getDealType());
        dealDto.setDescription(deal.getDescription());
        dealDto.setDealAmount(deal.getDealAmount());
        dealDto.setDefaultProbability(deal.getDefaultProbability());
        dealDto.setCurrYearEbita(deal.getCurrYearEbita());

        return dealDto;
    }

    public ApplicantDto dealToApplicantDto(Deal deal) {

        ApplicantDto applicantDto = new ApplicantDto();

        applicantDto.setApplicantExternalId(deal.getApplicantExternalId());
        applicantDto.setBorrowerDesc(deal.getBorrowerDesc());
        applicantDto.setBorrowerName(deal.getBorrowerName());
        applicantDto.setBorrowerCityName(deal.getBorrowerCityName());
        applicantDto.setBorrowerStateCode(deal.getBorrowerStateCode());
        applicantDto.setBorrowerCountyName(deal.getBorrowerCountyName());
        applicantDto.setFarmCreditElig(deal.getFarmCreditElig().getOption());
        applicantDto.setTaxId(deal.getTaxId());
        applicantDto.setBorrowerIndustry(deal.getBorrowerIndustry().getCode());
        applicantDto.setBusinessAge(deal.getBusinessAge());

        return applicantDto;
    }

    public Map<String, Object> dealDtoMapToDealMap(Map<String, Object> dealMap, Map<String, Object> applicantMap) {

        PicklistItem picklistItem;

        // Merge the applicant map into the deal map.
        dealMap.putAll(applicantMap);

        if ( dealMap.containsKey("dealIndustry") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Deal Industry", dealMap.get("dealIndustry").toString());
            dealMap.put("dealIndustry", createPicklistItemMap(picklistItem));
        }

        if ( dealMap.containsKey("dealStructure") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Deal Structure", dealMap.get("dealStructure").toString());
            dealMap.put("dealStructure", createPicklistItemMap(picklistItem));
        }

        /*
         *  Applicant data to borrower fields on the deal.
         */

        if ( dealMap.containsKey("farmCreditElig") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Farm Credit Eligibility", dealMap.get("farmCreditElig").toString());
            dealMap.put("farmCreditElig", createPicklistItemMap(picklistItem));
        }

        if ( dealMap.containsKey("borrowerIndustry") ) {
            dealMap.put("borrowerIndustry", Map.of("code", dealMap.get("borrowerIndustry").toString()));
        }

        return dealMap;
    }

}