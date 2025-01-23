package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.Stage;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StageRowMapper implements RowMapper<Stage> {

    @Override
    public Stage mapRow(ResultSet rs, int rowNum) throws SQLException {

        Stage stage = new Stage();
        stage.setId(rs.getLong("STAGE_ID"));
        stage.setName(rs.getString("STAGE_NAME"));
        stage.setTitle(rs.getString("TITLE_DESC"));
        stage.setSubtitle(rs.getString("SUBTITLE_DESC"));
        stage.setOrder(rs.getInt("ORDER_NBR"));

        return stage;
    }

}