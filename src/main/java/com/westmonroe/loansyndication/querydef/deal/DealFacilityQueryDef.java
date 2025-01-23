package com.westmonroe.loansyndication.querydef.deal;

public class DealFacilityQueryDef {

    private DealFacilityQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DEAL_FACILITY = """
        SELECT DF.DEAL_FACILITY_ID
             , DF.FACILITY_EXTERNAL_UUID
             , DF.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_NAME
             , DF.FACILITY_NAME
             , DF.FACILITY_AMT
             , DF.FACILITY_TYPE_ID
             , COALESCE(PD.OPTION_NAME, '') AS FACILITY_TYPE_NAME
             , DF.COLLATERAL_ID
             , COALESCE(PD4.OPTION_NAME, '') AS COLLATERAL_NAME
             , DF.TENOR_YRS_QTY
             , DF.PRICING_GRID_ID
             , DD.DISPLAY_NAME AS PG_DISPLAY_NAME
             , DD.DOCUMENT_NAME AS PG_DOCUMENT_NAME
             , DD.DOCUMENT_TYPE AS PG_DOCUMENT_TYPE
             , DD.DOCUMENT_DESC AS PG_DOCUMENT_DESC
             , DD.SOURCE_CD AS PG_SOURCE_CD
             , DF.PRICING_DESC
             , DF.CSA_DESC
             , DF.FACILITY_PURPOSE_ID
             , COALESCE(PD2.OPTION_NAME, '') AS FACILITY_PURPOSE_NAME
             , DF.PURPOSE_TEXT
             , DF.DAY_COUNT_ID
             , COALESCE(PD3.OPTION_NAME, '') AS DAY_COUNT_NAME
             , DF.REGULATORY_LOAN_TYPE_ID
             , COALESCE(PD5.OPTION_NAME, '') AS REGULATORY_LOAN_TYPE_NAME
             , DF.GUAR_INV_IND
             , DF.PATRONAGE_PAYING_IND
             , DF.FARM_CREDIT_TYPE_NAME
             , DF.REV_UTIL_PCT
             , DF.UPFRONT_FEES_DESC
             , DF.UNUSED_FEES_DESC
             , DF.AMORTIZATION_DESC
             , DF.CREATED_BY_ID
             , DF.MATURITY_DATE
             , DF.RENEWAL_DATE
             , DF.LGD_OPTION
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , DF.CREATED_DATE
             , DF.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , DF.UPDATED_DATE
          FROM DEAL_FACILITY DF LEFT JOIN DEAL_INFO DI
            ON DF.DEAL_ID = DI.DEAL_ID LEFT JOIN DEAL_DOCUMENT DD
            ON DF.PRICING_GRID_ID = DD.DEAL_DOCUMENT_ID LEFT JOIN PICKLIST_DEF PD
            ON DF.FACILITY_TYPE_ID = PD.PICKLIST_ID LEFT JOIN PICKLIST_DEF PD2
            ON DF.FACILITY_PURPOSE_ID = PD2.PICKLIST_ID LEFT JOIN PICKLIST_DEF PD3
            ON DF.DAY_COUNT_ID = PD3.PICKLIST_ID LEFT JOIN USER_INFO UIC
            ON DF.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON DF.UPDATED_BY_ID = UIU.USER_ID LEFT JOIN PICKLIST_DEF PD4
            ON DF.COLLATERAL_ID = PD4.PICKLIST_ID LEFT JOIN PICKLIST_DEF PD5
            ON DF.REGULATORY_LOAN_TYPE_ID = PD5.PICKLIST_ID
        """;
    public static final String INSERT_DEAL_FACILITY = """
        INSERT INTO DEAL_FACILITY
             ( FACILITY_EXTERNAL_UUID, DEAL_ID, FACILITY_NAME, FACILITY_AMT, FACILITY_TYPE_ID, TENOR_YRS_QTY, COLLATERAL_ID, PRICING_DESC
             , CSA_DESC, FACILITY_PURPOSE_ID, PURPOSE_TEXT, DAY_COUNT_ID, GUAR_INV_IND, PATRONAGE_PAYING_IND, FARM_CREDIT_TYPE_NAME, REV_UTIL_PCT
             , UPFRONT_FEES_DESC, UNUSED_FEES_DESC, AMORTIZATION_DESC, MATURITY_DATE, RENEWAL_DATE, CREATED_BY_ID, UPDATED_BY_ID, LGD_OPTION
             , REGULATORY_LOAN_TYPE_ID )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
        """;
    public static final String UPDATE_DEAL_FACILITY = """
        UPDATE DEAL_FACILITY
           SET FACILITY_AMT = ?
             , FACILITY_TYPE_ID = ?
             , TENOR_YRS_QTY = ?
             , COLLATERAL_ID = ?
             , PRICING_GRID_ID = ?
             , PRICING_DESC = ?
             , CSA_DESC = ?
             , FACILITY_PURPOSE_ID = ?
             , PURPOSE_TEXT = ?
             , DAY_COUNT_ID = ?
             , GUAR_INV_IND = ?
             , PATRONAGE_PAYING_IND = ?
             , FARM_CREDIT_TYPE_NAME = ?
             , REV_UTIL_PCT = ?
             , UPFRONT_FEES_DESC = ?
             , UNUSED_FEES_DESC = ?
             , AMORTIZATION_DESC = ?
             , MATURITY_DATE = ?
             , RENEWAL_DATE = ?
             , LGD_OPTION = ?
             , REGULATORY_LOAN_TYPE_ID = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP 
         WHERE DEAL_FACILITY_ID = ?
        """;
    public static final String DELETE_DEAL_FACILITY = "DELETE FROM DEAL_FACILITY";

}