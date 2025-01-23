package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;
import com.westmonroe.loansyndication.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.INVITE_SENT;

public class InviteSentEmailInfo implements EmailInfo {

    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public InviteSentEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.dealMemberDao = dealMemberDao;
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        return INVITE_SENT.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();
        List<User> recipients = (List<User>) templateDataMap.get("recipients");

        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode((String) templateDataMap.get("dealUid"), ORIGINATOR).stream().map(dm -> dm.getUser()).toList());
        recipientMap.put("participant", recipients);

        return recipientMap;
    }
}