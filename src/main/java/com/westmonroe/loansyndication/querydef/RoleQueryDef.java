package com.westmonroe.loansyndication.querydef;

public class RoleQueryDef {

    private RoleQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_ROLE = """
        SELECT ROLE_ID
             , ROLE_CD
             , ROLE_NAME
             , ROLE_DESC
             , VISIBLE_IND
          FROM ROLE_DEF
        """;
    public static final String INSERT_ROLE = """
        INSERT INTO ROLE_DEF
             ( ROLE_CD, ROLE_NAME, ROLE_DESC )
               VALUES
             ( ?, ?, ? )
        """;
    public static final String UPDATE_ROLE = """
        UPDATE ROLE_DEF
           SET ROLE_CD = ?
             , ROLE_NAME = ?
             , ROLE_DESC = ?
         WHERE ROLE_ID = ?
           AND VISIBLE_IND = 'Y'
        """;
    public static final String DELETE_ROLE = "DELETE FROM ROLE_DEF WHERE ROLE_ID = ? AND VISIBLE_IND = 'Y'";

}