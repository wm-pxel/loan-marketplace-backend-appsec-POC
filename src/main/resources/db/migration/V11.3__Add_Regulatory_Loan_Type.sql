INSERT INTO PICKLIST_CATEGORY_DEF ( PICKLIST_CATEGORY_NAME ) VALUES ('Regulatory Loan Type');

INSERT INTO PICKLIST_DEF ( PICKLIST_CATEGORY_ID, OPTION_NAME, ORDER_NBR ) VALUES
	(9, 'Agribusiness - Farm Related Business', 1),
	(9, 'Agribusiness - Loans to Cooperatives', 2),
	(9, 'Agribusiness - Processing and Marketing', 3),
	(9, 'Communications', 4),
	(9, 'Energy', 5),
	(9, 'International', 6),
	(9, 'Production Agriculture - Prod and Interm', 7),
	(9, 'Production Agriculture - RE Mort', 8),
	(9, 'Water / Waste Disposal', 9);

ALTER TABLE DEAL_FACILITY
ADD COLUMN REGULATORY_LOAN_TYPE_ID INTEGER NULL;
