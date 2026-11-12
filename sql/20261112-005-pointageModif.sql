DROP TABLE IF EXISTS pointage CASCADE;

CREATE TABLE pointage (
    id_pointage      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_employer      INTEGER NOT NULL REFERENCES employer(id_employer),
    date_pointage    DATE NOT NULL,
    heure_entree     TIME,
    heure_sortie     TIME,
    saisi_par        INTEGER REFERENCES utilisateur(id_utilisateur),
    commentaire      VARCHAR(500),
    CONSTRAINT uq_pointage_employe_date UNIQUE (id_employer, date_pointage)
);

CREATE INDEX IF NOT EXISTS idx_pointage_employe_date
    ON pointage(id_employer, date_pointage);