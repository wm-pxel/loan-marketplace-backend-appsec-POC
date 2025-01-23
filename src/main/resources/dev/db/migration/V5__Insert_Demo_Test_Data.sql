/*
 *  This is Devin's test data script.  It is not intended to be used in production.
 */

------------------------------------
--    Create Test Institutions    --
------------------------------------
INSERT INTO INSTITUTION_INFO
     ( INSTITUTION_UUID, INSTITUTION_NAME, OWNER_NAME, PERMISSION_SET_DESC, SSO_IND, ACTIVE_IND )
       VALUES
     ( 'db087f72-29bb-4fc6-8ba8-2f6d9b7aecab', 'United Farm Credit Services', 'Jane Halverson', 'Lead and Participant', 'Y', 'Y' ),
     ( '12693c4c-4286-4ec1-99f5-d5b4ca682d5c', 'Prairie Land Credit Bank', 'Pete Kowalski', 'Lead and Participant', 'N', 'Y' ),
     ( 'e5b36ce1-f617-433a-9d5b-668a07149ff1', 'GreenField Cooperative', 'Lucas Harper', 'Participant Only', 'N', 'Y' ),
     ( 'f7bf2764-b566-4fbc-936a-d320c4b40d12', 'Agricore Alliance', 'Claire Meadows', 'Participant Only', 'N', 'Y' ),
     ( 'cd6e65fa-dd9b-45f1-81cf-187c272476ca', 'CropCrest Association', 'Jackson Fields', 'Participant Only', 'N', 'Y' ),
     ( 'c2187f72-29bb-4fc6-8ba8-2f6d9b7aeb34', 'West Monroe Farm Credit', 'Griffin Kosonocky', 'Participant', 'Y', 'Y' );

------------------------------------
--       Create Test Users        --
------------------------------------
INSERT INTO USER_INFO
     ( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND )
       VALUES
     ( '075ad683-bc65-4edb-9ff7-211d0ea3208a', 5, 'Jane', 'Halverson', 'lm.jane.halverson@outlook.com', '1234567890', 'Y' ),
     ( '87f6624d-6e79-4c23-b45c-8a6b07d69d39', 5, 'Jimmy', 'Dale', 'lm.jimmy.dale@outlook.com', '1234567890', 'Y' ),
     ( '88bc9de5-c55a-4f02-90b5-e1284cf5597c', 5, 'Sarah', 'Trask', 'lm.sarah.trask@outlook.com', '1234567890', 'Y' ),
     ( '5cf4386b-c0b7-427c-a1c3-93c09ca53066', 6, 'Pete', 'Kowalski', 'lmn.pete.kowalski@outlook.com', '1234567890', 'Y' ),             -- Prairie Land Credit Bank
     ( '26bd442e-89ac-424b-a476-c7cc34006333', 6, 'Sai', 'Gao', 'lm.sai.gao@outlook.com', '1234567890', 'Y' ),                          -- Prairie Land Credit Bank
     ( '57322488-7f3d-477d-86c4-5ba5ad520cd6', 6, 'Sally', 'McPherson', 'lm.sally.mcpherson@outlook.com', '1234567890', 'Y' ),          -- Prairie Land Credit Bank
     ( '38203213-455c-47af-95cf-2eed52ad3d84', 7, 'Lucas', 'Harper', 'lm.lucas.harper@outlook.com', '1234567890', 'Y' ),                -- GreenField Cooperative
     ( 'a698fbee-c7d3-4c15-837d-5d65d68a872f', 8, 'Claire', 'Meadows', 'lm.claire.meadows@outlook.com', '1234567890', 'Y' ),            -- Agricore Alliance
     ( '9bd5669d-a1d5-4bdf-a074-4959d0dfd356', 9, 'Jackson', 'Fields', 'jackson_fields@cropcrest.com', '1234567890', 'Y' ),             -- CropCrest Association
     ( 'd1091f7d-b94f-4a45-afc7-1d33580e8a0d', 10, 'John', 'Doe', 'john.doe@westmonroe.com', '', 'Y' ),                                 -- West Monroe Farm Credit
     ( '135ad683-bc95-4edb-9ff7-291d0ea42c87', 10, 'Griffin', 'Kosonocky', 'gkosonocky+lmpart2@westmonroe.com', '', 'Y' ),              -- West Monroe Farm Credit
     ( '136ad683-bc95-4edb-9ff7-291d0ea42c88', 10, 'Gavin', 'Winkel', 'gwinkel+lmpart2@westmonroe.com', '', 'Y' ),                      -- West Monroe Farm Credit
     ( '137ad683-bc95-4edb-9ff7-291d0ea42c88', 10, 'Gabriel', 'Lopez', 'glopez+lmpart2@westmonroe.com', '', 'Y' ),                      -- West Monroe Farm Credit
     ( '138ad683-bc95-4edb-9ff7-291d0ea42c89', 10, 'Raz', 'Consta', 'rconstantinescu+lmpart2@westmonroe.com', '', 'Y' ),                -- West Monroe Farm Credit
     ( '139ad683-bc95-4edb-9ff7-291d0ea42c90', 10, 'Angel', 'Araya', 'aarayaquiros+lmpart2@westmonroe.com', '', 'Y' ),                  -- West Monroe Farm Credit
     ( 'd32f1b56-fd9f-4f94-a5fe-511db33e9bc6', 5, 'United Farm Credit Services', 'System', 'SystemUser-db087f72-29bb-4fc6-8ba8-2f6d9b7aecab', '', 'Y' ),
     ( 'ba446870-57f5-47ce-a695-533858848d10', 6, 'Prairie Land Credit Bank', 'System', 'SystemUser-12693c4c-4286-4ec1-99f5-d5b4ca682d5c', '', 'Y' ),
     ( 'cb50501f-b40a-4901-b6b3-7172f53608f8', 7, 'GreenField Cooperative', 'System', 'SystemUser-e5b36ce1-f617-433a-9d5b-668a07149ff1', '', 'Y' ),
     ( '49296f2e-f116-44c7-bd25-ab90b3705837', 8, 'Agricore Alliance', 'System', 'SystemUser-f7bf2764-b566-4fbc-936a-d320c4b40d12', '', 'Y' ),
     ( 'e523d692-d856-4ca4-9da5-e6bdba5cdf3c', 9, 'CropCrest Association', 'System', 'SystemUser-cd6e65fa-dd9b-45f1-81cf-187c272476ca', '', 'Y' );

------------------------------------
--     Create Test User Roles     --
------------------------------------
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
       VALUES
     ( 13, 2 ),
     ( 13, 3 ),
     ( 13, 4 ),
     ( 13, 5 ),
     ( 13, 6 ),
     ( 13, 7 ),     -- Jane Halverson as Deal Manager
     ( 13, 8 ),     -- Jane Halverson as Deal Part Manager
     ( 13, 9 ),
     ( 13, 10 ),
     ( 13, 11 ),
     ( 13, 12 ),
     ( 13, 13 ),    -- Jane Halverson as File Manager
     ( 13, 14 ),
     ( 14, 2 ),
     ( 15, 2 ),
     ( 16, 2 ),
     ( 16, 3 ),
     ( 16, 4 ),
     ( 16, 5 ),
     ( 16, 6 ),
     ( 16, 7 ),
     ( 16, 8 ),
     ( 16, 9 ),
     ( 16, 10 ),
     ( 16, 11 ),
     ( 16, 12 ),
     ( 16, 13 ),
     ( 16, 14 ),
     ( 16, 16 ),    -- Pete Kowalski as Institution Manager
     ( 19, 2 ),
     ( 19, 3 ),
     ( 19, 4 ),
     ( 19, 5 ),
     ( 19, 6 ),
     ( 19, 7 ),
     ( 19, 8 ),
     ( 19, 9 ),
     ( 19, 10 ),
     ( 19, 11 ),
     ( 19, 12 ),
     ( 19, 13 ),
     ( 19, 14),
     ( 20, 2 ),
     ( 20, 3 ),
     ( 20, 4 ),
     ( 20, 5 ),
     ( 20, 6 ),
     ( 20, 7 ),
     ( 20, 8 ),
     ( 20, 9 ),
     ( 20, 10 ),
     ( 20, 11 ),
     ( 20, 12 ),
     ( 20, 13 ),
     ( 20, 14),
     ( 21, 2 ),
     ( 21, 3 ),
     ( 21, 4 ),
     ( 21, 5 ),
     ( 21, 6 ),
     ( 21, 7 ),
     ( 21, 8 ),
     ( 21, 9 ),
     ( 21, 10 ),
     ( 21, 11 ),
     ( 21, 12 ),
     ( 21, 13 ),
     ( 21, 14 );

-- Add all roles for the West Monroe Farm Credit users.
INSERT INTO USER_ROLE_XREF
     ( USER_ID, ROLE_ID )
SELECT USER_ID, ROLE_ID
  FROM USER_INFO
     , ROLE_DEF
 WHERE EMAIL_ADDR LIKE '%+lmpart2@westmonroe.com'
   AND ROLE_ID > 1
   AND ROLE_ID < 15;

------------------------------------
--       Create Test Deals        --
------------------------------------
INSERT INTO DEAL_INFO
     ( DEAL_UUID, DEAL_EXTERNAL_UUID, DEAL_NAME, DEAL_INDUSTRY_ID, ORIGINATOR_ID, INITIAL_LENDER_IND, INITIAL_LENDER_ID, STAGE_ID
     , DEAL_STRUCTURE_ID, DEAL_TYPE_DESC, DEAL_DESC, DEAL_AMT, APPLICANT_EXTERNAL_UUID, BORROWER_NAME, BORROWER_DESC, BORROWER_CITY_NAME
     , BORROWER_STATE_CD, BORROWER_COUNTY_NAME, FARM_CR_ELIG_ID, TAX_ID_NBR, BORROWER_INDUSTRY_CD, BUSINESS_AGE_QTY
     , DEFAULT_PROB_PCT, DEBT_SRV_COV_RATIO, CY_EBITA_AMT, PATR_ELIG_IND, CREATED_BY_ID, UPDATED_BY_ID, ACTIVE_IND )
       VALUES
    ( 'a94d5ece-26a6-45e9-81c2-54162cdad5c9', 'c75a8d02-b7c3-46eb-a6fc-28df768febf5', 'Peanut Farming and Processing', 16, 5, 'Y', 5, 1, 4, 'New'
    , 'Nestled in the heart of rural Georgia, a thriving peanut farming and processing operation was poised for expansion. Eager to acquire cutting-edge processing equipment and increase production capacity, the operation sought financial backing through a distinctive loan participation deal facilitated by United Farm Credit Services (UFCS). Recognizing the potential of this venture, UFCS collaborated with other agricultural credit associations to structure a syndicated loan, bringing together a consortium of lenders. The syndicated loan allowed each participating institution to contribute a portion of the total amount based on their risk appetite and capacity. UFCS, as a leading member of the Farm Credit System, played a pivotal role in orchestrating the deal, ensuring that the terms aligned with the operational goals. By spreading the risk among multiple lenders, this collaborative approach provided the peanut farming and processing operation with access to a substantial pool of funds. As a result, the operation flourished, contributing not only to its success but also highlighting the efficacy of such loan participation deals in supporting ambitious agricultural endeavors. The success story of the peanut farming and processing operation became a beacon within the Farm Credit System, illustrating the effectiveness of loan participation deals in fostering sustainable growth in the agriculture sector. With the peanut fields expanding and the air filled with the aroma of freshly roasted peanuts, the syndicated loan, led by United Farm Credit Services, underscored the power of collective investment in shaping the future of American agriculture.'
    , 55000000.00, 'efc5b487-f237-4dd7-996f-813cabdafdb8', 'Smith Peanuts'
    , 'Smith Peanuts, led by the visionary farmer Thomas "Tommy" Smith, stood as a beacon of agricultural ambition. A third-generation farmer with weathered hands and a worn hat, Tommy''s steadfast dedication to his family''s legacy fueled his desire to modernize the peanut farming and processing operation. With a gravelly voice, Tommy spoke passionately about the transformative impact he envisioned for Smith Peanuts. His dreams of expanding the peanut fields and introducing cutting-edge processing equipment resonated deeply with lenders, making him the driving force behind seeking financial backing. As the aroma of freshly roasted peanuts wafted through the air, Tommy Smith, at the helm of Smith Peanuts, became a symbol of the potential unlocked through a singular focus on sustainable growth in the heart of American agriculture.'
    , 'Crawford', 'GA', 'Oglethorpe County', 1, '95-5555555', '424590', 21, 5, 1.2, 5500000.00, 'Y', 12, 12, 'Y' ),
    ( 'ad1bfc2f-0d1f-4f97-9ad6-a224d397e52a', '2b61ff23-268b-48aa-8da2-4f39adaba52b', 'Pumpkin Seed Facility', 16, 1, 'N', NULL, 1, 4, 'New'
    , 'In the vast expanse of the Texas countryside, a visionary entrepreneur set their sights on establishing a state-of-the-art pumpkin seed facility, aiming to meet the growing demand for premium pumpkin seed products. The ambitious project required significant capital, prompting the entrepreneur to approach the Farm Credit Bank of Texas (FCBT) for financial support. Recognizing the potential of this venture, FCBT took the lead in structuring a loan participation deal, bringing together a coalition of agricultural credit associations to fuel the growth of the pumpkin seed facility. The syndicated loan, orchestrated by FCBT, allowed various agricultural credit associations to collaborate on funding the venture, with each participating institution contributing a portion of the total loan amount based on their financial capacity. The unique partnership mitigated risk and provided the entrepreneur with access to a diverse pool of capital. As the pumpkin seed facility began operations, the loan participation deal not only demonstrated the commitment of FCBT to supporting innovative agricultural projects but also showcased the strength of collaboration within the Farm Credit System. The success of the pumpkin seed facility became a testament to the effectiveness of the loan participation deal in fostering agricultural development. With fields of pumpkins thriving and the facility processing high-quality seeds, the venture not only contributed to the entrepreneur''s success but also highlighted the collective impact of the Farm Credit Bank of Texas and its partner agricultural credit associations in nurturing agribusiness innovation in the Lone Star State.'
    , 80000000.00, '90d7353c-f6c8-439f-8328-c94ca304dd98', 'The Pumpkin Ranch'
    , 'The Pumpkin Ranch took root as a innovative project led by an ambitious entrepreneur. Driven to meet the surging demand for premium pumpkin seed products, the entrepreneur sought financial support to establish a state-of-the-art pumpkin seed facility. With a keen eye on agricultural innovation, the entrepreneur, a trailblazer in the field, embarked on the journey to bring The Pumpkin Ranch to life. As the facility began operations, the entrepreneur''s dedication not only contributed to personal success but also showcased a commitment to driving agribusiness innovation in the Lone Star State. Amidst thriving pumpkin fields and the steady hum of processing machinery, The Pumpkin Ranch became a symbol of entrepreneurial spirit, cultivating not just pumpkin seeds but a new era of agribusiness in the heart of Texas.'
    , 'Perryton', 'TX', 'Ochiltree County', 1, '96-6666666', '424910', 15, 7, 1.1, 7550000.00, 'Y', 2, 2, 'Y' ),
    ( '2c308757-6c63-439c-a2a4-06db90c36fe7', '092358c2-080c-4564-9c44-5217f143bf69', 'Dairy Production Plant', 16, 2, 'Y', 5, 1, 4, 'New'
    , 'Seated in the rolling hills of Wisconsin, a forward-thinking dairy farmer envisioned the establishment of a state-of-the-art dairy production plant to meet the growing demand for high-quality dairy products. Eager to turn this vision into reality, the farmer sought financial support from AgFirst Farm Credit Bank (AgFirst FCB), a leading Agricultural Credit Association (ACA) with a reputation for fostering innovation in the dairy sector. AgFirst FCB took the initiative to structure a loan participation deal, collaborating with other ACAs to create a consortium of lenders to support the ambitious dairy production project. The syndicated loan, led by AgFirst FCB, allowed multiple agricultural credit associations to join forces, with each participating institution contributing a portion of the total loan amount based on their financial capacity. This collaborative approach not only diversified the sources of funding but also spread the risk among the participating lenders. The dairy farmer, armed with the financial backing from this unique partnership, was able to invest in cutting-edge technology, expand their herd, and optimize production processes at the new dairy production plant. As the dairy production plant commenced operations, the success of the loan participation deal became a testament to the strength and versatility of the Farm Credit System. The collaborative efforts of AgFirst FCB and the participating ACAs not only empowered the dairy farmer to thrive but also underscored the pivotal role of strategic financial partnerships in advancing innovation and sustainability in the dairy industry. The idyllic Wisconsin landscape echoed with the hum of efficient machinery and the contented lowing of dairy cows, symbolizing the harmonious blend of agricultural tradition and modern financial support.'
    , 100000000.00, '9459915a-a377-4c4e-91ed-df47d51196b0', 'Moo Mesa'
    , 'In the serene hills of Wisconsin, Moo Mesa embodies the vision of a forward-thinking dairy farmer eager to meet the growing demand for premium dairy products. Seeking financial support, the farmer collaborated with multiple Agricultural Credit Associations to establish this state-of-the-art dairy production plant. The strategic partnership diversified funding sources, enabling investments in cutting-edge technology and herd expansion. As Moo Mesa flourished, it became a symbol of innovation and sustainability in the dairy industry, blending modern financial support with the rich traditions of dairy farming in the heart of Wisconsin.'
    , 'Hudson', 'WI', 'St. Croix County', 1, '97-7777777', '112120', 30, 11, 2.0, 7200000.00, 'Y', 4, 4, 'Y' ),
    ( '3c308757-6c63-439c-a2a4-06db90c36fe8', '69fcd8fa-dc21-4d94-8752-ff710a07ff68', 'Turtle Nursery', 16, 5, 'Y', 5, 1, 4, 'New'
    , 'Seated in the beaches of Costa Rica, a forward-thinking turtle farmer envisioned the establishment of a state-of-the-art turtle nurser to meet the growing demand for high-quality turtles. Eager to turn this vision into reality, the farmer sought financial support from AgFirst Farm Credit Bank (AgFirst FCB), a leading Agricultural Credit Association (ACA) with a reputation for fostering innovation in the turtle sector. AgFirst FCB took the initiative to structure a loan participation deal, collaborating with other ACAs to create a consortium of lenders to support the ambitious turtle production project. The syndicated loan, led by AgFirst FCB, allowed multiple agricultural credit associations to join forces, with each participating institution contributing a portion of the total loan amount based on their financial capacity. This collaborative approach not only diversified the sources of funding but also spread the risk among the participating lenders. The turtle farmer, armed with the financial backing from this unique partnership, was able to invest in cutting-edge technology, expand their bale, and optimize production processes at the new turtle nursery. As the turtle nursery commenced operations, the success of the loan participation deal became a testament to the strength and versatility of the Farm Credit System. The collaborative efforts of AgFirst FCB and the participating ACAs not only empowered the turtle farmer to thrive but also underscored the pivotal role of strategic financial partnerships in advancing innovation and sustainability in the turtle industry. The idyllic Costa Rica landscape echoed with the hum of efficient nests and the contented lowing of turtles, symbolizing the harmonious blend of agricultural tradition and modern financial support.'
    , 35000000.00, '402737bf-42c7-4c66-b66f-db056c669cc0', 'Moo Mesa'
    , 'In the serene hills of Wisconsin, Moo Mesa embodies the vision of a forward-thinking dairy farmer eager to meet the growing demand for premium dairy products. Seeking financial support, the farmer collaborated with multiple Agricultural Credit Associations to establish this state-of-the-art dairy production plant. The strategic partnership diversified funding sources, enabling investments in cutting-edge technology and herd expansion. As Moo Mesa flourished, it became a symbol of innovation and sustainability in the dairy industry, blending modern financial support with the rich traditions of dairy farming in the heart of Wisconsin.'
    , 'Hudson', 'WI', 'St. Croix County', 1, '97-7777777', '112120', 10, 1, 2.0, 7200000.00, 'Y', 12, 12, 'Y' );

------------------------------------
--       Create Test Events       --
------------------------------------
INSERT INTO EVENT_INFO
     ( EVENT_UUID, EVENT_EXTERNAL_UUID, DEAL_ID, EVENT_NAME, EVENT_TYPE_ID, STAGE_ID, PROJ_LAUNCH_DATE, COMMITMENT_DATE
     , COMMENTS_DUE_DATE, EFFECTIVE_DATE, PROJ_CLOSE_DATE, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 'a94d5ece-26a6-45e9-81c2-54162cdad5c0', 'c75a8d02-b7c3-46eb-a6fc-28df768febf6', 5, 'Origination', 1, 1
     , NULL, NULL, NULL, NULL, NULL, 12, 12 ),
     ( 'ad1bfc2f-0d1f-4f97-9ad6-a224d397e52b', '2b61ff23-268b-48aa-8da2-4f39adaba52c', 6, 'Origination', 1, 1
     , NULL, '2024-05-27', NULL, NULL, '2024-05-26', 2, 2 ),
     ( '2c308757-6c63-439c-a2a4-06db90c36fe8', '092358c2-080c-4564-9c44-5217f143bf60', 7, 'Origination', 1, 1
     , NULL, NULL, NULL, NULL, '2024-06-02', 4, 4 ),
     ( '3c308757-6c63-439c-a2a4-06db90c36fe9', '69fcd8fa-dc21-4d94-8752-ff710a07ff69', 8, 'Origination', 1, 1
     , NULL, NULL, NULL, NULL, NULL, 12, 12 );

----------------------------------------
--      Create Test Deal Members      --
----------------------------------------
INSERT INTO DEAL_MEMBER
     ( DEAL_ID, USER_ID, MEMBER_TYPE_CD, CREATED_BY_ID )
       VALUES
     ( 5, 13, 'O', 12 ),
     ( 5, 15, 'O', 12 ),
     ( 6, 1, 'O', 2 ),
     ( 7, 2, 'O', 4 ),
     ( 6, 13, 'P', 2 ),
     ( 6, 14, 'P', 2 ),
     ( 7, 13, 'P', 4 ),
     ( 7, 14, 'P', 4 ),
     ( 8, 13, 'O', 12 ),
     ( 8, 15, 'O', 12 );

----------------------------------------
--     Create Test Deal Covenants     --
----------------------------------------
INSERT INTO DEAL_COVENANT
     ( COVENANT_EXTERNAL_UUID, DEAL_ID, ENTITY_NAME, CATEGORY_NAME, COVENANT_TYPE_DESC, FREQUENCY_DESC, NEXT_EVAL_DATE
     , EFFECTIVE_DATE, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( '4b54adbf-b918-42b5-9eab-d4f3a4b9d0eb', 5, 'Smith Peanuts', 'Collateral', 'Real Estate Taxes', 'Annually', NULL, NULL, 12, 12 ),
     ( '122714f9-7ed3-43fd-877a-8fbb0e1ef9d2', 5, 'Smith Peanuts', 'Collateral', 'Insurance', 'Annually', NULL, NULL, 12, 12 ),
     ( '471ea6e1-58b4-44ce-908e-25af945dec6a', 5, 'Smith Peanuts', 'Financial Indicators', 'Minimum Current Ratio', 'Quarterly', NULL, NULL, 12, 12 ),
     ( '88066acb-fe23-4bd6-b495-d68be3ba375a', 5, 'Smith Roastery', 'Financial Indicators', 'Accounts Payable', 'Monthly', NULL, NULL, 12, 12 ),
     ( 'af2ad0af-1ed5-4261-b65c-620cf5f86270', 5, 'Smith Roastery', 'Financial Indicators', 'Minimum Working Capital', 'Quarterly', NULL, NULL, 12, 12 ),
     ( '1fb64d50-6bbb-4474-8eb3-f0e95612d06c', 6, 'Pumpkin Seed Facility', 'Collateral', 'Real Estate Taxes', 'Annually', NULL, NULL, 2, 2 ),
     ( 'a82592d8-29e4-4441-b950-db72e86cb6cd', 6, 'Pumpkin Seed Facility', 'Collateral', 'Insurance', 'Semi-Annually', NULL, NULL, 2, 2 ),
     ( 'ca29b72f-b5b8-49ef-aab0-9cc28b33077e', 7, 'Jimmy Mesa', 'Financial Statement Requirements', 'Personal Financial Statement', 'Quarterly', NULL, NULL, 4, 4 ),
     ( '0c387c92-b8d7-48df-a7e1-b28d2acb7b7f', 7, 'Moo Mesa', 'Collateral', 'Real Estate Taxes', 'Annually', NULL, NULL, 4, 4 ),
     ( '071e920f-fcbc-4f8a-83f2-9506e6b4ec2c', 8, 'Smith Peanuts', 'Collateral', 'Real Estate Taxes', 'Annually', NULL, NULL, 12, 12 ),
     ( '9feba736-6ff7-442b-aa32-4d67a5782718', 8, 'Smith Peanuts', 'Collateral', 'Insurance', 'Annually', NULL, NULL, 12, 12 ),
     ( '18b7e4ef-142e-4529-a68c-b8c3f0995375', 8, 'Smith Roastery', 'Financial Indicators', 'Minimum Working Capital', 'Quarterly', NULL, NULL, 12, 12 );

----------------------------------------
--     Create Test Deal Facilities    --
----------------------------------------
INSERT INTO DEAL_FACILITY
     ( FACILITY_EXTERNAL_UUID, DEAL_ID, FACILITY_NAME, FACILITY_AMT, FACILITY_TYPE_ID, TENOR_YRS_QTY, COLLATERAL_ID
     , PRICING_DESC, CSA_DESC, FACILITY_PURPOSE_ID, PURPOSE_TEXT, DAY_COUNT_ID, GUAR_INV_IND, FARM_CREDIT_TYPE_NAME
     , REV_UTIL_PCT, UPFRONT_FEES_DESC, UNUSED_FEES_DESC, AMORTIZATION_DESC, LGD_OPTION, PATRONAGE_PAYING_IND
     , CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( '8e99c969-395b-4cc3-9930-30bd3550c6b7', 5, 'Facility A', 15000000.00, 6, 10, 26, 'SOFR + 165.0bps', '10%', 13
     , 'Land purchase for new packing facility in the center of the town.'
     , 18, 'N', 'FLCA', NULL, '15 bps on the final allocation', '20 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 12, 12 ),
     ( '9d6954d6-5ca6-40e0-9131-5cf93d7c8cc9', 5, 'Facility B', 30000000.00, 10, 5, 26, 'SOFR + 135.0bps', '10%', 12
     , 'New packing facility build out and updates to existing packing facility to convert it into a processing facility.'
     , 18, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 30mm, 15.25 bps on the final allocation; For allocations less than 30mm, 5 bps on the final allocation'
     , '25 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'B', 'N', 12, 12 ),
     ( '240b5388-d045-4b4c-8dfc-507bbd15b66a', 5, 'Facility C', 10000000.00, 6, 5, 27, 'SOFR + 130.0bps', '10%', 11
     , 'Additional funds to support the business expansion during construction and updates to the different facilities.'
     , 19, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 40mm, 15 bps on the final allocation; For allocations less than 40mm, 10 bps on the final allocation'
     , '17.5 bps', NULL, 'C', 'N', 12, 12 ),
     ( 'e12872d3-f6b4-4ede-aa5c-759029fedca8', 6, 'Facility A', 80000000.00, 10, 7, 27, 'SOFR + 140.0bps', '10%', 12
     , 'Building out a new seed coating and packing plant to speed up operations to meet demand.'
     , 19, 'Y', 'PCA', NULL, 'For allocations greater than or equal to 40mm, 15 bps on the final allocation; For allocations less than 40mm, 10 bps on the final allocation'
     , '17.5 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 2, 2 ),
     ( 'a529e052-fe98-4fc8-91ad-ea3cddaeb522', 7, 'Facility A', 75000000.00, 6, 5, 27, 'SOFR + 130.0bps', '10%', 12
     , 'New milk processing plan in the western part of the state to service farms in that area.'
     , 20, 'Y', 'PCA', NULL, '15 bps on the final allocation', '20 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 4, 4 ),
     ( 'db715fa8-a6f4-46ef-987c-aed0442a7c7f', 7, 'Facility B', 25000000.00, 10, 7, 27, 'SOFR + 150.0bps', '10%', 11
     , 'Support the enhancement of buisness operations during the transition.'
     , 21, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 30mm, 15.25 bps on the final allocation; For allocations less than 30mm, 5 bps on the final allocation'
     , '25 bps', NULL, 'B', 'N', 4, 4 ),
     ( 'edc2d9e4-fa47-459d-8abc-eadbc051ccd6', 8, 'Facility A', 15000000.00, 6, 9, 28, 'SOFR + 130.0bps', '10%', 13
     , 'Land purchase for new turtle packing facility in the center of the town.'
     , 22, 'N', 'FLCA', NULL, 'For allocations greater than or equal to 40mm, 15 bps on the final allocation; For allocations less than 40mm, 10 bps on the final allocation'
     , '17.5 bps', '0.0% / 0.0% / 2.5% / 2.5% / 5.0%', 'A', 'Y', 12, 12 );

---------------------------------------------
-- Update Last Facility Number in the Deal --
---------------------------------------------
UPDATE DEAL_INFO DI
   SET LAST_FACILITY_NBR = ( SELECT COUNT(DEAL_FACILITY_ID)
                               FROM DEAL_FACILITY
                              WHERE DEAL_ID = DI.DEAL_ID )
 WHERE DEAL_ID BETWEEN 5 AND 8;

INSERT INTO DEAL_DOCUMENT
     ( DEAL_ID, DISPLAY_NAME, DOCUMENT_NAME, DOCUMENT_CATEGORY_ID, DOCUMENT_TYPE, DOCUMENT_DESC, SOURCE_CD, DOCUMENT_EXTERNAL_UUID
     , CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 5, '2021 Financials - Smith Peanuts.xlsx', '1707748739815.xlsx', 3, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
     , 'This is a test document.', 'M', 'f9a2d862-bc8c-4497-aa82-8037785c6650', 12, 12 ),
     ( 5, '2022 Financials - Smith Peanuts.xlsx', '1707748813048.xlsx', 3, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
     , 'This is a test document.', 'M', 'ac1444e7-5dbb-4bf1-b2b8-93e9feda23e0', 12, 12 ),
     ( 5, '2023 Financials - Smith Peanuts.xlsx', '1707748890725.xlsx', 3, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
     , 'This is a test document.', 'M', 'da990cf3-5010-43b2-9d7d-508b3ea63b73', 12, 12 ),
     ( 5, 'Smith Peanuts Credit Report.pdf', '1707748986902.pdf', 2, 'application/pdf', 'This is a test document.', 'M'
     , '06cc7d4b-ab6e-4963-ae96-f28172248a14', 12, 12 ),
     ( 5, 'Smith Peanuts CIM.docx', '1707749030000.docx', 2, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
     , 'This is a test document.', 'M', 'b3420678-cf35-4c07-8f6b-d17b4be4cf86', 12, 12 ),
     ( 5, 'Smith Peanuts Background.pptx', '1707749088042.pptx', 2, 'application/vnd.openxmlformats-officedocument.presentationml.presentation'
     , 'This is a test document.', 'M', '85af9961-61d8-45cc-9ac2-918c6cb7d7cb', 12, 12 ),
     ( 5, '2023 Roastery Assessment - Smith Peanuts.pdf', '1707749163603.pdf', 1, 'application/pdf', 'This is a test document.'
     , 'M', '85e7e6b9-8c86-43f3-a2df-ca891db7b4f9', 12, 12 ),
     ( 5, '2023 400 Peanut Road Land Value - Smith Peanuts.pdf', '1707749206234.pdf', 1, 'application/pdf', 'This is a test document.'
     , 'M', '60d6a99d-7274-4a95-b39e-57607d30ddfb', 12, 12 ),
     ( 5, 'Roaster Image.png', '1707749262144.png', 1, 'image/png', 'This is a test document.', 'M', 'e51791d7-aacc-4d9c-9897-baace62d9d22', 12, 12 ),
     ( 5, '400 Peanut Road.jpeg', '1707749297027.jpeg', 1, 'image/jpeg', 'This is a test document.', 'M', 'fc81ff9d-2495-47c0-9c9e-9385f28b32ff', 12, 12 );

INSERT INTO EVENT_DEAL_FACILITY
     ( EVENT_ID, DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 5, 16, 1, 1 ),
     ( 5, 17, 1, 1 ),
     ( 5, 18, 1, 1 ),
     ( 6, 19, 2, 2 ),
     ( 7, 20, 4, 4 ),
     ( 7, 21, 4, 4 ),
     ( 8, 22, 12, 12 );

----------------------------------------
--   Create Demo Event Participants   --
----------------------------------------
INSERT INTO EVENT_PARTICIPANT
     ( EVENT_ID, PARTICIPANT_ID, PARTICIPANT_STEP_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 6, 5, 2, 2, 2 ),
     ( 7, 5, 2, 4, 4 );

-----------------------------------------------
-- Create Demo Event Participant Origination --
-----------------------------------------------
INSERT INTO EVENT_PARTICIPANT_ORIGINATION
     ( EVENT_PARTICIPANT_ID, INVITE_RECIPIENT_ID, MESSAGE_DESC )
       VALUES
     ( 9, 13, NULL ),
     ( 10, 13, NULL );

INSERT INTO EVENT_PARTICIPANT_FACILITY
     ( EVENT_PARTICIPANT_ID, EVENT_DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 9, 19, 12, 12 ),
     ( 9, 20, 12, 12 ),
     ( 9, 21, 12, 12 ),
     ( 10, 19, 12, 12 ),
     ( 10, 20, 12, 12 ),
     ( 10, 21, 12, 12 );

----------------------------------------------
--    Create Test Event Lead Facilities     --
----------------------------------------------
INSERT INTO EVENT_LEAD_FACILITY
     ( EVENT_ID, EVENT_DEAL_FACILITY_ID, CREATED_BY_ID, UPDATED_BY_ID )
       VALUES
     ( 5, 16, 1, 1 ),
     ( 5, 17, 1, 1 ),
     ( 5, 18, 1, 1 ),
     ( 6, 19, 2, 2 ),
     ( 7, 20, 4, 4 ),
     ( 7, 21, 4, 4 ),
     ( 8, 22, 12, 12 );

--------------------------------------------
-- Initialize (Integration) Customer Data --
--------------------------------------------
INSERT INTO CUSTOMER_DATA
     ( MARKETPLACE_JSON, UNSUPPORTED_JSON, CREATED_BY_ID )
       VALUES
     ( '{"deal": {"uid": "a94d5ece-26a6-45e9-81c2-54162cdad5c9", "dealExternalId": "c75a8d02-b7c3-46eb-a6fc-28df768febf5"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 12 ),
     ( '{"deal": {"uid": "ad1bfc2f-0d1f-4f97-9ad6-a224d397e52a", "dealExternalId": "2b61ff23-268b-48aa-8da2-4f39adaba52b"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 2 ),
     ( '{"deal": {"uid": "2c308757-6c63-439c-a2a4-06db90c36fe7", "dealExternalId": "092358c2-080c-4564-9c44-5217f143bf69"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 4 ),
     ( '{"deal": {"uid": "3c308757-6c63-439c-a2a4-06db90c36fe8", "dealExternalId": "69fcd8fa-dc21-4d94-8752-ff710a07ff68"}}'
     , '{"trackedChanges": [{"fieldType": "applicant","field": "applicationRbcLabel","date": "1900-01-01"},{"fieldType": "collateral","field": "applicationRbcLabel"}]}', 12 );

-------------------------------------------
-- Create Test Confidentiality Agreement --
-------------------------------------------
INSERT INTO INSTITUTION_CONFIDENTIALITY_AGREEMENT
    ( INSTITUTION_ID, CREATED_BY_ID, CONF_AGRMNT_DESC )
      VALUES
    ( 5, 12, '<p><strong>CONFIDENTIAL INFORMATION MEMORANDUM</strong></p><p><br></p><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas convallis eleifend erat semper pharetra. In tempor sem vitae purus dignissim rutrum. Duis pretium nibh egestas, convallis neque et, pellentesque risus. Suspendisse pharetra eu odio sit amet imperdiet. Nulla posuere rhoncus erat, ut sagittis tellus pulvinar a. Integer mollis lacinia iaculis. Suspendisse sapien metus, maximus a enim eu, faucibus lobortis lectus. Morbi ut laoreet magna, et dapibus arcu. Nullam vehicula pulvinar est quis pretium. Praesent faucibus quis justo gravida convallis. Sed vitae odio bibendum, gravida dolor egestas, porttitor tortor. Fusce vitae rutrum nisi, sed tincidunt leo. Mauris tempus mattis orci, non aliquam ligula sagittis molestie. In et porttitor nulla. Ut faucibus, felis vel rutrum scelerisque, tortor ipsum suscipit mi, tempus viverra urna leo nec sem. Nullam elementum neque nec nibh viverra bibendum. Vestibulum quam justo, mattis et scelerisque a, placerat et nisi. Aliquam tincidunt vitae odio sit amet volutpat. Maecenas ac neque est. Aenean laoreet non leo eget maximus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Mauris ac mi at risus dignissim euismod fringilla nec mi. Aliquam consectetur, turpis at pulvinar fermentum, risus metus accumsan quam, id tempus sem nibh eget lorem. In in porttitor turpis. Nulla pharetra turpis nisi, non dapibus risus semper ac. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent lobortis mauris eu ante tempor, sit amet auctor nisi luctus. Proin massa leo, scelerisque non scelerisque at, imperdiet aliquet massa. Nulla dolor mi, efficitur sit amet metus aliquam, condimentum ultrices enim. Curabitur ut consequat nulla, eget mollis enim. Sed semper sed ligula sed eleifend. Mauris fringilla iaculis ligula, a dapibus sem blandit sed. Duis congue lectus sed malesuada euismod. Suspendisse eget enim scelerisque felis viverra eleifend. Quisque porta leo eget dolor tincidunt, et ultrices enim vestibulum. Sed iaculis eget tellus non bibendum. Morbi a eros lorem. Sed placerat, lectus et ornare mattis, arcu velit rhoncus ipsum, in venenatis mauris erat vitae mauris. Duis maximus ante non nisi sodales, a tincidunt lacus finibus. Aenean sollicitudin eros vel ex consequat lacinia. Duis bibendum faucibus risus at lobortis. Maecenas at nulla mi. Curabitur erat ligula, accumsan eu suscipit quis, posuere vel dui. Aenean ut tempor erat. Integer faucibus odio vel velit porttitor, et volutpat lacus aliquet. Phasellus a lacus non velit tempus tempus ut non erat. Pellentesque commodo sit amet erat facilisis fringilla. Nulla purus felis, bibendum aliquam nunc eu, lobortis pharetra risus. Phasellus accumsan lobortis est quis dictum. Vestibulum vulputate accumsan quam at varius. In faucibus ligula lorem. Ut egestas lacinia dictum. Etiam lobortis neque ut lectus pharetra convallis. Proin convallis sollicitudin pretium. Phasellus aliquam id turpis sit amet dignissim. Mauris placerat vehicula ligula. Ut pharetra ac sapien eget consequat. Proin purus tortor, laoreet a tortor quis, iaculis suscipit neque. Donec nec quam nisi. Curabitur tincidunt porttitor nisi, quis sollicitudin elit bibendum ut. Phasellus sed nibh a tellus ultrices luctus a sit amet diam. Quisque quis ligula urna. Fusce ornare velit at tortor luctus, elementum ullamcorper sem rutrum. Aliquam vulputate nibh urna. Mauris pretium velit nec magna congue fermentum. Nullam neque dui, feugiat eu ultrices at, consequat sed dui. Aenean porttitor nisi nec interdum aliquet. Nam commodo facilisis nunc sit amet iaculis. Donec et mauris dui. Nunc ac congue diam, eu porttitor mi. Sed id pellentesque nisl, eu luctus eros. Nulla et ligula enim. Aliquam fermentum vitae magna a ornare. Integer sapien ligula, ultrices facilisis efficitur sed, congue a ligula. Ut consectetur ex nec dui gravida consequat. Suspendisse cursus elit nulla, eu scelerisque metus commodo sit amet. Sed varius finibus dapibus. Duis sed arcu congue, venenatis dolor id, commodo tellus. Fusce ac lobortis diam. Aliquam semper neque et consequat scelerisque. Sed felis enim, pretium ac tellus non, condimentum malesuada neque. Donec nisl ipsum, tincidunt ut auctor molestie, ornare et eros. Praesent venenatis accumsan orci sit amet sollicitudin. Mauris a sapien sit amet lorem hendrerit luctus. Suspendisse aliquet turpis at nibh interdum vehicula. Suspendisse quis interdum erat. Duis euismod gravida tellus eu ornare. Maecenas in magna eu purus pretium aliquam vel quis sem. Proin pellentesque tincidunt aliquet. Aenean placerat id ipsum eget interdum. Cras vel euismod lectus. Vestibulum placerat lobortis rhoncus. Quisque vehicula erat eu ex ultricies luctus. Curabitur tincidunt mollis lectus, ac suscipit ex consectetur sit amet. Ut porttitor convallis dapibus. Fusce placerat vulputate ligula ac pellentesque. Sed tincidunt aliquet hendrerit. Nullam finibus enim et consequat fermentum. Maecenas felis justo, convallis ac scelerisque id, convallis nec mauris. Nulla venenatis, purus sit amet rutrum volutpat, eros erat consectetur sapien, sed accumsan sem ex at ex. Donec mattis purus vel vehicula vulputate. Sed convallis egestas ligula id pulvinar. Praesent vel pellentesque nibh, id faucibus ex. Nulla cursus a augue eu semper. Nunc nulla lectus, consectetur a ex a, viverra pharetra sem. Aliquam aliquet purus venenatis tortor finibus mattis. Vivamus tempus lobortis tortor ac sodales. Proin dictum, nibh fringilla volutpat condimentum, tellus quam consequat justo, sed varius mauris ipsum id risus. Morbi ornare erat id velit venenatis hendrerit. Cras dictum ac lacus ac ornare. Nulla at volutpat nisl, ut molestie elit. Ut eget arcu ut turpis aliquet molestie. Aliquam lobortis mauris dapibus justo viverra, rutrum faucibus erat dignissim. Sed sagittis consectetur enim. Pellentesque rutrum nunc ac dolor accumsan tempus vel eu nisl. Nullam semper, lacus ac pharetra vestibulum, libero eros sodales ex, eget dapibus augue ante eget justo. Mauris mattis convallis elit, ultricies hendrerit felis euismod quis. Praesent mauris lorem, placerat id sem non, congue dapibus metus. Ut volutpat risus sit amet est dapibus faucibus. Suspendisse auctor, lorem a congue ultrices, lectus dui vulputate nulla, et auctor ipsum lorem vel erat. Vivamus tempor urna non hendrerit volutpat. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aenean accumsan egestas nulla tincidunt faucibus. Nulla blandit porttitor turpis feugiat laoreet. Proin porttitor placerat lacus. Proin et rutrum purus. Aenean ullamcorper ipsum risus, eu imperdiet dolor pellentesque pellentesque. Sed fermentum nisl eu finibus hendrerit. Integer orci ligula, scelerisque eu arcu eu, pharetra gravida nunc. Morbi fringilla lacinia nisl, eu volutpat sem sodales dapibus. Mauris iaculis tincidunt enim sed sollicitudin. Phasellus a nisi laoreet, lacinia enim sit amet, egestas metus. Nunc eleifend massa id nibh vulputate, a eleifend sem sagittis. Curabitur malesuada orci lectus, porttitor interdum libero suscipit et. Integer ac lacus vel metus sagittis commodo. Donec quis nisl iaculis, mollis turpis a, ornare nisi. Mauris auctor neque id dui semper ullamcorper. Curabitur pulvinar justo at blandit congue. Curabitur sollicitudin interdum mi, id convallis enim porta quis. Aliquam ultrices fringilla nibh finibus ornare. Ut fermentum sit amet turpis non fringilla. Pellentesque id ipsum aliquet lectus ultricies placerat eu in purus. Morbi ac elementum dolor, vel ultrices sem. Praesent imperdiet venenatis rutrum. Praesent leo nisi, mollis et purus ultricies, faucibus consequat metus. Pellentesque euismod ipsum eu aliquet accumsan. In lobortis ultricies diam nec feugiat. Nulla facilisi. Sed fringilla vehicula nulla, ut efficitur odio tincidunt in. Nunc tristique malesuada neque. Duis vestibulum in sapien eu fringilla. Ut eu sem at quam imperdiet varius. Cras nec leo tempus diam imperdiet vestibulum. Vestibulum tempus, ligula id aliquam ullamcorper, libero nisi consequat est, vitae consequat risus est ac ante. Aliquam malesuada blandit dolor blandit sagittis. Maecenas vulputate diam et consequat rutrum. Donec erat erat, aliquet non finibus at, consequat at mi. Duis vulputate accumsan ipsum in scelerisque. Proin a nisl dignissim, lobortis nisl quis, mattis ante. In tempor massa sit amet maximus maximus. Morbi sollicitudin, ex a dignissim blandit, nisi nisi pellentesque tellus, nec elementum lectus lacus id orci. Aliquam consectetur, enim imperdiet ultricies imperdiet, dui est luctus ipsum, dapibus sagittis velit dolor id neque.</p>'),
    ( 6, 15, 'This is the confidentiality agreement for Prairie Land Credit Bank'),
    ( 7, 18, 'This is the confidentiality agreement for GreenField Cooperative'),
    ( 8, 19, 'This is the confidentiality agreement for Agricore Alliance' ),
    ( 9, 20, 'This is the confidentiality agreement for CropCrest Association');