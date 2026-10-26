-- 1. Création de la nouvelle table de référence
CREATE TABLE motif_demande (
    id_motif_demande SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Insertion des données demandées
INSERT INTO motif_demande (libelle) 
VALUES ('Creation'), ('Remplacement d''un poste');

-- 3. Ajout de la nouvelle colonne (clé étrangère) dans la table demande_recrutement
ALTER TABLE demande_recrutement 
ADD COLUMN id_motif_demande INTEGER REFERENCES motif_demande(id_motif_demande);

-- 4. (Optionnel) Migration des données existantes
-- Si tu as déjà des demandes avec un motif texte, on leur assigne le motif "Creation" par défaut
UPDATE demande_recrutement 
SET id_motif_demande = (SELECT id_motif_demande FROM motif_demande WHERE libelle = 'Creation')
WHERE id_motif_demande IS NULL;

-- 5. Rendre la colonne obligatoire (NOT NULL)
ALTER TABLE demande_recrutement 
ALTER COLUMN id_motif_demande SET NOT NULL;

-- 6. Suppression de l'ancienne colonne texte
ALTER TABLE demande_recrutement 
DROP COLUMN motif;