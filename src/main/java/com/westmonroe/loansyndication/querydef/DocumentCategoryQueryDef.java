package com.westmonroe.loansyndication.querydef;

public class DocumentCategoryQueryDef {

    private DocumentCategoryQueryDef() {
        throw new IllegalStateException("Utility class");
    }

    public static final String SELECT_DOCUMENT_CATEGORY = "SELECT DOCUMENT_CATEGORY_ID"
                                                             + ", DOCUMENT_CATEGORY_NAME"
                                                             + ", ORDER_NBR"
                                                             + ", DEAL_DOCUMENT_IND "
                                                          + "FROM DOCUMENT_CATEGORY_DEF";

}