ALTER TABLE document ADD COLUMN date_expiration DATE;
ALTER TABLE document ADD COLUMN statut_document VARCHAR(50) DEFAULT 'VALIDE';