package com.westmonroe.loansyndication.model.email;

import java.util.Map;

public interface EmailInfo {

    String getTemplateName();

    Map<String, Object> getRecipients();

}