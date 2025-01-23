package com.westmonroe.loansyndication.querydef.deal;

public class DealMemberQueryDef {

    private DealMemberQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    // TODO: Update this query as part of LM-2493
    public static final String SELECT_DEAL_MEMBER = """
                SELECT DM.DEAL_ID
                     , DI.DEAL_UUID
                     , DI.DEAL_NAME
                     , DI.ORIGINATOR_ID
                     , DII.INSTITUTION_UUID AS ORIG_INSTITUTION_UUID
                     , DII.INSTITUTION_NAME AS ORIG_INSTITUTION_NAME
                     , DM.USER_ID
                     , UI.USER_UUID
                     , UI.FIRST_NAME
                     , UI.LAST_NAME
                     , UI.EMAIL_ADDR
                     , UI.PASSWORD_DESC
                     , UI.ACTIVE_IND
                     , UI.CREATED_DATE
                     , UI.INSTITUTION_ID AS USER_INSTITUTION_ID
                     , UII.INSTITUTION_UUID AS USER_INSTITUTION_UUID
                     , UII.INSTITUTION_NAME AS USER_INSTITUTION_NAME
                     , DM.MEMBER_TYPE_CD
                     , CASE DM.MEMBER_TYPE_CD
                           WHEN 'O' THEN 'Originator'
                           WHEN 'P' THEN 'Participant'
                           ELSE 'Unknown'
                       END AS MEMBER_TYPE_DESC
                     , EPO.EVENT_PARTICIPANT_ID
                     , EPO.INVITE_DATE
                     , EPO.FULL_DEAL_ACCESS_DATE
                     , EP.PARTICIPANT_STEP_ID
                     , EPO.DECLINED_IND
                     , EPO.REMOVED_IND
                     , PSD.STEP_NAME
                     , PSD.ORDER_NBR
                     , DM.CREATED_BY_ID
                     , UIC.USER_UUID AS CREATED_BY_UUID
                     , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
                     , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
                     , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
                     , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
                     , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
                     , DM.CREATED_DATE
                  FROM DEAL_MEMBER DM LEFT JOIN DEAL_INFO DI
                    ON DM.DEAL_ID = DI.DEAL_ID LEFT JOIN INSTITUTION_INFO DII
                    ON DI.ORIGINATOR_ID = DII.INSTITUTION_ID LEFT JOIN USER_INFO UI
                    ON DM.USER_ID = UI.USER_ID LEFT JOIN INSTITUTION_INFO UII
                    ON UI.INSTITUTION_ID = UII.INSTITUTION_ID LEFT JOIN USER_INFO UIC
                    ON DM.CREATED_BY_ID = UIC.USER_ID LEFT JOIN EVENT_INFO EI
                    ON DM.DEAL_ID = EI.DEAL_ID LEFT JOIN EVENT_PARTICIPANT EP
                    ON EI.EVENT_ID = EP.EVENT_ID AND EP.PARTICIPANT_ID = UI.INSTITUTION_ID LEFT JOIN EVENT_PARTICIPANT_ORIGINATION EPO
                    ON EP.EVENT_PARTICIPANT_ID = EPO.EVENT_PARTICIPANT_ID LEFT JOIN PARTICIPANT_STEP_DEF PSD
                    ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID
            """;
    public static final String INSERT_DEAL_MEMBER = """
        INSERT INTO DEAL_MEMBER
             ( DEAL_ID, USER_ID, MEMBER_TYPE_CD, CREATED_BY_ID )
               VALUES
             ( ?, ?, ?, ? )
    """;
    public static final String DELETE_DEAL_MEMBER = "DELETE FROM DEAL_MEMBER";

}