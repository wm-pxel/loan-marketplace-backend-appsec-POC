

UPDATE user_info
SET institution_id = (SELECT institution_id FROM institution_info WHERE institution_uuid = '1cb7d8dd-06bb-48fb-bf91-bd1d3db322ba')
WHERE email_addr like '%lmdev@westmonroe.com';
