package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.ActivityCreationException;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.ActivityUtils.activityValueToString;
import static com.westmonroe.loansyndication.utils.DealFacilityTypeEnum.*;

@Slf4j
public class DealInfoUpdatedActivity  implements ActivityFormat {

    private boolean validFacilityTypeForAmortization(String facilityType) {
        return facilityType != null && !facilityType.equals(REVOLVER.getName());
    }

    private boolean validFacilityTypeForRevolverUtil(String facilityType){
        return facilityType != null && (facilityType.equals(REVOLVER.getName())
                || facilityType.equals(REVOLVING_TERM_LOAN.getName())
                || facilityType.equals(DELAYED_DRAW_TERM_LOAN.getName()));
    }

    private boolean validFacilityTypeForRenewalDate(String facilityType){
        return facilityType != null && (facilityType.equals(REVOLVER.getName())
                || facilityType.equals(REVOLVING_TERM_LOAN.getName())
                || facilityType.equals(DELAYED_DRAW_TERM_LOAN.getName()));
    }


    private void buildDealFacilityJson(Map<String, Object> jsonMap, Map<String, Object> activityMap) {

        Map<String, Object> dealFacilityMap = (Map) activityMap.get("dealFacilityMap");
        DealFacility oldDealFacility = (DealFacility) activityMap.get("oldDealFacility");
        DealFacility newDealFacility = (DealFacility) activityMap.get("newDealFacility");
        String facilityType = activityValueToString(oldDealFacility.getFacilityType());

        // Required ID to identify FE timeline events
        jsonMap.putIfAbsent("facilityId", newDealFacility.getId());

        if ( dealFacilityMap.containsKey("facilityType") ) {
            facilityType = activityValueToString(newDealFacility.getFacilityType());
            jsonMap.put("facilityType", Map.of(
                    "old", activityValueToString(oldDealFacility.getFacilityType()),
                    "new", activityValueToString(newDealFacility.getFacilityType()))
            );
        }

        if(validFacilityTypeForAmortization(facilityType)){
            if (dealFacilityMap.containsKey("amortization")) {
                ((Map) jsonMap.get("full")).put("amortization", Map.of(
                        "old", activityValueToString(oldDealFacility.getAmortization()),
                        "new", activityValueToString(newDealFacility.getAmortization()))
                );
            } else if (!validFacilityTypeForAmortization(activityValueToString(oldDealFacility.getFacilityType()))){
                ((Map) jsonMap.get("full")).put("amortization", Map.of(
                        "old", activityValueToString(oldDealFacility.getAmortization()),
                        "new", activityValueToString(null))
                );
            }
        }

        if(validFacilityTypeForRevolverUtil(facilityType)) {
            if (dealFacilityMap.containsKey("revolverUtil")) {
                ((Map) jsonMap.get("full")).put("revolverUtil", Map.of(
                        "old", activityValueToString(oldDealFacility.getRevolverUtil()),
                        "new", activityValueToString(newDealFacility.getRevolverUtil()))
                );
            } else if (!validFacilityTypeForRevolverUtil(activityValueToString(oldDealFacility.getFacilityType()))) {
                ((Map) jsonMap.get("full")).put("revolverUtil", Map.of(
                        "old", activityValueToString(oldDealFacility.getRevolverUtil()),
                        "new","")
                );
            }
        }

        if(validFacilityTypeForRenewalDate(facilityType)) {
            if ( dealFacilityMap.containsKey("renewalDate") ) {
                ((Map) jsonMap.get("full")).put("renewalDate", Map.of(
                        "old", activityValueToString(oldDealFacility.getRenewalDate()),
                        "new", activityValueToString(newDealFacility.getRenewalDate()))
                );
            } else if (!validFacilityTypeForRenewalDate(activityValueToString(oldDealFacility.getFacilityType()))) {
                ((Map) jsonMap.get("full")).put("renewalDate", Map.of(
                        "old", activityValueToString(oldDealFacility.getRenewalDate()),
                        "new", activityValueToString(""))
                );
            }
        }

        if ( dealFacilityMap.containsKey("wasPricingGridUploaded")){
            if (newDealFacility.getPricingGrid() == null) {
                jsonMap.put("pricingGridId", Map.of(
                        "old", activityValueToString(null),
                        "new", activityValueToString(null)));
            } else {
                jsonMap.put("pricingGridId", Map.of(
                        "old", activityValueToString(null),
                        "new", activityValueToString(activityValueToString(newDealFacility.getPricingGrid().getId()))));
            }
        }

        if ( dealFacilityMap.containsKey("unusedFees") ){
            ((Map) jsonMap.get("full")).put("unusedFees", Map.of(
                "old", activityValueToString(oldDealFacility.getUnusedFees()),
                "new", activityValueToString(newDealFacility.getUnusedFees()))
            );
        }

        if ( dealFacilityMap.containsKey("facilityPurpose")){
            ((Map) jsonMap.get("full")).put("facilityPurpose", Map.of(
                "old", activityValueToString(oldDealFacility.getFacilityPurpose()),
                "new", activityValueToString(newDealFacility.getFacilityPurpose()))
            );
        }

        if ( dealFacilityMap.containsKey("purposeDetail")){
            ((Map) jsonMap.get("full")).put("purposeDetail", Map.of(
                "old", activityValueToString(oldDealFacility.getPurposeDetail()),
                "new", activityValueToString(newDealFacility.getPurposeDetail()))
            );
        }

        if ( dealFacilityMap.containsKey("dayCount")){
            ((Map) jsonMap.get("full")).put("dayCount", Map.of(
                "old", activityValueToString(oldDealFacility.getDayCount()),
                "new", activityValueToString(newDealFacility.getDayCount()))
            );
        }

        if ( dealFacilityMap.containsKey("regulatoryLoanType")){
            ((Map) jsonMap.get("full")).put("regulatoryLoanType", Map.of(
                "old", activityValueToString(oldDealFacility.getRegulatoryLoanType()),
                "new", activityValueToString(newDealFacility.getRegulatoryLoanType()))
            );
        }

        if ( dealFacilityMap.containsKey("guarInvFlag")){
            ((Map) jsonMap.get("full")).put("guarInvFlag", Map.of(
                "old", activityValueToString(oldDealFacility.getGuarInvFlag()),
                "new", activityValueToString(newDealFacility.getGuarInvFlag()))
            );
        }

        if ( dealFacilityMap.containsKey("patronagePayingFlag")){
            ((Map) jsonMap.get("full")).put("patronagePayingFlag", Map.of(
                "old", activityValueToString(oldDealFacility.getPatronagePayingFlag()),
                "new", activityValueToString(newDealFacility.getPatronagePayingFlag()))
            );
        }

        if ( dealFacilityMap.containsKey("farmCreditType")){
            ((Map) jsonMap.get("full")).put("farmCreditType", Map.of(
                "old", activityValueToString(oldDealFacility.getFarmCreditType()),
                "new", activityValueToString(newDealFacility.getFarmCreditType()))
            );
        }

        if ( dealFacilityMap.containsKey("facilityAmount") ) {
            if (!oldDealFacility.getFacilityAmount().equals(newDealFacility.getFacilityAmount())) {
                jsonMap.put("facilityAmount", Map.of(
                        "old", activityValueToString(oldDealFacility.getFacilityAmount()),
                        "new", activityValueToString(newDealFacility.getFacilityAmount()))
                );
            }
        }

        if ( dealFacilityMap.containsKey("pricing") ) {
            jsonMap.put("pricing", Map.of(
                "old", activityValueToString(oldDealFacility.getPricing()),
                "new", activityValueToString(newDealFacility.getPricing()))
            );
        }

        if ( dealFacilityMap.containsKey("creditSpreadAdj") ) {
            jsonMap.put("creditSpreadAdj", Map.of(
                "old", activityValueToString(oldDealFacility.getCreditSpreadAdj()),
                "new", activityValueToString(newDealFacility.getCreditSpreadAdj()))
            );
        }

        if ( dealFacilityMap.containsKey("tenor") ) {
            jsonMap.put("tenor", Map.of(
                "old", activityValueToString(oldDealFacility.getTenor()),
                "new", activityValueToString(newDealFacility.getTenor()))
            );
        }

        if ( dealFacilityMap.containsKey("collateral") ) {
            ((Map) jsonMap.get("full")).put("collateral", Map.of(
                "old", activityValueToString(oldDealFacility.getCollateral()),
                "new", activityValueToString(newDealFacility.getCollateral()))
            );
        }

        if ( dealFacilityMap.containsKey("upfrontFees") ) {
            jsonMap.put("upfrontFees", Map.of(
                "old", activityValueToString(oldDealFacility.getUpfrontFees()),
                "new", activityValueToString(newDealFacility.getUpfrontFees()))
            );
        }

        if ( dealFacilityMap.containsKey("maturityDate") ) {
            ((Map) jsonMap.get("full")).put("maturityDate", Map.of(
                "old", activityValueToString(oldDealFacility.getMaturityDate()),
                "new", activityValueToString(newDealFacility.getMaturityDate()))
            );
        }

        if ( dealFacilityMap.containsKey("lgdOption") ) {
            ((Map) jsonMap.get("full")).put("lgdOption", Map.of(
                "old", activityValueToString(oldDealFacility.getLgdOption()),
                "new", activityValueToString(newDealFacility.getLgdOption()))
            );
        }
    }

    private void buildDealJson(Map<String, Object> jsonMap, Map<String, Object> activityMap) {

        Map<String, Object> dealMap = (Map) activityMap.get("dealMap");
        Deal oldDeal = (Deal) activityMap.get("oldDeal");
        Deal newDeal = (Deal) activityMap.get("newDeal");

        if ( dealMap.containsKey("name") ) {
            jsonMap.put("name", Map.of(
                "old", activityValueToString(oldDeal.getName()),
                "new", activityValueToString(newDeal.getName()))
            );
        }

        if ( dealMap.containsKey("dealIndustry") ) {
            ((Map) jsonMap.get("full")).put("dealIndustry", Map.of(
                    "old", activityValueToString(oldDeal.getDealIndustry()),
                    "new", activityValueToString(newDeal.getDealIndustry()))
            );
        }

        if ( dealMap.containsKey("dealStructure") ) {
            ((Map) jsonMap.get("full")).put("dealStructure", Map.of(
                    "old", activityValueToString(oldDeal.getDealStructure()),
                    "new", activityValueToString(newDeal.getDealStructure()))
            );
        }

        if ( dealMap.containsKey("dealType") ) {
            ((Map) jsonMap.get("full")).put("dealType", Map.of(
                    "old", activityValueToString(oldDeal.getDealType()),
                    "new", activityValueToString(newDeal.getDealType()))
            );
        }

        if ( dealMap.containsKey("description") ) {
            jsonMap.put("description", Map.of(
                    "old", activityValueToString(oldDeal.getDescription()),
                    "new", activityValueToString(newDeal.getDescription()))
            );
        }

        if ( dealMap.containsKey("dealAmount") ) {
            jsonMap.put("dealAmount", Map.of(
                    "old", activityValueToString(oldDeal.getDealAmount()),
                    "new", activityValueToString(newDeal.getDealAmount()))
            );
        }

//TODO:        if ( dealMap.containsKey("projectedLaunchDate") ) {
//            jsonMap.put("projectedLaunchDate", Map.of(
//                    "old", activityValueToString(oldDeal.getProjectedLaunchDate()),
//                    "new", activityValueToString(newDeal.getProjectedLaunchDate()))
//            );
//        }
//
//        if ( dealMap.containsKey("projectedCloseDate") ) {
//            jsonMap.put("projectedCloseDate", Map.of(
//                    "old", activityValueToString(oldDeal.getProjectedCloseDate()),
//                    "new", activityValueToString(newDeal.getProjectedCloseDate()))
//            );
//        }

        if ( dealMap.containsKey("borrowerDesc") ) {
            ((Map) jsonMap.get("full")).put("borrowerDesc", Map.of(
                    "old", activityValueToString(oldDeal.getBorrowerDesc()),
                    "new", activityValueToString(newDeal.getBorrowerDesc()))
            );
        }

        if ( dealMap.containsKey("borrowerName") ) {
            ((Map) jsonMap.get("full")).put("borrowerName", Map.of(
                    "old", activityValueToString(oldDeal.getBorrowerName()),
                    "new", activityValueToString(newDeal.getBorrowerName()))
            );
        }

        if ( dealMap.containsKey("borrowerCityName") ) {
            ((Map) jsonMap.get("full")).put("borrowerCityName", Map.of(
                    "old", activityValueToString(oldDeal.getBorrowerCityName()),
                    "new", activityValueToString(newDeal.getBorrowerCityName()))
            );
        }

        if ( dealMap.containsKey("borrowerStateCode") ) {
            ((Map) jsonMap.get("full")).put("borrowerStateCode", Map.of(
                    "old", activityValueToString(oldDeal.getBorrowerStateCode()),
                    "new", activityValueToString(newDeal.getBorrowerStateCode()))
            );
        }

        if ( dealMap.containsKey("borrowerCountyName") ) {
            ((Map) jsonMap.get("full")).put("borrowerCountyName", Map.of(
                    "old", activityValueToString(oldDeal.getBorrowerCountyName()),
                    "new", activityValueToString(newDeal.getBorrowerCountyName()))
            );
        }

        if ( dealMap.containsKey("farmCreditElig") ) {
            jsonMap.put("farmCreditElig", Map.of(
                    "old", activityValueToString(oldDeal.getFarmCreditElig()),
                    "new", activityValueToString(newDeal.getFarmCreditElig()))
            );
        }

        if ( dealMap.containsKey("taxId") ) {
            ((Map) jsonMap.get("full")).put("taxId", Map.of(
                    "old", activityValueToString(oldDeal.getTaxId()),
                    "new", activityValueToString(newDeal.getTaxId()))
            );
        }

        if ( dealMap.containsKey("borrowerIndustry") ) {
            ((Map) jsonMap.get("full")).put("borrowerIndustry", Map.of(
                    "old", activityValueToString(oldDeal.getBorrowerIndustry()),
                    "new", activityValueToString(newDeal.getBorrowerIndustry()))
            );
        }

        if ( dealMap.containsKey("businessAge") ) {
            ((Map) jsonMap.get("full")).put("businessAge", Map.of(
                    "old", activityValueToString(oldDeal.getBusinessAge()),
                    "new", activityValueToString(newDeal.getBusinessAge()))
            );
        }

        if ( dealMap.containsKey("defaultProbability") ) {
            jsonMap.put("defaultProbability", Map.of(
                    "old", activityValueToString(oldDeal.getDefaultProbability()),
                    "new", activityValueToString(newDeal.getDefaultProbability()))
            );
        }

        if ( dealMap.containsKey("currYearEbita") ) {
            jsonMap.put("currYearEbita", Map.of(
                    "old", activityValueToString(oldDeal.getCurrYearEbita()),
                    "new", activityValueToString(newDeal.getCurrYearEbita()))
            );
        }

        if ( dealMap.containsKey("active") ) {
            jsonMap.put("active", Map.of(
                    "old", activityValueToString(oldDeal.getActive()),
                    "new", activityValueToString(newDeal.getActive()))
            );
        }
    }

    @Override
    public String getJson(Map<String, Object> activityMap) {
        Map<String, Object> jsonMap = new HashMap<>();

        // Create the "full" entry for fields that are only viewed by participants with full deal access.
        jsonMap.put("full", new HashMap<String, Object>());

        // Get the changed deal info map, along with the old and new deal information from the activity map
        if ( activityMap.containsKey("dealFacilityMap")){
            buildDealFacilityJson(jsonMap, activityMap);
        } else if ( activityMap.containsKey("dealMap")){
            buildDealJson(jsonMap, activityMap);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";

        try {
            json = objectMapper.writeValueAsString(jsonMap);
        } catch ( JsonProcessingException e ) {
            // Throw exception.
            throw new ActivityCreationException("There was an error creating the activity.");
        }

        return json;
    }

}