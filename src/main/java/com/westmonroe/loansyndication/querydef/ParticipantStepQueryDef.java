package com.westmonroe.loansyndication.querydef;

public class ParticipantStepQueryDef {

    private ParticipantStepQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_PARTICIPANT_STEP = "SELECT PARTICIPANT_STEP_ID"
                                                            + ", STEP_NAME"
                                                            + ", ORIG_STATUS_DESC"
                                                            + ", PART_STATUS_DESC"
                                                            + ", ORDER_NBR "
                                                         + "FROM PARTICIPANT_STEP_DEF";

}