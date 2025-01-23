package com.westmonroe.loansyndication.utils;

public class TestConstants {

    private TestConstants() {
        throw new IllegalStateException("The class cannot be instantiated. This class is only for test constants.");
    }

    /*
     *  Test UUIDs
     */

    public static final String TEST_DUMMY_UUID = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
    public static final String TEST_DEAL_UUID_1 = "6f865256-e16e-441a-b495-bfb6ea856623";        // Texas Dairy Farm
    public static final String TEST_DEAL_UUID_2 = "3eabdf8a-f591-43a7-9f7a-10af85f0e707";        // Kentucky Processing Plant
    public static final String TEST_EVENT_UUID_1 = "6f865256-e16e-441a-b495-bfb6ea856624";       // Origination Event for Texas Dairy Farm
    public static final String TEST_EVENT_UUID_2 = "3eabdf8a-f591-43a7-9f7a-10af85f0e708";       // Origination Event for Kentucky Processing Plant
    public static final String TEST_INSTITUTION_UUID_1 = "def408de-1472-4903-ab01-e0e528138e77"; // Farm Credit Bank of Texas
    public static final String TEST_INSTITUTION_UUID_2 = "df52a3a8-131c-4b3b-9eec-b7bd6f320270"; // AgFirst Farm Credit Bank
    public static final String TEST_INSTITUTION_UUID_3 = "716ec19b-1af1-44ba-8167-ef1fc3ddc75e"; // Horizon Farm Credit, ACA
    public static final String TEST_INSTITUTION_UUID_4 = "383ebe76-fba7-4563-befe-5dde11431c09"; // River Valley AgCredit, ACA

    public static final String TEST_USER_UUID_1 = "4d7ac607-9c66-41bc-bf6c-1458d192ff75";       // Lenor Anderson (Role: SUPER_ADM, ACCESS_ALL_INST_DEALS)
    public static final String TEST_USER_UUID_2 = "16f0545d-f1ce-4c2d-be90-2d8bef9af8fe";       // Annie Palinto (Role: ACCESS_ALL_INST_DEALS, EDIT_DEAL_INFO) & Deal Team Member
    public static final String TEST_USER_UUID_3 = "93ed81d3-5c49-4ccd-8e99-37336af26da6";       // Leon T. (Tim) Amerson (Role: ACCESS_ALL_INST_DEALS, MNG_PART_INST, MNG_DEAL_FILES) & Deal Team Member
    public static final String TEST_USER_UUID_4 = "503d6ef2-8197-4eda-ba1f-267bf00e5bc1";       // Georgia Washington (Role: ACCESS_ALL_INST_DEALS)
    public static final String TEST_USER_UUID_5 = "59f4ebaf-e7a0-457b-acbf-1cdc4fc6b6d0";       // Thomas H.', 'Truitt (Role: ACCESS_ALL_INST_DEALS)
    public static final String TEST_USER_UUID_6 = "b9f32681-598f-4ddf-ba2b-33b51ae36676";       // Lambda Service (Role: APP_SERVICE)

    public static final String TEST_USER_EMAIL_1 = "Lenor.Anderson@test.com";                   // Lenor Anderson (Role: SUPER_ADM, ACCESS_ALL_INST_DEALS)
    public static final String TEST_USER_EMAIL_2 = "Annie.Palinto@test.com";                    // Annie Palinto (Role: ACCESS_ALL_INST_DEALS, EDIT_DEAL_INFO) & Deal Team Member
    public static final String TEST_USER_EMAIL_3 = "Leon.Amerson@test.com";                     // Leon T. (Tim) Amerson (Role: ACCESS_ALL_INST_DEALS, MNG_PART_INST, MNG_DEAL_FILES) & Deal Team Member
    public static final String TEST_USER_EMAIL_4 = "Georgia.Washington@test.com";               // Georgia Washington (Role: ACCESS_ALL_INST_DEALS)
    public static final String TEST_USER_EMAIL_5 = "Thomas.Truitt@test.com";                    // Thomas H.', 'Truitt (Role: ACCESS_ALL_INST_DEALS)
    public static final String TEST_USER_EMAIL_6 = "lambda-service-account";                    // Lambda Service (Role: APP_SERVICE)

    public static final String TEST_NEW_USER_UUID  = "31c2c841-7dd5-49e3-82a6-69be71995e41";
    public static final String TEST_NEW_USER_EMAIL = "new.user@westmonroe.com";

}