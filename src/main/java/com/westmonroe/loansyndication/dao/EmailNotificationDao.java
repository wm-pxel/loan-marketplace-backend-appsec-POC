package com.westmonroe.loansyndication.dao;

import com.westmonroe.loansyndication.exception.DatabaseException;
import com.westmonroe.loansyndication.mapper.EmailNotificationRowMapper;
import com.westmonroe.loansyndication.model.EmailNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.westmonroe.loansyndication.querydef.EmailNotificationQueryDef.*;

@Repository
@Slf4j
public class EmailNotificationDao {
    private final JdbcTemplate jdbcTemplate;

    public EmailNotificationDao(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EmailNotification> findAll(){
        String sql = SELECT_EMAIL_NOTIFICATION;
        return jdbcTemplate.query(sql, new EmailNotificationRowMapper());
    }

    public List<EmailNotification> findAllUnprocessedByEmailType(String emailTypeCd){
        String sql = SELECT_EMAIL_NOTIFICATION +
                " WHERE ENI.EMAIL_TYPE_CD = ? AND ENI.PROCESSED_IND = 'N'" + "ORDER BY ENI.CREATED_DATE DESC";

        return jdbcTemplate.query(sql, new EmailNotificationRowMapper(), emailTypeCd);
    }

    public void update(EmailNotification en){
        jdbcTemplate.update(UPDATE_EMAIL_NOTIFICATION, en.getProcessedInd(), en.getId());
    }

    public void updateBatch(List<EmailNotification> notifications) {
        jdbcTemplate.batchUpdate(UPDATE_EMAIL_NOTIFICATION, notifications, notifications.size(),
                (ps, emailNotification) -> {
                    ps.setString(1, emailNotification.getProcessedInd());
                    ps.setLong(2, emailNotification.getId());
                });
    }

    public EmailNotification save(EmailNotification emailNotification){
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_EMAIL_NOTIFICATION, new String[] {"email_notification_id"});
            int index = 1;

            ps.setLong(index++, emailNotification.getDeal().getId());
            ps.setString(index++, emailNotification.getEmailTypeCd());
            ps.setString(index++, "N");
            ps.setString(index, emailNotification.getTemplateDataJson());

            return ps;
        }, keyHolder);

        try {
            emailNotification.setId(keyHolder.getKey().longValue());
        } catch (NullPointerException e) {
            log.error("Error retrieving unique id for Email Notification");
            throw new DatabaseException("Error retrieving unique id for Email Notification");
        }

        return emailNotification;
    }
}
