package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.model.event.EventLeadFacility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllocationAmountSetActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        List<EventLeadFacility> facilities = (List<EventLeadFacility>) activityMap.get("facilities");

        List<Map<String, Object>> facilitiesDetail = new ArrayList<>();

        for ( EventLeadFacility facility : facilities ) {
            Map<String, Object> facilityMap = new HashMap<>();
            facilityMap.put("facilityId", facility.getEventDealFacility().getId());
            facilityMap.put("type", facility.getEventDealFacility().getDealFacility().getFacilityType().getOption());
            facilityMap.put("amount", facility.getAllocationAmount());
            facilitiesDetail.add(facilityMap);
        }
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("facilities", facilitiesDetail);

        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}
