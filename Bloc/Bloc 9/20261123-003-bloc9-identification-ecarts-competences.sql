-- =========================================================================
-- BLOC 9 : Évaluation, compétences et formation
-- Identifier les écarts entre compétences attendues et acquises
-- =========================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS competence (
    id_competence       SERIAL PRIMARY KEY,
    code                VARCHAR(30) NOT NULL UNIQUE,
    libelle             VARCHAR(150) NOT NULL,
    description         TEXT,
    niveau_max          INTEGER NOT NULL DEFAULT 5,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation       TIMESTAMP NOT NULL DEFAULT now(),
    date_maj            TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_competence_niveau_max
        CHECK (niveau_max BETWEEN 2 AND 10)
);

CREATE TABLE IF NOT EXISTS competence_attendue (
    id_competence_attendue  SERIAL PRIMARY KEY,
    id_employer             INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    id_poste                INTEGER REFERENCES poste(id_poste),
    id_competence           INTEGER NOT NULL REFERENCES competence(id_competence),
    niveau_attendu          INTEGER NOT NULL,
    date_debut              DATE NOT NULL DEFAULT CURRENT_DATE,
    date_fin                DATE,
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_competence_attendue_dates
        CHECK (date_fin IS NULL OR date_fin >= date_debut),
    CONSTRAINT ck_competence_attendue_niveau
        CHECK (niveau_attendu > 0)
);

CREATE INDEX IF NOT EXISTS idx_competence_attendue_employe
    ON competence_attendue(id_employer, id_competence);

CREATE TABLE IF NOT EXISTS competence_acquise (
    id_competence_acquise   SERIAL PRIMARY KEY,
    id_employer             INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    id_competence           INTEGER NOT NULL REFERENCES competence(id_competence),
    niveau_acquis           INTEGER NOT NULL,
    date_acquisition        DATE NOT NULL DEFAULT CURRENT_DATE,
    id_evaluateur           INTEGER REFERENCES utilisateur(id_utilisateur),
    source_evaluation       VARCHAR(30),
    id_evaluation           INTEGER REFERENCES evaluation(id_evaluation),
    commentaire             VARCHAR(500),
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_competence_acquise_niveau
        CHECK (niveau_acquis >= 0),
    CONSTRAINT ck_competence_acquise_source
        CHECK (source_evaluation IS NULL OR source_evaluation IN
               ('ENTRETIEN', 'TEST', 'FORMATION', 'AUTO_EVALUATION', 'MANAGER'))
);

CREATE INDEX IF NOT EXISTS idx_competence_acquise_employe
    ON competence_acquise(id_employer, id_competence);

CREATE TABLE IF NOT EXISTS ecart_competence (
    id_ecart_competence     SERIAL PRIMARY KEY,
    id_employer             INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    id_competence           INTEGER NOT NULL REFERENCES competence(id_competence),
    niveau_attendu          INTEGER NOT NULL,
    niveau_acquis           INTEGER NOT NULL,
    ecart                   INTEGER NOT NULL,
    niveau_ecart            VARCHAR(20) NOT NULL,
    date_analyse            DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire             VARCHAR(500),
    id_analyste             INTEGER REFERENCES utilisateur(id_utilisateur),
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_ecart_competence_niveaux
        CHECK (niveau_attendu >= 0 AND niveau_acquis >= 0),
    CONSTRAINT ck_ecart_competence_valeur
        CHECK (ecart = niveau_attendu - niveau_acquis),
    CONSTRAINT ck_ecart_competence_niveau
        CHECK (niveau_ecart IN ('AUCUN', 'FAIBLE', 'MODERE', 'ELEVE', 'CRITIQUE'))
);

CREATE INDEX IF NOT EXISTS idx_ecart_competence_employe
    ON ecart_competence(id_employer, date_analyse);

CREATE OR REPLACE VIEW vue_ecarts_competences AS
SELECT
    c.id_employer,
    c.id_competence,
    cmp.code,
    cmp.libelle,
    a.niveau_attendu,
    c.niveau_acquis,
    (a.niveau_attendu - c.niveau_acquis) AS ecart,
    CASE
        WHEN a.niveau_attendu <= c.niveau_acquis THEN 'AUCUN'
        WHEN a.niveau_attendu - c.niveau_acquis = 1 THEN 'FAIBLE'
        WHEN a.niveau_attendu - c.niveau_acquis = 2 THEN 'MODERE'
        WHEN a.niveau_attendu - c.niveau_acquis = 3 THEN 'ELEVE'
        ELSE 'CRITIQUE'
    END AS niveau_ecart
FROM competence_attendue a
JOIN LATERAL (
    SELECT ca.*
    FROM competence_acquise ca
    WHERE ca.id_employer = a.id_employer
      AND ca.id_competence = a.id_competence
      AND ca.date_acquisition <= COALESCE(a.date_fin, CURRENT_DATE)
    ORDER BY ca.date_acquisition DESC
    LIMIT 1
) c ON TRUE
JOIN competence cmp ON cmp.id_competence = a.id_competence
WHERE a.date_debut <= COALESCE(a.date_fin, CURRENT_DATE)
  AND COALESCE(a.date_fin, CURRENT_DATE) >= CURRENT_DATE;

COMMIT;
