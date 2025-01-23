package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;
import com.westmonroe.loansyndication.model.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.EmailTypeEnum.DEAL_CREATED;

public class DealCreatedEmailInfo implements EmailInfo {

    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public DealCreatedEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.dealMemberDao = dealMemberDao;
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        return DEAL_CREATED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();
        recipientMap.put("lead", Arrays.asList((User) templateDataMap.get("recipient")));
        return recipientMap;
    }

}