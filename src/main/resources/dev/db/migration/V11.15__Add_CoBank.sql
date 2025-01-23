
INSERT INTO INSTITUTION_INFO
( INSTITUTION_UUID, INSTITUTION_NAME, OWNER_NAME, PERMISSION_SET_DESC, SSO_IND, ACTIVE_IND )
VALUES
    ( '01912bed-483e-79da-8922-bf3447c032c7', 'CoBank', '', 'Lead and Participant', 'Y', 'Y' );


INSERT INTO INSTITUTION_CONFIDENTIALITY_AGREEMENT
( INSTITUTION_ID, CREATED_BY_ID, CONF_AGRMNT_DESC )
VALUES
    ((SELECT institution_id FROM institution_info WHERE institution_name = 'CoBank')
     , 1, 'Placeholder confidentiality agreement for CoBank');


INSERT INTO USER_INFO
( USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND, SYSTEM_USER_IND )
VALUES
    ( gen_random_uuid(), (SELECT institution_id FROM institution_info WHERE institution_name = 'CoBank'), 'CoBank', 'System', 'SystemUser-01912bed-483e-79da-8922-bf3447c032c7', '', 'Y', 'Y' );
