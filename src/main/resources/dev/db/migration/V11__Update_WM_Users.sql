UPDATE user_info
SET email_addr = 'iiykeazubogu+lmdev@westmonroe.com'
WHERE user_uuid = 'b5fc4bcf-d970-4caa-9642-56311585c032';

INSERT INTO user_info (USER_UUID, INSTITUTION_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDR, PASSWORD_DESC, ACTIVE_IND)
VALUES ('fec29793-5296-4f90-aa09-cc21fdfcfec2', 10, 'Gavin', 'Winkel', 'gwinkel+lmdev@westmonroe.com', '', 'Y'),
       ('fec29793-5296-4f90-aa09-cc21fdfcfec3', 5, 'Gavin', 'Winkel', 'gwinkel+lmorig@westmonroe.com', '', 'Y'),
       ('fec29793-5296-4f90-aa09-cc21fdfcfec4', 6, 'Gavin', 'Winkel', 'gwinkel+lmpart1@westmonroe.com', '', 'Y');


INSERT INTO USER_ROLE_XREF
    (USER_ID, ROLE_ID)
SELECT UI.USER_ID
     , RD.ROLE_ID
FROM USER_INFO UI
   , ROLE_DEF RD
WHERE UI.email_addr IN
      ('gwinkel+lmdev@westmonroe.com',
       'gwinkel+lmorig@westmonroe.com',
       'gwinkel+lmpart1@westmonroe.com')
  AND RD.ROLE_ID > 1
  AND RD.ROLE_ID < 14;

