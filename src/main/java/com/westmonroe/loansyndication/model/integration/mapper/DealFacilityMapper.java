package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.PicklistItem;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import com.westmonroe.loansyndication.model.integration.DealFacilityDto;
import com.westmonroe.loansyndication.service.PicklistService;

import java.util.Map;

import static java.util.Map.entry;

public class DealFacilityMapper {

    private final PicklistService picklistService;

    public DealFacilityMapper(PicklistService picklistService) {
        this.picklistService = picklistService;
    }

    private Map<String, Object> createPicklistItemMap(PicklistItem picklistItem) {
        return Map.ofEntries(
                entry("id", picklistItem.getId()),
                entry("option", picklistItem.getOption()),
                entry("order", picklistItem.getOrder())
        );
    }

    public DealFacility dealFacilityDtoToDealFacility(DealFacilityDto dealFacilityDto, Deal deal) {

        DealFacility dealFacility = new DealFacility();

        dealFacility.setFacilityExternalId(dealFacilityDto.getFacilityExternalId());
        dealFacility.setDeal(deal);
        dealFacility.setFacilityAmount(dealFacilityDto.getFacilityAmount());

        PicklistItem facilityType = picklistService.getPicklistForCategoryAndOption(
            "Facility Type", dealFacilityDto.getFacilityType()
        );
        dealFacility.setFacilityType(facilityType);

        PicklistItem collateral = picklistService.getPicklistForCategoryAndOption(
            "Collateral", dealFacilityDto.getCollateral()
        );
        dealFacility.setCollateral(collateral);

        dealFacility.setTenor(dealFacilityDto.getTenor());
        dealFacility.setPricing(dealFacilityDto.getPricing());
        dealFacility.setCreditSpreadAdj(dealFacilityDto.getCreditSpreadAdj());

        PicklistItem facilityPurpose = picklistService.getPicklistForCategoryAndOption(
            "Facility Purpose", dealFacilityDto.getFacilityPurpose()
        );
        dealFacility.setFacilityPurpose(facilityPurpose);

        dealFacility.setPurposeDetail(dealFacilityDto.getPurposeDetail());

        PicklistItem dayCount = picklistService.getPicklistForCategoryAndOption(
            "Day Count", dealFacilityDto.getDayCount()
        );
        dealFacility.setDayCount(dayCount);

        PicklistItem regulatoryLoanType = picklistService.getPicklistForCategoryAndOption(
            "Regulatory Loan Type", dealFacilityDto.getRegulatoryLoanType()
        );
        dealFacility.setRegulatoryLoanType(regulatoryLoanType);

        dealFacility.setGuarInvFlag(dealFacilityDto.getGuarInvFlag());
        dealFacility.setPatronagePayingFlag(dealFacilityDto.getPatronagePayingFlag());
        dealFacility.setFarmCreditType(dealFacilityDto.getFarmCreditType());
        dealFacility.setRevolverUtil(dealFacilityDto.getRevolverUtil());
        dealFacility.setUpfrontFees(dealFacilityDto.getUpfrontFees());
        dealFacility.setUnusedFees(dealFacilityDto.getUnusedFees());
        dealFacility.setAmortization(dealFacilityDto.getAmortization());
        dealFacility.setLgdOption(dealFacilityDto.getLgdOption());

        return dealFacility;
    }

    public DealFacilityDto dealFacilityToDealFacilityDto(DealFacility dealFacility) {

        DealFacilityDto dealFacilityDto = new DealFacilityDto();

        dealFacilityDto.setFacilityExternalId(dealFacility.getFacilityExternalId());
        dealFacilityDto.setFacilityName(dealFacility.getFacilityName());
        dealFacilityDto.setFacilityAmount(dealFacility.getFacilityAmount());
        dealFacilityDto.setFacilityType(dealFacility.getFacilityType() == null ? null : dealFacility.getFacilityType().getOption());
        dealFacilityDto.setCollateral(dealFacility.getCollateral() == null ? null : dealFacility.getCollateral().getOption());
        dealFacilityDto.setTenor(dealFacility.getTenor());
        dealFacilityDto.setPricing(dealFacility.getPricing());
        dealFacilityDto.setCreditSpreadAdj(dealFacility.getCreditSpreadAdj());
        dealFacilityDto.setFacilityPurpose(dealFacility.getFacilityPurpose() == null ? null : dealFacility.getFacilityPurpose().getOption());
        dealFacilityDto.setPurposeDetail(dealFacility.getPurposeDetail());
        dealFacilityDto.setDayCount(dealFacility.getDayCount() == null ? null : dealFacility.getDayCount().getOption());
        dealFacilityDto.setRegulatoryLoanType(dealFacility.getRegulatoryLoanType() == null ? null : dealFacility.getRegulatoryLoanType().getOption());
        dealFacilityDto.setGuarInvFlag(dealFacility.getGuarInvFlag());
        dealFacilityDto.setPatronagePayingFlag(dealFacility.getPatronagePayingFlag());
        dealFacilityDto.setFarmCreditType(dealFacility.getFarmCreditType());
        dealFacilityDto.setRevolverUtil(dealFacility.getRevolverUtil());
        dealFacilityDto.setUpfrontFees(dealFacility.getUpfrontFees());
        dealFacilityDto.setUnusedFees(dealFacility.getUnusedFees());
        dealFacilityDto.setAmortization(dealFacility.getAmortization());
        dealFacilityDto.setLgdOption(dealFacility.getLgdOption());

        return dealFacilityDto;
    }

    public Map<String, Object> facilityDtoMapToFacilityMap(Map<String, Object> facilityMap, Long dealFacilityId, String dealUid) {

        PicklistItem picklistItem;

        facilityMap.put("id", dealFacilityId);
        facilityMap.put("deal", Map.of("uid", dealUid));

        if ( facilityMap.containsKey("facilityType") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Facility Type", facilityMap.get("facilityType").toString());
            facilityMap.put("facilityType", createPicklistItemMap(picklistItem));
        }

        if ( facilityMap.containsKey("collateral") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Collateral", facilityMap.get("collateral").toString());
            facilityMap.put("collateral", createPicklistItemMap(picklistItem));
        }

        if ( facilityMap.containsKey("facilityPurpose") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Facility Purpose", facilityMap.get("facilityPurpose").toString());
            facilityMap.put("facilityPurpose", createPicklistItemMap(picklistItem));
        }

        if ( facilityMap.containsKey("dayCount") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Day Count", facilityMap.get("dayCount").toString());
            facilityMap.put("dayCount", createPicklistItemMap(picklistItem));
        }

        if ( facilityMap.containsKey("regulatoryLoanType") ) {
            picklistItem = picklistService.getPicklistForCategoryAndOption("Regulatory Loan Type", facilityMap.get("regulatoryLoanType").toString());
            facilityMap.put("regulatoryLoanType", createPicklistItemMap(picklistItem));
        }

        return facilityMap;
    }

}