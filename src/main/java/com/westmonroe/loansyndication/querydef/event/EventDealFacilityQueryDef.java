package com.westmonroe.loansyndication.querydef.event;

public class EventDealFacilityQueryDef {

    private EventDealFacilityQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_EVENT_DEAL_FACILITY = """
        SELECT EDF.EVENT_DEAL_FACILITY_ID
             , EDF.EVENT_ID
             , EI.EVENT_UUID
             , EI.EVENT_EXTERNAL_UUID
             , EI.EVENT_NAME
             , EDF.DEAL_FACILITY_ID
             , DF.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_NAME
             , DF.FACILITY_EXTERNAL_UUID
             , DF.FACILITY_NAME
             , DF.FACILITY_AMT
             , EDF.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , EDF.CREATED_DATE
             , EDF.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , EDF.UPDATED_DATE
          FROM EVENT_DEAL_FACILITY EDF LEFT JOIN DEAL_FACILITY DF
            ON EDF.DEAL_FACILITY_ID = DF.DEAL_FACILITY_ID LEFT JOIN DEAL_INFO DI
            ON DF.DEAL_ID = DI.DEAL_ID LEFT JOIN EVENT_INFO EI
            ON EDF.EVENT_ID = EI.EVENT_ID LEFT JOIN USER_INFO UIC
            ON EDF.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON EDF.UPDATED_BY_ID = UIU.USER_ID
   """;
    public static final String INSERT_EVENT_DEAL_FACILITY = """
        INSERT INTO EVENT_DEAL_FACILITY
             ( EVENT_ID, DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
               VALUES
             ( ?, ?, ?, ? )
    """;
    public static final String INSERT_EVENT_DEAL_FACILITIES_FOR_EVENT = """
        INSERT INTO EVENT_DEAL_FACILITY
             ( EVENT_ID, DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
        SELECT ? AS EVENT_ID
             , DEAL_FACILITY_ID
             , ? AS CREATED_BY_ID
             , ? AS UPDATED_BY_ID
          FROM DEAL_FACILITY
         WHERE DEAL_ID = ?
    """;
    public static final String DELETE_EVENT_DEAL_FACILITY = "DELETE FROM EVENT_DEAL_FACILITY";

}