package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.FeatureFlag;
import com.westmonroe.loansyndication.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeatureFlagRowMapper implements RowMapper<FeatureFlag> {

    @Override
    public FeatureFlag mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeatureFlag featureFlag = new FeatureFlag();

        featureFlag.setId(rs.getLong("FEATURE_FLAG_ID"));
        featureFlag.setFeatureName(rs.getString("FEATURE_NAME"));
        featureFlag.setDescription(rs.getString("DESCRIPTION"));
        featureFlag.setIsEnabled(rs.getString("IS_ENABLED"));
        featureFlag.setCreatedDate(rs.getString("CREATED_DATE"));
        featureFlag.setUpdatedDate(rs.getString("UPDATED_DATE"));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));

        featureFlag.setCreatedBy(createdBy);

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        featureFlag.setUpdatedBy(updatedBy);

        featureFlag.setUpdatedDate(rs.getString("UPDATED_DATE"));

        return featureFlag;
    }
}
