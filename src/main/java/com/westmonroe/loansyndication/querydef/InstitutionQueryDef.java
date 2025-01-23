package com.westmonroe.loansyndication.querydef;

public class InstitutionQueryDef {

    private InstitutionQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_INSTITUTION = """
        SELECT II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , II.BRAND_NAME
             , II.COMMUNITY_EXT_DESC
             , II.COMMUNITY_NAME
             , II.COMMUNITY_NETWORK_ID
             , II.LOOKUP_KEY_DESC
             , II.OWNER_NAME
             , II.PERMISSION_SET_DESC
             , II.ACTIVE_IND
             , II.SSO_IND
             , ( SELECT SUM(DC.DEAL_COUNT)
                   FROM ( SELECT COUNT(DI.DEAL_ID) AS DEAL_COUNT
                            FROM DEAL_INFO DI
                           WHERE DI.ORIGINATOR_ID = II.INSTITUTION_ID
                           UNION
                          SELECT COUNT(DISTINCT EI.DEAL_ID) AS DEAL_COUNT
                            FROM EVENT_INFO EI LEFT JOIN EVENT_PARTICIPANT EP
                              ON EI.EVENT_ID = EP.EVENT_ID
                           WHERE EP.PARTICIPANT_ID = II.INSTITUTION_ID ) AS DC ) AS DEAL_COUNT
             , ( SELECT COUNT(UI.USER_ID)
                   FROM USER_INFO UI
                  WHERE UI.INSTITUTION_ID = II.INSTITUTION_ID )  AS MEMBER_COUNT
             , BD.BILLING_CD
             , BD.BILLING_DESC
          FROM INSTITUTION_INFO II LEFT JOIN BILLING_DEF BD
            ON II.BILLING_CD = BD.BILLING_CD
        """;
    public static final String INSERT_INSTITUTION = """
        INSERT INTO INSTITUTION_INFO
             ( INSTITUTION_UUID, INSTITUTION_NAME, BRAND_NAME, COMMUNITY_EXT_DESC, COMMUNITY_NAME, COMMUNITY_NETWORK_ID
             , LOOKUP_KEY_DESC, OWNER_NAME, PERMISSION_SET_DESC, ACTIVE_IND )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        """;
    public static final String UPDATE_INSTITUTION = """
        UPDATE INSTITUTION_INFO
           SET INSTITUTION_NAME = ?
             , BRAND_NAME = ?
             , COMMUNITY_EXT_DESC = ?
             , COMMUNITY_NAME = ?
             , COMMUNITY_NETWORK_ID = ?
             , LOOKUP_KEY_DESC = ?
             , OWNER_NAME = ?
             , PERMISSION_SET_DESC = ?
             , ACTIVE_IND = ?
         WHERE INSTITUTION_ID = ?
        """;
    public static final String DELETE_INSTITUTION = "DELETE FROM INSTITUTION_INFO";
}