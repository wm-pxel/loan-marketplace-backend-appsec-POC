package com.westmonroe.loansyndication.querydef;

public class ConfidentialityAgreementQueryDef {

    private ConfidentialityAgreementQueryDef() {
        throw new IllegalStateException("Utility class");
    }

        public static final String INSERT_CONFIDENTIALITY_AGREEMENT = """
        INSERT INTO INSTITUTION_CONFIDENTIALITY_AGREEMENT
             ( INSTITUTION_ID, CONF_AGRMNT_DESC, CREATED_BY_ID )
               VALUES
             ( ?, ?, ? )
        """;
        public static final String SELECT_CONFIDENTIALITY_AGREEMENT = """
        SELECT CONF_AGRMNT_ID
             , CONF_AGRMNT_DESC
             , INSTITUTION_ID
          FROM INSTITUTION_CONFIDENTIALITY_AGREEMENT
        """;

        public static final String DELETE_CONFIDENTIALITY_AGREEMENT = """
        DELETE FROM INSTITUTION_CONFIDENTIALITY_AGREEMENT
        """;
}
