package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DealInterestActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {
        if ( !activityMap.containsKey("dealParticipantId") ) {
            log.error("dealParticipantId was not provided in the activity");
            throw new MissingDataException("dealParticipantId was not provided in the activity");
        }
        String json = String.format("""
                {
                    "participantId": "%d"
                }
                """, activityMap.get("dealParticipantId"));

        return json;
    }

}