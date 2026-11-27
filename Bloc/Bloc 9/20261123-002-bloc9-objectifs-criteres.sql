-- =========================================================================
-- BLOC 9 : Évaluation, compétences et formation
-- Objectifs et critères d'évaluation
-- =========================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS objectif_evaluation (
    id_objectif_evaluation  SERIAL PRIMARY KEY,
    id_employer             INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    titre                   VARCHAR(150) NOT NULL,
    description             TEXT,
    date_debut              DATE NOT NULL,
    date_fin                DATE NOT NULL,
    poids                   NUMERIC(5,2) NOT NULL DEFAULT 100,
    statut                  VARCHAR(20) NOT NULL DEFAULT 'DEFINI',
    id_createur             INTEGER REFERENCES utilisateur(id_utilisateur),
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_objectif_evaluation_dates
        CHECK (date_fin >= date_debut),
    CONSTRAINT ck_objectif_evaluation_poids
        CHECK (poids > 0 AND poids <= 100),
    CONSTRAINT ck_objectif_evaluation_statut
        CHECK (statut IN ('DEFINI', 'EN_COURS', 'ATTEINT', 'NON_ATTEINT', 'ANNULE'))
);

CREATE INDEX IF NOT EXISTS idx_objectif_evaluation_employe_dates
    ON objectif_evaluation(id_employer, date_debut, date_fin);

CREATE TABLE IF NOT EXISTS critere_evaluation (
    id_critere_evaluation   SERIAL PRIMARY KEY,
    code                    VARCHAR(30) NOT NULL UNIQUE,
    libelle                 VARCHAR(150) NOT NULL,
    description             TEXT,
    echelle_min             NUMERIC(5,2) NOT NULL DEFAULT 1,
    echelle_max             NUMERIC(5,2) NOT NULL DEFAULT 5,
    poids                   NUMERIC(5,2) NOT NULL DEFAULT 100,
    actif                   BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_critere_evaluation_echelle
        CHECK (echelle_max > echelle_min),
    CONSTRAINT ck_critere_evaluation_poids
        CHECK (poids > 0 AND poids <= 100)
);

CREATE TABLE IF NOT EXISTS objectif_critere_evaluation (
    id_objectif_evaluation  INTEGER NOT NULL
        REFERENCES objectif_evaluation(id_objectif_evaluation) ON DELETE CASCADE,
    id_critere_evaluation   INTEGER NOT NULL
        REFERENCES critere_evaluation(id_critere_evaluation) ON DELETE CASCADE,
    poids                   NUMERIC(5,2) NOT NULL DEFAULT 100,
    PRIMARY KEY (id_objectif_evaluation, id_critere_evaluation),
    CONSTRAINT ck_objectif_critere_poids
        CHECK (poids > 0 AND poids <= 100)
);

CREATE TABLE IF NOT EXISTS resultat_critere_evaluation (
    id_resultat_critere     SERIAL PRIMARY KEY,
    id_objectif_evaluation  INTEGER NOT NULL
        REFERENCES objectif_evaluation(id_objectif_evaluation) ON DELETE CASCADE,
    id_critere_evaluation   INTEGER NOT NULL
        REFERENCES critere_evaluation(id_critere_evaluation),
    id_evaluateur           INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    note                    NUMERIC(5,2) NOT NULL,
    commentaire             VARCHAR(500),
    date_evaluation         TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_resultat_critere_objectif
        UNIQUE (id_objectif_evaluation, id_critere_evaluation),
    CONSTRAINT ck_resultat_critere_note
        CHECK (note >= 0)
);

INSERT INTO critere_evaluation
    (code, libelle, description, echelle_min, echelle_max, poids)
VALUES
    ('QUALITE', 'Qualité du travail', 'Précision, fiabilité et conformité du travail réalisé.', 1, 5, 100),
    ('PRODUCTIVITE', 'Productivité', 'Capacité à atteindre les objectifs dans les délais prévus.', 1, 5, 100),
    ('AUTONOMIE', 'Autonomie', 'Capacité à prendre des décisions et résoudre les problèmes de manière indépendante.', 1, 5, 100),
    ('COLLABORATION', 'Collaboration', 'Contribution au travail collectif et qualité des relations professionnelles.', 1, 5, 100),
    ('COMPETENCES', 'Compétences techniques', 'Maîtrise des savoir-faire nécessaires au poste.', 1, 5, 100)
ON CONFLICT (code) DO NOTHING;

COMMIT;
