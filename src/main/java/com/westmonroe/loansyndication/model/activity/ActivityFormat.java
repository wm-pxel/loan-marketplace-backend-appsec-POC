package com.westmonroe.loansyndication.model.activity;

import java.util.Map;

public interface ActivityFormat {

    String getJson(Map<String, Object> activityMap);

}