package com.westmonroe.loansyndication.querydef;

public class PicklistCategoryQueryDef {

    private PicklistCategoryQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_PICKLIST_CATEGORY = "SELECT PICKLIST_CATEGORY_ID"
                                                             + ", PICKLIST_CATEGORY_NAME "
                                                          + "FROM PICKLIST_CATEGORY_DEF";
    public static final String INSERT_PICKLIST_CATEGORY = "INSERT INTO PICKLIST_CATEGORY_DEF "
                                                             + "( PICKLIST_CATEGORY_NAME ) "
                                                               + "VALUES "
                                                             + "( ? )";
    public static final String UPDATE_PICKLIST_CATEGORY = "UPDATE PICKLIST_CATEGORY_DEF "
                                                           + "SET PICKLIST_CATEGORY_NAME = ? "
                                                         + "WHERE PICKLIST_CATEGORY_ID = ?";
    public static final String DELETE_PICKLIST_CATEGORY = "DELETE FROM PICKLIST_CATEGORY_DEF";

}