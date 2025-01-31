package com.westmonroe.loansyndication.querydef.event;

public class EventOriginationParticipantQueryDef {

    private EventOriginationParticipantQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_EVENT_PARTICIPANT_ORIG = """
        SELECT EP.EVENT_PARTICIPANT_ID
             , EP.EVENT_ID
             , EI.EVENT_UUID
             , EI.EVENT_EXTERNAL_UUID
             , EI.EVENT_NAME
             , EI.LAUNCH_DATE
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
             , EOP.INVITE_RECIPIENT_ID
             , UIR.USER_UUID AS INVITE_RECIPIENT_UUID
             , UIR.FIRST_NAME AS INVITE_RECIPIENT_FIRST_NAME
             , UIR.LAST_NAME AS INVITE_RECIPIENT_LAST_NAME
             , UIR.EMAIL_ADDR AS INVITE_RECIPIENT_EMAIL_ADDR
             , UIR.PASSWORD_DESC AS INVITE_RECIPIENT_PASSWORD_DESC
             , UIR.ACTIVE_IND AS INVITE_RECIPIENT_ACTIVE_IND
             , UIR.INSTITUTION_ID AS INVITE_RECIPIENT_INSTITUTION_ID
             , EOP.INVITE_DATE
             , EOP.FULL_DEAL_ACCESS_DATE
             , EOP.MESSAGE_DESC
             , EOP.RESPONSE_DESC
             , EOP.COMMITMENT_LETTER_ID
             , DD.DISPLAY_NAME AS CL_DISPLAY_NAME
             , DD.DOCUMENT_NAME AS CL_DOCUMENT_NAME
             , DD.DOCUMENT_TYPE AS CL_DOCUMENT_TYPE
             , DD.DOCUMENT_DESC AS CL_DOCUMENT_DESC
             , DD.SOURCE_CD AS CL_SOURCE_CD
             , EOP.PARTICIPANT_CERTIFICATE_ID
             , DD2.DISPLAY_NAME AS PC_DISPLAY_NAME
             , DD2.DOCUMENT_NAME AS PC_DOCUMENT_NAME
             , DD2.DOCUMENT_TYPE AS PC_DOCUMENT_TYPE
             , DD2.DOCUMENT_DESC AS PC_DOCUMENT_DESC
             , DD2.SOURCE_CD AS PC_SOURCE_CD
             , EOP.SIGNED_CERTIFICATE_ID
             , DD3.DISPLAY_NAME AS SPC_DISPLAY_NAME
             , DD3.DOCUMENT_NAME AS SPC_DOCUMENT_NAME
             , DD3.DOCUMENT_TYPE AS SPC_DOCUMENT_TYPE
             , DD3.DOCUMENT_DESC AS SPC_DOCUMENT_DESC
             , DD3.SOURCE_CD AS SPC_SOURCE_CD
             , ( SELECT SUM(EPF.INVITATION_AMT)
                   FROM EVENT_PARTICIPANT_FACILITY EPF
                  WHERE EPF.EVENT_PARTICIPANT_ID = EOP.EVENT_PARTICIPANT_ID
                    AND EPF.INVITATION_AMT IS NOT NULL ) AS TOTAL_INVITATION_AMT
             , ( SELECT SUM(EPF.COMMITMENT_AMT)
                   FROM EVENT_PARTICIPANT_FACILITY EPF
                  WHERE EPF.EVENT_PARTICIPANT_ID = EOP.EVENT_PARTICIPANT_ID
                    AND EPF.COMMITMENT_AMT IS NOT NULL ) AS TOTAL_COMMITMENT_AMT
             , ( SELECT SUM(EPF.ALLOCATION_AMT)
                   FROM EVENT_PARTICIPANT_FACILITY EPF
                  WHERE EPF.EVENT_PARTICIPANT_ID = EOP.EVENT_PARTICIPANT_ID
                    AND EPF.ALLOCATION_AMT IS NOT NULL ) AS TOTAL_ALLOCATION_AMT
             , EOP.DECLINED_IND
             , EOP.DECLINED_DESC
             , EOP.DECLINED_DATE
             , EOP.REMOVED_IND
             , EOP.REMOVED_DATE
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
            ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID LEFT JOIN EVENT_PARTICIPANT_ORIGINATION EOP
            ON EP.EVENT_PARTICIPANT_ID = EOP.EVENT_PARTICIPANT_ID LEFT JOIN USER_INFO UIR
            ON EOP.INVITE_RECIPIENT_ID = UIR.USER_ID LEFT JOIN DEAL_DOCUMENT DD
            ON EOP.COMMITMENT_LETTER_ID = DD.DEAL_DOCUMENT_ID LEFT JOIN DEAL_DOCUMENT DD2
            ON EOP.PARTICIPANT_CERTIFICATE_ID = DD2.DEAL_DOCUMENT_ID LEFT JOIN DEAL_DOCUMENT DD3
            ON EOP.SIGNED_CERTIFICATE_ID = DD3.DEAL_DOCUMENT_ID LEFT JOIN USER_INFO UIC
            ON EP.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON EP.UPDATED_BY_ID = UIU.USER_ID
    """;
    public static final String INSERT_EVENT_PARTICIPANT_ORIG = """
        INSERT INTO EVENT_PARTICIPANT_ORIGINATION
             ( EVENT_PARTICIPANT_ID, INVITE_RECIPIENT_ID, MESSAGE_DESC, RESPONSE_DESC, DECLINED_DESC )
               VALUES
             ( ?, ?, ?, ?, ? )
    """;
    public static final String UPDATE_EVENT_PARTICIPANT_ORIG = """
        UPDATE EVENT_PARTICIPANT_ORIGINATION
           SET INVITE_RECIPIENT_ID = ?
             , MESSAGE_DESC = ?
             , RESPONSE_DESC = ?
             , DECLINED_DESC = ?
         WHERE EVENT_PARTICIPANT_ID = ?
    """;
    public static final String UPDATE_EVENT_PARTICIPANT_ORIG_INVITE_DATE = """
        UPDATE EVENT_PARTICIPANT_ORIGINATION
           SET INVITE_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
        """;
    public static final String UPDATE_EVENT_PARTICIPANT_ORIG_FULL_DEAL_ACCESS_DATE = """
        UPDATE EVENT_PARTICIPANT_ORIGINATION
           SET FULL_DEAL_ACCESS_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
        """;
    public static final String UPDATE_EVENT_PARTICIPANT_ORIG_DECLINED_EVENT = """
        UPDATE EVENT_PARTICIPANT_ORIGINATION
           SET DECLINED_IND = 'Y'
             , DECLINED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
    """;
    public static final String UPDATE_EVENT_PARTICIPANT_ORIG_REMOVE_FROM_EVENT = """
        UPDATE EVENT_PARTICIPANT_ORIGINATION
           SET REMOVED_IND = 'Y'
             , REMOVED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_PARTICIPANT_ID = ?
    """;
    public static final String DELETE_EVENT_PARTICIPANT_ORIG = "DELETE FROM EVENT_PARTICIPANT_ORIGINATION";

    //TODO: This needs to be moved to the EventParticipantFacility query.
    public static final String UPDATE_EVENT_PARTICIPANT_ALLOCATIONS = """
        UPDATE EVENT_PARTICIPANT_FACILITY
        SET ALLOCATION_AMT = NULL
        WHERE EVENT_PARTICIPANT_ID = ?
    """;

}