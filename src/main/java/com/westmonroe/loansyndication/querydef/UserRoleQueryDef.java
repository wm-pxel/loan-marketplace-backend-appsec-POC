package com.westmonroe.loansyndication.querydef;

public class UserRoleQueryDef {

    private UserRoleQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_USER_ROLES = """
        SELECT UI.USER_ID
             , UI.USER_UUID
             , UI.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , UI.FIRST_NAME
             , UI.LAST_NAME
             , UI.EMAIL_ADDR
             , UI.PASSWORD_DESC
             , UI.ACTIVE_IND
             , UI.CREATED_DATE
             , RD.ROLE_ID
             , RD.ROLE_CD
             , RD.ROLE_NAME
             , RD.ROLE_DESC
             , RD.VISIBLE_IND
          FROM USER_ROLE_XREF URX LEFT JOIN USER_INFO UI
            ON URX.USER_ID = UI.USER_ID LEFT JOIN INSTITUTION_INFO II
            ON UI.INSTITUTION_ID = II.INSTITUTION_ID LEFT JOIN ROLE_DEF RD
            ON URX.ROLE_ID = RD.ROLE_ID
    """;
    public static final String INSERT_USER_ROLE = """
        INSERT INTO USER_ROLE_XREF
             ( USER_ID, ROLE_ID )
               VALUES
             ( ?, ? )
    """;
    public static final String DELETE_USER_ROLE = "DELETE FROM USER_ROLE_XREF";

}