package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.FULL_DEAL_ACCESS;

public class FullDealAccessEmailInfo implements EmailInfo {

    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public FullDealAccessEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.dealMemberDao = dealMemberDao;
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() { return FULL_DEAL_ACCESS.getTemplateName(); }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();
        // add all originators deal team members as recipients
        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), ORIGINATOR)
                .stream().
                map(dm -> dm.getUser()).
                collect(Collectors.toList()));

        // check if deal was launched, if so send full deal access notification to participant users
        if ((Boolean) templateDataMap.get("isDealLaunched")) {
            recipientMap.put("participant", dealMemberDao.findAllByDealUidAndInstitutionUid(templateDataMap.get("dealUid").toString(), templateDataMap.get("participantInstitutionUid").toString())
                    .stream()
                    .map(dm -> dm.getUser())
                    .collect(Collectors.toList()));
        }

        return recipientMap;
    }
}
