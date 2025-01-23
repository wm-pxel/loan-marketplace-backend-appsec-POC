------------------------------------
--     Initialize User Roles      --
------------------------------------
INSERT INTO ROLE_DEF
     ( ROLE_CD, ROLE_NAME, ROLE_DESC, VISIBLE_IND )
       VALUES
     ( 'SUPER_ADM', 'Super Admin', 'Full administrative capabilities for application.', 'N' ),
     ( 'ACCESS_ALL_INST_DEALS', 'Deal Viewer', 'View all deals my institution has been invited.', 'Y' ),
     ( 'MNG_INST_USR', 'Institution User Manager', 'Add, edit, and remove users and their permissions for an institution.', 'Y' ),
     ( 'MNG_DEAL_MEMBERS', 'Deal User Manager', 'Add and remove my institutions team members included on a deal.', 'Y' ),
     ( 'NDA_MGR', 'NDA Manager', 'Add, edit, and sign non-disclosure agreements (NDAs) with institutions.', 'Y' ),
     ( 'BILLING_MGR', 'Billing Manager', 'Update billing and payment details for the marketplace.', 'Y' ),
     ( 'EDIT_DEAL_INFO', 'Deal Manager', 'Edit a deal, manage files and the fields within a deal.', 'Y' ),
     ( 'MNG_PART_INST', 'Deal Participant Manager', 'Invite institutions to a deal, confirm their allocation in the deal, and send virtual letters of commitment to participants.', 'Y' ),
     ( 'COMM_PART_USR', 'Participant Communicator', 'Communicate with participating institutions. Send updates to all participants and direct messages participant institutions.', 'Y' ),
     ( 'MNG_PART', 'Responding User', 'Respond to deal invitations and request data room access.', 'Y' ),
     ( 'COMMIT_USR', 'Commitment User', 'Commit to deals. Sign a virtual letter of commitment for a deal.', 'Y' ),
     ( 'COMM_ORIG_USR', 'Originator Communicator', 'Communicate with the originating institution. Send and receive direct messages with the originator.', 'Y' ),
     ( 'MNG_DEAL_FILES', 'File Manager', 'Manage a deal files. Upload, rename, replace and remove files.', 'Y' ),
     ( 'RECV_ALL_INST_INVS', 'Deal Invitation Recipient', 'Default contact for deal invitation, when deal invitation is sent will receive email notification for institution', 'Y' ),
     ( 'APP_SERVICE', 'Application Service', 'Role for application to application communications.', 'N' ),
     ( 'MNG_INST', 'Institution Manager', 'Edit institution details.', 'Y' );

------------------------------------
--     Initialize Deal Stages     --
------------------------------------
INSERT INTO STAGE_DEF
     ( STAGE_NAME, TITLE_DESC, SUBTITLE_DESC, ORDER_NBR )
       VALUES
     ( 'Event Created', NULL, NULL, 1 ),
     ( 'Gathering Interest', 'Launch Deal', 'Launch the deal to give interested, approved participants full deal access and start the commitment process. Before launch, invited participants will only be able to provide their interest in the deal.', 2 ),
     ( 'Launched', NULL, NULL, 3 ),
     ( 'Awaiting Draft Loan Documents', 'Upload Draft Loan Documents', 'Confirm when the Draft Loan Documents are uploaded in the Files tab for participants to review', 4 ),
     ( 'Draft Loan Documents Complete', 'Upload Final Loan Documents', 'Confirm when the Final Loan Documents are uploaded in the Files tab for participants to receive', 5 ),
     ( 'Final Loan Documentation Complete', NULL, NULL, 6 ),
     ( 'Awaiting Closing Memo', 'Upload Closing Memo', 'Confirm when the Closing Memo is uploaded in the Files tab for participants to receive', 7 ),
     ( 'Awaiting Closing', 'Close  Deal', 'Confirm when all steps in the deal origination and funding are complete', 8 ),
     ( 'Event Closed', NULL, NULL, 9 );

------------------------------------
--  Initialize Participant Steps  --
------------------------------------
INSERT INTO PARTICIPANT_STEP_DEF
     ( STEP_NAME, ORIG_STATUS_DESC, PART_STATUS_DESC, ORDER_NBR )
       VALUES
     ( 'Added to Deal as Draft', 'Complete the draft invitation to add a participant', NULL, 1 ),
     ( 'Invited to Deal', 'Waiting for participant interest', 'Review the invitation details and deal summary in the Information tab. Express interest in the deal to request full access upon deal launch.', 2 ),
     ( 'Indicated Interest', 'Approve full deal access to interested participant', 'Interest sent. Wait for the deal to be launched to view full deal Information.', 3 ),
     ( 'Awaiting Deal Launch', 'Full deal access approved', 'Interest sent. Wait for the deal to be launched to view full deal Information.', 4 ),
     ( 'Full Deal Access Provided', 'Waiting for participant commitment amount'
     , 'Full access to deal information now available.  Review full deal details in the Information and Files tabs and provide your institution''s commitment by the due date.', 5 ),
     ( 'Committed', 'Send the participant allocation amount', 'Commitment sent to Lead institution. Wait for deal allocations to be determined.', 6 ),
     ( 'Allocated', 'Allocation Sent', 'Review your institution’s allocation in the deal assigned by the Lead institution.', 7 ),
     ( 'Awaiting Participant Certificate', 'Send Participation Certificate document', 'Draft Loan Documents available for your review in the Files tab.', 8 ),
     ( 'Participant Certificate Provided', 'Waiting for the completed Participation Certificate'
     , 'Participation Certificate sent by the Lead institution. Upload or verify your completion of the Participation Certificate.', 9 ),
     ( 'Participant Certificate Signed', 'Participation Certificate complete', 'Participation Certificate completed.', 10 ),
     ( 'Declined', 'Participant declined deal', NULL, 11 ),
     ( 'Removed', 'Participant removed from deal', NULL, 12 );

------------------------------------
-- Initialize Document Categories --
------------------------------------
INSERT INTO DOCUMENT_CATEGORY_DEF
     ( DOCUMENT_CATEGORY_NAME, ORDER_NBR, DEAL_DOCUMENT_IND )
       VALUES
     ( 'Collateral', 4, 'Y' ),
     ( 'Entity Documents', 3, 'Y' ),
     ( 'Financials', 1, 'Y' ),
     ( 'Other / Misc', 7, 'Y' ),
     ( 'Participant Documents', 5, 'Y' ),
     ( 'Loan Documents', 6, 'Y' ),
     ( 'Underwriting', 2, 'Y' ),
     ( 'Commitment Letter', 8, 'N' ),
     ( 'Participant Certificate', 9, 'N' ),
     ( 'Signed Participant Certificate', 10, 'N' ),
     ( 'Pricing Grid', 11, 'N' );

------------------------------------
-- Initialize Picklist Categories --
------------------------------------
INSERT INTO PICKLIST_CATEGORY_DEF
     ( PICKLIST_CATEGORY_NAME )
       VALUES
     ( 'Farm Credit Eligibility' ),
     ( 'Deal Structure' ),
     ( 'Facility Type' ),
     ( 'Facility Purpose' ),
     ( 'Deal Industry'),
     ( 'Day Count' ),
     ( 'Deal Type' ),
     ( 'Collateral' );

------------------------------------
-- Initialize Picklist Categories --
------------------------------------
INSERT INTO PICKLIST_DEF
     ( PICKLIST_CATEGORY_ID, OPTION_NAME, ORDER_NBR )
       VALUES
     ( 1, 'Association Eligible', 1 ),
     ( 1, 'CoBank Eligible', 2 ),
     ( 1, 'Similar Entity', 3 ),
     ( 2, 'Participation', 1 ),
     ( 2, 'Syndication', 2 ),
     ( 3, 'Term', 1 ),
     ( 3, 'Revolver', 2 ),
     ( 3, 'Revolving Term Loan', 3 ),
     ( 3, 'Delayed Draw Term Loan', 4 ),
     ( 3, 'Other', 5 ),
     ( 4, 'Existing Business Expansion', 1 ),
     ( 4, 'New Construction', 2 ),
     ( 4, 'Real Estate', 3 ),
     ( 4, 'Refinance', 4 ),
     ( 4, 'Other', 5 ),
     ( 5, 'Farm Credit', 1 ),
     ( 5, 'Other', 2 ),
     ( 6, 'Actual/Actual', 1 ),
     ( 6, 'Actual/360', 2 ),
     ( 6, 'Actual/365', 3 ),
     ( 6, '30/360', 4 ),
     ( 6, '30/Actual', 5 ),
     ( 7, 'New', 1 ),
     ( 7, 'Renewal', 2 ),
     ( 7, 'Modification', 3 ),
     ( 8, 'Secured', 1 ),
     ( 8, 'Secured excluding real estate', 2),
     ( 8, 'Secured including real estate', 3 ),
     ( 8, 'Unsecured', 4 );

------------------------------------
--      Initialize US States      --
------------------------------------
INSERT INTO STATE_DEF
     ( STATE_CD, STATE_NAME )
       VALUES
     ( 'AL', 'Alabama' ),
     ( 'AK', 'Alaska' ),
     ( 'AZ', 'Arizona' ),
     ( 'AR', 'Arkansas' ),
     ( 'CA', 'California' ),
     ( 'CO', 'Colorado' ),
     ( 'CT', 'Connecticut' ),
     ( 'DE', 'Delaware' ),
     ( 'FL', 'Florida' ),
     ( 'GA', 'Georgia' ),
     ( 'HI', 'Hawaii' ),
     ( 'ID', 'Idaho' ),
     ( 'IL', 'Illinois' ),
     ( 'IN', 'Indiana' ),
     ( 'IA', 'Iowa' ),
     ( 'KS', 'Kansas' ),
     ( 'KY', 'Kentucky' ),
     ( 'LA', 'Louisiana' ),
     ( 'ME', 'Maine' ),
     ( 'MD', 'Maryland' ),
     ( 'MA', 'Massachusetts' ),
     ( 'MI', 'Michigan' ),
     ( 'MN', 'Minnesota' ),
     ( 'MS', 'Mississippi' ),
     ( 'MO', 'Missouri' ),
     ( 'MT', 'Montana' ),
     ( 'NE', 'Nebraska' ),
     ( 'NV', 'Nevada' ),
     ( 'NH', 'New Hampshire' ),
     ( 'NJ', 'New Jersey' ),
     ( 'NM', 'New Mexico' ),
     ( 'NY', 'New York' ),
     ( 'NC', 'North Carolina' ),
     ( 'ND', 'North Dakota' ),
     ( 'OH', 'Ohio' ),
     ( 'OK', 'Oklahoma' ),
     ( 'OR', 'Oregon' ),
     ( 'PA', 'Pennsylvania' ),
     ( 'RI', 'Rhode Island' ),
     ( 'SC', 'South Carolina' ),
     ( 'SD', 'South Dakota' ),
     ( 'TN', 'Tennessee' ),
     ( 'TX', 'Texas' ),
     ( 'UT', 'Utah' ),
     ( 'VT', 'Vermont' ),
     ( 'VA', 'Virginia' ),
     ( 'WA', 'Washington' ),
     ( 'WV', 'West Virginia' ),
     ( 'WI', 'Wisconsin' ),
     ( 'WY', 'Wyoming');

------------------------------------
--     Initialize Event Types     --
------------------------------------
INSERT INTO EVENT_TYPE_DEF
     ( EVENT_TYPE_NAME )
       VALUES
     ( 'Origination' ),
     ( 'Voting' ),
     ( 'Simple Renewal' ),
     ( 'Complex Renewal' );