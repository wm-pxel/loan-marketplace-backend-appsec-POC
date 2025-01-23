package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.PicklistCategory;
import com.westmonroe.loansyndication.model.PicklistItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PicklistRowMapper implements RowMapper<PicklistItem> {

    @Override
    public PicklistItem mapRow(ResultSet rs, int rowNum) throws SQLException {

        PicklistItem picklistItem = new PicklistItem();

        picklistItem.setId(rs.getLong("PICKLIST_ID"));
        picklistItem.setOption(rs.getString("OPTION_NAME"));
        picklistItem.setOrder(rs.getInt("ORDER_NBR"));

        PicklistCategory category = new PicklistCategory();
        category.setId(rs.getLong("PICKLIST_CATEGORY_ID"));
        category.setName(rs.getString("PICKLIST_CATEGORY_NAME"));

        picklistItem.setCategory(category);

        return picklistItem;
    }

}