package com.westmonroe.loansyndication.model.activity;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DealCreatedActivity implements ActivityFormat {

    public String getJson(Map<String, Object> activityMap) {

        String json = String.format("""
        {
            "message" : "Deal Created in Lamina"
        }
        """);

        return json;
    }

}