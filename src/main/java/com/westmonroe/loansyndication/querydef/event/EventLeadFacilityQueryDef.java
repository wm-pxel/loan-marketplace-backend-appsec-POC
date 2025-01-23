package com.westmonroe.loansyndication.querydef.event;

public class EventLeadFacilityQueryDef {

    private EventLeadFacilityQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_EVENT_LEAD_FACILITY = """
        SELECT ELF.EVENT_ID
             , EI.EVENT_UUID
             , EI.EVENT_EXTERNAL_UUID
             , EI.EVENT_NAME
             , ELF.EVENT_DEAL_FACILITY_ID
             , EDF.DEAL_FACILITY_ID
             , DF.FACILITY_EXTERNAL_UUID
             , DF.DEAL_ID
             , DF.FACILITY_NAME
             , DF.FACILITY_AMT
             , DF.FACILITY_TYPE_ID
             , COALESCE(PD.OPTION_NAME, '') AS FACILITY_TYPE_NAME
             , ELF.INVITATION_AMT
             , ELF.COMMITMENT_AMT
             , ELF.ALLOCATION_AMT
             , ELF.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , ELF.CREATED_DATE
             , ELF.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , ELF.UPDATED_DATE
          FROM EVENT_LEAD_FACILITY ELF LEFT JOIN EVENT_INFO EI
            ON ELF.EVENT_ID = EI.EVENT_ID LEFT JOIN EVENT_DEAL_FACILITY EDF
            ON ELF.EVENT_DEAL_FACILITY_ID = EDF.EVENT_DEAL_FACILITY_ID LEFT JOIN DEAL_FACILITY DF
            ON EDF.DEAL_FACILITY_ID = DF.DEAL_FACILITY_ID LEFT JOIN PICKLIST_DEF PD
            ON DF.FACILITY_TYPE_ID = PD.PICKLIST_ID LEFT JOIN USER_INFO UIC
            ON ELF.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON ELF.UPDATED_BY_ID = UIU.USER_ID
    """;
    public static final String INSERT_EVENT_LEAD_FACILITY = """
        INSERT INTO EVENT_LEAD_FACILITY
             ( EVENT_ID, EVENT_DEAL_FACILITY_ID, INVITATION_AMT, COMMITMENT_AMT, ALLOCATION_AMT, CREATED_BY_ID, UPDATED_BY_ID )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ? )
    """;
    public static final String UPDATE_EVENT_LEAD_FACILITY = """
        UPDATE EVENT_LEAD_FACILITY
           SET INVITATION_AMT = ?
             , COMMITMENT_AMT = ?
             , ALLOCATION_AMT = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ?
           AND EVENT_DEAL_FACILITY_ID = ?
    """;
    public static final String UPDATE_EVENT_LEAD_FACILITY_ALLOCATION = """
        UPDATE EVENT_LEAD_FACILITY
           SET ALLOCATION_AMT = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE EVENT_ID = ( SELECT EVENT_ID FROM EVENT_INFO WHERE EVENT_UUID = ? )
           AND EVENT_DEAL_FACILITY_ID = ?
    """;
    public static final String DELETE_EVENT_LEAD_FACILITY = "DELETE FROM EVENT_LEAD_FACILITY";

}