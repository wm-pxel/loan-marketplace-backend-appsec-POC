package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FileRemovedActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        if ( !activityMap.containsKey("dealDocument") ) {
            log.error("Document was not provided in the activity");
            throw new MissingDataException("Document was not provided in the activity");
        }

        DealDocument dealDocument = (DealDocument) activityMap.get("dealDocument");

        String json = String.format("""
        {
            "documentDisplayName" : "%s",
            "documentCategoryName" : "%s"
        }
        """, dealDocument.getDisplayName(), dealDocument.getCategory().getName());

        return json;
    }

}