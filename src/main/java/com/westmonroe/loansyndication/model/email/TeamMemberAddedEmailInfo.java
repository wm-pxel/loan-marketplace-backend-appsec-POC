package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.model.deal.DealMember;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.westmonroe.loansyndication.utils.EmailTypeEnum.TEAM_MEMBER_ADDED;

public class TeamMemberAddedEmailInfo implements EmailInfo {

    private Map<String, Object> templateDataMap;

    public TeamMemberAddedEmailInfo(Map<String, Object> templateDataMap) {
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        return TEAM_MEMBER_ADDED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();
        DealMember dealMember = (DealMember) templateDataMap.get("recipient");

        if (templateDataMap.get("leadInstitutionUid").equals(templateDataMap.get("addedByInstitutionUid"))) {
            recipientMap.put("lead", Arrays.asList(dealMember.getUser()));
        } else {
            recipientMap.put("participant", Arrays.asList(dealMember.getUser()));
        }
        return recipientMap;
    }

}