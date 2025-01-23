package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.DocumentCategory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentCategoryRowMapper implements RowMapper<DocumentCategory> {

    @Override
    public DocumentCategory mapRow(ResultSet rs, int rowNum) throws SQLException {

        DocumentCategory category = new DocumentCategory();
        category.setId(rs.getLong("DOCUMENT_CATEGORY_ID"));
        category.setName(rs.getString("DOCUMENT_CATEGORY_NAME"));
        category.setOrder(rs.getInt("ORDER_NBR"));
        category.setDealDocumentFlag(rs.getString("DEAL_DOCUMENT_IND"));

        return category;
    }

}