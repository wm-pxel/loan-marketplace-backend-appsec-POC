------------------------------------
--   Create Test Activities       --
------------------------------------

INSERT INTO ACTIVITY_INFO
     ( DEAL_ID, PARTICIPANT_ID, ACTIVITY_TYPE_ID, ACTIVITY_JSON, SOURCE_CD, CREATED_BY_ID )
       VALUES
     ( 5, NULL, 3, '{"message" : "Deal Created in Lamina"}', 'M', 12 ),
     ( 6, NULL, 3, '{"message" : "Deal Created in Lamina"}', 'M', 2 ),
     ( 7, NULL, 3, '{"message" : "Deal Created in Lamina"}', 'M', 2 ),
     ( 8, NULL, 3, '{"message" : "Deal Created in Lamina"}', 'M', 12 );
