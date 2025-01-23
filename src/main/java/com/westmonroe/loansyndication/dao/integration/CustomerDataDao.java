package com.westmonroe.loansyndication.dao.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.DataConversionException;
import com.westmonroe.loansyndication.exception.DataNotFoundException;
import com.westmonroe.loansyndication.mapper.integration.CustomerDataRowMapper;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.CustomerData;
import com.westmonroe.loansyndication.model.integration.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import static com.westmonroe.loansyndication.querydef.integration.CustomerDataQueryDef.*;

@Repository
@Slf4j
public class CustomerDataDao {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper;

    public CustomerDataDao(JdbcTemplate jdbcTemplate, ObjectMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    public CustomerData findByDealExternalId(String dealExternalId) {

        String sql = SELECT_CUSTOMER_DATA + " WHERE MARKETPLACE_JSON->'deal'->>'dealExternalId' = ?";
        CustomerData data;

        try {
            data = jdbcTemplate.queryForObject(sql, new CustomerDataRowMapper(mapper), dealExternalId);
        } catch ( EmptyResultDataAccessException e ) {

            log.error(String.format("Customer data was not found for deal external id. ( id = %s )", dealExternalId));
            throw new DataNotFoundException("Customer data was not found for deal external id.");

        }

        return data;
    }

    public int save(Payload payload, User user) {

        String marketplaceData;
        String unsupportedData = null;

        /*
         *  Convert marketplace data object to string.
         */
        try {
            marketplaceData = mapper.writeValueAsString(payload.getMarketplaceData());
        } catch (JsonProcessingException e) {

            log.error(String.format("Error converting marketplace data to a JSON string (deal external id = %s)."
                    , payload.getMarketplaceData().getDeal().getDealExternalId()));
            throw new DataConversionException("An error was encountered converting the marketplace data to JSON.");

        }

        /*
         *  Convert unsupported data map to string.
         */
        try {

            if ( payload.getUnsupportedData() != null ) {
                unsupportedData = mapper.writeValueAsString(payload.getUnsupportedData());
            }

        } catch (JsonProcessingException e) {

            log.error(String.format("Error converting unsupported map to a JSON string (deal external id = %s)."
                    , payload.getMarketplaceData().getDeal().getDealExternalId()));
            throw new DataConversionException("An error was encountered converting the unsupported to JSON.");

        }

        return jdbcTemplate.update(INSERT_CUSTOMER_DATA, marketplaceData, unsupportedData, user.getId());
    }

    public int deleteByOriginatorId(Long originatorId) {
        String sql = DELETE_CUSTOMER_DATA + " WHERE MARKETPLACE_JSON->'deal'->>'originatorId' = ( SELECT INSTITUTION_UUID FROM INSTITUTION_INFO WHERE INSTITUTION_ID = ? )";
        return jdbcTemplate.update(sql, originatorId);
    }

}