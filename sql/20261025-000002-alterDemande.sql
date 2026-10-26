ALTER TABLE demande_recrutement 
ADD COLUMN id_etat_demande INTEGER REFERENCES etat_demande(id_etat_demande);


ALTER TABLE demande_recrutement 
DROP COLUMN statut_demande_recrutement CASCADE; 

INSERT INTO etat_demande (intitule) 
VALUES ('Brouillon'), ('Cree'),('ValideRH'),('ValideFinance'),('Valide')
ON CONFLICT (intitule) DO NOTHING;

INSERT INTO etat_demande (intitule) 
VALUES ('Refuse');

UPDATE demande_recrutement 
SET id_etat_demande = (SELECT id_etat_demande FROM etat_demande WHERE intitule = 'Brouillon')
WHERE id_etat_demande IS NULL;

ALTER TABLE demande_recrutement 
ALTER COLUMN id_etat_demande SET NOT NULL;

ALTER TABLE etat_demande 
ALTER COLUMN intitule SET NOT NULL;

ALTER TABLE etat_demande 
ADD CONSTRAINT uk_etatDemande_intitule UNIQUE (intitule);

ALTER TABLE demande_recrutement
ADD COLUMN commentaires TEXT;

ALTER TABLE historique_demande_recrutement
ADD COLUMN commentaires TEXT;

