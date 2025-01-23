package com.westmonroe.loansyndication.querydef;

public class StateQueryDef {

    private StateQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_STATE = "SELECT STATE_CD"
                                                 + ", STATE_NAME "
                                              + "FROM STATE_DEF";

}