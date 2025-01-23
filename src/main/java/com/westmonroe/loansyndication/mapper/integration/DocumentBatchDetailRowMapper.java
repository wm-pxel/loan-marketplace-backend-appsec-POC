package com.westmonroe.loansyndication.mapper.integration;

import com.westmonroe.loansyndication.model.integration.DealDocumentDto;
import com.westmonroe.loansyndication.model.integration.DocumentBatchDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class DocumentBatchDetailRowMapper implements RowMapper<DocumentBatchDetail> {

    @Override
    public DocumentBatchDetail mapRow(ResultSet rs, int rowNum) throws SQLException {

        DocumentBatchDetail detail = new DocumentBatchDetail();

        detail.setId(rs.getLong("DOCUMENT_BATCH_DETAIL_ID"));
        detail.setDocumentBatchId(rs.getLong("DOCUMENT_BATCH_ID"));
        detail.setDocumentExternalId(rs.getString("DOCUMENT_EXTERNAL_UUID"));
        detail.setSalesforceId(rs.getString("SALESFORCE_ID"));
        detail.setExtension(rs.getString("DOCUMENT_EXT_DESC"));
        detail.setUrl(rs.getString("DOCUMENT_URL"));

        DealDocumentDto dealDocument = new DealDocumentDto();
        dealDocument.setDocumentExternalId(rs.getString("DOCUMENT_EXTERNAL_UUID"));
        dealDocument.setUrl(rs.getString("DOCUMENT_URL"));
        dealDocument.setDisplayName(rs.getString("DD_DISPLAY_NAME"));
        dealDocument.setDocumentName(rs.getString("DD_DOCUMENT_NAME"));
        dealDocument.setType(rs.getString("DD_DOCUMENT_TYPE"));
        dealDocument.setExtension("DOCUMENT_EXT_DESC");
        dealDocument.setCategory(rs.getString("CATEGORY_NAME"));
        dealDocument.setSource(rs.getString("DD_SOURCE_CD"));
        dealDocument.setCreatedById(rs.getLong("DD_CREATED_BY_ID"));
        detail.setDealDocument(dealDocument);

        detail.setDisplayName(rs.getString("DISPLAY_NAME"));
        detail.setCategory(rs.getString("CATEGORY_NAME"));
        detail.setProcessStartDate(rs.getObject("PROCESS_START_DATE", OffsetDateTime.class));
        detail.setProcessEndDate(rs.getObject("PROCESS_END_DATE", OffsetDateTime.class));
        detail.setCreatedById(rs.getLong("CREATED_BY_ID"));
        detail.setCreatedBy(rs.getString("CREATED_BY_FULL_NAME"));
        detail.setCreatedDate(rs.getString("CREATED_DATE"));

        return detail;
    }

}