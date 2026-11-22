CREATE TABLE IF NOT EXISTS type_decompte (
    id_type_decompte  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code              VARCHAR(20) NOT NULL UNIQUE,
    libelle           VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO type_decompte (code, libelle) VALUES
('HEURES', 'Décompte en heures'),
('JOURS', 'Décompte en jours');

-- 2.1 Ajout des colonnes manquantes
ALTER TABLE regime_travail
    ADD COLUMN IF NOT EXISTS code                          VARCHAR(30),
    ADD COLUMN IF NOT EXISTS type_decompte_id              INTEGER,
    ADD COLUMN IF NOT EXISTS jours_annuels_reference       INTEGER,
    ADD COLUMN IF NOT EXISTS pointage_obligatoire          BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS seuil_heures_supplementaires  DECIMAL(3,1),
    ADD COLUMN IF NOT EXISTS duree_max_hebdomadaire        INTEGER,
    ADD COLUMN IF NOT EXISTS duree_max_journaliere         INTEGER;

-- 2.2 heures_hebdomadaires : INTEGER → DECIMAL (pour 28h30)
ALTER TABLE regime_travail
    ALTER COLUMN heures_hebdomadaires TYPE DECIMAL(3,1);

-- 2.3 Suppression des CHECK (ta règle : backoffice garantit l'intégrité)
ALTER TABLE regime_travail DROP CONSTRAINT IF EXISTS regime_travail_heures_hebdomadaires_check;
ALTER TABLE regime_travail DROP CONSTRAINT IF EXISTS regime_travail_jours_hebdomadaires_check;

-- 2.4 Remplissage des données existantes
UPDATE regime_travail
SET code = 'R_' || id_regime_travail
WHERE code IS NULL;

UPDATE regime_travail
SET type_decompte_id = (SELECT id_type_decompte FROM type_decompte WHERE code = 'HEURES')
WHERE type_decompte_id IS NULL;

-- 2.5 NOT NULL après remplissage
ALTER TABLE regime_travail ALTER COLUMN code SET NOT NULL;
ALTER TABLE regime_travail ALTER COLUMN type_decompte_id SET NOT NULL;

-- 1. Supprimer la colonne jours_hebdomadaires (devenue inutile, redondante avec la table horaire_travail)
ALTER TABLE regime_travail DROP COLUMN jours_hebdomadaires;

-- 2. Rendre heures_hebdomadaires nullable (pour permettre le forfait jours sans heures)
ALTER TABLE regime_travail ALTER COLUMN heures_hebdomadaires DROP NOT NULL;

-- 2.6 Unicité sur code (index, idempotent)
CREATE UNIQUE INDEX IF NOT EXISTS uq_regime_travail_code
    ON regime_travail(code);

-- 2.7 FK (idempotent)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_regime_type_decompte') THEN
        ALTER TABLE regime_travail
            ADD CONSTRAINT fk_regime_type_decompte
            FOREIGN KEY (type_decompte_id) REFERENCES type_decompte(id_type_decompte);
    END IF;
END $$;