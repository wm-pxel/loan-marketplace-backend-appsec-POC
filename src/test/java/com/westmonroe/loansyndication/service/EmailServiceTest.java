package com.westmonroe.loansyndication.service;

import com.westmonroe.loansyndication.dao.EmailNotificationDao;
import com.westmonroe.loansyndication.model.EmailNotification;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailNotificationDao emailNotificationDao;

    @Test
    void givenEmailNotifications_whenSendingEmails_thenVerifyProcessing() {
        // Arrange
        Deal deal = new Deal();
        deal.setId(1L);

        EmailNotification emailNotification = new EmailNotification();
        emailNotification.setId(1L);
        emailNotification.setDeal(deal);
        emailNotification.setEmailTypeCd(Long.toString(EmailTypeEnum.FILE_UPLOADED.getId()));
        emailNotification.setTemplateDataJson("{\"key\":\"value\"}");

        emailNotificationDao.save(emailNotification);

        // Act
        emailService.sendFileUploadedEmailNotification();

        // Assert
        List<EmailNotification> notifications = emailNotificationDao.findAll();
        notifications.forEach(notification -> assertThat(notification.getProcessedInd()).isEqualTo("Y"));
    }

    @Test
    void givenNoEmailNotifications_whenSendingEmails_thenVerifyNoProcessing() {
        // Arrange
        // Ensure the database is empty or contains no relevant notifications
        List<EmailNotification> existingNotifications = emailNotificationDao.findAll();
        assertThat(existingNotifications).isEmpty();

        // Act
        emailService.sendFileUploadedEmailNotification();

        // Assert
        // Verify that no notifications were processed
        List<EmailNotification> notifications = emailNotificationDao.findAll();
        assertThat(notifications).isEmpty();
    }
}