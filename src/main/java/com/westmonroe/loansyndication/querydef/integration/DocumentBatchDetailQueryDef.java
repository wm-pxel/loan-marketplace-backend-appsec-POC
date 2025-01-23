package com.westmonroe.loansyndication.querydef.integration;

public class DocumentBatchDetailQueryDef {

    private DocumentBatchDetailQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DOCUMENT_BATCH_DETAIL = """
        SELECT DBD.DOCUMENT_BATCH_DETAIL_ID
             , DBD.DOCUMENT_BATCH_ID
             , DBD.DOCUMENT_EXTERNAL_UUID
             , DBD.DISPLAY_NAME
             , DBD.CATEGORY_NAME
             , DBD.DOCUMENT_EXT_DESC
             , DBD.DOCUMENT_URL
             , DBD.SALESFORCE_ID
             , DBD.PROCESS_START_DATE
             , DBD.PROCESS_END_DATE
             , DBD.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.FIRST_NAME || ' ' || UIC.LAST_NAME AS CREATED_BY_FULL_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , DBD.CREATED_DATE
             , DD.DOCUMENT_NAME
             , DD.DOCUMENT_NAME AS DD_DOCUMENT_NAME
             , DD.DISPLAY_NAME AS DD_DISPLAY_NAME
             , DD.DOCUMENT_TYPE AS DD_DOCUMENT_TYPE
             , DD.SOURCE_CD AS DD_SOURCE_CD
             , DD.CREATED_BY_ID AS DD_CREATED_BY_ID
          FROM DOCUMENT_BATCH_DETAIL DBD LEFT JOIN USER_INFO UIC
            ON DBD.CREATED_BY_ID = UIC.USER_ID LEFT JOIN DEAL_DOCUMENT DD
            ON DBD.DOCUMENT_EXTERNAL_UUID = DD.DOCUMENT_EXTERNAL_UUID
        """;
    public static final String INSERT_DOCUMENT_BATCH_DETAIL = """
        INSERT INTO DOCUMENT_BATCH_DETAIL
             ( DOCUMENT_BATCH_ID, DOCUMENT_EXTERNAL_UUID, DOCUMENT_EXT_DESC, DOCUMENT_URL, DISPLAY_NAME, CATEGORY_NAME
             , SALESFORCE_ID, CREATED_BY_ID )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ? )
        """;
    public static final String UPDATE_DOCUMENT_BATCH_DETAIL_PROCESS_START_DATE = """
        UPDATE DOCUMENT_BATCH_DETAIL
           SET PROCESS_START_DATE = CURRENT_TIMESTAMP
         WHERE DOCUMENT_BATCH_ID = ?
           AND DOCUMENT_BATCH_DETAIL_ID = ?
           AND PROCESS_START_DATE IS NULL
        """;
    public static final String UPDATE_DOCUMENT_BATCH_DETAIL_PROCESS_END_DATE = """
        UPDATE DOCUMENT_BATCH_DETAIL
           SET PROCESS_END_DATE = CURRENT_TIMESTAMP
         WHERE DOCUMENT_BATCH_ID = ?
           AND DOCUMENT_BATCH_DETAIL_ID = ?
           AND PROCESS_END_DATE IS NULL
        """;
    public static final String DELETE_DOCUMENT_BATCH_DETAIL = "DELETE FROM DOCUMENT_BATCH_DETAIL";

}