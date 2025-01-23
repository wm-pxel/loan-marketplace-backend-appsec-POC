package com.westmonroe.loansyndication.model.email;


import com.westmonroe.loansyndication.model.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.EmailTypeEnum.INSTITUTION_MEMBER_ADDED;

public class InstitutionMemberAddedInfo implements EmailInfo {

    private Map<String, Object> templateDataMap;

    public InstitutionMemberAddedInfo (Map<String, Object> templateDataMap) {
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        return INSTITUTION_MEMBER_ADDED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();
        recipientMap.put("participant", Arrays.asList((User) templateDataMap.get("user")));

        return recipientMap;
    }
}
