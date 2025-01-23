package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FullDealAccessActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("dealParticipant") ) {
            log.error("DealParticipant was not provided in the activity");
            throw new MissingDataException("DealParticipant was not provided in the activity");
        }

        EventOriginationParticipant eventOriginationParticipant = (EventOriginationParticipant) activityMap.get("eventOriginationParticipant");

        String json = String.format("""
        {
            "institutionName" : "%s",
            "participantId": "%d"
        }
        """, eventOriginationParticipant.getParticipant().getName(), eventOriginationParticipant.getParticipant().getId());

        return json;
    }

}