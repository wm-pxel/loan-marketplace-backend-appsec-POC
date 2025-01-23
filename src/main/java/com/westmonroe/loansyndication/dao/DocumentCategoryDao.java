package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.DocumentCategoryRowMapper;
import com.westmonroe.loansyndication.model.DocumentCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.westmonroe.loansyndication.querydef.DocumentCategoryQueryDef.SELECT_DOCUMENT_CATEGORY;

@Repository
@Slf4j
public class DocumentCategoryDao {

    private final JdbcTemplate jdbcTemplate;

    public DocumentCategoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocumentCategory> findAll() {
        String sql = SELECT_DOCUMENT_CATEGORY + " WHERE DEAL_DOCUMENT_IND = 'Y' ORDER BY ORDER_NBR";
        return jdbcTemplate.query(sql, new DocumentCategoryRowMapper());
    }

    public DocumentCategory findById(Long id) {
        String sql = SELECT_DOCUMENT_CATEGORY + " WHERE DOCUMENT_CATEGORY_ID = ?";
        DocumentCategory category;

        try {
            category = jdbcTemplate.queryForObject(sql, new DocumentCategoryRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Document category was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Document category was not found for id.");

        }

        return category;
    }

    public DocumentCategory findByName(String name) {
        String sql = SELECT_DOCUMENT_CATEGORY + " WHERE UPPER(DOCUMENT_CATEGORY_NAME) = ?";
        DocumentCategory category;

        try {
            category = jdbcTemplate.queryForObject(sql, new DocumentCategoryRowMapper(), name.toUpperCase());
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Document category was not found for name. ( name = '%s' )", name));
            throw new DataNotFoundException("Document category was not found for name.");

        }

        return category;
    }

}