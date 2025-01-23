package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TeamMemberRemovedActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("teamMemberFullName") ) {
            log.error("Removed team member's full name was not provided in the activity");
            throw new MissingDataException("Removed team member's full name was not provided in the activity");
        }

        String json = String.format("""
        {
            "message" : "Removed team members from this deal.",
            "teamMemberFullName" : "%s"
        }
        """, activityMap.get("teamMemberFullName"));

        return json;
    }

}