package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ParticipationCertificateSentActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("dealParticipant") ) {
            log.error("DealParticipant was not provided in the activity");
            throw new MissingDataException("DealParticipant was not provided in the activity");
        }
        if ( !activityMap.containsKey("externalPC") ) {
            log.error("externalPC indicator was not provided in the activity");
            throw new MissingDataException("externalPC indicator was not provided in the activity");
        }

        EventOriginationParticipant eventOriginationParticipant = (EventOriginationParticipant) activityMap.get("eventOriginationParticipant");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("participatingInstitutionName", eventOriginationParticipant.getParticipant().getName());
        jsonMap.put("externalPC", activityMap.get("externalPC"));
        jsonMap.put("participantId", eventOriginationParticipant.getParticipant().getId());

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