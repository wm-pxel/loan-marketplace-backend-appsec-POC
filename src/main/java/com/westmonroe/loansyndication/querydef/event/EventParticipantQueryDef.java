package com.westmonroe.loansyndication.querydef.event;

public class EventParticipantQueryDef {

    private EventParticipantQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_EVENT_PARTICIPANT = """
        SELECT EP.EVENT_PARTICIPANT_ID
             , EP.EVENT_ID
             , EI.EVENT_UUID
             , EI.EVENT_EXTERNAL_UUID
             , EI.EVENT_NAME
             , ETD.EVENT_TYPE_ID
             , ETD.EVENT_TYPE_NAME
             , EP.PARTICIPANT_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , EP.PARTICIPANT_STEP_ID
             , PSD.STEP_NAME
             , PSD.ORIG_STATUS_DESC
             , PSD.PART_STATUS_DESC
             , PSD.ORDER_NBR
             , EP.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , EP.CREATED_DATE
             , EP.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , EP.UPDATED_DATE
          FROM EVENT_PARTICIPANT EP LEFT JOIN EVENT_INFO EI
            ON EP.EVENT_ID = EI.EVENT_ID LEFT JOIN EVENT_TYPE_DEF ETD
            ON EI.EVENT_TYPE_ID = ETD.EVENT_TYPE_ID LEFT JOIN INSTITUTION_INFO II
            ON EP.PARTICIPANT_ID = II.INSTITUTION_ID LEFT JOIN PARTICIPANT_STEP_DEF PSD
            ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID LEFT JOIN USER_INFO UIC
            ON EP.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON EP.UPDATED_BY_ID = UIU.USER_ID
    """;
    public static final String INSERT_EVENT_PARTICIPANT = """
        INSERT INTO EVENT_PARTICIPANT
             ( EVENT_ID, PARTICIPANT_ID, PARTICIPANT_STEP_ID, CREATED_BY_ID, UPDATED_BY_ID )
               VALUES
             ( ?, ?, ?, ?, ? )
    """;
    public static final String UPDATE_EVENT_PARTICIPANT = """
        UPDATE EVENT_PARTICIPANT
           SET PARTICIPANT_ID = ?
             , PARTICIPANT_STEP_ID = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
    """;
    public static final String UPDATE_EVENT_PARTICIPANT_STEP = """
        UPDATE EVENT_PARTICIPANT
           SET PARTICIPANT_STEP_ID = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
    """;
    public static final String UPDATE_EVENT_PARTICIPANT_UPDATED_BY = """
        UPDATE EVENT_PARTICIPANT
           SET UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
    """;
    public static final String DELETE_EVENT_PARTICIPANT = "DELETE FROM EVENT_PARTICIPANT";

}