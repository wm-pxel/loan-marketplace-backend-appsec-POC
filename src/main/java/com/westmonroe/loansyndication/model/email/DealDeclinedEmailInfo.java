package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.DEAL_DECLINED;

public class DealDeclinedEmailInfo implements EmailInfo {
    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public DealDeclinedEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.templateDataMap = templateDataMap;
        this.dealMemberDao = dealMemberDao;
    }

    @Override
    public String getTemplateName() {
        return DEAL_DECLINED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();

        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), ORIGINATOR)
                .stream()
                .map(dm -> dm.getUser())
                .collect(Collectors.toList()));

        recipientMap.put("participant", dealMemberDao.findAllByDealUidAndInstitutionUid(templateDataMap.get("dealUid").toString(), templateDataMap.get("participantInstitutionUid").toString())
                .stream()
                .map(dm -> dm.getUser())
                .collect(Collectors.toList()));

        return recipientMap;
    }
}
