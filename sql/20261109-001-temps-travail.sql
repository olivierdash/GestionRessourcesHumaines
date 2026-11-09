CREATE TABLE IF NOT EXISTS regime_travail (
    id_regime_travail SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    heures_hebdomadaires INTEGER NOT NULL CHECK (heures_hebdomadaires > 0),
    jours_hebdomadaires INTEGER NOT NULL CHECK (jours_hebdomadaires BETWEEN 1 AND 7),
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS horaire_travail (
    id_horaire_travail SERIAL PRIMARY KEY,
    id_regime_travail INTEGER NOT NULL REFERENCES regime_travail(id_regime_travail) ON DELETE CASCADE,
    jour_semaine VARCHAR(12) NOT NULL,
    heure_debut TIME,
    heure_fin TIME,
    pause_minutes INTEGER NOT NULL DEFAULT 0 CHECK (pause_minutes >= 0),
    CONSTRAINT uq_horaire_regime_jour UNIQUE (id_regime_travail, jour_semaine)
);

CREATE TABLE IF NOT EXISTS planning (
    id_planning SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    id_regime_travail INTEGER NOT NULL REFERENCES regime_travail(id_regime_travail),
    date_debut DATE NOT NULL,
    date_fin DATE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_planning_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE TABLE IF NOT EXISTS affectation_planning (
    id_affectation_planning SERIAL PRIMARY KEY,
    id_planning INTEGER NOT NULL REFERENCES planning(id_planning) ON DELETE CASCADE,
    id_employer INTEGER NOT NULL REFERENCES employer(id_employer),
    date_debut DATE NOT NULL,
    date_fin DATE,
    CONSTRAINT ck_affectation_planning_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE INDEX IF NOT EXISTS idx_affectation_planning_employe
    ON affectation_planning(id_employer, date_debut);
