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
public class InviteSentActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("dealParticipant") ) {
            log.error("DealParticipant was not provided in the activity");
            throw new MissingDataException("DealParticipant was not provided in the activity");
        }

        EventOriginationParticipant eventOriginationParticipant = (EventOriginationParticipant) activityMap.get("eventOriginationParticipant");

        List<EventParticipantFacility> eventParticipantFacilities = (List<EventParticipantFacility>) activityMap.get("eventParticipantFacilities");

        List<Map<String, Object>> facilitiesDetail = new ArrayList<>();

        for ( EventParticipantFacility participantFacility : eventParticipantFacilities ) {
            Map<String, Object> facilityMap = new HashMap<>();
            facilityMap.put("amount", participantFacility.getInvitationAmount());
            facilityMap.put("facilityType", participantFacility.getEventDealFacility().getDealFacility().getFacilityType().getOption());
            facilitiesDetail.add(facilityMap);
        }

        // Create a map for the JSON object
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("participantName", eventOriginationParticipant.getParticipant().getName());
        jsonMap.put("participantId", eventOriginationParticipant.getParticipant().getId());
        jsonMap.put("facilities", facilitiesDetail);
        jsonMap.put("total", eventOriginationParticipant.getTotalInvitationAmount());
        String primaryContactName = eventOriginationParticipant.getInviteRecipient() == null ? null : eventOriginationParticipant.getInviteRecipient().getFullName() ;
        if (primaryContactName != null) {
            jsonMap.put("primaryContact" , primaryContactName);
        }

        String inviteMessage = eventOriginationParticipant.getMessage();
        if (inviteMessage != null) {
            jsonMap.put("inviteMessage",inviteMessage);
        }

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