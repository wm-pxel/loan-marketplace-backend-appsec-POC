package com.westmonroe.loansyndication.querydef.activity;

public class ActivityTypeQueryDef {

    private ActivityTypeQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_ACTIVITY_TYPE = """
        SELECT ATD.ACTIVITY_TYPE_ID
             , ATD.ACTIVITY_TYPE_NAME
             , ACD.ACTIVITY_CATEGORY_ID
             , ACD.ACTIVITY_CATEGORY_NAME
          FROM ACTIVITY_TYPE_DEF ATD LEFT JOIN ACTIVITY_CATEGORY_DEF ACD
            ON ATD.ACTIVITY_CATEGORY_ID = ACD.ACTIVITY_CATEGORY_ID
        """;

}