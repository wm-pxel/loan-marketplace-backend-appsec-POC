package com.westmonroe.loansyndication.querydef;

public class UserQueryDef {

    private UserQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_USER_ID = """
        SELECT UI.USER_ID
          FROM USER_INFO UI LEFT JOIN INSTITUTION_INFO II
            ON UI.INSTITUTION_ID = II.INSTITUTION_ID
    """;
    public static final String SELECT_USER = """
        SELECT UI.USER_ID
             , UI.USER_UUID
             , II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , II.ACTIVE_IND AS INSTITUTION_ACTIVE_IND
             , II.SSO_IND
             , UI.FIRST_NAME
             , UI.LAST_NAME
             , UI.EMAIL_ADDR
             , UI.PASSWORD_DESC
             , UI.SYSTEM_USER_IND
             , UI.INVITE_STATUS_CD
             , ISD.INVITE_STATUS_DESC
             , UI.ACTIVE_IND
             , UI.CREATED_DATE
          FROM USER_INFO UI LEFT JOIN INSTITUTION_INFO II
            ON UI.INSTITUTION_ID = II.INSTITUTION_ID LEFT JOIN INVITE_STATUS_DEF ISD
            ON UI.INVITE_STATUS_CD = ISD.INVITE_STATUS_CD
    """;
    public static final String INSERT_USER = """
        INSERT INTO USER_INFO
             ( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, INVITE_STATUS_CD, ACTIVE_IND )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ? )
    """;
    public static final String UPDATE_USER_BY_ID = """
        UPDATE USER_INFO
           SET FIRST_NAME = ?
             , LAST_NAME = ?
             , EMAIL_ADDR = ?
             , PASSWORD_DESC = ?
             , ACTIVE_IND = ?
         WHERE USER_ID = ?
    """;
    public static final String UPDATE_USER_BY_UUID = """
        UPDATE USER_INFO
           SET FIRST_NAME = ?
             , LAST_NAME = ?
             , EMAIL_ADDR = ?
             , PASSWORD_DESC = ?
             , ACTIVE_IND = ?
         WHERE USER_UUID = ?
    """;
    public static final String UPDATE_USER_INACTIVE_BY_ID = """
        UPDATE USER_INFO
           SET ACTIVE_IND = 'N'
         WHERE USER_ID = ?
    """;
    public static final String DELETE_USER = "DELETE FROM USER_INFO";

}