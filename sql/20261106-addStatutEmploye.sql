-- ============================================================
-- 1. Table de référence
-- ============================================================
CREATE TABLE statut_employe (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO statut_employe (libelle) VALUES
('ACTIF'),
('INACTIF'),
('EN_ONBOARDING');

-- ============================================================
-- 2. Ajout de la FK sur employer
-- ============================================================
ALTER TABLE employer ADD COLUMN statut_employe_id INT;

ALTER TABLE employer ADD CONSTRAINT fk_statut_employe
    FOREIGN KEY (statut_employe_id) REFERENCES statut_employe(id);

UPDATE employer
SET statut_employe_id = (SELECT id FROM statut_employe WHERE libelle = 'ACTIF');

ALTER TABLE employer
    ALTER COLUMN statut_employe_id SET NOT NULL;

-- ============================================================
-- 5. Ancienne colonne String : conservée temporairement, nullable
--    À supprimer une fois toutes les couches migrées :
--    ALTER TABLE employer DROP COLUMN status_employer;
-- ============================================================
ALTER TABLE employer
    ALTER COLUMN status_employer DROP NOT NULL;