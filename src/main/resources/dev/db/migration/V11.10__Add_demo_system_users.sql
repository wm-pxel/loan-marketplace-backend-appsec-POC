
-- Create a system user for each of the existing institutions
INSERT INTO user_info (user_uuid, institution_id, first_name, last_name, email_addr, active_ind, system_user_ind)
(SELECT gen_random_uuid(), institution_id, institution_name, 'System', CONCAT('SystemUser-', institution_uuid), 'Y', 'Y' FROM institution_info)
ON CONFLICT DO NOTHING;
