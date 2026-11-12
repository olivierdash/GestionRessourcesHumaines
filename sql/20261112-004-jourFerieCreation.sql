CREATE TABLE IF NOT EXISTS jour_ferie (
    id_jour_ferie  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jour           INTEGER NOT NULL,
    mois           INTEGER NOT NULL,
    annee          INTEGER, -- NULL pour les fêtes fixes annuelles
    libelle        VARCHAR(25) NOT NULL
);

-- Index partiel pour les fêtes fixes (ex: 01/01 tous les ans)
CREATE UNIQUE INDEX IF NOT EXISTS uq_jour_ferie_fixe
    ON jour_ferie(jour, mois)
    WHERE annee IS NULL;

-- Index partiel pour les fêtes mobiles spécifiques à une année (ex: Aïd 2025)
CREATE UNIQUE INDEX IF NOT EXISTS uq_jour_ferie_mobile
    ON jour_ferie(jour, mois, annee)
    WHERE annee IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_jour_ferie_annee
    ON jour_ferie(annee);