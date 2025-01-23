package com.westmonroe.loansyndication.mapper.deal;

import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.deal.DealCovenant;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DealCovenantRowMapper implements RowMapper<DealCovenant> {

    @Override
    public DealCovenant mapRow(ResultSet rs, int rowNum) throws SQLException {

        DealCovenant covenant = new DealCovenant();

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));
        covenant.setDeal(deal);

        covenant.setId(rs.getLong("DEAL_COVENANT_ID"));
        covenant.setCovenantExternalId(rs.getString("COVENANT_EXTERNAL_UUID"));
        covenant.setEntityName(rs.getString("ENTITY_NAME"));
        covenant.setCategoryName(rs.getString("CATEGORY_NAME"));
        covenant.setCovenantType(rs.getString("COVENANT_TYPE_DESC"));
        covenant.setFrequency(rs.getString("FREQUENCY_DESC"));
        covenant.setNextEvalDate(rs.getObject("NEXT_EVAL_DATE", LocalDate.class));
        covenant.setEffectiveDate(rs.getObject("EFFECTIVE_DATE", LocalDate.class));

        User createdBy = new User();
        createdBy.setId(rs.getLong("CREATED_BY_ID"));
        createdBy.setUid(rs.getString("CREATED_BY_UUID"));
        createdBy.setFirstName(rs.getString("CREATED_BY_FIRST_NAME"));
        createdBy.setLastName(rs.getString("CREATED_BY_LAST_NAME"));
        createdBy.setEmail(rs.getString("CREATED_BY_EMAIL_ADDR"));
        createdBy.setPassword(rs.getString("CREATED_BY_PASSWORD_DESC"));
        createdBy.setActive(rs.getString("CREATED_BY_ACTIVE_IND"));
        covenant.setCreatedBy(createdBy);

        covenant.setCreatedDate(rs.getString("CREATED_DATE"));

        User updatedBy = new User();
        updatedBy.setId(rs.getLong("UPDATED_BY_ID"));
        updatedBy.setUid(rs.getString("UPDATED_BY_UUID"));
        updatedBy.setFirstName(rs.getString("UPDATED_BY_FIRST_NAME"));
        updatedBy.setLastName(rs.getString("UPDATED_BY_LAST_NAME"));
        updatedBy.setEmail(rs.getString("UPDATED_BY_EMAIL_ADDR"));
        updatedBy.setPassword(rs.getString("UPDATED_BY_PASSWORD_DESC"));
        updatedBy.setActive(rs.getString("UPDATED_BY_ACTIVE_IND"));
        covenant.setUpdatedBy(updatedBy);

        covenant.setUpdatedDate(rs.getString("UPDATED_DATE"));


        return covenant;
    }

}