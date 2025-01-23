package com.westmonroe.loansyndication.querydef;

public class EndUserAgreementQueryDef {

    private EndUserAgreementQueryDef() { throw new IllegalStateException("Utility class"); }

    public static final String SELECT_END_USER_AGREEMENT = """
            SELECT EUA.EUA_ID
                 , EUA.EUA_CONTENT
                 , EUA.CREATED_DATE
                 , BD.BILLING_CD
                 , BD.BILLING_DESC
            FROM END_USER_AGREEMENT EUA LEFT JOIN BILLING_DEF BD
                ON EUA.BILLING_CD = BD.BILLING_CD           
    """;
}
