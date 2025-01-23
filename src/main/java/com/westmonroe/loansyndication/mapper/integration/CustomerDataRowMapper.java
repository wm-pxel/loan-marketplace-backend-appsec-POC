package com.westmonroe.loansyndication.mapper.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.exception.DataConversionException;
import com.westmonroe.loansyndication.model.integration.CustomerData;
import com.westmonroe.loansyndication.model.integration.MarketplaceData;
import com.westmonroe.loansyndication.model.integration.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public class CustomerDataRowMapper implements RowMapper<CustomerData> {

    private ObjectMapper mapper;

    public CustomerDataRowMapper(ObjectMapper mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public CustomerData mapRow(ResultSet rs, int rowNum) throws SQLException {

        CustomerData data = new CustomerData();
        Payload payload = new Payload();

        /*
         *  Convert Marketplace data from JSON to domain object.
         */
        MarketplaceData marketplaceData = null;

        if ( rs.getString("MARKETPLACE_JSON") != null ) {

            try {
                marketplaceData = mapper.readValue(rs.getString("MARKETPLACE_JSON"), MarketplaceData.class);
            } catch (JsonProcessingException e) {
                log.error(String.format("Error converting the stored marketplace data JSON to an object (id = %s).", payload.getMarketplaceData().getDeal().getDealExternalId()));
                throw new DataConversionException("An error was encountered converting the marketplace data JSON from the database.");
            }

        }

        payload.setMarketplaceData(marketplaceData);

        /*
         *  Convert Unsupported data from JSON to Map.
         */
        Map<String, Object> unsupportedData = null;

        if ( rs.getString("UNSUPPORTED_JSON") != null ) {

            try {
                unsupportedData = mapper.readValue(rs.getString("UNSUPPORTED_JSON"), Map.class);
            } catch ( JsonProcessingException e ) {
                log.error(String.format("Error converting the stored unsupported data JSON to a map (id = %s).", payload.getMarketplaceData().getDeal().getDealExternalId()));
                throw new DataConversionException("An error was encountered converting the unsupported data JSON from the database.");
            }

        }

        payload.setUnsupportedData(unsupportedData);

        payload.setCreatedById(rs.getLong("CREATED_BY_ID"));
        payload.setCreatedBy(rs.getString("CREATED_BY_NAME"));
        payload.setCreatedDate(rs.getString("CREATED_DATE"));

        data.setPayload(payload);

        return data;
    }

}