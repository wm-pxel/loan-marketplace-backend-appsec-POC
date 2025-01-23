package com.westmonroe.loansyndication.querydef.integration;

public class CustomerDataQueryDef {

    private CustomerDataQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_CUSTOMER_DATA = """
        SELECT CD.CUSTOMER_DATA_ID
             , CD.MARKETPLACE_JSON
             , CD.UNSUPPORTED_JSON
             , CD.CREATED_BY_ID
             , UI.FIRST_NAME || ' ' || UI.LAST_NAME AS CREATED_BY_NAME
             , CD.CREATED_DATE
          FROM CUSTOMER_DATA CD LEFT JOIN USER_INFO UI
            ON CD.CREATED_BY_ID = UI.USER_ID
        """;
    public static final String INSERT_CUSTOMER_DATA = """
        INSERT INTO CUSTOMER_DATA
             ( MARKETPLACE_JSON, UNSUPPORTED_JSON, CREATED_BY_ID )
               VALUES
             ( ?::JSONB, ?::JSONB, ? )
        """;
    public static final String DELETE_CUSTOMER_DATA = "DELETE FROM CUSTOMER_DATA";

}