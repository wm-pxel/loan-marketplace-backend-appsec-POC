------------------------------------
--    Create Test Institutions    --
------------------------------------
INSERT INTO INSTITUTION_INFO
     ( INSTITUTION_UUID, INSTITUTION_NAME, OWNER_NAME, PERMISSION_SET_DESC, ACTIVE_IND )
       VALUES
     ( 'def408de-1472-4903-ab01-e0e528138e77', 'Farm Credit Bank of Texas', 'Amie Pala', 'Lead and Participant', 'Y' ),
     ( 'df52a3a8-131c-4b3b-9eec-b7bd6f320270', 'AgFirst Farm Credit Bank', 'Leon T. (Tim) Amerson', 'Lead and Participant', 'Y' ),
     ( '716ec19b-1af1-44ba-8167-ef1fc3ddc75e', 'Horizon Farm Credit, ACA', 'Thomas H. Truitt', 'Lead and Participant', 'Y' ),
     ( '383ebe76-fba7-4563-befe-5dde11431c09', 'River Valley AgCredit, ACA', 'Kyle M Yancey', 'Lead and Participant', 'Y' );

------------------------------------
--       Create Test Users        --
------------------------------------
INSERT INTO USER_INFO
     ( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND )
       VALUES
     ( '6180dcb8-d3ef-4e36-b8b0-4b9bdc3d3d2a', 1, 'Amie', 'Pala', 'lm.amie.pala@outlook.com', '1234567890', 'Y' ),
     ( '16f0545d-f1ce-4c2d-be90-2d8bef9af8fe', 1, 'Annie', 'Palinto', 'Annie.Palinto@test.com', '1234567890', 'Y' ),
     ( '93ed81d3-5c49-4ccd-8e99-37336af26da6', 2, 'Leon T. (Tim)', 'Amerson', 'Leon.Amerson@test.com', '1234567890', 'Y' ),
     ( '4d7ac607-9c66-41bc-bf6c-1458d192ff75', 2, 'Lenor', 'Anderson', 'Lenor.Anderson@test.com', '1234567890', 'Y' ),
     ( '3aa836ce-5c8d-466c-b644-d7c6a9f9db34', 2, 'Chris', 'Lender', 'Chris.Lender@test.com', '1234567890', 'Y' ),
     ( '429a53d3-17af-4be1-bb82-44f48ae1e74e', 2, 'Frank', 'Bank', 'Frank.Bank@test.com', '1234567890', 'Y' ),
     ( '59f4ebaf-e7a0-457b-acbf-1cdc4fc6b6d0', 3, 'Thomas H.', 'Truitt', 'Thomas.Truitt@test.com', '1234567890', 'Y' ),
     ( 'c20b4a1e-fbad-410e-a085-d21f1d95ead2', 3, 'Dana', 'Teller', 'Dana.Teller@test.com', '1234567890', 'Y' ),
     ( '0a5a099b-ee01-4e34-81a5-91421bb1a104', 3, 'Benjamin', 'Bucks', 'Benjamin.Bucks@test.com', '1234567890', 'Y' ),
     ( '503d6ef2-8197-4eda-ba1f-267bf00e5bc1', 4, 'Georgia', 'Washington', 'Georgia.Washington@test.com', '1234567890', 'Y' ),
     ( 'b4549afa-2261-48bb-8bdc-321d5c570523', 4, 'Kyle M', 'Yancey', 'Kyle.Yancey@test.com', '1234567890', 'Y' ),
     ( 'b9f32681-598f-4ddf-ba2b-33b51ae36676', NULL, 'Lambda', 'Service Account', 'lambda-service-account', '1234567890', 'Y' );

------------------------------------
--     Create Test User Roles     --
------------------------------------
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
       VALUES
     ( 1, 1 ),
     ( 1, 2 ),
     ( 1, 14),
     ( 2, 2 ),
     ( 2, 3 ),
     ( 2, 4 ),
     ( 2, 7 ),
     ( 2, 8 ),
     ( 3, 2 ),
     ( 3, 8 ),
     ( 3, 13 ),
     ( 4, 1 ),
     ( 4, 2 ),
     ( 4, 8 ),
     ( 4, 14),
     ( 5, 2 ),
     ( 6, 2 ),
     ( 7, 2 ),
     ( 8, 2 ),
     ( 9, 2 ),
     ( 9, 7 ),
     ( 9, 8 ),
     ( 9, 14),
     ( 10, 2 ),
     ( 11, 2 ),
     ( 11, 4 ),
     ( 11, 14 ),
     ( 12, 15 );

----------------------------------------
--     Create Test Initial Lenders    --
----------------------------------------
INSERT INTO INITIAL_LENDER_INFO
     ( LENDER_NAME, ACTIVE_IND )
       VALUES
     ( 'JPMorgan Chase', 'Y' ),
     ( 'Bank of America', 'Y' ),
     ( 'Wells Fargo', 'Y' ),
     ( 'Goldman Sachs', 'Y' ),
     ( 'Morgan Stanley', 'N'),
     ( 'U.S. Bancorp', 'Y' ),
     ( 'Truist Financial', 'N' ),
     ( 'Capital One', 'Y' ),
     ( 'BMO USA', 'Y' ),
     ( 'HSBC Bank USA', 'Y' );

------------------------------------
--       Create Test Deals        --
------------------------------------
INSERT INTO DEAL_INFO
     ( DEAL_UUID, DEAL_EXTERNAL_UUID, DEAL_NAME, DEAL_INDUSTRY_ID, ORIGINATOR_ID, INITIAL_LENDER_IND, INITIAL_LENDER_ID
     , STAGE_ID, DEAL_STRUCTURE_ID, DEAL_TYPE_DESC, DEAL_DESC, DEAL_AMT, APPLICANT_EXTERNAL_UUID, BORROWER_NAME
     , BORROWER_DESC, BORROWER_CITY_NAME, BORROWER_STATE_CD, BORROWER_COUNTY_NAME, FARM_CR_ELIG_ID, TAX_ID_NBR, BORROWER_INDUSTRY_CD
     , BUSINESS_AGE_QTY, DEFAULT_PROB_PCT, DEBT_SRV_COV_RATIO, CY_EBITA_AMT, PATR_ELIG_IND, CREATED_BY_ID, UPDATED_BY_ID, ACTIVE_IND )
       VALUES
     ( '6f865256-e16e-441a-b495-bfb6ea856623', 'd1893fb4-09bf-4b8b-8c8a-81f1e8e809f3', 'Texas Dairy Farm', 16, 1, 'Y', 3, 1, 4, 'New'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 10000000.11, 'fb4d1a87-c243-4fb5-b8e2-bf6843e01028', 'Manny Bags'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 'Lincoln', 'NE', 'Lincoln County', 1, '91-1111111', '111191', 5, 5, 1.1, 5500000.00, 'Y', 1, 1, 'Y' ),
     ( '3eabdf8a-f591-43a7-9f7a-10af85f0e707', 'b86517b4-0693-4ec6-b880-06de4c0507f3', 'Kentucky Processing Plant', 16, 2, 'N', NULL, 1, 4, 'Renewal'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 20000000.22, '5ff602a7-fb79-4e64-896d-480399022d35', 'Lotta Cash'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 'Kansas City', 'MO', 'Kansas County', 3, '92-2222222', '111910', 35, 14, 1.2, 13500000.00, 'N', 3, 3, 'Y' ),
     ( '3add224e-0c1a-46bc-95d1-533fb873226a', '66b64292-8d36-41c3-a1a2-c6b87a4b2490', 'Florida State Feed', 16, 3, 'Y', 5, 3, 4, 'Modification'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 30000000.33, '18b727a5-3d31-4484-ba53-067adaede1e3', 'Xavier Walter Change'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 'Mount Vernon', 'IL', 'Jefferson County', 1, '93-3333333', '111998', 2, 12, 1.3, 7550000.00, 'Y', 7, 7, 'Y' ),
     ( '6416100a-ea7e-45a9-b1d3-a8a248e82262', '0e364225-35bb-4345-a3a7-4dd3f65a4684', 'Eggland Best of Illinois', 16, 4, 'N', NULL, 1, 4, 'New'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 40000000.44, 'e2229dfe-074a-4005-807c-8315cfb097ee', 'Connie Yu'
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 'Pensacola', 'FL', 'Seminole', 3, '94-4444444', '112320', 12, 2, 1.4, 11300000.00, 'N', 10, 10, 'Y' );

------------------------------------
--       Create Test Events       --
------------------------------------
INSERT INTO EVENT_INFO
     ( EVENT_UUID, EVENT_EXTERNAL_UUID, DEAL_ID, EVENT_NAME, EVENT_TYPE_ID, STAGE_ID, PROJ_LAUNCH_DATE, COMMITMENT_DATE
     , COMMENTS_DUE_DATE, EFFECTIVE_DATE, PROJ_CLOSE_DATE, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( '6f865256-e16e-441a-b495-bfb6ea856624', 'd1893fb4-09bf-4b8b-8c8a-81f1e8e809f4', 1, 'Origination', 1, 1
     , '2023-09-15', '2023-10-15', '2023-11-30', '2023-12-02', '2023-12-01', 1, 1 ),
     ( '3eabdf8a-f591-43a7-9f7a-10af85f0e708', 'b86517b4-0693-4ec6-b880-06de4c0507f4', 2, 'Origination', 1, 1
     , '2023-09-15', '2023-10-17', '2023-11-30', '2023-12-03', '2023-12-02', 3, 3 ),
     ( '3add224e-0c1a-46bc-95d1-533fb873226b', '66b64292-8d36-41c3-a1a2-c6b87a4b2491', 3, 'Origination', 1, 1
     , '2023-09-15', '2023-10-18', '2023-11-30', '2023-12-04', '2023-12-03', 7, 7 ),
     ( '6416100a-ea7e-45a9-b1d3-a8a248e82263', '0e364225-35bb-4345-a3a7-4dd3f65a4685', 4, 'Origination', 1, 1
     , '2023-09-15', '2023-10-19', '2023-11-30', '2023-11-05', '2023-11-04', 10, 10 );

----------------------------------------
--      Create Test Deal Members      --
----------------------------------------
INSERT INTO DEAL_MEMBER
     ( DEAL_ID, USER_ID, MEMBER_TYPE_CD, CREATED_BY_ID )
       VALUES
     ( 1, 1, 'O', 1 ),
     ( 2, 3, 'O', 3 ),
     ( 2, 4, 'O', 3 ),
     ( 3, 7, 'O', 7 ),
     ( 3, 8, 'O', 7 ),
     ( 4, 10, 'O', 10 ),
     ( 1, 4, 'P', 1 ),
     ( 1, 5, 'P', 1 ),
     ( 1, 7, 'P', 1 ),
     ( 1, 9, 'P', 1 ),
     ( 3, 2, 'P', 7 ),
     ( 3, 3, 'P', 7 ),
     ( 3, 4, 'P', 7 ),
     ( 3, 5, 'P', 7 ),
     ( 3, 6, 'P', 7 ),
     ( 3, 10, 'P', 7 ),
     ( 4, 1, 'P', 10 ),
     ( 4, 3, 'P', 10 ),
     ( 4, 5, 'P', 10 ),
     ( 4, 7, 'P', 10 ),
     ( 4, 8, 'P', 10 );

----------------------------------------
--     Create Test Deal Covenants     --
----------------------------------------
INSERT INTO DEAL_COVENANT
     ( COVENANT_EXTERNAL_UUID, DEAL_ID, ENTITY_NAME, CATEGORY_NAME, COVENANT_TYPE_DESC, FREQUENCY_DESC, NEXT_EVAL_DATE
     , EFFECTIVE_DATE, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 'fe36d315-1509-4c0e-96c6-60585514dc89', 1, 'Anita Mooney', 'Collateral', 'Real Estate Taxes', 'Annually', '2023-12-12', '2023-11-01', 1, 1 ),
     ( '02b0924a-916a-411a-b3cd-45ac29e54e15', 1, '', 'Financial Indicators', 'Accounts Payable', 'Quarterly', NULL, NULL, 1, 1 ),
     ( '187a9430-7270-4847-be74-0854d71fab62', 1, 'Carl Knox', 'Financial Statement Requirements', 'Business Financial Statement', 'Semi-Annually', '2023-11-11', '2023-11-11', 1, 1 ),
     ( 'f75bbfaf-b788-470c-a6f1-f73690e7d9b2', 1, 'Amanda Huggins', 'Term Covenants', 'Term Covenants', 'Monthly', '2024-04-04', '2024-01-01', 1, 1 ),
     ( 'ca541857-b25d-4022-832e-9eb4fc2688ba', 2, '', 'Default Covenants', 'Maintain Accounts', 'Quarterly', NULL, NULL, 3, 3 ),
     ( '2380f37a-6d5e-4cf9-9494-9f57f25cf4f4', 2, 'Candice Savers', 'Collateral', 'Insurance', 'Annually', '2024-03-07', '2024-02-28', 3, 3 ),
     ( '9bc97e10-7d66-4975-9973-5ab54b7c09d2', 2, 'Henry Owens', 'Financial Indicators', 'Debt Service Coverage of Borrower', 'Semi-Annually', '2024-02-17', '2024-02-01', 3, 3 ),
     ( '60852a11-ef0a-4901-b2f3-a35d3cc5429b', 3, '', 'Collateral', 'Real Estate Taxes', 'Annually', '2024-12-15', '2024-12-01', 7, 7 ),
     ( '9ee6cf26-8cf6-4b53-8dee-32f64005e84e', 3, 'Nicholas Billings', 'Default Covenants', 'Collateral Insurance', 'Every 2 Months', NULL, NULL, 7, 7 ),
     ( 'dc4a4603-0c0f-4154-9a76-67aa8b3b2506', 3, 'Veronika Burroughs', 'Financial Indicators', 'Minimum Current Ratio', 'Quarterly', '2024-03-12', '2024-03-01', 7, 7 ),
     ( '4b653ee7-619b-428c-9797-9ebeb0932f23', 3, 'Jill Jillian', 'Financial Statement Requirements', 'Personal Financial Statement', 'Semi-Annually', '2024-02-02', '2024-01-01', 7, 7 ),
     ( '32045b52-5aaa-449f-9da7-d60b6ce7f1e2', 4, '', 'Default Covenants', 'Default Covenants', 'Annually', '2024-02-12', '2024-02-01', 10, 10 ),
     ( 'dfb3ebc1-b763-4508-bb7f-98dfb27ba539', 4, 'Jack Jillian', 'Collateral', 'Insurance', 'Every 2 Months', NULL, NULL, 10, 10 ),
     ( 'b0fd32f7-b8f6-4d02-abbd-88c018ab3c9c', 4, 'Manny Nickels', 'Term Covenants', 'Term Covenants', 'Semi-Annually', '2024-01-20', '2024-01-01', 10, 10 ),
     ( '504196ac-aeb6-49c0-97ad-df0e11049a84', 4, 'Penelope Quarters', 'Financial Statement Requirements', 'Financial Statement Requirements', 'Quarterly', '2023-12-15', '2023-12-01', 10, 10 );

INSERT INTO DEAL_DOCUMENT
     ( DEAL_ID, DISPLAY_NAME, DOCUMENT_NAME, DOCUMENT_CATEGORY_ID, DOCUMENT_TYPE, DOCUMENT_DESC, SOURCE_CD, DOCUMENT_EXTERNAL_UUID
     , CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 2, '2021 Financials - Smith Peanuts.xlsx', '1707748739815.xlsx', 3, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'This is a test document.', 'M', '2b440773-7166-4bb8-a9f7-e725fc176ab9', 4, 4 ),
     ( 2, '2022 Financials - Smith Peanuts.xlsx', '1707748813048.xlsx', 3, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'This is a test document.', 'M', 'a3b1147b-5d5b-4182-8a20-47f74b402a68', 4, 4 ),
     ( 2, '2023 Financials - Smith Peanuts.xlsx', '1707748890725.xlsx', 3, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'This is a test document.', 'M', '3e888695-4c1e-40f0-9832-19d2e587e716', 4, 4 ),
     ( 2, 'Smith Peanuts Credit Report.pdf', '1707748986902.pdf', 2, 'application/pdf', 'This is a test document.', 'M', 'e3b573b9-2011-463f-8316-729f0e919837', 4, 4 ),
     ( 2, 'Smith Peanuts CIM.docx', '1707749030000.docx', 2, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'This is a test document.', 'M', '5fef982d-8ee7-4a40-a79c-8496c031e90f', 4, 4 ),
     ( 2, 'Smith Peanuts Background.pptx', '1707749088042.pptx', 2, 'application/vnd.openxmlformats-officedocument.presentationml.presentation', 'This is a test document.', 'M', '01ea02c2-3a56-4409-b218-309df6e62412', 4, 4 ),
     ( 2, '2023 Roastery Assessment - Smith Peanuts.pdf', '1707749163603.pdf', 1, 'application/pdf', 'This is a test document.', 'M', 'eb8cae25-b042-4685-840a-2be198c60abd', 4, 4 ),
     ( 2, '2023 400 Peanut Road Land Value - Smith Peanuts.pdf', '1707749206234.pdf', 1, 'application/pdf', 'This is a test document.', 'M', '952505d9-325c-473a-9ade-846bdfe26ecd', 4, 4 ),
     ( 2, 'Roaster Image.png', '1707749262144.png', 1, 'image/png', 'This is a test document.', 'M', 'af895d93-91eb-4322-becb-21d8ad62cf77', 4, 4 ),
     ( 2, '400 Peanut Road.jpeg', '1707749297027.jpeg', 1, 'image/jpeg', 'This is a test document.', 'M', 'a4355bb0-0872-41ab-9cb8-8f671fb50748', 4, 4 );

----------------------------------------
--     Create Test Deal Facilities    --
----------------------------------------
INSERT INTO DEAL_FACILITY
     ( FACILITY_EXTERNAL_UUID, DEAL_ID, FACILITY_NAME, FACILITY_AMT, FACILITY_TYPE_ID, TENOR_YRS_QTY, COLLATERAL_ID
     , PRICING_DESC, CSA_DESC, FACILITY_PURPOSE_ID, PURPOSE_TEXT, DAY_COUNT_ID, GUAR_INV_IND, FARM_CREDIT_TYPE_NAME
     , REV_UTIL_PCT, UPFRONT_FEES_DESC, UNUSED_FEES_DESC, AMORTIZATION_DESC, LGD_OPTION, PATRONAGE_PAYING_IND
     , CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 'e63b2463-b190-4740-9c17-e6054ce34871', 1, 'Facility A', 10000011.11, 6, 10, 26, 'SOFR + 140.0bps', '24%', 11
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 18, 'Y', 'PCA', NULL, '20.25 bps on the final allocation', '15 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 1, 1 ),
     ( 'e0b49e3f-cba6-4857-ba1d-f534be3dfe4d', 1, 'Facility B', 12000022.22, 10, 11, 27, 'SOFR + 140.0bps', '23%', 12
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 18, 'N', 'FLCA', NULL, 'For commitments greater than or equal to 20mm, 17.5 bps on the final allocation; For commitments less than 20mm, 15 bps on the final allocation'
     , '20 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'B', 'N', 1, 1 ),
     ( '89008bc2-c5f3-407a-baa1-2638c761b19c', 1, 'Facility C', 3000033.33, 6, 12, 27, 'SOFR + 140.0bps', '22%', 13
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 18, 'N', 'PCA', NULL, 'For allocations greater than or equal to 30mm, 15.25 bps on the final allocation; For allocations less than 30mm, 5 bps on the final allocation'
     , '25 bps', NULL, 'C', 'N', 1, 1 ),
     ( '3916411e-3161-4530-9370-df696d769559', 1, 'Facility D', 4000044.44, 10, 13, 28, 'SOFR + 140.0bps', '21%', 11
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 19, 'Y', 'FLCA', NULL, 'For allocations greater than or equal to 40mm, 10 bps on the final allocation; For allocations less than 40mm, 5 bps on the final allocation'
     , '17.5 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'D', 'N', 1, 1 ),
     ( 'e5adadf5-01b7-4279-ab09-9bf35078705a', 2, 'Facility A', 5000055.55, 6, 14, 26, 'SOFR + 140.0bps', '20%', 12
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 19, 'N', 'PCA', NULL, '20.25 bps on the final allocation', '15 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 3, 3 ),
     ( '0ce24020-4720-4827-82c5-c23c0fa1bd0c', 2, 'Facility B', 6000066.66, 10, 15, 26, 'SOFR + 140.0bps', '19%', 13
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 19, 'Y', 'PCA', NULL, 'For commitments greater than or equal to 20mm, 17.5 bps on the final allocation; For commitments less than 20mm, 15 bps on the final allocation'
     , '20 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'B', 'N', 3, 3 ),
     ( '322fb5bc-1fa6-4cc4-bec1-9787d5554e2d', 2, 'Facility C', 17000077.77, 6, 16, 27, 'SOFR + 140.0bps', '18%', 11
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 20, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 30mm, 15.25 bps on the final allocation; For allocations less than 30mm, 5 bps on the final allocation'
     , '25 bps', NULL, 'C', 'N', 3, 3 ),
     ( '4a4b9feb-b603-4237-ab24-d867c1a2a972', 3, 'Facility A', 8000088.88, 10, 17, 27, 'SOFR + 140.0bps', '17%', 14
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 20, 'Y', 'PCA', NULL, '20.25 bps on the final allocation', '15 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 7, 7 ),
     ( '67e19953-c13a-45eb-b1c9-dd7f4804030f', 3, 'Facility B', 9000099.99, 6, 18, 26, 'SOFR + 140.0bps', '16%', 12
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 20, 'N', 'PCA', NULL, 'For commitments greater than or equal to 20mm, 17.5 bps on the final allocation; For commitments less than 20mm, 15 bps on the final allocation'
     , '20 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'B', 'N', 7, 7 ),
     ( '3debd780-e56e-4e34-96f1-14dd3ddb219a', 3, 'Facility C', 10000010.01, 10, 19, 26, 'SOFR + 140.0bps', '15%', 11
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 20, 'Y', 'FLCA', NULL, 'For allocations greater than or equal to 30mm, 15.25 bps on the final allocation; For allocations less than 30mm, 5 bps on the final allocation'
     , '25 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'C', 'N', 7, 7 ),
     ( '4ad5790a-0f2e-4d0b-9b15-7d83f1e91afe', 3, 'Facility D', 11000011.11, 6, 20, 27, 'SOFR + 140.0bps', '14%', 13
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 21, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 40mm, 10 bps on the final allocation; For allocations less than 40mm, 5 bps on the final allocation'
     , '17.5 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'D', 'N', 7, 7 ),
     ( '725f0496-0096-47b0-916c-45cd84b51fd8', 4, 'Facility A', 12000012.12, 10, 21, 28, 'SOFR + 140.0bps', '13%', 14
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 21, 'Y', 'PCA', NULL, '20.25 bps on the final allocation', '15 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 10, 10 ),
     ( '6f505287-41d6-4f9f-971a-735aa14812ba', 4, 'Facility B', 13000013.13, 6, 22, 26, 'SOFR + 140.0bps', '12%', 11
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 21, 'N', 'PCA', NULL, 'For commitments greater than or equal to 20mm, 17.5 bps on the final allocation; For commitments less than 20mm, 15 bps on the final allocation'
     , '20 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'B', 'N', 10, 10 ),
     ( '457b76ec-728e-4f45-9618-af4c81661cb6', 4, 'Facility C', 14000014.14, 10, 23, 28, 'SOFR + 140.0bps', '11%', 12
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 22, 'Y', 'PCA', NULL, 'For allocations greater than or equal to 30mm, 15.25 bps on the final allocation; For allocations less than 30mm, 5 bps on the final allocation'
     , '25 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'C', 'N', 10, 10 ),
     ( '1f9344db-aa7d-4d85-a39c-ff950aeed63c', 4, 'Facility D', 15000015.15, 6, 24, 27, 'SOFR + 140.0bps', '10%', 13
     , 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
     , 22, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 40mm, 10 bps on the final allocation; For allocations less than 40mm, 5 bps on the final allocation'
     , '17.5 bps', NULL, 'D', 'N', 10, 10 );

---------------------------------------------
-- Update Last Facility Number in the Deal --
---------------------------------------------
UPDATE DEAL_INFO DI
   SET LAST_FACILITY_NBR = ( SELECT COUNT(DEAL_FACILITY_ID)
                               FROM DEAL_FACILITY
                              WHERE DEAL_ID = DI.DEAL_ID )
 WHERE DEAL_ID BETWEEN 1 AND 4;

----------------------------------------
--   Create Test Event Participants   --
----------------------------------------
INSERT INTO EVENT_PARTICIPANT
     ( EVENT_ID, PARTICIPANT_ID, PARTICIPANT_STEP_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 1, 2, 1, 1, 1 ),
     ( 1, 3, 1, 1, 1 ),
     ( 3, 1, 1, 7, 7 ),
     ( 3, 2, 1, 7, 7 ),
     ( 3, 4, 1, 7, 7 ),
     ( 4, 1, 2, 10, 10 ),
     ( 4, 2, 1, 10, 10 ),
     ( 4, 3, 1, 10, 10 );

-----------------------------------------------
-- Create Test Event Participant Origination --
-----------------------------------------------
INSERT INTO EVENT_PARTICIPANT_ORIGINATION
     ( EVENT_PARTICIPANT_ID, INVITE_RECIPIENT_ID, MESSAGE_DESC )
       VALUES
     ( 1, 3, NULL ),
     ( 2, 7, NULL ),
     ( 3, 1, NULL ),
     ( 4, 3, NULL ),
     ( 5, 10, NULL ),
     ( 6, 2, 'No message' ),
     ( 7, 3, NULL ),
     ( 8, 7, NULL );

----------------------------------------------
--    Create Test Event Deal Facilities     --
----------------------------------------------
INSERT INTO EVENT_DEAL_FACILITY
     ( EVENT_ID, DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 1, 1, 1, 1 ),
     ( 1, 2, 1, 1 ),
     ( 1, 3, 1, 1 ),
     ( 1, 4, 1, 1 ),
     ( 2, 5, 3, 3 ),
     ( 2, 6, 3, 3 ),
     ( 2, 7, 3, 3 ),
     ( 3, 8, 7, 7 ),
     ( 3, 9, 7, 7 ),
     ( 3, 10, 7, 7 ),
     ( 3, 11, 7, 7 ),
     ( 4, 12, 10, 10 ),
     ( 4, 13, 10, 10 ),
     ( 4, 14, 10, 10 ),
     ( 4, 15, 10, 10 );

----------------------------------------------
-- Create Test Event Participant Facilities --
----------------------------------------------
INSERT INTO EVENT_PARTICIPANT_FACILITY
     ( EVENT_PARTICIPANT_ID, EVENT_DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 1, 1, 1, 1 ),
     ( 1, 2, 1, 1 ),
     ( 1, 3, 1, 1 ),
     ( 1, 4, 1, 1 ),
     ( 2, 1, 1, 1 ),
     ( 2, 2, 1, 1 ),
     ( 2, 3, 1, 1 ),
     ( 2, 4, 1, 1 ),
     ( 3, 8, 7, 7 ),
     ( 3, 9, 7, 7 ),
     ( 3, 10, 7, 7 ),
     ( 3, 11, 7, 7 ),
     ( 4, 8, 7, 7 ),
     ( 4, 9, 7, 7 ),
     ( 4, 10, 7, 7 ),
     ( 4, 11, 7, 7 ),
     ( 5, 8, 7, 7 ),
     ( 5, 9, 7, 7 ),
     ( 5, 10, 7, 7 ),
     ( 5, 11, 7, 7 ),
     ( 6, 12, 10, 10 ),
     ( 6, 13, 10, 10 ),
     ( 6, 14, 10, 10 ),
     ( 6, 15, 10, 10 ),
     ( 7, 12, 10, 10 ),
     ( 7, 13, 10, 10 ),
     ( 7, 14, 10, 10 ),
     ( 7, 15, 10, 10 ),
     ( 8, 12, 10, 10 ),
     ( 8, 13, 10, 10 ),
     ( 8, 14, 10, 10 ),
     ( 8, 15, 10, 10 );

----------------------------------------------
--    Create Test Event Lead Facilities     --
----------------------------------------------
INSERT INTO EVENT_LEAD_FACILITY
     ( EVENT_ID, EVENT_DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 1, 1, 1, 1 ),
     ( 1, 2, 1, 1 ),
     ( 1, 3, 1, 1 ),
     ( 1, 4, 1, 1 ),
     ( 2, 5, 3, 3 ),
     ( 2, 6, 3, 3 ),
     ( 2, 7, 3, 3 ),
     ( 3, 8, 7, 7 ),
     ( 3, 9, 7, 7 ),
     ( 3, 10, 7, 7 ),
     ( 3, 11, 7, 7 ),
     ( 4, 12, 10, 10 ),
     ( 4, 13, 10, 10 ),
     ( 4, 14, 10, 10 ),
     ( 4, 15, 10, 10 );

--------------------------------------------
-- Initialize (Integration) Customer Data --
--------------------------------------------
INSERT INTO CUSTOMER_DATA
      ( MARKETPLACE_JSON, UNSUPPORTED_JSON, CREATED_BY_ID )
       VALUES
      ( '{"deal": {"uid": "6f865256-e16e-441a-b495-bfb6ea856623", "dealExternalId": "d1893fb4-09bf-4b8b-8c8a-81f1e8e809f3"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 1 ),
      ( '{"deal": {"uid": "3eabdf8a-f591-43a7-9f7a-10af85f0e707", "dealExternalId": "b86517b4-0693-4ec6-b880-06de4c0507f3"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 3 ),
      ( '{"deal": {"uid": "3add224e-0c1a-46bc-95d1-533fb873226a", "dealExternalId": "66b64292-8d36-41c3-a1a2-c6b87a4b2490"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 7 ),
      ( '{"deal": {"uid": "6416100a-ea7e-45a9-b1d3-a8a248e82262", "dealExternalId": "0e364225-35bb-4345-a3a7-4dd3f65a4684"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 10 );

-------------------------------------------
-- Create Test Confidentiality Agreement --
-------------------------------------------
INSERT INTO INSTITUTION_CONFIDENTIALITY_AGREEMENT
      ( INSTITUTION_ID, CREATED_BY_ID, CONF_AGRMNT_DESC )
       VALUES
      ( 1, 1, 'This is the confidentiality agreement for Farm Credit Bank of Texas' ),
      ( 2, 3, 'This is the confidentiality agreement for AgFirst Farm Credit Bank'),
      ( 3, 7, 'This is the confidentiality agreement for Horizon Farm Credit, ACA'),
      ( 4, 10, 'This is the confidentiality agreement for River Valley AgCredit, ACA' );

INSERT INTO FEATURE_FLAG_INFO
     ( FEATURE_NAME, DESCRIPTION, IS_ENABLED, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 'Participation V2', 'All UI updates for V2 redesign.', 'N', 1, 1 );