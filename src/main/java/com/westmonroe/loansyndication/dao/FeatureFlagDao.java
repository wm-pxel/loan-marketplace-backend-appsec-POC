package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.FeatureFlagRowMapper;
import com.westmonroe.loansyndication.model.FeatureFlag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.FeatureFlagQueryDef.*;

@Repository
@Slf4j
public class FeatureFlagDao {

    private final JdbcTemplate jdbcTemplate;

    public FeatureFlagDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FeatureFlag> findAll(){
        String sql = SELECT_FEATURE_FLAG;

        return jdbcTemplate.query(sql, new FeatureFlagRowMapper());
    }

    public FeatureFlag findById(Long id){
        String sql = SELECT_FEATURE_FLAG + " WHERE FF.FEATURE_FLAG_ID = ? ORDER BY FEATURE_NAME, CREATED_DATE";
        FeatureFlag featureFlag;

        try{
            featureFlag = jdbcTemplate.queryForObject(sql, new FeatureFlagRowMapper(), id);
        } catch (EmptyResultDataAccessException e){

            log.error(String.format("Feature Flag was not found for id. (id = %s )", id));
            throw new DataNotFoundException("Feature flag was not found for id.");
        }

        return featureFlag;
    }

    public FeatureFlag findByName(String featureName){
        String sql = SELECT_FEATURE_FLAG + " WHERE FF.FEATURE_NAME = ? ORDER BY FEATURE_NAME, CREATED_DATE";

        FeatureFlag featureFlag;

        try{
            featureFlag = jdbcTemplate.queryForObject(sql, new FeatureFlagRowMapper(), featureName);
        } catch (EmptyResultDataAccessException e){
            log.error(String.format("Feature Flag with featureName = %s was not found", featureName));
            throw new DataNotFoundException(String.format("Feature flag with featureName = %s was not found", featureName));
        }

        return featureFlag;
    }

    public FeatureFlag save(FeatureFlag featureFlag){
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection ->{
            int index = 1;

            PreparedStatement ps = connection.prepareStatement(INSERT_FEATURE_FLAG, new String[] {"feature_flag_id"});
            ps.setString(index++, featureFlag.getFeatureName());
            ps.setString(index++, featureFlag.getIsEnabled());
            ps.setString(index++, featureFlag.getDescription());
            ps.setLong(index++, featureFlag.getCreatedBy().getId());
            ps.setLong(index, featureFlag.getUpdatedBy().getId());

            return ps;
        }, keyHolder);

        try {
            // Assign the unique id returned from the insert operation.
            featureFlag.setId(keyHolder.getKey().longValue());
        } catch (NullPointerException e) {
            log.error("Error retrieving unique id for Feature Flag.");
            throw new DatabaseException("Error retrieving unique id for Feature Flag");
        }

        return featureFlag;
    }

    public void update(FeatureFlag ff) {
        jdbcTemplate.update(UPDATE_FEATURE_FLAG, ff.getFeatureName(), ff.getIsEnabled(), ff.getDescription(),
                ff.getUpdatedBy().getId(), ff.getId());
    }

    public void deleteById(Long featureFlagId){
        String sql = DELETE_FEATURE_FLAG + " WHERE FEATURE_FLAG_ID = ?";
        jdbcTemplate.update(sql, featureFlagId);
    }

    public void deleteAll(){
        String sql = DELETE_FEATURE_FLAG;
        jdbcTemplate.update(sql);
    }



}
