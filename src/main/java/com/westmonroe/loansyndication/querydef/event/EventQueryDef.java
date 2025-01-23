package com.westmonroe.loansyndication.querydef.event;

public class EventQueryDef {

    private EventQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_EVENT = """
        SELECT EI.EVENT_ID
             , EI.EVENT_UUID
             , EI.EVENT_EXTERNAL_UUID
             , EI.EVENT_NAME
             , DI.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_EXTERNAL_UUID
             , DI.DEAL_NAME
             , II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , ETD.EVENT_TYPE_ID
             , ETD.EVENT_TYPE_NAME
             , SD.STAGE_ID
             , SD.STAGE_NAME
             , SD.ORDER_NBR
             , EI.PROJ_LAUNCH_DATE
             , EI.LAUNCH_DATE
             , EI.COMMITMENT_DATE
             , EI.COMMENTS_DUE_DATE
             , EI.EFFECTIVE_DATE
             , EI.PROJ_CLOSE_DATE
             , EI.CLOSE_DATE
             , ( SELECT SUM(ELF.INVITATION_AMT)
                   FROM EVENT_LEAD_FACILITY ELF
                  WHERE ELF.EVENT_ID = EI.EVENT_ID
                    AND ELF.INVITATION_AMT IS NOT NULL ) AS TOTAL_INVITATION_AMT
             , ( SELECT SUM(ELF.COMMITMENT_AMT)
                   FROM EVENT_LEAD_FACILITY ELF
                  WHERE ELF.EVENT_ID = EI.EVENT_ID
                    AND ELF.COMMITMENT_AMT IS NOT NULL ) AS TOTAL_COMMITMENT_AMT
             , ( SELECT SUM(ELF.ALLOCATION_AMT)
                   FROM EVENT_LEAD_FACILITY ELF
                  WHERE ELF.EVENT_ID = EI.EVENT_ID
                    AND ELF.ALLOCATION_AMT IS NOT NULL ) AS TOTAL_ALLOCATION_AMT
             , EI.LEAD_INVITATION_DATE
             , EI.LEAD_COMMITMENT_DATE
             , EI.LEAD_ALLOCATION_DATE
             , EI.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , EI.CREATED_DATE
             , EI.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , EI.UPDATED_DATE
          FROM EVENT_INFO EI LEFT JOIN DEAL_INFO DI
            ON EI.DEAL_ID = DI.DEAL_ID LEFT JOIN INSTITUTION_INFO II
            ON DI.ORIGINATOR_ID = II.INSTITUTION_ID LEFT JOIN EVENT_TYPE_DEF ETD
            ON EI.EVENT_TYPE_ID = ETD.EVENT_TYPE_ID LEFT JOIN STAGE_DEF SD
            ON EI.STAGE_ID = SD.STAGE_ID LEFT JOIN USER_INFO UIC
            ON EI.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON EI.UPDATED_BY_ID = UIU.USER_ID
   """;
    public static final String INSERT_EVENT = """
        INSERT INTO EVENT_INFO
             ( EVENT_UUID, EVENT_EXTERNAL_UUID, DEAL_ID, EVENT_NAME, EVENT_TYPE_ID, STAGE_ID, PROJ_LAUNCH_DATE
             , COMMITMENT_DATE, COMMENTS_DUE_DATE, EFFECTIVE_DATE, PROJ_CLOSE_DATE, CREATED_BY_ID, UPDATED_BY_ID )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
    """;
    public static final String UPDATE_EVENT = """
        UPDATE EVENT_INFO
           SET EVENT_NAME = ?
             , EVENT_TYPE_ID = ?
             , PROJ_LAUNCH_DATE = ?
             , COMMITMENT_DATE = ?
             , COMMENTS_DUE_DATE = ?
             , EFFECTIVE_DATE = ?
             , PROJ_CLOSE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_STAGE = """
        UPDATE EVENT_INFO
           SET STAGE_ID = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_LAUNCH_DATES = """
        UPDATE EVENT_INFO
           SET LAUNCH_DATE = CURRENT_TIMESTAMP
             , COMMITMENT_DATE = ?
             , PROJ_CLOSE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_DATES = """
        UPDATE EVENT_INFO
           SET PROJ_LAUNCH_DATE = ?
             , COMMITMENT_DATE = ?
             , COMMENTS_DUE_DATE = ?
             , EFFECTIVE_DATE = ?
             , PROJ_CLOSE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_CLOSE_DATES = """
        UPDATE EVENT_INFO
           SET EFFECTIVE_DATE = ?
             , CLOSE_DATE = CURRENT_TIMESTAMP
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_LEAD_INVITATION_DATE = """
        UPDATE EVENT_INFO
           SET LEAD_INVITATION_DATE = CURRENT_TIMESTAMP
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_LEAD_COMMITMENT_DATE = """
        UPDATE EVENT_INFO
           SET LEAD_COMMITMENT_DATE = CURRENT_TIMESTAMP
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String UPDATE_EVENT_LEAD_ALLOCATION_DATE = """
        UPDATE EVENT_INFO
           SET LEAD_ALLOCATION_DATE = CURRENT_TIMESTAMP
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
    """;
    public static final String DELETE_EVENT = "DELETE FROM EVENT_INFO";

}