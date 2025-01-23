package com.westmonroe.loansyndication.model.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.MissingDataException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SignedParticipantCertificateSentActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {
        if ( !activityMap.containsKey("externalPC") ) {
            log.error("externalPC indicator was not provided in the activity");
            throw new MissingDataException("externalPC indicator was not provided in the activity");
        }

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("externalPC", activityMap.get("externalPC"));
        jsonMap.put("participantId", activityMap.get("participantId"));


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