package com.westmonroe.loansyndication.querydef;

public class InitialLenderQueryDef {

    private InitialLenderQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_INITIAL_LENDER = "SELECT INITIAL_LENDER_ID"
                                                          + ", LENDER_NAME"
                                                          + ", CREATED_DATE"
                                                          + ", UPDATED_DATE"
                                                          + ", ACTIVE_IND "
                                                       + "FROM INITIAL_LENDER_INFO";
    public static final String INSERT_INITIAL_LENDER = "INSERT INTO INITIAL_LENDER_INFO "
                                                          + "( LENDER_NAME, ACTIVE_IND ) "
                                                            + "VALUES "
                                                          + "( ?, ? )";
    public static final String UPDATE_INITIAL_LENDER = "UPDATE INITIAL_LENDER_INFO "
                                                        + "SET LENDER_NAME = ?"
                                                          + ", UPDATED_DATE = CURRENT_TIMESTAMP"
                                                          + ", ACTIVE_IND = ? "
                                                      + "WHERE INITIAL_LENDER_ID = ?";
    public static final String DELETE_INITIAL_LENDER = "DELETE FROM INITIAL_LENDER_INFO";

}