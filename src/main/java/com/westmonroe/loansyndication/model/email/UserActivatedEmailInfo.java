package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.model.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserActivatedEmailInfo implements EmailInfo {

    private Map<String, Object> templateDataMap;

    public UserActivatedEmailInfo (Map<String, Object> templateDataMap) {
        this.templateDataMap = templateDataMap;
    }

    @Override
    public String getTemplateName() {
        User updatedUser = (User) templateDataMap.get("updatedUser");
        String updatedActiveFlag = updatedUser.getActive();

        return "N".equals(updatedActiveFlag) ? "UserDeactivated" : "UserReactivated";
    }

    @Override
    public Map<String, Object> getRecipients() {
        Map<String, Object> recipientMap = new HashMap<>();
        recipientMap.put("lead", Arrays.asList((User) templateDataMap.get("user")));

        return recipientMap;
    }

}
