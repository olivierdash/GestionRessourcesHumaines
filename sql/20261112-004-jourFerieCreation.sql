CREATE TABLE IF NOT EXISTS jour_ferie (
    id_jour_ferie  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jour           INTEGER,
    mois           INTEGER,
    annee          INTEGER,
    libelle        VARCHAR(25) NOT NULL,
    CONSTRAINT uq_jour_ferie UNIQUE (jour, mois, annee)
);

CREATE INDEX IF NOT EXISTS idx_jour_ferie_annee
    ON jour_ferie(annee);

    INSERT INTO jour_ferie (jour, mois, annee, libelle) VALUES
(1,  1,  NULL, 'Nouvel An'),
(29, 3,  NULL, 'Martyrs 1947'),
(1,  5,  NULL, 'Fête du Travail'),
(26, 6,  NULL, 'Indépendance'),
(15, 8,  NULL, 'Assomption'),
(1,  11, NULL, 'Toussaint'),
(25, 12, NULL, 'Noël');