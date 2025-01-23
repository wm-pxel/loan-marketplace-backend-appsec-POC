package com.westmonroe.loansyndication.querydef.integration;

public class DocumentBatchQueryDef {

    private DocumentBatchQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DOCUMENT_BATCH = """
        SELECT DB.DOCUMENT_BATCH_ID
             , DB.DEAL_EXTERNAL_UUID
             , DI.DEAL_UUID
             , DB.TRANSFER_TYPE_CD
             , DB.PROCESS_START_DATE
             , DB.PROCESS_END_DATE
             , DB.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.FIRST_NAME || ' ' || UIC.LAST_NAME AS CREATED_BY_FULL_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , DB.CREATED_DATE
          FROM DOCUMENT_BATCH DB LEFT JOIN DEAL_INFO DI
            ON DB.DEAL_EXTERNAL_UUID = DI.DEAL_EXTERNAL_UUID LEFT JOIN USER_INFO UIC
            ON DB.CREATED_BY_ID = UIC.USER_ID
        """;
    public static final String INSERT_DOCUMENT_BATCH = """
        INSERT INTO DOCUMENT_BATCH
             ( DEAL_EXTERNAL_UUID, TRANSFER_TYPE_CD, CREATED_BY_ID )
               VALUES
             ( ?, ?, ? )
        """;
    public static final String UPDATE_DOCUMENT_BATCH_PROCESS_START_DATE = """
        UPDATE DOCUMENT_BATCH
           SET PROCESS_START_DATE = CURRENT_TIMESTAMP
         WHERE DOCUMENT_BATCH_ID = ?
           AND PROCESS_START_DATE IS NULL
        """;
    public static final String UPDATE_DOCUMENT_BATCH_PROCESS_END_DATE = """
        UPDATE DOCUMENT_BATCH
           SET PROCESS_END_DATE = CURRENT_TIMESTAMP
         WHERE DOCUMENT_BATCH_ID = ?
           AND PROCESS_END_DATE IS NULL
        """;
    public static final String DELETE_DOCUMENT_BATCH = "DELETE FROM DOCUMENT_BATCH";

}