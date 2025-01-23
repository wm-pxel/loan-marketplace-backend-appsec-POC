package com.westmonroe.loansyndication.querydef;

public class UserEuaQueryDef {
    private UserEuaQueryDef() { throw new IllegalStateException("Utility class"); }

    public static final String SELECT_USER_EUA = """
        SELECT UEX.USER_ID
             , UEX.AGREEMENT_DATE
             , EUA.EUA_ID
             , EUA.EUA_CONTENT
             , EUA.CREATED_DATE
             , BD.BILLING_CD
             , BD.BILLING_DESC
        FROM USER_EUA_XREF UEX LEFT JOIN END_USER_AGREEMENT EUA
            ON UEX.EUA_ID = EUA.EUA_ID LEFT JOIN BILLING_DEF BD
            ON EUA.BILLING_CD = BD.BILLING_CD
    """;

    public static final String INSERT_USER_EUA = """
        INSERT INTO USER_EUA_XREF
        ( USER_ID, EUA_ID, AGREEMENT_DATE )
        VALUES
        ( ?, ?, CURRENT_TIMESTAMP )       
    """;
}
