package com.westmonroe.loansyndication.querydef;

public class StageQueryDef {

    private StageQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_STAGE = "SELECT STAGE_ID"
                                                 + ", STAGE_NAME"
                                                 + ", TITLE_DESC"
                                                 + ", SUBTITLE_DESC"
                                                 + ", ORDER_NBR "
                                              + "FROM STAGE_DEF";

}