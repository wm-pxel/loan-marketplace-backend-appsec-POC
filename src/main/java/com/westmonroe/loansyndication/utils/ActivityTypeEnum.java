package com.westmonroe.loansyndication.utils;

public enum ActivityTypeEnum {

    TEAM_MEMBER_ADDED(1, "Team Member Added"),      // Keep this special case for lead at #1
    TEAM_MEMBER_REMOVED(2, "Team Member Removed"),  // Keep this special case for lead at #2
    DEAL_CREATED(3, "Deal Created"),
    DEAL_INFO_UPDATED(4, "Deal Info Updated"),
    FILE_UPLOADED(5, "File Uploaded"),
    FILE_RENAMED(6, "File Renamed"),
    FILE_REMOVED(7, "File Removed"),
    INVITE_SENT(8, "Invite Sent"),
    DEAL_INTEREST(9, "Deal Interest"),
    DEAL_LAUNCHED(11, "Deal Launched"),
    COMMITMENTS_SENT(12, "Commitments Sent"),
    ALLOCATIONS_SENT(13, "Allocations Sent"),
    DEAL_DECLINED(14, "Deal Declined"),
    PARTICIPANT_REMOVED(15, "Participant Removed"),
    DEAL_DATES_UPDATED(16, "Deal Dates Updated"),
    PART_CERT_SENT(17, "Participation Certificate Sent"),
    SIGNED_PC_SENT(18, "Signed Participation Certificate Sent"),
    DRAFT_LOAN_DOCS_UPLOADED(19, "Draft Loan Documents Uploaded"),
    FINAL_LOAN_DOCS_UPLOADED(20, "Final Loan Documents Uploaded"),
    CLOSING_MEMO_UPLOADED(21, "Closing Memo Uploaded"),
    DEAL_CLOSED(22, "Deal Closed"),
    INVITE_AMOUNT_SET(23, "Invite Amount Set"),
    COMMITMENT_AMOUNT_SET(24, "Commitment Amount Set"),
    ALLOCATION_AMOUNT_SET(25, "Allocation Amount Set");

    private final long id;
    private final String name;

    private ActivityTypeEnum(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static ActivityTypeEnum valueOfId(long id) {

        for ( ActivityTypeEnum ate : values() ) {
            if ( ate.id == id ) {
                return ate;
            }
        }

        return null;
    }

    public static ActivityTypeEnum valueOfName(String name) {

        for ( ActivityTypeEnum ate : values() ) {
            if ( ate.name.equals(name) ) {
                return ate;
            }
        }

        return null;
    }

}