package com.westmonroe.loansyndication.utils;

public enum EmailTypeEnum {

    TEAM_MEMBER_ADDED(1, "Team Member Added"),
    DEAL_CREATED(2, "Deal Created"),
    DEAL_INFO_UPDATED(3, "Deal Info Updated"),
    FILE_UPLOADED(4, "File Uploaded"),
    INVITE_SENT(5, "Invite Sent"),
    DEAL_INTEREST(6, "Deal Interest"),
    FULL_DEAL_ACCESS(7, "Full Deal Access"),
    DEAL_LAUNCHED(8, "Deal Launched"),
    COMMITMENTS_SENT(9, "Commitments Sent"),
    ALLOCATIONS_SENT(10, "Allocations Sent"),
    DEAL_DECLINED(11, "Deal Declined"),
    PARTICIPANT_REMOVED(12, "Participant Removed"),
    DEAL_DATES_UPDATED(13, "Deal Dates Updated"),
    PART_CERT_SENT(14, "Participation Certificate Sent"),
    SIGNED_PC_SENT(15, "Signed Participation Certificate Sent"),
    DRAFT_LOAN_DOCS_UPLOADED(16, "Draft Loan Documents Uploaded"),
    FINAL_LOAN_DOCS_UPLOADED(17, "Final Loan Documents Uploaded"),
    USER_ACTIVATED(20, "User Activated"),
    INSTITUTION_MEMBER_ADDED(21, "Institution Member Added");

    private final long id;
    private final String name;

    private EmailTypeEnum(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getTemplateName() {
        return this.name.replace(" ", "");
    }


    public static EmailTypeEnum valueOfId(long id) {

        for ( EmailTypeEnum ete : values() ) {
            if ( ete.id == id ) {
                return ete;
            }
        }

        return null;
    }

    public static EmailTypeEnum valueOfName(String name) {

        for ( EmailTypeEnum ete : values() ) {
            if ( ete.getName().equals(name) ) {
                return ete;
            }
        }

        return null;
    }

}