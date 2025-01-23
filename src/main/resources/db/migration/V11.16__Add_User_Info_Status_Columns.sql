------------------------------------
--   Create Invite Status Table   --
------------------------------------
CREATE TABLE INVITE_STATUS_DEF (
    INVITE_STATUS_CD            VARCHAR(1)   NOT NULL,
    INVITE_STATUS_DESC          VARCHAR(50)  NOT NULL,
    PRIMARY KEY ( INVITE_STATUS_CD )
);

------------------------------------
--   Initialize Invite Statuses   --
------------------------------------
INSERT INTO INVITE_STATUS_DEF
     ( INVITE_STATUS_CD, INVITE_STATUS_DESC )
       VALUES
     ( 'I', 'Invited' ),
     ( 'C', 'Completed' );

------------------------------------------
-- Add Invitation Columns to User Table --
------------------------------------------
ALTER TABLE USER_INFO
  ADD COLUMN INVITE_STATUS_CD VARCHAR(1) NULL,
  ADD FOREIGN KEY ( INVITE_STATUS_CD ) REFERENCES INVITE_STATUS_DEF ( INVITE_STATUS_CD );