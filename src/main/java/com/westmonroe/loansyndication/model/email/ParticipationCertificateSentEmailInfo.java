package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;

import java.util.HashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.PART_CERT_SENT;

public class ParticipationCertificateSentEmailInfo implements EmailInfo {
    private final DealMemberDao dealMemberDao;
    private final Map<String, Object> templateDataMap;

    public ParticipationCertificateSentEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.dealMemberDao = dealMemberDao;
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        return PART_CERT_SENT.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();

        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), ORIGINATOR)
                .stream().map(dm -> dm.getUser()).toList());

        recipientMap.put("participant", dealMemberDao.findAllByDealUidAndInstitutionUid(templateDataMap.get("dealUid").toString(), templateDataMap.get("participantInstitutionUid").toString())
                .stream().map(dm -> dm.getUser()).toList());

        return recipientMap;

    }

}
