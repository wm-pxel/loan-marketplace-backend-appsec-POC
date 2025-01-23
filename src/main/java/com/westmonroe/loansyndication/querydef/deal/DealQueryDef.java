package com.westmonroe.loansyndication.querydef.deal;

public class DealQueryDef {

    private DealQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DEAL_ID = """
        SELECT DI.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_EXTERNAL_UUID
             , DI.DEAL_NAME
             , II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
          FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II
            ON DI.ORIGINATOR_ID = II.INSTITUTION_ID
    """;
    public static final String SELECT_DEAL_EVENT_SUMMARY = """
        SELECT DI.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_NAME
             , DI.DEAL_AMT
             , 'Originator' AS RELATION_DESC
             , II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , EI.EVENT_ID
             , EI.EVENT_UUID
             , COALESCE(EI.EVENT_NAME, '--') AS EVENT_NAME
             , EI.PROJ_LAUNCH_DATE
             , EI.LAUNCH_DATE
             , EI.COMMITMENT_DATE
             , EI.PROJ_CLOSE_DATE
             , EI.COMMENTS_DUE_DATE
             , EI.EFFECTIVE_DATE
             , EI.CLOSE_DATE
             , ETD.EVENT_TYPE_ID
             , ETD.EVENT_TYPE_NAME
             , SD.STAGE_ID
             , SD.STAGE_NAME
             , SD.ORDER_NBR
             , NULL AS EVENT_PARTICIPANT_ID
             , NULL AS DECLINED_IND
             , NULL AS REMOVED_IND
             , NULL AS PARTICIPANT_STEP_ID
             , NULL AS STEP_NAME
             , NULL AS ORDER_NBR
             , DI.ACTIVE_IND
          FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II
            ON DI.ORIGINATOR_ID = II.INSTITUTION_ID LEFT JOIN EVENT_INFO EI
            ON DI.DEAL_ID = EI.DEAL_ID LEFT JOIN EVENT_TYPE_DEF ETD
            ON EI.EVENT_TYPE_ID = ETD.EVENT_TYPE_ID LEFT JOIN STAGE_DEF SD
            ON EI.STAGE_ID = SD.STAGE_ID
         WHERE DI.ORIGINATOR_ID = ?
           AND ( EXISTS ( SELECT RD.ROLE_CD
                            FROM USER_ROLE_XREF URX LEFT JOIN ROLE_DEF RD
                              ON URX.ROLE_ID = RD.ROLE_ID
                           WHERE URX.USER_ID = ?
                             AND RD.ROLE_CD = 'ACCESS_ALL_INST_DEALS' )
            OR EXISTS ( SELECT DM.DEAL_ID
                          FROM DEAL_MEMBER DM
                         WHERE DM.DEAL_ID = DI.DEAL_ID
                           AND DM.USER_ID = ?
                           AND DM.MEMBER_TYPE_CD = 'O' ))
         UNION
        SELECT DI.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_NAME
             , DI.DEAL_AMT
             , 'Participant' AS RELATION_DESC
             , II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , EI.EVENT_ID
             , EI.EVENT_UUID
             , COALESCE(EI.EVENT_NAME, '--') AS EVENT_NAME
             , EI.PROJ_LAUNCH_DATE
             , EI.LAUNCH_DATE
             , EI.COMMITMENT_DATE
             , EI.PROJ_CLOSE_DATE
             , EI.COMMENTS_DUE_DATE
             , EI.EFFECTIVE_DATE
             , EI.CLOSE_DATE
             , ETD.EVENT_TYPE_ID
             , ETD.EVENT_TYPE_NAME
             , SD.STAGE_ID
             , SD.STAGE_NAME
             , SD.ORDER_NBR
             , EP.EVENT_PARTICIPANT_ID
             , EPO.DECLINED_IND
             , EPO.REMOVED_IND
             , EP.PARTICIPANT_STEP_ID
             , PSD.STEP_NAME
             , PSD.ORDER_NBR
             , DI.ACTIVE_IND
          FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II
            ON DI.ORIGINATOR_ID = II.INSTITUTION_ID LEFT JOIN EVENT_INFO EI
            ON DI.DEAL_ID = EI.DEAL_ID LEFT JOIN EVENT_TYPE_DEF ETD
            ON EI.EVENT_TYPE_ID = ETD.EVENT_TYPE_ID LEFT JOIN STAGE_DEF SD
            ON EI.STAGE_ID = SD.STAGE_ID LEFT JOIN EVENT_PARTICIPANT EP
            ON EI.EVENT_ID = EP.EVENT_ID LEFT JOIN PARTICIPANT_STEP_DEF PSD
            ON EP.PARTICIPANT_STEP_ID = PSD.PARTICIPANT_STEP_ID LEFT JOIN EVENT_PARTICIPANT_ORIGINATION EPO
            ON EP.EVENT_PARTICIPANT_ID = EPO.EVENT_PARTICIPANT_ID
         WHERE EP.PARTICIPANT_ID = ?
           AND PSD.ORDER_NBR > 1
           AND ( EXISTS ( SELECT RD.ROLE_CD
                            FROM USER_ROLE_XREF URX LEFT JOIN ROLE_DEF RD
                              ON URX.ROLE_ID = RD.ROLE_ID
                           WHERE URX.USER_ID = ?
                             AND RD.ROLE_CD = 'ACCESS_ALL_INST_DEALS' )
            OR EXISTS ( SELECT DM.DEAL_ID
                          FROM DEAL_MEMBER DM
                         WHERE DM.DEAL_ID = DI.DEAL_ID
                           AND DM.USER_ID = ?
                           AND DM.MEMBER_TYPE_CD = 'P' ))
    """;
    public static final String SELECT_DEAL_SUMMARY = SELECT_DEAL_EVENT_SUMMARY;
    public static final String SELECT_DEAL_EVENT = """
        SELECT DI.DEAL_ID
             , DI.DEAL_UUID
             , DI.DEAL_EXTERNAL_UUID
             , DI.DEAL_NAME
             , DI.DEAL_INDUSTRY_ID
             , COALESCE(PD.OPTION_NAME, '') AS DEAL_INDUSTRY_NAME
             , II.INSTITUTION_ID
             , II.INSTITUTION_UUID
             , II.INSTITUTION_NAME
             , DI.INITIAL_LENDER_IND
             , DI.INITIAL_LENDER_ID
             , ILI.LENDER_NAME
             , ( SELECT EI.EVENT_UUID
                   FROM EVENT_INFO EI
                  WHERE EI.DEAL_ID = DI.DEAL_ID
                    AND EI.CLOSE_DATE IS NULL ) AS OPEN_EVENT_UUID
             , CASE
                 WHEN DM.USER_ID > 0 THEN 'Y'
                 ELSE 'N'
               END AS MEMBER_IND
             , CASE
                 WHEN DM.MEMBER_TYPE_CD IS NULL THEN 'U'
                 ELSE DM.MEMBER_TYPE_CD
               END AS MEMBER_TYPE_CD
             , CASE
                 WHEN DI.ORIGINATOR_ID = ? THEN 'Y'
                 ELSE 'N'
               END AS ORIG_INST_USER_IND
             , CASE
                 WHEN ( SELECT COUNT(EP.PARTICIPANT_ID)
                          FROM EVENT_INFO EI LEFT JOIN EVENT_PARTICIPANT EP
                            ON EI.EVENT_ID = EP.EVENT_ID
                         WHERE EI.DEAL_ID = DI.DEAL_ID
                           AND EP.PARTICIPANT_ID = ? ) > 0 THEN 'Y'
                 ELSE 'N'
               END AS PART_INST_USER_IND
             , ( SELECT STRING_AGG(RD.ROLE_CD, ',' ORDER BY RD.ROLE_NAME)
                   FROM USER_ROLE_XREF URX LEFT JOIN ROLE_DEF RD
                     ON URX.ROLE_ID = RD.ROLE_ID
                  WHERE URX.USER_ID = ? ) AS USER_ROLES_DESC
             , DI.DEAL_STRUCTURE_ID
             , COALESCE(PD2.OPTION_NAME, '') AS DEAL_STRUCTURE_DESC
             , DI.DEAL_TYPE_DESC
             , DI.DEAL_DESC
             , DI.DEAL_AMT
             , DI.LAST_FACILITY_NBR
             , DI.APPLICANT_EXTERNAL_UUID
             , DI.BORROWER_DESC
             , DI.BORROWER_NAME
             , DI.BORROWER_CITY_NAME
             , DI.BORROWER_STATE_CD
             , DI.BORROWER_COUNTY_NAME
             , DI.FARM_CR_ELIG_ID
             , COALESCE(PD3.OPTION_NAME, '') AS FARM_CR_ELIG_DESC
             , DI.TAX_ID_NBR
             , DI.BORROWER_INDUSTRY_CD
             , ND.TITLE_NAME AS BORROWER_INDUSTRY_NAME
             , DI.BUSINESS_AGE_QTY
             , DI.DEFAULT_PROB_PCT
             , DI.CY_EBITA_AMT
             , DI.CREATED_BY_ID
             , UIC.USER_UUID AS CREATED_BY_UUID
             , UIC.FIRST_NAME AS CREATED_BY_FIRST_NAME
             , UIC.LAST_NAME AS CREATED_BY_LAST_NAME
             , UIC.EMAIL_ADDR AS CREATED_BY_EMAIL_ADDR
             , UIC.PASSWORD_DESC AS CREATED_BY_PASSWORD_DESC
             , UIC.ACTIVE_IND AS CREATED_BY_ACTIVE_IND
             , DI.CREATED_DATE
             , DI.UPDATED_BY_ID
             , UIU.USER_UUID AS UPDATED_BY_UUID
             , UIU.FIRST_NAME AS UPDATED_BY_FIRST_NAME
             , UIU.LAST_NAME AS UPDATED_BY_LAST_NAME
             , UIU.EMAIL_ADDR AS UPDATED_BY_EMAIL_ADDR
             , UIU.PASSWORD_DESC AS UPDATED_BY_PASSWORD_DESC
             , UIU.ACTIVE_IND AS UPDATED_BY_ACTIVE_IND
             , DI.UPDATED_DATE
             , DI.ACTIVE_IND
          FROM DEAL_INFO DI LEFT JOIN INSTITUTION_INFO II
            ON DI.ORIGINATOR_ID = II.INSTITUTION_ID LEFT JOIN DEAL_MEMBER DM
            ON DI.DEAL_ID = DM.DEAL_ID AND DM.USER_ID = ? LEFT JOIN INITIAL_LENDER_INFO ILI
            ON DI.INITIAL_LENDER_ID = ILI.INITIAL_LENDER_ID LEFT JOIN PICKLIST_DEF PD
            ON DI.DEAL_INDUSTRY_ID = PD.PICKLIST_ID LEFT JOIN PICKLIST_DEF PD2
            ON DI.DEAL_STRUCTURE_ID = PD2.PICKLIST_ID LEFT JOIN PICKLIST_DEF PD3
            ON DI.FARM_CR_ELIG_ID = PD3.PICKLIST_ID LEFT JOIN NAICS_DEF ND
            ON DI.BORROWER_INDUSTRY_CD = ND.NAICS_CD LEFT JOIN USER_INFO UIC
            ON DI.CREATED_BY_ID = UIC.USER_ID LEFT JOIN USER_INFO UIU
            ON DI.UPDATED_BY_ID = UIU.USER_ID
   """;
    public static final String SELECT_DEAL = SELECT_DEAL_EVENT;
    public static final String INSERT_DEAL = """
        INSERT INTO DEAL_INFO
             ( DEAL_UUID, DEAL_EXTERNAL_UUID, DEAL_NAME, DEAL_INDUSTRY_ID, ORIGINATOR_ID, INITIAL_LENDER_IND
             , INITIAL_LENDER_ID, STAGE_ID, DEAL_STRUCTURE_ID, DEAL_TYPE_DESC, DEAL_DESC, DEAL_AMT, APPLICANT_EXTERNAL_UUID
             , BORROWER_DESC, BORROWER_NAME, BORROWER_CITY_NAME, BORROWER_STATE_CD, BORROWER_COUNTY_NAME, FARM_CR_ELIG_ID
             , TAX_ID_NBR, BORROWER_INDUSTRY_CD, BUSINESS_AGE_QTY, DEFAULT_PROB_PCT, CY_EBITA_AMT
             , CREATED_BY_ID, UPDATED_BY_ID, ACTIVE_IND )
               VALUES
             ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
    """;
    public static final String UPDATE_DEAL = """
        UPDATE DEAL_INFO
           SET DEAL_NAME = ?
             , DEAL_INDUSTRY_ID = ?
             , INITIAL_LENDER_IND = ?
             , INITIAL_LENDER_ID = ?
             , DEAL_STRUCTURE_ID = ?
             , DEAL_TYPE_DESC = ?
             , DEAL_DESC = ?
             , DEAL_AMT = ?
             , BORROWER_DESC = ?
             , BORROWER_NAME = ?
             , BORROWER_CITY_NAME = ?
             , BORROWER_STATE_CD = ?
             , BORROWER_COUNTY_NAME = ?
             , FARM_CR_ELIG_ID = ?
             , TAX_ID_NBR = ?
             , BORROWER_INDUSTRY_CD = ?
             , BUSINESS_AGE_QTY = ?
             , DEFAULT_PROB_PCT = ?
             , CY_EBITA_AMT = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
             , ACTIVE_IND  = ?
         WHERE DEAL_ID = ?
    """;
    public static final String UPDATE_DEAL_STAGE = """
        UPDATE DEAL_INFO
           SET STAGE_ID = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE DEAL_ID = ?
    """;
    public static final String UPDATE_DEAL_LAUNCH_DATES = """
        UPDATE DEAL_INFO
           SET LAUNCH_DATE = CURRENT_TIMESTAMP
             , COMMITMENT_DATE = ?
             , PROJECTED_CLOSE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE DEAL_ID = ?
    """;
    public static final String UPDATE_DEAL_DATES = """
        UPDATE DEAL_INFO
               SET PROJECTED_LAUNCH_DATE = ?
             , COMMITMENT_DATE = ?
             , PROJECTED_CLOSE_DATE = ?
             , COMMENTS_DUE_BY_DATE = ?
             , EFFECTIVE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE DEAL_ID = ?
    """;
    public static final String UPDATE_DEAL_CLOSE_DATES = """
        UPDATE DEAL_INFO
           SET CLOSE_DATE = CURRENT_TIMESTAMP
             , EFFECTIVE_DATE = ?
             , UPDATED_BY_ID = ?
             , UPDATED_DATE = CURRENT_TIMESTAMP
         WHERE DEAL_ID = ?
    """;
    public static final String UPDATE_LAST_FACILITY_NBR = """
        UPDATE DEAL_INFO
           SET LAST_FACILITY_NBR = LAST_FACILITY_NBR + 1
         WHERE DEAL_ID = ?
    """;
    public static final String DELETE_DEAL = "DELETE FROM DEAL_INFO";

}