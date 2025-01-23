package com.westmonroe.loansyndication.querydef;

public class EmailNotificationQueryDef {

    private EmailNotificationQueryDef() { throw new IllegalStateException("Utility class"); }

    public static final String SELECT_EMAIL_NOTIFICATION = """
            SELECT ENI.EMAIL_NOTIFICATION_ID
                 , ENI.DEAL_ID
                 , ENI.TEMPLATE_JSON
                 , ENI.EMAIL_TYPE_CD
                 , ENI.PROCESSED_IND
                 , ENI.CREATED_DATE
                 , DI.DEAL_UUID
                 , DI.DEAL_NAME
                 , DI.DEAL_EXTERNAL_UUID
            FROM EMAIL_NOTIFICATION_INFO ENI
            LEFT JOIN DEAL_INFO DI ON ENI.DEAL_ID = DI.DEAL_ID
            """;

    public static final String UPDATE_EMAIL_NOTIFICATION = """
            UPDATE EMAIL_NOTIFICATION_INFO 
                SET PROCESSED_IND = ?
            WHERE EMAIL_NOTIFICATION_ID = ?
            """;

    public static final String INSERT_EMAIL_NOTIFICATION = """
            INSERT INTO EMAIL_NOTIFICATION_INFO
                ( DEAL_ID, EMAIL_TYPE_CD, PROCESSED_IND, TEMPLATE_JSON)
            VALUES ( ?, ?, ?, ?::JSONB )
            """;
}
