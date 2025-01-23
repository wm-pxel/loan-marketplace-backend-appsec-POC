package com.westmonroe.loansyndication.querydef;

public class FeatureFlagQueryDef {

    private FeatureFlagQueryDef() { throw new IllegalStateException("Utility class"); }

    public static final String SELECT_FEATURE_FLAG = """
            SELECT FF.FEATURE_FLAG_ID
                 , FF.FEATURE_NAME
                 , FF.DESCRIPTION
                 , FF.IS_ENABLED
                 , FF.CREATED_DATE
                 , FF.CREATED_BY_ID
                 , UIC.USER_UUID AS CREATED_BY_UUID
                 , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
                 , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
                 , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
                 , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
                 , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
                 , FF.UPDATED_DATE
                 , FF.UPDATED_BY_ID
                 , UIU.USER_UUID AS UPDATED_BY_UUID
                 , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
                 , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
                 , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
                 , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
                 , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
            FROM FEATURE_FLAG_INFO FF
            LEFT JOIN USER_INFO UIC ON FF.CREATED_BY_ID = UIC.USER_ID
            LEFT JOIN USER_INFO UIU ON FF.UPDATED_BY_ID = UIU.USER_ID 
            """;

    public static final String INSERT_FEATURE_FLAG = """
        INSERT INTO FEATURE_FLAG_INFO
            ( FEATURE_NAME, IS_ENABLED, DESCRIPTION, CREATED_BY_ID,  UPDATED_BY_ID )
            VALUES
            ( ?, ?, ?, ?, ? )
        """;

    public static final String UPDATE_FEATURE_FLAG = """
       UPDATE FEATURE_FLAG_INFO
          SET FEATURE_NAME = ?
            , IS_ENABLED = ?
            , DESCRIPTION = ?
            , UPDATED_BY_ID = ?
            , UPDATED_DATE = CURRENT_TIMESTAMP    
       WHERE FEATURE_FLAG_ID = ?
       """;

    public static final String DELETE_FEATURE_FLAG = "DELETE FROM FEATURE_FLAG_INFO";
}

