package com.westmonroe.loansyndication.querydef.event;

public class EventTypeQueryDef {

    private EventTypeQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_EVENT_TYPE = """
        SELECT EVENT_TYPE_ID
             , EVENT_TYPE_NAME
          FROM EVENT_TYPE_DEF
        """;

}