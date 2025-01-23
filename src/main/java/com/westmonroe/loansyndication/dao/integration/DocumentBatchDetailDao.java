package com.westmonroe.loansyndication.dao.integration;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.mapper.integration.DocumentBatchDetailRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.DocumentBatchDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.integration.DocumentBatchDetailQueryDef.*;

@Repository
@Slf4j
public class DocumentBatchDetailDao {

    private final JdbcTemplate jdbcTemplate;

    public DocumentBatchDetailDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocumentBatchDetail> findAll() {
        throw new OperationNotAllowedException("The findAll() method is not implemented.");
    }

    public List<DocumentBatchDetail> findAllByDocumentBatchId(Long documentBatchId) {
        String sql = SELECT_DOCUMENT_BATCH_DETAIL + " WHERE DBD.DOCUMENT_BATCH_ID = ?";
        return jdbcTemplate.query(sql, new DocumentBatchDetailRowMapper(), documentBatchId);
    }

    public DocumentBatchDetail findById(Long id) {
        String sql = SELECT_DOCUMENT_BATCH_DETAIL + " WHERE DBD.DOCUMENT_BATCH_DETAIL_ID = ?";
        DocumentBatchDetail document;

        try {
            document = jdbcTemplate.queryForObject(sql, new DocumentBatchDetailRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Document batch detail was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Document batch detail was not found for id.");

        }

        return document;
    }

    public DocumentBatchDetail save(DocumentBatchDetail dbd, User currentUser) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_DOCUMENT_BATCH_DETAIL, new String[] { "document_batch_detail_id" });
            ps.setLong(index++, dbd.getDocumentBatchId());
            ps.setString(index++, dbd.getDocumentExternalId());
            ps.setString(index++, dbd.getExtension());
            ps.setString(index++, dbd.getUrl());
            ps.setString(index++, dbd.getDisplayName());
            ps.setString(index++, dbd.getCategory());
            ps.setString(index++, dbd.getSalesforceId());
            ps.setLong(index, currentUser.getId());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            dbd.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for Deal Document.");
            throw new DatabaseException("Error retrieving unique id for Deal Document.");

        }

        return dbd;
    }

    public void updateProcessStartDate(Long batchId, Long detailId) {
        jdbcTemplate.update(UPDATE_DOCUMENT_BATCH_DETAIL_PROCESS_START_DATE, batchId, detailId);
    }

    public void updateProcessEndDate(Long batchId, Long detailId) {
        jdbcTemplate.update(UPDATE_DOCUMENT_BATCH_DETAIL_PROCESS_END_DATE, batchId, detailId);
    }

    public void deleteById(Long id) {
        String sql = DELETE_DOCUMENT_BATCH_DETAIL + " WHERE DOCUMENT_BATCH_DETAIL_ID = ?";
        jdbcTemplate.update(sql, id);
    }

}