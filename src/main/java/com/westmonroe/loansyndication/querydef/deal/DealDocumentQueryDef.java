package com.westmonroe.loansyndication.querydef.deal;

public class DealDocumentQueryDef {

    private DealDocumentQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DEAL_DOCUMENT = """
        SELECT DD.DEAL_DOCUMENT_ID
             , DD.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_NAME
             , DI.DEAL_EXTERNAL_UUID
             , DD.DISPLAY_NAME
             , DD.DOCUMENT_NAME
             , DD.DOCUMENT_CATEGORY_ID
             , DCD.DOCUMENT_CATEGORY_NAME
             , DCD.ORDER_NBR
             , DD.DOCUMENT_TYPE
             , DD.DOCUMENT_DESC
             , DD.SOURCE_CD
             , DD.DOCUMENT_EXTERNAL_UUID
             , DD.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , DD.CREATED_DATE
             , DD.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , DD.UPDATED_DATE
          FROM DEAL_DOCUMENT DD LEFT JOIN DEAL_INFO DI
            ON DD.DEAL_ID = DI.DEAL_ID LEFT JOIN DOCUMENT_CATEGORY_DEF DCD
            ON DD.DOCUMENT_CATEGORY_ID = DCD.DOCUMENT_CATEGORY_ID LEFT JOIN USER_INFO UIC
            ON DD.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON DD.UPDATED_BY_ID = UIU.USER_ID
        """;
    public static final String INSERT_DEAL_DOCUMENT = """
        INSERT INTO DEAL_DOCUMENT
             ( DEAL_ID, DISPLAY_NAME, DOCUMENT_NAME, DOCUMENT_CATEGORY_ID, DOCUMENT_TYPE, DOCUMENT_DESC, SOURCE_CD
             , DOCUMENT_EXTERNAL_UUID, CREATED_BY_ID, UPDATED_BY_ID )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        """;
    public static final String UPDATE_DEAL_DOCUMENT = """
        UPDATE DEAL_DOCUMENT
           SET DISPLAY_NAME = ?
             , DOCUMENT_DESC = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE DEAL_DOCUMENT_ID = ?
        """;
    public static final String DELETE_DEAL_DOCUMENT = "DELETE FROM DEAL_DOCUMENT";

}