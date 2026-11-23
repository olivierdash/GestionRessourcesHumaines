-- =========================================================================
-- BLOC 9 : Évaluation, compétences et formation
-- Mesurer l'évolution après formation
-- =========================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS formation (
    id_formation            SERIAL PRIMARY KEY,
    id_besoin_formation     INTEGER REFERENCES besoin_formation(id_besoin_formation),
    titre                   VARCHAR(150) NOT NULL,
    organisme               VARCHAR(150),
    mode_formation          VARCHAR(20) NOT NULL DEFAULT 'PRESENTIEL',
    date_debut              DATE,
    date_fin                DATE,
    duree_heures            NUMERIC(6,2),
    statut                  VARCHAR(30) NOT NULL DEFAULT 'PLANIFIEE',
    commentaire             VARCHAR(500),
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_formation_dates
        CHECK (date_fin IS NULL OR date_debut IS NULL OR date_fin >= date_debut),
    CONSTRAINT ck_formation_mode
        CHECK (mode_formation IN ('PRESENTIEL', 'DISTANCIEL', 'HYBRIDE', 'E_LEARNING')),
    CONSTRAINT ck_formation_duree
        CHECK (duree_heures IS NULL OR duree_heures > 0),
    CONSTRAINT ck_formation_statut
        CHECK (statut IN ('PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE'))
);

CREATE TABLE IF NOT EXISTS participation_formation (
    id_participation        SERIAL PRIMARY KEY,
    id_formation            INTEGER NOT NULL REFERENCES formation(id_formation) ON DELETE CASCADE,
    id_employer             INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    statut                  VARCHAR(30) NOT NULL DEFAULT 'INSCRIT',
    taux_presence           NUMERIC(5,2),
    note_finale             NUMERIC(5,2),
    certificat_obtenu       BOOLEAN NOT NULL DEFAULT FALSE,
    commentaire             VARCHAR(500),
    date_participation      TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_participation_formation_employe
        UNIQUE (id_formation, id_employer),
    CONSTRAINT ck_participation_statut
        CHECK (statut IN ('INSCRIT', 'EN_COURS', 'TERMINE', 'ABANDONNE', 'ABSENT')),
    CONSTRAINT ck_participation_presence
        CHECK (taux_presence IS NULL OR (taux_presence >= 0 AND taux_presence <= 100)),
    CONSTRAINT ck_participation_note
        CHECK (note_finale IS NULL OR (note_finale >= 0 AND note_finale <= 100))
);

CREATE TABLE IF NOT EXISTS mesure_evolution_formation (
    id_mesure_evolution     SERIAL PRIMARY KEY,
    id_participation        INTEGER NOT NULL
        REFERENCES participation_formation(id_participation) ON DELETE CASCADE,
    id_competence           INTEGER NOT NULL REFERENCES competence(id_competence),
    niveau_avant            NUMERIC(5,2) NOT NULL,
    niveau_apres            NUMERIC(5,2),
    niveau_attendu          NUMERIC(5,2),
    progression             NUMERIC(5,2),
    taux_atteinte           NUMERIC(5,2),
    mesure                    VARCHAR(30) NOT NULL DEFAULT 'NIVEAU_COMPETENCE',
    date_mesure_avant       DATE,
    date_mesure_apres       DATE,
    commentaire             VARCHAR(500),
    id_evaluateur           INTEGER REFERENCES utilisateur(id_utilisateur),
    date_creation           TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_mesure_evolution_niveaux
        CHECK (niveau_avant >= 0
               AND (niveau_apres IS NULL OR niveau_apres >= 0)
               AND (niveau_attendu IS NULL OR niveau_attendu >= 0)),
    CONSTRAINT ck_mesure_evolution_avancement
        CHECK (taux_atteinte IS NULL OR (taux_atteinte >= 0 AND taux_atteinte <= 100)),
    CONSTRAINT ck_mesure_evolution_type
        CHECK (mesure IN ('NIVEAU_COMPETENCE', 'TEST', 'OBJECTIF', 'AUTO_EVALUATION'))
);

CREATE INDEX IF NOT EXISTS idx_mesure_evolution_competence
    ON mesure_evolution_formation(id_competence, date_mesure_apres);

CREATE INDEX IF NOT EXISTS idx_participation_employe
    ON participation_formation(id_employer, statut);

CREATE OR REPLACE VIEW vue_evolution_apres_formation AS
SELECT
    p.id_employer,
    f.id_formation,
    f.titre AS formation,
    f.date_fin,
    m.id_competence,
    c.code,
    c.libelle,
    m.niveau_avant,
    m.niveau_apres,
    m.niveau_attendu,
    CASE
        WHEN m.niveau_apres IS NULL THEN NULL
        ELSE m.niveau_apres - m.niveau_avant
    END AS progression_calculee,
    CASE
        WHEN m.niveau_attendu IS NULL OR m.niveau_attendu <= 0 THEN NULL
        WHEN m.niveau_apres IS NULL THEN 0
        ELSE ROUND(
            LEAST(
                100,
                GREATEST(
                    0,
                    ((m.niveau_apres - m.niveau_avant)
                     / NULLIF(m.niveau_attendu - m.niveau_avant, 0)) * 100
                )
            )::NUMERIC,
            2
        )
    END AS taux_atteinte_calcule,
    p.statut AS statut_participation,
    p.certificat_obtenu
FROM mesure_evolution_formation m
JOIN participation_formation p
    ON p.id_participation = m.id_participation
JOIN formation f
    ON f.id_formation = p.id_formation
JOIN competence c
    ON c.id_competence = m.id_competence;

CREATE OR REPLACE FUNCTION calculer_evolution_formation(
    p_id_mesure INTEGER
) RETURNS VOID AS $$
DECLARE
    v_mesure mesure_evolution_formation%ROWTYPE;
    v_progression NUMERIC(5,2);
    v_taux NUMERIC(5,2);
BEGIN
    SELECT *
    INTO v_mesure
    FROM mesure_evolution_formation
    WHERE id_mesure_evolution = p_id_mesure;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Mesure d''évolution introuvable : %', p_id_mesure;
    END IF;

    IF v_mesure.niveau_apres IS NULL THEN
        RETURN;
    END IF;

    v_progression := v_mesure.niveau_apres - v_mesure.niveau_avant;

    IF v_mesure.niveau_attendu IS NULL OR v_mesure.niveau_attendu <= v_mesure.niveau_avant THEN
        v_taux := CASE
            WHEN v_progression > 0 THEN 100
            ELSE 0
        END;
    ELSE
        v_taux := ROUND(
            GREATEST(
                0,
                LEAST(
                    100,
                    (v_progression / (v_mesure.niveau_attendu - v_mesure.niveau_avant)) * 100
                )
            )::NUMERIC,
            2
        );
    END IF;

    UPDATE mesure_evolution_formation
    SET progression = v_progression,
        taux_atteinte = v_taux,
        date_maj = now()
    WHERE id_mesure_evolution = p_id_mesure;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_calculer_evolution_formation()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.niveau_apres IS NOT NULL THEN
        NEW.progression := NEW.niveau_apres - NEW.niveau_avant;

        IF NEW.niveau_attendu IS NULL OR NEW.niveau_attendu <= NEW.niveau_avant THEN
            NEW.taux_atteinte :=
                CASE WHEN NEW.progression > 0 THEN 100 ELSE 0 END;
        ELSE
            NEW.taux_atteinte := ROUND(
                GREATEST(
                    0,
                    LEAST(
                        100,
                        (NEW.progression
                         / (NEW.niveau_attendu - NEW.niveau_avant)) * 100
                    )
                )::NUMERIC,
                2
            );
        END IF;
    END IF;

    NEW.date_maj := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_mesure_evolution_formation
    ON mesure_evolution_formation;

CREATE TRIGGER trg_mesure_evolution_formation
    BEFORE INSERT OR UPDATE OF niveau_avant, niveau_apres, niveau_attendu
    ON mesure_evolution_formation
    FOR EACH ROW
    EXECUTE FUNCTION trg_calculer_evolution_formation();

COMMIT;
