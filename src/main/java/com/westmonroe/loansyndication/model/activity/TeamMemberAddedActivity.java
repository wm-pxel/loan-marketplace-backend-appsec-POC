package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TeamMemberAddedActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("teamMemberFullName") ) {
            log.error("Team member's full name was not provided in the activity");
            throw new MissingDataException("Team member's full name was not provided in the activity");
        }

        String json = String.format("""
        {
            "message" : "Added team members to this deal.",
            "teamMemberFullName" : "%s"
        }
        """, activityMap.get("teamMemberFullName"));

        return json;
    }

}