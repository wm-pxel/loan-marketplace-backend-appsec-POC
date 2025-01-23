package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.FINAL_LOAN_DOCS_UPLOADED;

public class FinalLoanDocsUploadedEmailInfo implements EmailInfo {
    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public FinalLoanDocsUploadedEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.templateDataMap = templateDataMap;
        this.dealMemberDao = dealMemberDao;
    }

    @Override
    public String getTemplateName() {
        return FINAL_LOAN_DOCS_UPLOADED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();

        // Notification goes to all deal team members of the lead institution
        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), ORIGINATOR)
                .stream()
                .map(dm -> dm.getUser())
                .collect(Collectors.toList()));

        // Notification goes to all deal team members of participating institution(s)
        recipientMap.put("participant", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), PARTICIPANT)
                .stream()
                .filter(dm -> dm.getEventOriginationParticipant().getDeclinedFlag().equals("N"))
                .filter(dm -> dm.getEventOriginationParticipant().getRemovedFlag().equals("N"))
                .map(dm -> dm.getUser())
                .collect(Collectors.toList()));

        return recipientMap;
    }
}
