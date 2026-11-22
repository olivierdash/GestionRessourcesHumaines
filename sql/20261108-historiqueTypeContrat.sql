ALTER TABLE type_contrat
    ADD COLUMN IF NOT EXISTS duree_max_mois INTEGER;

CREATE TABLE IF NOT EXISTS historique_type_contrat (
    id_historique     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_type_contrat   INTEGER NOT NULL,
    id_utilisateur    INTEGER NOT NULL,
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),
    ancienne_duree    INTEGER,
    nouvelle_duree    INTEGER,
    CONSTRAINT fk_hist_tc_type FOREIGN KEY (id_type_contrat)
        REFERENCES type_contrat(id_type_contrat),
    CONSTRAINT fk_hist_tc_user FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
);

CREATE INDEX IF NOT EXISTS idx_hist_tc_type
    ON historique_type_contrat(id_type_contrat, date_modification DESC);

-- Seed : durées légales françaises
UPDATE type_contrat SET duree_max_mois = 18 WHERE libelle = 'CDD';
UPDATE type_contrat SET duree_max_mois = 6  WHERE libelle = 'STAGE';
-- CDI : NULL = pas de limite