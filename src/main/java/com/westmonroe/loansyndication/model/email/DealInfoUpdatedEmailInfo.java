package com.westmonroe.loansyndication.model.email;

import com.westmonroe.loansyndication.dao.deal.DealMemberDao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.westmonroe.loansyndication.utils.DealRelationEnum.ORIGINATOR;
import static com.westmonroe.loansyndication.utils.DealRelationEnum.PARTICIPANT;
import static com.westmonroe.loansyndication.utils.EmailTypeEnum.DEAL_INFO_UPDATED;
import static com.westmonroe.loansyndication.utils.ParticipantStepEnum.STEP_5;

public class DealInfoUpdatedEmailInfo implements EmailInfo  {

    private final DealMemberDao dealMemberDao;
    private Map<String, Object> templateDataMap;

    public DealInfoUpdatedEmailInfo(DealMemberDao dealMemberDao, Map<String, Object> templateDataMap) {
        this.templateDataMap = templateDataMap;
        this.dealMemberDao = dealMemberDao;
    }

    @Override
    public String getTemplateName() {
        return DEAL_INFO_UPDATED.getTemplateName();
    }

    @Override
    public Map<String, Object> getRecipients() {
        templateDataMap.put("summary", false);
        templateDataMap.put("full", false);

        if (templateDataMap.get("category").equals("Facility")) {

            HashMap<String, Object> facilityMap = (HashMap<String, Object>) templateDataMap.get("facilityMap");
            List<String> participantSummaryViewFields = Arrays.asList("facilityType", "tenor", "pricing", "creditSpreadAdj", "upfrontFees", "lgdOption", "patronagePayingFlag");
            List<String> participantFullViewFields = Arrays.asList("facilityPurpose", "purposeDetail", "dayCount", "guarInvFlag", "regulatoryLoanType",
                    "farmCreditType", "revolverUtil", "unusedFees", "amortization", "maturityDate", "renewalDate");

            for (String key : participantSummaryViewFields) {
                if (facilityMap.containsKey(key)) {
                    templateDataMap.put("summary", true);
                    break;
                }
            }

            for (String key : participantFullViewFields) {
                if (facilityMap.containsKey(key)) {
                    templateDataMap.put("full", true);
                    break;
                }
            }


        } else {


            HashMap<String, Object> dealMap = (HashMap<String, Object>) templateDataMap.get("dealMap");

            List<String> participantSummaryViewFields = Arrays.asList("description", "dealAmount",
                    "farmCreditElig", "defaultProbability", "currYearEbita");
            List<String> participantFullViewFields = Arrays.asList("businessAge", "borrowerDesc");


            for (String key : participantSummaryViewFields) {
                if (dealMap.containsKey(key)) {
                    templateDataMap.put("summary", true);
                    if (key.equals("description") || key.equals("dealAmount")) {
                        templateDataMap.put("category", "Deal Overview");
                    } else if (key.equals("farmCreditElig")) {
                        templateDataMap.put("category", "Borrower");
                    } else { // defaultProbability, currYearEbita
                        templateDataMap.put("category", "Financial Metrics");
                    }
                    break;
                }
            }

            for (String key : participantFullViewFields) {
                if (dealMap.containsKey(key)) {
                    templateDataMap.put("full", true);
                    if (key.equals("businessAge") || key.equals("borrowerDesc")) {
                        templateDataMap.put("category", "Borrower");
                    } else { // lgdOption
                        templateDataMap.put("category", "Financial Metrics");
                    }
                    break;
                }
            }
        }

        Map<String, Object> recipientMap = new HashMap<>();

        // Notification goes to all deal team members of the lead institution
        recipientMap.put("lead", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), ORIGINATOR)
                .stream()
                .map(dm -> dm.getUser())
                .collect(Collectors.toList()));

        // TODO: Update filters as part of LM-2493
        if ((Boolean) templateDataMap.get("summary")) {
            recipientMap.put("participant", dealMemberDao.findAllByDealUidAndMemberTypeCode(templateDataMap.get("dealUid").toString(), PARTICIPANT)
                    .stream()
                    .filter(dm -> dm.getEventOriginationParticipant().getDeclinedFlag().equals("N"))
                    .filter(dm -> dm.getEventOriginationParticipant().getRemovedFlag().equals("N"))
                    .map(dm -> dm.getUser())
                    .collect(Collectors.toList()));
        }
        // Deal has been launched, participant has full deal access, and the updated field is full
        else if ((Boolean) templateDataMap.get("isDealLaunched") && (Boolean) templateDataMap.get("full")) {
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
