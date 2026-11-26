BEGIN;

CREATE TABLE IF NOT EXISTS onboarding_modele (
    id_modele SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    actif BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS onboarding_modele_tache (
    id_tache_modele SERIAL PRIMARY KEY,
    id_modele INTEGER NOT NULL REFERENCES onboarding_modele(id_modele) ON DELETE CASCADE,
    titre VARCHAR(255) NOT NULL,
    id_role_responsable INTEGER NOT NULL REFERENCES roles(id_role),
    delai_jours INTEGER NOT NULL CHECK (delai_jours >= 0),
    est_obligatoire BOOLEAN NOT NULL DEFAULT TRUE,
    id_type_document_requis INTEGER REFERENCES type_document(id_type_document) ON DELETE SET NULL,
    ordre INTEGER NOT NULL CHECK (ordre >= 1)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_onboarding_modele_actif
    ON onboarding_modele (actif)
    WHERE actif = TRUE;

ALTER TABLE onboarding_tache
    ADD COLUMN IF NOT EXISTS ordre INTEGER NOT NULL DEFAULT 0;

ALTER TABLE onboarding_tache
    ADD COLUMN IF NOT EXISTS id_type_document_requis INTEGER
        REFERENCES type_document(id_type_document) ON DELETE SET NULL;

WITH taches_ordre AS (
    SELECT id_tache,
           ROW_NUMBER() OVER (PARTITION BY id_onboarding ORDER BY date_echeance NULLS LAST, id_tache) AS nouvel_ordre
    FROM onboarding_tache
)
UPDATE onboarding_tache AS t
SET ordre = taches_ordre.nouvel_ordre
FROM taches_ordre
WHERE t.id_tache = taches_ordre.id_tache
  AND t.ordre = 0;

INSERT INTO type_document (libelle)
VALUES ('Contrat signé'), ('RIB')
ON CONFLICT (libelle) DO NOTHING;

UPDATE onboarding_tache AS t
SET id_type_document_requis = td.id_type_document
FROM type_document AS td
WHERE t.id_type_document_requis IS NULL
  AND ((t.titre = 'Contrat signé' AND td.libelle = 'Contrat signé')
       OR (t.titre = 'Inscription à la paie' AND td.libelle = 'RIB'));

INSERT INTO onboarding_modele (nom, actif)
SELECT 'Parcours standard', NOT EXISTS (
    SELECT 1 FROM onboarding_modele WHERE actif = TRUE
)
WHERE NOT EXISTS (
    SELECT 1 FROM onboarding_modele WHERE LOWER(nom) = LOWER('Parcours standard')
);

WITH definitions (ordre, titre, delai_jours, est_obligatoire, type_document) AS (
    VALUES
        (1, 'Contrat signé', 3, TRUE, 'Contrat signé'),
        (2, 'Formation', 14, TRUE, NULL::VARCHAR),
        (3, 'Inscription à la paie', 14, TRUE, 'RIB')
)
INSERT INTO onboarding_modele_tache (
    id_modele, titre, id_role_responsable, delai_jours,
    est_obligatoire, id_type_document_requis, ordre
)
SELECT m.id_modele, d.titre, r.id_role, d.delai_jours,
       d.est_obligatoire, td.id_type_document, d.ordre
FROM onboarding_modele AS m
CROSS JOIN definitions AS d
JOIN roles AS r ON UPPER(r.libelle) = 'RH'
LEFT JOIN type_document AS td ON td.libelle = d.type_document
WHERE m.nom = 'Parcours standard'
  AND NOT EXISTS (
      SELECT 1 FROM onboarding_modele_tache AS mt WHERE mt.id_modele = m.id_modele
  );

COMMIT;
