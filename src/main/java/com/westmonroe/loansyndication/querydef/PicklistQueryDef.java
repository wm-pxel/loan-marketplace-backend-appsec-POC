package com.westmonroe.loansyndication.querydef;

public class PicklistQueryDef {

    private PicklistQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_PICKLIST = "SELECT PD.PICKLIST_ID"
                                                    + ", PD.PICKLIST_CATEGORY_ID"
                                                    + ", PCD.PICKLIST_CATEGORY_NAME"
                                                    + ", PD.OPTION_NAME"
                                                    + ", PD.ORDER_NBR "
                                                 + "FROM PICKLIST_DEF PD LEFT JOIN PICKLIST_CATEGORY_DEF PCD "
                                                   + "ON PD.PICKLIST_CATEGORY_ID = PCD.PICKLIST_CATEGORY_ID";
    public static final String INSERT_PICKLIST = "INSERT INTO PICKLIST_DEF "
                                                    + "( PICKLIST_CATEGORY_ID, OPTION_NAME, ORDER_NBR ) "
                                                      + "VALUES "
                                                    + "( ?, ?, ? )";
    public static final String UPDATE_PICKLIST = "UPDATE PICKLIST_DEF "
                                                  + "SET OPTION_NAME = ?"
                                                    + ", ORDER_NBR = ? "
                                                + "WHERE PICKLIST_ID = ?";
    public static final String DELETE_PICKLIST = "DELETE FROM PICKLIST_DEF";

}