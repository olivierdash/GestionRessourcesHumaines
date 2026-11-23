-- =========================================================================
-- BLOC 9 : Donnees de test
-- A executer apres les scripts schema du Bloc 9
-- =========================================================================

BEGIN;

-- Competences de reference
INSERT INTO competence (code, libelle, description, niveau_max, actif)
VALUES
    ('JAVA', 'Programmation Java', 'Maîtrise du développement Java et des bonnes pratiques.', 5, TRUE),
    ('SPRING', 'Spring Framework', 'Conception d applications avec Spring Boot.', 5, TRUE),
    ('SQL', 'SQL et bases de donnees', 'Modélisation, requêtes et optimisation SQL.', 5, TRUE),
    ('COMMUNICATION', 'Communication professionnelle', 'Communication écrite, orale et relations interpersonnelles.', 5, TRUE),
    ('GESTION_PROJET', 'Gestion de projet', 'Planification, suivi des tâches et gestion des priorités.', 5, TRUE)
ON CONFLICT (code) DO UPDATE
SET libelle = EXCLUDED.libelle,
    description = EXCLUDED.description,
    niveau_max = EXCLUDED.niveau_max,
    actif = EXCLUDED.actif;

-- Objectifs d evaluation pour les quatre employés de test
INSERT INTO objectif_evaluation (
    id_employer,
    titre,
    description,
    date_debut,
    date_fin,
    poids,
    statut,
    id_createur
)
SELECT
    employe.id_employer,
    'Améliorer les compétences techniques',
    'Atteindre les objectifs techniques définis pour le poste sur la période.',
    CURRENT_DATE - 90,
    CURRENT_DATE + 90,
    40,
    'EN_COURS',
    (SELECT id_utilisateur FROM utilisateur WHERE nom = 'Alice RH' LIMIT 1)
FROM employer employe
WHERE employe.id_employer IN (1, 2, 3, 4)
ON CONFLICT DO NOTHING;

INSERT INTO objectif_evaluation (
    id_employer,
    titre,
    description,
    date_debut,
    date_fin,
    poids,
    statut,
    id_createur
)
SELECT
    employe.id_employer,
    'Renforcer la collaboration',
    'Améliorer la communication avec l équipe et la gestion des priorités.',
    CURRENT_DATE - 60,
    CURRENT_DATE + 120,
    30,
    'EN_COURS',
    (SELECT id_utilisateur FROM utilisateur WHERE nom = 'Bob Manager' LIMIT 1)
FROM employer employe
WHERE employe.id_employer IN (1, 2, 3, 4)
ON CONFLICT DO NOTHING;

-- Competences attendues selon les postes
INSERT INTO competence_attendue (
    id_employer,
    id_poste,
    id_competence,
    niveau_attendu,
    date_debut,
    date_fin
)
SELECT
    employe.id_employer,
    affectation.id_poste,
    competence.id_competence,
    CASE competence.code
        WHEN 'JAVA' THEN 4
        WHEN 'SPRING' THEN 4
        WHEN 'SQL' THEN 3
        WHEN 'COMMUNICATION' THEN 3
        WHEN 'GESTION_PROJET' THEN 3
    END,
    CURRENT_DATE - 120,
    CURRENT_DATE + 180
FROM employer employe
JOIN affectation
    ON affectation.id_affectation = (
        SELECT a.id_affectation
        FROM affectation a
        WHERE a.id_employer = employe.id_employer
          AND a.date_fin IS NULL
        ORDER BY a.date_debut DESC
        LIMIT 1
    )
CROSS JOIN competence
WHERE employe.id_employer IN (1, 2, 3, 4)
  AND competence.code IN ('JAVA', 'SPRING', 'SQL', 'COMMUNICATION', 'GESTION_PROJET')
ON CONFLICT DO NOTHING;

-- Competences acquises
INSERT INTO competence_acquise (
    id_employer,
    id_competence,
    niveau_acquis,
    date_acquisition,
    id_evaluateur,
    source_evaluation,
    commentaire
)
SELECT
    employe.id_employer,
    competence.id_competence,
    CASE competence.code
        WHEN 'JAVA' THEN CASE employe.id_employer WHEN 1 THEN 4 WHEN 2 THEN 3 WHEN 3 THEN 2 WHEN 4 THEN 2 END
        WHEN 'SPRING' THEN CASE employe.id_employer WHEN 1 THEN 4 WHEN 2 THEN 3 WHEN 3 THEN 2 WHEN 4 THEN 1 END
        WHEN 'SQL' THEN CASE employe.id_employer WHEN 1 THEN 3 WHEN 2 THEN 4 WHEN 3 THEN 2 WHEN 4 THEN 2 END
        WHEN 'COMMUNICATION' THEN CASE employe.id_employer WHEN 1 THEN 4 WHEN 2 THEN 4 WHEN 3 THEN 3 WHEN 4 THEN 2 END
        WHEN 'GESTION_PROJET' THEN CASE employe.id_employer WHEN 1 THEN 3 WHEN 2 THEN 3 WHEN 3 THEN 2 WHEN 4 THEN 2 END
    END,
    CURRENT_DATE - 30,
    (SELECT id_utilisateur FROM utilisateur WHERE nom = 'Alice RH' LIMIT 1),
    'MANAGER',
    'Niveau constaté lors de la dernière évaluation.'
FROM employer employe
CROSS JOIN competence
WHERE employe.id_employer IN (1, 2, 3, 4)
  AND competence.code IN ('JAVA', 'SPRING', 'SQL', 'COMMUNICATION', 'GESTION_PROJET')
ON CONFLICT DO NOTHING;

-- Écarts entre niveaux attendus et acquis
INSERT INTO ecart_competence (
    id_employer,
    id_competence,
    niveau_attendu,
    niveau_acquis,
    ecart,
    niveau_ecart,
    date_analyse,
    commentaire,
    id_analyste
)
SELECT
    attendue.id_employer,
    attendue.id_competence,
    attendue.niveau_attendu,
    acquise.niveau_acquis,
    attendue.niveau_attendu - acquise.niveau_acquis,
    CASE
        WHEN attendue.niveau_attendu <= acquise.niveau_acquis THEN 'AUCUN'
        WHEN attendue.niveau_attendu - acquise.niveau_acquis = 1 THEN 'FAIBLE'
        WHEN attendue.niveau_attendu - acquise.niveau_acquis = 2 THEN 'MODERE'
        WHEN attendue.niveau_attendu - acquise.niveau_acquis = 3 THEN 'ELEVE'
        ELSE 'CRITIQUE'
    END,
    CURRENT_DATE,
    'Analyse automatique des compétences attendues et acquises.',
    (SELECT id_utilisateur FROM utilisateur WHERE nom = 'Alice RH' LIMIT 1)
FROM competence_attendue attendue
JOIN LATERAL (
    SELECT acquise.niveau_acquis
    FROM competence_acquise acquise
    WHERE acquise.id_employer = attendue.id_employer
      AND acquise.id_competence = attendue.id_competence
    ORDER BY acquise.date_acquisition DESC
    LIMIT 1
) acquise ON TRUE
ON CONFLICT DO NOTHING;

-- Associations objectifs - critères
INSERT INTO objectif_critere_evaluation (
    id_objectif_evaluation,
    id_critere_evaluation,
    poids
)
SELECT
    objectif.id_objectif_evaluation,
    critere.id_critere_evaluation,
    100
FROM objectif_evaluation objectif
CROSS JOIN critere_evaluation critere
WHERE objectif.id_employer IN (1, 2, 3, 4)
  AND critere.code IN ('QUALITE', 'PRODUCTIVITE', 'AUTONOMIE', 'COLLABORATION', 'COMPETENCES')
ON CONFLICT DO NOTHING;

-- Besoin de formation pour les écarts positifs
INSERT INTO besoin_formation (
    id_employer,
    id_competence,
    id_ecart_competence,
    titre,
    description,
    priorite,
    statut,
    date_identification,
    date_souhaitee,
    id_demandeur
)
SELECT
    ecart.id_employer,
    ecart.id_competence,
    ecart.id_ecart_competence,
    'Formation : ' || competence.libelle,
    'Développer la compétence identifiée comme insuffisante lors de l évaluation.',
    CASE ecart.niveau_ecart
        WHEN 'CRITIQUE' THEN 'CRITIQUE'
        WHEN 'ELEVE' THEN 'HAUTE'
        WHEN 'MODERE' THEN 'MOYENNE'
        ELSE 'FAIBLE'
    END,
    'IDENTIFIE',
    CURRENT_DATE,
    CURRENT_DATE + 60,
    (SELECT id_utilisateur FROM utilisateur WHERE nom = 'Alice RH' LIMIT 1)
FROM ecart_competence ecart
JOIN competence ON competence.id_competence = ecart.id_competence
WHERE ecart.ecart > 0
ON CONFLICT DO NOTHING;

-- Déclenchements enregistrés
INSERT INTO declenchement_besoin_formation (
    id_besoin_formation,
    motif,
    message,
    id_utilisateur
)
SELECT
    besoin.id_besoin_formation,
    CASE
        WHEN ecart.niveau_ecart = 'CRITIQUE' THEN 'ECART_CRITIQUE'
        WHEN ecart.niveau_ecart = 'ELEVE' THEN 'ECART_ELEVE'
        ELSE 'ECART_MODERE'
    END,
    'Besoin déclenché automatiquement à partir des données de test du Bloc 9.',
    besoin.id_demandeur
FROM besoin_formation besoin
JOIN ecart_competence ecart
    ON ecart.id_ecart_competence = besoin.id_ecart_competence
ON CONFLICT DO NOTHING;

-- Historiques initiaux
INSERT INTO historique_besoin_formation (
    id_besoin_formation,
    statut_avant,
    statut_apres,
    commentaire,
    id_utilisateur
)
SELECT
    id_besoin_formation,
    NULL,
    'IDENTIFIE',
    'Création du besoin avec les données de test.',
    id_demandeur
FROM besoin_formation
WHERE statut = 'IDENTIFIE'
ON CONFLICT DO NOTHING;

-- Formations de test
INSERT INTO formation (
    id_besoin_formation,
    titre,
    organisme,
    mode_formation,
    date_debut,
    date_fin,
    duree_heures,
    statut,
    commentaire
)
SELECT
    besoin.id_besoin_formation,
    'Formation pratique : ' || competence.libelle,
    'Centre SIRH Formation',
    'PRESENTIEL',
    CURRENT_DATE + 7,
    CURRENT_DATE + 9,
    21,
    'PLANIFIEE',
    'Formation proposée suite à un écart de compétence.'
FROM besoin_formation besoin
JOIN competence ON competence.id_competence = besoin.id_competence
WHERE besoin.statut = 'IDENTIFIE'
LIMIT 4;

-- Participations aux formations
INSERT INTO participation_formation (
    id_formation,
    id_employer,
    statut,
    taux_presence,
    note_finale,
    certificat_obtenu,
    commentaire
)
SELECT
    formation.id_formation,
    besoin.id_employer,
    'INSCRIT',
    NULL,
    NULL,
    FALSE,
    'Inscription créée avec les données de test.'
FROM formation
JOIN besoin_formation besoin
    ON besoin.id_besoin_formation = formation.id_besoin_formation
ON CONFLICT (id_formation, id_employer) DO NOTHING;

-- Mesures d évolution avant/après formation
INSERT INTO mesure_evolution_formation (
    id_participation,
    id_competence,
    niveau_avant,
    niveau_apres,
    niveau_attendu,
    mesure,
    date_mesure_avant,
    date_mesure_apres,
    commentaire,
    id_evaluateur
)
SELECT
    participation.id_participation,
    besoin.id_competence,
    ecart.niveau_acquis,
    NULL,
    ecart.niveau_attendu,
    'NIVEAU_COMPETENCE',
    CURRENT_DATE - 30,
    NULL,
    'Mesure après formation à renseigner.',
    (SELECT id_utilisateur FROM utilisateur WHERE nom = 'Alice RH' LIMIT 1)
FROM participation_formation participation
JOIN formation
    ON formation.id_formation = participation.id_formation
JOIN besoin_formation besoin
    ON besoin.id_besoin_formation = formation.id_besoin_formation
JOIN ecart_competence ecart
    ON ecart.id_ecart_competence = besoin.id_ecart_competence
ON CONFLICT DO NOTHING;

COMMIT;
