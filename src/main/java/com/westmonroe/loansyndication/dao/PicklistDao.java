package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.PicklistRowMapper;
import com.westmonroe.loansyndication.model.PicklistItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.PicklistQueryDef.*;

@Repository
@Slf4j
public class PicklistDao {

    private final JdbcTemplate jdbcTemplate;

    public PicklistDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PicklistItem> findAllByCategoryId(Long categoryId) {
        String sql = SELECT_PICKLIST + " WHERE PD.PICKLIST_CATEGORY_ID = ? ORDER BY ORDER_NBR";
        return jdbcTemplate.query(sql, new PicklistRowMapper(), categoryId);
    }

    public List<PicklistItem> findAllByCategoryName(String categoryName) {
        String sql = SELECT_PICKLIST + " WHERE UPPER(PCD.PICKLIST_CATEGORY_NAME) = ? ORDER BY ORDER_NBR";
        return jdbcTemplate.query(sql, new PicklistRowMapper(), categoryName.toUpperCase());
    }

    public PicklistItem findById(Long id) {
        String sql = SELECT_PICKLIST + " WHERE PD.PICKLIST_ID = ?";
        PicklistItem picklistItem;

        try {
            picklistItem = jdbcTemplate.queryForObject(sql, new PicklistRowMapper(), id);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Picklist was not found for id. ( id = %s )", id));
            throw new DataNotFoundException("Picklist was not found for id.");

        }

        return picklistItem;
    }

    public PicklistItem findByCategoryNameAndOptionName(String categoryName, String optionName) {
        String sql = SELECT_PICKLIST + " WHERE PCD.PICKLIST_CATEGORY_NAME = ? AND PD.OPTION_NAME = ?";
        PicklistItem picklistItem;

        try {
            picklistItem = jdbcTemplate.queryForObject(sql, new PicklistRowMapper(), categoryName, optionName);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Picklist was not found for category and option. ( category = %s, option = %s )", categoryName, optionName));
            throw new DataNotFoundException(String.format("Option (%s) was not found for category (%s).", optionName, categoryName));

        }

        return picklistItem;
    }

    public int findCountByIdAndCategoryName(Long itemId, String categoryName) {
        String sql = SELECT_PICKLIST + " WHERE PICKLIST_ID = ? AND PCD.PICKLIST_CATEGORY_NAME = ?";
        List<PicklistItem> picklistItems = jdbcTemplate.query(sql, new PicklistRowMapper(), itemId, categoryName);

        return picklistItems.size();
    }

    public int findCountByOptionAndCategoryName(String option, String categoryName) {
        String sql = SELECT_PICKLIST + " WHERE OPTION_NAME = ? AND PCD.PICKLIST_CATEGORY_NAME = ?";
        List<PicklistItem> picklistItems = jdbcTemplate.query(sql, new PicklistRowMapper(), option, categoryName);

        return picklistItems.size();
    }

    public PicklistItem save(PicklistItem picklistItem) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_PICKLIST, new String[] { "picklist_id" });
            ps.setLong(1, picklistItem.getCategory().getId());
            ps.setString(2, picklistItem.getOption());
            ps.setInt(3, picklistItem.getOrder());
            return ps;
        }, keyHolder);

        try {

            // Assign the unique id returned from the insert operation.
            picklistItem.setId(keyHolder.getKey().longValue());

        } catch ( NullPointerException e ) {

            log.error("Error retrieving unique id for picklist.");
            throw new DatabaseException("Error retrieving unique id for picklist.");

        }

        return picklistItem;
    }

    public void update(PicklistItem picklistItem) {
        jdbcTemplate.update(UPDATE_PICKLIST, picklistItem.getOption(), picklistItem.getId());
    }

    public int deleteById(Long id) {
        String sql = DELETE_PICKLIST + " WHERE PICKLIST_ID = ?";
        return jdbcTemplate.update(sql, id);
    }

}