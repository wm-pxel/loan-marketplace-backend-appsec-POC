package com.westmonroe.loansyndication.mapper;

import com.westmonroe.loansyndication.model.EmailNotification;
import com.westmonroe.loansyndication.model.deal.Deal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j

public class EmailNotificationRowMapper implements RowMapper<EmailNotification> {

    @Override
    public EmailNotification mapRow(ResultSet rs, int RowNum) throws SQLException {
        EmailNotification emailNotification = new EmailNotification();

        emailNotification.setId(rs.getLong("EMAIL_NOTIFICATION_ID"));
        emailNotification.setEmailTypeCd(rs.getString("EMAIL_TYPE_CD"));
        emailNotification.setProcessedInd(rs.getString("PROCESSED_IND"));
        emailNotification.setCreatedDate(rs.getString("CREATED_DATE"));

        Deal deal = new Deal();
        deal.setId(rs.getLong("DEAL_ID"));
        deal.setUid(rs.getString("DEAL_UUID"));
        deal.setName(rs.getString("DEAL_NAME"));
        deal.setDealExternalId(rs.getString("DEAL_EXTERNAL_UUID"));
        emailNotification.setDeal(deal);

        if ( rs.getObject("TEMPLATE_JSON") != null) {
            emailNotification.setTemplateDataJson(rs.getString("TEMPLATE_JSON"));
        }

        return emailNotification;
    }
}
