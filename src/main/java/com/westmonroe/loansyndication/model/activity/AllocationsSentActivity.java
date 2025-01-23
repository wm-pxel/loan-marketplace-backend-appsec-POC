package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import com.westmonroe.loansyndication.model.event.EventParticipantFacility;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AllocationsSentActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("dealParticipant") ) {
            log.error("DealParticipant was not provided in the activity");
            throw new MissingDataException("DealParticipant was not provided in the activity");
        }

        if ( !activityMap.containsKey("dealParticipantFacility") ) {
            log.error("dealParticipantFacility was not provided in the activity");
            throw new MissingDataException("dealParticipantFacility was not provided in the activity");
        }

        EventOriginationParticipant eventOriginationParticipant = (EventOriginationParticipant) activityMap.get("eventParticipant");

        List<EventParticipantFacility> eventParticipantFacilities = (List<EventParticipantFacility>) activityMap.get("eventParticipantFacility");

        List<Map<String, Object>> facilitiesDetail = new ArrayList<>();

        for ( EventParticipantFacility participantFacility : eventParticipantFacilities ) {
            Map<String, Object> facilityMap = new HashMap<>();
            facilityMap.put("amount", participantFacility.getAllocationAmount());
            facilityMap.put("facilityType", participantFacility.getEventDealFacility().getDealFacility().getFacilityType().getOption());
            facilitiesDetail.add(facilityMap);
        }

        // Create a map for the JSON object
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("participantId", eventOriginationParticipant.getId());
        jsonMap.put("message", String.format("Allocation sent to %s.", eventOriginationParticipant.getParticipant().getName()));
        jsonMap.put("facilities", facilitiesDetail);
        jsonMap.put("total", eventOriginationParticipant.getTotalAllocationAmount());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

}