package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ClosingMemoUploadedActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("documentLink") ) {
            log.error("Document was not provided in the activity");
            throw new MissingDataException("Document was not provided in the activity");
        }

        String json = String.format("""
        {
            "message" : "Confirmed Closing Memo is available in Files.",
            "file" : "%s"
        }
        """, activityMap.get("documentLink"));

        return json;
    }

}