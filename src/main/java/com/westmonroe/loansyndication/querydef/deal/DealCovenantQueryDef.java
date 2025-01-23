package com.westmonroe.loansyndication.querydef.deal;

public class DealCovenantQueryDef {

    private DealCovenantQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DEAL_COVENANT = """
        SELECT DC.DEAL_COVENANT_ID
             , DC.COVENANT_EXTERNAL_UUID
             , DC.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_NAME
             , DC.ENTITY_NAME
             , DC.CATEGORY_NAME
             , DC.COVENANT_TYPE_DESC
             , DC.FREQUENCY_DESC
             , DC.NEXT_EVAL_DATE
             , DC.EFFECTIVE_DATE
             , DC.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , DC.CREATED_DATE
             , DC.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , DC.UPDATED_DATE
          FROM DEAL_COVENANT DC LEFT JOIN DEAL_INFO DI
            ON DC.DEAL_ID = DI.DEAL_ID LEFT JOIN USER_INFO UIC
            ON DI.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON DI.UPDATED_BY_ID = UIU.USER_ID
        """;
    public static final String INSERT_DEAL_COVENANT = """
        INSERT INTO DEAL_COVENANT
             ( COVENANT_EXTERNAL_UUID, DEAL_ID, ENTITY_NAME, CATEGORY_NAME, COVENANT_TYPE_DESC, FREQUENCY_DESC
             , NEXT_EVAL_DATE, EFFECTIVE_DATE, CREATED_BY_ID, UPDATED_BY_ID )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        """;
    public static final String UPDATE_DEAL_COVENANT = """
        UPDATE DEAL_COVENANT
           SET ENTITY_NAME = ?
             , CATEGORY_NAME = ?
             , COVENANT_TYPE_DESC = ?
             , FREQUENCY_DESC = ?
             , NEXT_EVAL_DATE = ?
             , EFFECTIVE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE DEAL_COVENANT_ID = ?
        """;
    public static final String DELETE_DEAL_COVENANT = "DELETE FROM DEAL_COVENANT";

}