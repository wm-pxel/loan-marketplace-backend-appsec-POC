package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.FILE_UPLOADED;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_5;

public class FileUploadedEmailInfo implements EmailInfo {

    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public FileUploadedEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.dealMemberDao = dealMemberDao;
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        return FILE_UPLOADED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();

        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), ORIGINATOR)
                .stream().map(dm -> dm.getUser()).toList());

        // TODO: Update filters as part of LM-2493
        if ((Boolean) templateDataMap.get("isDealLaunched")) {
            recipientMap.put("participant", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), PARTICIPANT)
                    .stream()
                    .filter(dm -> dm.getEventOriginationParticipant() != null)
                    .filter(dm -> dm.getEventOriginationParticipant().getStep().getOrder() >= STEP_5.getOrder())
                    .filter(dm -> dm.getEventOriginationParticipant().getDeclinedFlag().equals("N"))
                    .filter(dm -> dm.getEventOriginationParticipant().getRemovedFlag().equals("N"))
                    .map(dm -> dm.getUser())
                    .collect(Collectors.toList()));
        }

        return recipientMap;
    }

}