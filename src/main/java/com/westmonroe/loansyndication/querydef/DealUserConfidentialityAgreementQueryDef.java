package com.westmonroe.loansyndication.querydef;

public class DealUserConfidentialityAgreementQueryDef {

    private DealUserConfidentialityAgreementQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DEAL_USER_CONF_AGRMNT = """
        SELECT * FROM DEAL_USER_CONF_AGRMNT_XREF""";
    public static final String INSERT_DEAL_USER_CONF_AGRMNT = """
        INSERT INTO DEAL_USER_CONF_AGRMNT_XREF
        ( DEAL_ID, USER_ID, CONF_AGRMNT_ID, AGREEMENT_DATE )
        VALUES
        ( ?, ?, ?, CURRENT_TIMESTAMP )
        """;
    public static final String DELETE_DEAL_USER_CONF_AGRMNT = """
        DELETE FROM DEAL_USER_CONF_AGRMNT_XREF
    """;

}
