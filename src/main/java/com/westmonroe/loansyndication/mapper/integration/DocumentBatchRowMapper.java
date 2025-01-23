package com.westmonroe.loansyndication.mapper.integration;

import com.westmonroe.loansyndication.model.integration.DocumentBatch;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class DocumentBatchRowMapper implements RowMapper<DocumentBatch> {

    @Override
    public DocumentBatch mapRow(ResultSet rs, int rowNum) throws SQLException {

        DocumentBatch batch = new DocumentBatch();

        batch.setId(rs.getLong("DOCUMENT_BATCH_ID"));
        batch.setDealExternalId(rs.getString("DEAL_EXTERNAL_UUID"));
        batch.setDealId(rs.getString("DEAL_UUID"));
        batch.setTransferType(rs.getString("TRANSFER_TYPE_CD"));
        batch.setProcessStartDate(rs.getObject("PROCESS_START_DATE", OffsetDateTime.class));
        batch.setProcessEndDate(rs.getObject("PROCESS_END_DATE", OffsetDateTime.class));
        batch.setCreatedById(rs.getLong("CREATED_BY_ID"));
        batch.setCreatedBy(rs.getString("CREATED_BY_FULL_NAME"));
        batch.setCreatedDate(rs.getString("CREATED_DATE"));

        return batch;
    }

}