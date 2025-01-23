package com.westmonroe.loansyndication.utils;

public class Constants {

    private Constants() {
        throw new IllegalStateException("The class cannot be instantiated. It is for defining constants.");
    }

    /*
     *  Error Constants
     */
    public static final String ERR_UNAUTH_INSTITUTION_MEMBER = "User is not authorized for the institution.";
    public static final String ERR_UNAUTH_DEAL_MEMBER = "User is not authorized for the deal.";
    public static final String ERR_UNAUTH_DEAL_PARTICIPANT = "User's institution is not a participant on the deal.";
    public static final String ERR_FILE_ALREADY_EXISTS = "The %s document has already been uploaded.";

    /*
     *  Security Constants
     */
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";

    /*
     *  View Type Constants
     */
    public static final String VIEW_TYPE_FULL = "Full";
    public static final String VIEW_TYPE_SUMMARY = "Summary";
    public static final String VIEW_TYPE_NO_ACCESS = "No Access";

    /*
     *  GraphQL Constants
     */
    public static final String GQL_CLASSIFICATION = "classification";
    public static final String GQL_STATUS_CODE = "statusCode";
    public static final String GQL_HTTP_STATUS = "httpStatus";
    public static final String GQL_VALIDATION_ERRORS = "validationErrors";
    public static final String GQL_VIOLATION = "violation";

    /*
     *  Regex Validation Constants
     */
    public static final String REGEX_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    public static final String REGEX_YN = "(Y|N)";
    public static final String REGEX_TRANSFER_TYPE= "(U|D)";
    public static final String REGEX_DEAL_TYPE = "(New|Renewal|Modification)";
    public static final String REGEX_FARM_CREDIT = "(PCA|FLCA)";
    public static final String REGEX_LGD= "(A|B|C|D|E|F)";
    public static final String REGEX_PERMISSION_SET = "(Lead and Participant|Participant Only)";

    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /*
     *  System Source Constants
     */
    public static final String SYSTEM_MARKETPLACE = "M";    // Marketplace
    public static final String SYSTEM_INTEGRATION = "I";    // Integration

}