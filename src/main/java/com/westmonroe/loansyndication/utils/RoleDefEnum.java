package com.westmonroe.loansyndication.utils;

public enum RoleDefEnum {
    SUPER_ADMIN(1, "Super Admin", "Full administrative capabilities for application."),
    ACCESS_ALL_INST_DEALS(2, "Access All Institution Deals", "View all deals my institution has been invited."),
    MNG_INST_USR(3, "Institution User Manager", "Add, edit, and remove users and their permissions for an institution."),
    MNG_DEAL_MEMBERS(4, "Deal User Manager", "Add and remove my institution's team members included on a deal."),
    NDA_MGR(5, "NDA Manager", "Add, edit, and sign non-disclosure agreements (NDAs) with institutions."),
    BILLING_MGR(6, "Billing Manager", "Update billing and payment details for the marketplace."),
    EDIT_DEAL_INFO(7, "Deal Manager", "Edit a deal, manage files and the fields within a deal."),
    MNG_PART_INST(8, "Deal Participant Manager", "Invite institutions to a deal, confirm their allocation in the deal, and send virtual letters of commitment to participants."),
    COMM_PART_USR(9, "Participant Communicator", "Communicate with participating institutions. Send updates to all participants and direct messages to participant institutions."),
    MNG_PART(10, "Responding User", "Respond to deal invitations and request data room access."),
    COMMIT_USR(11, "Commitment User", "Commit to deals. Sign a virtual letter of commitment for a deal."),
    COMM_ORIG_USR(12, "Originator Communicator", "Communicate with the originating institution. Send and receive direct messages with the originator."),
    MNG_DEAL_FILES(13, "File Manager", "Manage deal files. Upload, rename, replace, and remove files."),
    RECV_ALL_INST_INVS(14, "Deal Invitation Recipient", "Default contact for deal invitation, receives email notification for institution when deal invitation is sent."),
    APP_SERVICE(15, "Application Service", "Role for application to application communications."),
    MNG_INST(16, "Institution Manager", "Ability to edit institution details.");

    private final long id;
    private final String code;
    private final String description;
    RoleDefEnum(long id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}