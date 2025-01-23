package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DealLaunchActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("deal") ) {
            log.error("Deal was not provided for the activity");
            throw new MissingDataException("Deal was not provided for the activity");
        }

        Deal deal = (Deal) activityMap.get("deal");
        Event event = (Event) activityMap.get("event");

        String json = String.format("""
        {
            "message" : "%s launched the deal.",
            "commitmentDueDate" : "%s",
            "projectedCloseDate" : "%s"
        }
        """, deal.getOriginator().getName(), event.getCommitmentDate(), event.getProjectedCloseDate());

        return json;
    }

}