package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.DocumentCategory;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealDocument;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DealDocumentRowMapper implements RowMapper<DealDocument> {

    @Override
    public DealDocument mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealDocument doc = new DealDocument();

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));
        deal.setDealExternalId(rs.getString("DEAL_EXTERNAL_UUID"));
        doc.setDeal(deal);

        DocumentCategory category = new DocumentCategory();
        category.setId(rs.getLong("DOCUMENT_CATEGORY_ID"));
        category.setName(rs.getString("DOCUMENT_CATEGORY_NAME"));
        category.setOrder(rs.getInt("ORDER_NBR"));
        doc.setCategory(category);

        doc.setId(rs.getLong("DEAL_DOCUMENT_ID"));
        doc.setDisplayName(rs.getString("DISPLAY_NAME"));
        doc.setDocumentName(rs.getString("DOCUMENT_NAME"));
        doc.setDocumentType(rs.getString("DOCUMENT_TYPE"));
        doc.setDescription(rs.getString("DOCUMENT_DESC"));
        doc.setSource(rs.getString("SOURCE_CD"));
        doc.setDocumentExternalId(rs.getString("DOCUMENT_EXTERNAL_UUID"));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        doc.setCreatedBy(createdBy);

        doc.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        doc.setUpdatedBy(updatedBy);

        doc.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return doc;
    }

}