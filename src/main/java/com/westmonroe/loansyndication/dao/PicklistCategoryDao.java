package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.PicklistCategoryRowMapper;
import com.westmonroe.loansyndication.model.PicklistCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.PicklistCategoryQueryDef.*;

@Repository
@Slf4j
public class PicklistCategoryDao {

    private final JdbcTemplate jdbcTemplate;

    public PicklistCategoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PicklistCategory> findAll() {
        String sql = SELECT_PICKLIST_CATEGORY + " ORDER BY PICKLIST_CATEGORY_NAME";
        return jdbcTemplate.query(sql, new PicklistCategoryRowMapper());
    }

    public PicklistCategory findById(Long id) {
        String sql = SELECT_PICKLIST_CATEGORY + " WHERE PICKLIST_CATEGORY_ID = ?";
        PicklistCategory category;

        try {
            category = jdbcTemplate.queryForObject(sql, new PicklistCategoryRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Picklist category was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Picklist category was not found for id.");

        }

        return category;
    }

    public PicklistCategory findByName(String name) {
        String sql = SELECT_PICKLIST_CATEGORY + " WHERE UPPER(PICKLIST_CATEGORY_NAME) = ?";
        PicklistCategory category;

        try {
            category = jdbcTemplate.queryForObject(sql, new PicklistCategoryRowMapper(), name.toUpperCase());
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Picklist category was not found for name. ( name = '%s' )", name));
            throw new DataNotFoundException("Picklist category was not found for name.");

        }

        return category;
    }

    public PicklistCategory save(PicklistCategory category) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_PICKLIST_CATEGORY, new String[] { "picklist_category_id" });
            ps.setString(1, category.getName());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            category.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for picklist category.");
            throw new DatabaseException("Error retrieving unique id for picklist category.");

        }

        return category;
    }

    public void update(PicklistCategory category) {
        jdbcTemplate.update(UPDATE_PICKLIST_CATEGORY, category.getName(), category.getId());
    }

    public int delete(PicklistCategory category) {
        return deleteById(category.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_PICKLIST_CATEGORY + " WHERE PICKLIST_CATEGORY_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

}