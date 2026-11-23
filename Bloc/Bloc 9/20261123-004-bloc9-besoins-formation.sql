-- =========================================================================
-- BLOC 9 : Évaluation, compétences et formation
-- Déclenchement des besoins de formation
-- =========================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS besoin_formation (
    id_besoin_formation     SERIAL PRIMARY KEY,
    id_employer             INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    id_competence           INTEGER NOT NULL REFERENCES competence(id_competence),
    id_ecart_competence     INTEGER REFERENCES ecart_competence(id_ecart_competence),
    titre                   VARCHAR(150) NOT NULL,
    description             TEXT,
    priorite                VARCHAR(20) NOT NULL DEFAULT 'MOYENNE',
    statut                  VARCHAR(30) NOT NULL DEFAULT 'IDENTIFIE',
    date_identification     DATE NOT NULL DEFAULT CURRENT_DATE,
    date_souhaitee          DATE,
    id_demandeur            INTEGER REFERENCES utilisateur(id_utilisateur),
    id_validateur           INTEGER REFERENCES utilisateur(id_utilisateur),
    date_validation         TIMESTAMP,
    commentaire_validation  VARCHAR(500),
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_besoin_formation_priorite
        CHECK (priorite IN ('FAIBLE', 'MOYENNE', 'HAUTE', 'CRITIQUE')),
    CONSTRAINT ck_besoin_formation_statut
        CHECK (statut IN
               ('IDENTIFIE', 'EN_VALIDATION', 'VALIDE', 'PLANIFIE', 'SATISFAIT', 'ANNULE')),
    CONSTRAINT ck_besoin_formation_date_souhaitee
        CHECK (date_souhaitee IS NULL OR date_souhaitee >= date_identification)
);

CREATE INDEX IF NOT EXISTS idx_besoin_formation_employe
    ON besoin_formation(id_employer, statut);

CREATE INDEX IF NOT EXISTS idx_besoin_formation_competence
    ON besoin_formation(id_competence, priorite);

CREATE TABLE IF NOT EXISTS declenchement_besoin_formation (
    id_declenchement        SERIAL PRIMARY KEY,
    id_besoin_formation     INTEGER NOT NULL
        REFERENCES besoin_formation(id_besoin_formation) ON DELETE CASCADE,
    motif                   VARCHAR(30) NOT NULL,
    message                 VARCHAR(500),
    id_utilisateur          INTEGER REFERENCES utilisateur(id_utilisateur),
    date_declenchement      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_declenchement_besoin_motif
        CHECK (motif IN
               ('ECART_CRITIQUE', 'ECART_ELEVE', 'ECART_MODERE',
                'DEMANDE_MANAGER', 'DEMANDE_EMPLOYE', 'EVOLUTION_POSTE',
                'REGLEMENTATION'))
);

CREATE TABLE IF NOT EXISTS historique_besoin_formation (
    id_historique_besoin    SERIAL PRIMARY KEY,
    id_besoin_formation     INTEGER NOT NULL
        REFERENCES besoin_formation(id_besoin_formation) ON DELETE CASCADE,
    statut_avant            VARCHAR(30),
    statut_apres            VARCHAR(30) NOT NULL,
    commentaire             VARCHAR(500),
    id_utilisateur          INTEGER REFERENCES utilisateur(id_utilisateur),
    date_changement         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_historique_besoin
    ON historique_besoin_formation(id_besoin_formation, date_changement);

CREATE OR REPLACE FUNCTION priorite_besoin_depuis_ecart(
    p_niveau_ecart VARCHAR
) RETURNS VARCHAR AS $$
BEGIN
    RETURN CASE p_niveau_ecart
        WHEN 'CRITIQUE' THEN 'CRITIQUE'
        WHEN 'ELEVE' THEN 'HAUTE'
        WHEN 'MODERE' THEN 'MOYENNE'
        ELSE 'FAIBLE'
    END;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION declencher_besoin_formation(
    p_id_ecart_competence INTEGER,
    p_id_demandeur INTEGER DEFAULT NULL
) RETURNS INTEGER AS $$
DECLARE
    v_id_besoin INTEGER;
    v_ecart ecart_competence%ROWTYPE;
    v_competence competence%ROWTYPE;
    v_motif VARCHAR(30);
BEGIN
    SELECT *
    INTO v_ecart
    FROM ecart_competence
    WHERE id_ecart_competence = p_id_ecart_competence;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Écart de compétence introuvable : %', p_id_ecart_competence;
    END IF;

    SELECT *
    INTO v_competence
    FROM competence
    WHERE id_competence = v_ecart.id_competence;

    IF v_ecart.ecart <= 0 THEN
        RAISE EXCEPTION 'Aucun besoin de formation : la compétence est acquise';
    END IF;

    v_motif := CASE v_ecart.niveau_ecart
        WHEN 'CRITIQUE' THEN 'ECART_CRITIQUE'
        WHEN 'ELEVE' THEN 'ECART_ELEVE'
        ELSE 'ECART_MODERE'
    END;

    INSERT INTO besoin_formation (
        id_employer,
        id_competence,
        id_ecart_competence,
        titre,
        description,
        priorite,
        statut,
        id_demandeur
    )
    VALUES (
        v_ecart.id_employer,
        v_ecart.id_competence,
        v_ecart.id_ecart_competence,
        'Besoin de formation : ' || v_competence.libelle,
        'Écart détecté entre le niveau attendu ' || v_ecart.niveau_attendu
            || ' et le niveau acquis ' || v_ecart.niveau_acquis,
        priorite_besoin_depuis_ecart(v_ecart.niveau_ecart),
        'IDENTIFIE',
        p_id_demandeur
    )
    RETURNING id_besoin_formation INTO v_id_besoin;

    INSERT INTO declenchement_besoin_formation (
        id_besoin_formation,
        motif,
        message,
        id_utilisateur
    )
    VALUES (
        v_id_besoin,
        v_motif,
        'Besoin déclenché automatiquement à partir de l’analyse d’un écart de compétence.',
        p_id_demandeur
    );

    INSERT INTO historique_besoin_formation (
        id_besoin_formation,
        statut_avant,
        statut_apres,
        commentaire,
        id_utilisateur
    )
    VALUES (
        v_id_besoin,
        NULL,
        'IDENTIFIE',
        'Besoin de formation créé automatiquement.',
        p_id_demandeur
    );

    RETURN v_id_besoin;
END;
$$ LANGUAGE plpgsql;

COMMIT;
