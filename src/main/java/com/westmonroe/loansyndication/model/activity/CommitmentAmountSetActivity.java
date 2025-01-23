package com.westmonroe.loansyndication.model.activity;

import com.westmonroe.loansyndication.exception.MissingDataException;
import com.westmonroe.loansyndication.model.event.EventOriginationParticipant;

import java.util.Map;

public class CommitmentAmountSetActivity implements ActivityFormat{

    public String getJson(Map<String, Object> activityMap) {

        String json = String.format("""
        {
            "message" : "Commitment Amount Set"
        }
        """);

        return json;
    }
}
