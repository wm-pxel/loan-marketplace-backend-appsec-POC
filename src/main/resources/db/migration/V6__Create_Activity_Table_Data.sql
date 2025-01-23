--------------------------------
-- Create Activity Type Table --
--------------------------------
CREATE TABLE ACTIVITY_CATEGORY_DEF (
   ACTIVITY_CATEGORY_ID        SERIAL       NOT NULL,
   ACTIVITY_CATEGORY_NAME      VARCHAR(100) NOT NULL,
   PRIMARY KEY ( ACTIVITY_CATEGORY_ID )
);

------------------------------------
-- Initialize Activity Categories --
------------------------------------
INSERT INTO ACTIVITY_CATEGORY_DEF
     ( ACTIVITY_CATEGORY_NAME )
       VALUES
     ( 'Team' ),
     ( 'Information' ),
     ( 'Files' ),
     ( 'Participation' );

--------------------------------
-- Create Activity Type Table --
--------------------------------
CREATE TABLE ACTIVITY_TYPE_DEF (
    ACTIVITY_TYPE_ID            SERIAL       NOT NULL,
    ACTIVITY_TYPE_NAME          VARCHAR(100) NOT NULL,
    ACTIVITY_CATEGORY_ID        INTEGER      NOT NULL,
    PRIMARY KEY ( ACTIVITY_TYPE_ID ),
    FOREIGN KEY ( ACTIVITY_CATEGORY_ID ) REFERENCES ACTIVITY_CATEGORY_DEF ( ACTIVITY_CATEGORY_ID )
);

------------------------------------
--   Initialize Activity Types    --
------------------------------------
INSERT INTO ACTIVITY_TYPE_DEF
     ( ACTIVITY_TYPE_NAME, ACTIVITY_CATEGORY_ID )
       VALUES
     ( 'Team Member Added', 1 ),    -- Keep this special case for lead as #1
     ( 'Team Member Removed', 1 ),  -- Keep this special case for lead as #2
     ( 'Deal Created', 4 ),
     ( 'Deal Info Updated', 2 ),
     ( 'File Uploaded', 3 ),
     ( 'File Renamed', 3 ),
     ( 'File Removed', 3 ),
     ( 'Invite Sent', 4 ),
     ( 'Deal Interest', 4 ),
     ( 'Full Deal Access', 4 ),
     ( 'Deal Launched', 4 ),
     ( 'Commitments Sent', 4 ),
     ( 'Allocations Sent', 4 ),
     ( 'Deal Declined', 4 ),
     ( 'Participant Removed', 4 ),
     ( 'Deal Dates Updated', 4 ),
     ( 'Participation Certificate Sent', 4 ),
     ( 'Signed Participation Certificate Sent', 4 ),
     ( 'Draft Loan Documents Uploaded', 4 ),
     ( 'Final Loan Documents Uploaded', 4 ),
     ( 'Closing Memo Uploaded', 4 ),
     ( 'Deal Closed', 4 );

--------------------------------
--   Create Activity Table    --
--------------------------------
CREATE TABLE ACTIVITY_INFO (
    ACTIVITY_ID                 SERIAL      NOT NULL,
    DEAL_ID                     INTEGER     NOT NULL,
    PARTICIPANT_ID              INTEGER     NULL,
    ACTIVITY_TYPE_ID            INTEGER     NOT NULL,
    ACTIVITY_JSON               JSONB       NULL,
    SOURCE_CD                   VARCHAR(1)  NOT NULL,
    CREATED_BY_ID               INTEGER     NOT NULL,
    CREATED_DATE                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ( ACTIVITY_ID ),
    FOREIGN KEY ( DEAL_ID ) REFERENCES DEAL_INFO ( DEAL_ID ),
    FOREIGN KEY ( PARTICIPANT_ID ) REFERENCES INSTITUTION_INFO ( INSTITUTION_ID ),
    FOREIGN KEY ( ACTIVITY_TYPE_ID ) REFERENCES ACTIVITY_TYPE_DEF ( ACTIVITY_TYPE_ID ),
    FOREIGN KEY ( CREATED_BY_ID ) REFERENCES USER_INFO ( USER_ID )
);

CREATE INDEX IDX_ACTIVITY_INFO_DEAL_PARTICIPANT ON ACTIVITY_INFO ( DEAL_ID, PARTICIPANT_ID );