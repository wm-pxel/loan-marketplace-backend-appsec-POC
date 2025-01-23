-- Lambda account should not be the creator of deals/activities/documents
UPDATE activity_info SET created_by_id = 13 WHERE created_by_id = 12;
UPDATE deal_document SET created_by_id = 13 WHERE created_by_id = 12;
UPDATE deal_info SET created_by_id = 13 WHERE created_by_id = 12;
