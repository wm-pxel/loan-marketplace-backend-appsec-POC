package com.westmonroe.loansyndication.model.activity;

import java.util.Map;

public class InviteAmountSetActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        String json = String.format("""
        {
            "message" : "Invite Amount Set"
        }
        """);

        return json;
    }
}
