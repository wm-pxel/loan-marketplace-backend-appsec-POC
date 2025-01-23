package com.westmonroe.loansyndication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.westmonroe.loansyndication.dao.EmailNotificationDao;
import com.westmonroe.loansyndication.dao.deal.DealMemberDao;
import com.westmonroe.loansyndication.model.EmailNotification;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.Deal;
import com.westmonroe.loansyndication.model.email.*;
import com.westmonroe.loansyndication.utils.EmailTypeEnum;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest;

import java.util.*;

@Service
@Slf4j
public class EmailService {

    private final DealMemberDao dealMemberDao;
    private final EmailNotificationDao emailNotificationDao;
    private final ObjectMapper mapper;
    private final SesClient sesClient;
    private final AddressFilterService filter;

    @Value("${lamina.email-service.enabled}")
    private boolean emailEnabled;

    @Value("${lamina.email-service.filter}")
    private boolean emailFilterEnabled;

    @Value("${lamina.base-url}")
    private String baseUrl;

    public EmailService(DealMemberDao dealMemberDao, EmailNotificationDao emailNotificationDao, ObjectMapper mapper, SesClient sesClient, AddressFilterService filter) {
        this.dealMemberDao = dealMemberDao;
        this.emailNotificationDao = emailNotificationDao;
        this.mapper = mapper;
        this.filter = filter;
        this.sesClient = sesClient;
    }

    private EmailInfo getEmailInfo(EmailTypeEnum activityType, Map<String, Object> templateDataMap) {
        EmailInfo emailInfo;

        switch(activityType) {
            case TEAM_MEMBER_ADDED -> emailInfo = new TeamMemberAddedEmailInfo(templateDataMap);
            case DEAL_CREATED -> emailInfo = new DealCreatedEmailInfo(dealMemberDao, templateDataMap);
            case DEAL_INFO_UPDATED -> emailInfo = new DealInfoUpdatedEmailInfo(dealMemberDao, templateDataMap);
            case FILE_UPLOADED -> emailInfo = new FileUploadedEmailInfo(dealMemberDao, templateDataMap);
            case INVITE_SENT -> emailInfo = new InviteSentEmailInfo(dealMemberDao, templateDataMap);
            case DEAL_INTEREST -> emailInfo = new DealInterestEmailInfo(dealMemberDao, templateDataMap);
            case FULL_DEAL_ACCESS -> emailInfo = new FullDealAccessEmailInfo(dealMemberDao, templateDataMap);
            case DEAL_LAUNCHED -> emailInfo = new DealLaunchedEmailInfo(dealMemberDao, templateDataMap);
            case COMMITMENTS_SENT -> emailInfo = new CommitmentsSentEmailInfo(dealMemberDao, templateDataMap);
            case DEAL_DATES_UPDATED -> emailInfo = new DealDatesUpdatedEmailInfo(dealMemberDao, templateDataMap);
            case ALLOCATIONS_SENT -> emailInfo = new AllocationsSentEmailInfo(dealMemberDao, templateDataMap);
            case DEAL_DECLINED -> emailInfo = new DealDeclinedEmailInfo(dealMemberDao, templateDataMap);
            case PARTICIPANT_REMOVED ->  emailInfo = new ParticipantRemovedEmailInfo(dealMemberDao, templateDataMap);
            case DRAFT_LOAN_DOCS_UPLOADED -> emailInfo = new DraftLoanDocsUploadedEmailInfo(dealMemberDao, templateDataMap);
            case FINAL_LOAN_DOCS_UPLOADED -> emailInfo = new FinalLoanDocsUploadedEmailInfo(dealMemberDao, templateDataMap);
            case PART_CERT_SENT -> emailInfo = new ParticipationCertificateSentEmailInfo(dealMemberDao, templateDataMap);
            case SIGNED_PC_SENT -> emailInfo = new SignedParticipationCertificateSentEmailInfo(dealMemberDao, templateDataMap);
            case USER_ACTIVATED -> emailInfo = new  UserActivatedEmailInfo(templateDataMap);
            case INSTITUTION_MEMBER_ADDED -> emailInfo = new InstitutionMemberAddedInfo((templateDataMap));
            default -> throw new IllegalStateException("Activity type is not defined.");
        }

        return emailInfo;
    }

    @Async
    public void sendEmail(EmailTypeEnum emailType, @Nullable Deal deal, Map<String, Object> templateDataMap) {
        if (!emailEnabled) {
            System.out.println("Email service is disabled. No email will be sent.");
            return;
        }

        templateDataMap.put("baseUrl", baseUrl);
        EmailInfo emailInfo = getEmailInfo(emailType, templateDataMap);

        // check if email recipients are from lead institution
        if (emailInfo.getRecipients().containsKey("lead")) {
            templateDataMap.put("lead", "Y"); // Add the lead flag to the template data map
            processRecipients((List<User>) emailInfo.getRecipients().get("lead"), deal, emailInfo, templateDataMap, emailType);
        }
        // check if email recipients are from participant institution
        if (emailInfo.getRecipients().containsKey("participant")) {
            templateDataMap.remove("lead"); // Remove the lead flag from the template data map
            processRecipients((List<User>) emailInfo.getRecipients().get("participant"), deal, emailInfo, templateDataMap, emailType);
        }
    }

    public List<EmailNotification> getUnprocessedEmailNotificationsForEmailType(String emailTypeCd) {
        return emailNotificationDao.findAllUnprocessedByEmailType(emailTypeCd);
    }

    public EmailNotification save(EmailNotification emailNotification) {
        return emailNotificationDao.save(emailNotification);
    }

    public EmailNotification update(EmailNotification emailNotification) {
        emailNotificationDao.update(emailNotification);
        return emailNotification;
    }

    @Scheduled(cron = "${lamina.email-notification.process-interval}")
    public void sendFileUploadedEmailNotification() {
        System.out.println("Processing file uploaded email notifications");
        List<EmailNotification> emailNotifications = getUnprocessedEmailNotificationsForEmailType(Long.toString(EmailTypeEnum.FILE_UPLOADED.getId()));
        Set<Long> processedDealIds = new HashSet<>();
        List<EmailNotification> toUpdate = new ArrayList<>();

        for (EmailNotification emailNotification : emailNotifications) {
            Long dealId = emailNotification.getDeal().getId();

            if (!processedDealIds.contains(dealId)) {
                try {
                    Map<String, Object> templateData = mapper.readValue(emailNotification.getTemplateDataJson(), Map.class);
                    sendEmail(EmailTypeEnum.FILE_UPLOADED, emailNotification.getDeal(), templateData);
                    processedDealIds.add(dealId);
                    emailNotification.setProcessedInd("Y");
                } catch (Exception e) {
                    log.error("Error processing email for deal ID {}: {}", dealId, e.getMessage(), e);
                }
            } else {
                emailNotification.setProcessedInd("Y");
            }
            toUpdate.add(emailNotification);
        }

        if (!toUpdate.isEmpty()) {
            emailNotificationDao.updateBatch(toUpdate);
        }
        System.out.println("Finishing processing file uploaded email notifications");
    }

    private void processRecipients(List<User> recipients, @Nullable Deal deal, EmailInfo emailInfo, Map<String, Object> templateDataMap, EmailTypeEnum emailType) {
        String templateData;

        // convert the template data map to a JSON string for Amazon SES
        try {
            templateData = mapper.writeValueAsString(templateDataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Loop through the list of users and email each one. Performed this way because on bad email will cause all to fail.
        for (User recipient : recipients) {
            if (!emailFilterEnabled || this.filter.validateAddress(recipient.getEmail())) {
                try {
                    SendTemplatedEmailRequest request = SendTemplatedEmailRequest.builder()
                            .source("Lamina <" + templateDataMap.get("from").toString() + ">")
                            .destination(Destination.builder()
                                    .toAddresses(recipient.getEmail())
                                    .build())
                            .template(emailInfo.getTemplateName())
                            .configurationSetName("ses-config-set")
                            .templateData(templateData)
                            .build();
                    this.sesClient.sendTemplatedEmail(request);
                } catch (Exception ex) {
                    String dealInfo = deal != null ? deal.getUid() : "N/A";
                    log.error(String.format("EmailException: Error sending \"%s\" email for deal %s", emailType.getName(), dealInfo));
                    log.error(ex.toString());
                }
            }
        }
    }
}