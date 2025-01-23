package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DealClosedActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("event") ) {
            log.error("Event was not provided for the activity");
            throw new MissingDataException("Event was not provided for the activity");
        }

        Event event = (Event) activityMap.get("event");

        String json = String.format("""
        {
            "message" : "Deal Origination Closed",
            "effectiveDate" : "%s"
        }
        """, event.getEffectiveDate());

        return json;
    }

}
