package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.PicklistCategory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PicklistCategoryRowMapper implements RowMapper<PicklistCategory> {

    @Override
    public PicklistCategory mapRow(ResultSet rs, int rowNum) throws SQLException {

        PicklistCategory category = new PicklistCategory();
        category.setId(rs.getLong("PICKLIST_CATEGORY_ID"));
        category.setName(rs.getString("PICKLIST_CATEGORY_NAME"));

        return category;
    }

}