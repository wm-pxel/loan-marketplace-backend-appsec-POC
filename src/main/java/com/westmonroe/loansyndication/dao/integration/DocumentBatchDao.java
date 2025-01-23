package com.westmonroe.loansyndication.dao.integration;

import com.westmonroe.loansyndication.exception.DataIntegrityException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.exception.OperationNotAllowedException;
import com.westmonroe.loansyndication.mapper.integration.DocumentBatchRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.DocumentBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.integration.DocumentBatchQueryDef.*;

@Repository
@Slf4j
public class DocumentBatchDao {

    private final JdbcTemplate jdbcTemplate;

    public DocumentBatchDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocumentBatch> findAll() {
        throw new OperationNotAllowedException("The findAll() method is not implemented.");
    }

    public DocumentBatch findById(Long id) {
        String sql = SELECT_DOCUMENT_BATCH + " WHERE DB.DOCUMENT_BATCH_ID = ?";
        DocumentBatch document;

        try {
            document = jdbcTemplate.queryForObject(sql, new DocumentBatchRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Document batch was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Document batch was not found for id.");

        }

        return document;
    }

    public DocumentBatch save(DocumentBatch batch, User currentUser) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_DOCUMENT_BATCH, new String[] { "document_batch_id" });
            ps.setString(1, batch.getDealExternalId());
            ps.setString(2, batch.getTransferType());
            ps.setLong(3, currentUser.getId());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            batch.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException npe ) {

            log.error("Error retrieving unique id for document batch.");
            throw new DatabaseException("Error retrieving unique id for document batch.");

        } catch ( DataIntegrityViolationException dive ) {

            log.error(dive.getMessage());
            throw new DataIntegrityException("Data integrity exception: Verify that the dealExternalId exists for a deal.");

        }

        return batch;
    }

    public void updateProcessStartDate(Long documentBatchId) {
        jdbcTemplate.update(UPDATE_DOCUMENT_BATCH_PROCESS_START_DATE, documentBatchId);
    }

    public void updateProcessEndDate(Long documentBatchId) {
        jdbcTemplate.update(UPDATE_DOCUMENT_BATCH_PROCESS_END_DATE, documentBatchId);
    }

    public void deleteById(Long id) {
        String sql = DELETE_DOCUMENT_BATCH + " WHERE DOCUMENT_BATCH_ID = ?";
        jdbcTemplate.update(sql, id);
    }

}