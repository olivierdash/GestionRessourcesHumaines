BEGIN;

-- =========================================================================
-- BLOC 11 : DISCIPLINE (Concepts Séparés) 
-- =========================================================================

-- Table 1 : Le fait brut signalé
CREATE TABLE incident(
    id_incident          SERIAL PRIMARY KEY,
    id_employer          INTEGER NOT NULL REFERENCES employer(id_employer),
    id_declarant         INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    date_fait            DATE NOT NULL,
    descriptions         VARCHAR(500) NOT NULL,
    gravite              VARCHAR(20) NOT NULL 
        CHECK (gravite IN ('FAIBLE', 'MOYENNE', 'GRAVE', 'TRES_GRAVE')),
    statut_instruction   VARCHAR(25) NOT NULL DEFAULT 'OUVERT'
        CHECK (statut_instruction IN ('OUVERT', 'EXPLICATION_EN_COURS', 'REPONSE_RECUE', 'DECIDE', 'CLOTURE_SANS_SUITE'))
);

-- Table 2 : Le courrier envoyé (Lié directement à l'incident)
CREATE TABLE demande_explication(
    id_demande           SERIAL PRIMARY KEY,
    id_incident          INTEGER NOT NULL UNIQUE REFERENCES incident(id_incident) ON DELETE CASCADE,
    id_envoyeur          INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    date_envoi           DATE NOT NULL DEFAULT CURRENT_DATE,
    date_limite_reponse  DATE NOT NULL,
    motif                VARCHAR(500) NOT NULL,
    CONSTRAINT ck_dates_explication CHECK (date_limite_reponse >= date_envoi)
);

-- Table 3 : Le retour de l'employé et la décision finale
CREATE TABLE reponse_et_decision (
    id_decision          SERIAL PRIMARY KEY,
    id_demande           INTEGER NOT NULL UNIQUE REFERENCES demande_explication(id_demande) ON DELETE CASCADE,
    date_reponse         DATE NULL,
    contenu_reponse      VARCHAR(500) NULL,
    type_sanction        VARCHAR(30) NOT NULL DEFAULT 'AUCUNE'
        CHECK (type_sanction IN ('AUCUNE', 'AVERTISSEMENT', 'BLAME', 'MISE_A_PIED', 'LICENCIEMENT')),
    date_notification    DATE,
    commentaire_decision VARCHAR(500)
);

-- Table 3bis : Pièces justificatives rattachées à un incident
CREATE TABLE incident_document (
    id_incident_document SERIAL PRIMARY KEY,
    id_incident          INTEGER NOT NULL REFERENCES incident(id_incident) ON DELETE CASCADE,
    nom_fichier          VARCHAR(255) NOT NULL,
    fichier_url          VARCHAR(500) NOT NULL,
    type_piece           VARCHAR(30) NOT NULL
        CHECK (type_piece IN ('RAPPORT', 'TEMOIGNAGE', 'COURRIER', 'PREUVE', 'AUTRE')),
    id_depose_par        INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    date_depot           TIMESTAMP NOT NULL DEFAULT now()
);

-- Marqueur « sensible » sur le journal d'audit existant
ALTER TABLE journal_audit ADD COLUMN IF NOT EXISTS sensible BOOLEAN NOT NULL DEFAULT FALSE;

-- Trigger de journalisation automatique des modifications sur les tables sensibles.
-- L'app Java doit faire : SET LOCAL app.user_id = '<id>' avant chaque requête
-- (ou passer l'id via une variable Spring). Sinon on retombe sur la FK du row.
CREATE OR REPLACE FUNCTION trg_audit_sensible() RETURNS TRIGGER AS $$
DECLARE
    v_user_id  INTEGER;
    v_entity   INTEGER;
BEGIN
    v_user_id := NULLIF(current_setting('app.user_id', true), '')::INTEGER;

    IF TG_OP = 'DELETE' THEN
        CASE TG_TABLE_NAME
            WHEN 'incident' THEN
                v_entity  := OLD.id_incident;
                v_user_id := COALESCE(v_user_id, OLD.id_declarant);
            WHEN 'demande_explication' THEN
                v_entity  := OLD.id_demande;
                v_user_id := COALESCE(v_user_id, OLD.id_envoyeur);
            WHEN 'reponse_et_decision' THEN
                v_entity  := OLD.id_decision;
                v_user_id := COALESCE(v_user_id, 1);
        END CASE;
    ELSE
        CASE TG_TABLE_NAME
            WHEN 'incident' THEN
                v_entity  := NEW.id_incident;
                v_user_id := COALESCE(v_user_id, NEW.id_declarant);
            WHEN 'demande_explication' THEN
                v_entity  := NEW.id_demande;
                v_user_id := COALESCE(v_user_id, NEW.id_envoyeur);
            WHEN 'reponse_et_decision' THEN
                v_entity  := NEW.id_decision;
                v_user_id := COALESCE(v_user_id, 1);
        END CASE;
    END IF;

    INSERT INTO journal_audit (id_utilisateur, action, entite_cible, id_entite_cible, details, sensible)
    VALUES (v_user_id, TG_OP, TG_TABLE_NAME, v_entity,
            'Auto-audit : ' || TG_OP || ' sur ' || TG_TABLE_NAME, TRUE);

    IF TG_OP = 'DELETE' THEN RETURN OLD; ELSE RETURN NEW; END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_incident
    AFTER INSERT OR UPDATE OR DELETE ON incident
    FOR EACH ROW EXECUTE FUNCTION trg_audit_sensible();

CREATE TRIGGER trg_audit_demande
    AFTER INSERT OR UPDATE OR DELETE ON demande_explication
    FOR EACH ROW EXECUTE FUNCTION trg_audit_sensible();

CREATE TRIGGER trg_audit_decision
    AFTER INSERT OR UPDATE OR DELETE ON reponse_et_decision
    FOR EACH ROW EXECUTE FUNCTION trg_audit_sensible();

-- =========================================================================
-- BLOC 12 : RUPTURE & OFFBOARDING (Concepts Séparés)
-- =========================================================================

-- Table 4 : Le cœur juridique de la fin de contrat
CREATE TABLE rupture_contrat (
    id_rupture           SERIAL PRIMARY KEY,
    id_employer          INTEGER NOT NULL REFERENCES employer(id_employer),
    id_contract          INTEGER NOT NULL UNIQUE REFERENCES contract(id_contract),
    motif_rupture        VARCHAR(30) NOT NULL 
        CHECK (motif_rupture IN ('DEMISSION', 'LICENCIEMENT', 'FIN_CDD', 'RETRAITE', 'RUPTURE_CONVENTIONNELLE', 'FIN_PERIODE_ESSAI')),
    statut_rupture       VARCHAR(25) NOT NULL DEFAULT 'INITIEE'
        CHECK (statut_rupture IN ('INITIEE', 'PREAVIS_EN_COURS', 'OFFBOARDING_EN_COURS', 'CLOTUREE', 'ANNULEE')),
    date_notification    DATE NOT NULL DEFAULT CURRENT_DATE,
    duree_preavis_jours  INTEGER NOT NULL DEFAULT 0 CHECK (duree_preavis_jours >= 0),
    date_sortie          DATE NOT NULL,
    commentaire          VARCHAR(500),
    id_initie_par        INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    date_creation        TIMESTAMP NOT NULL DEFAULT now(),
    archive              BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_rupture_dates CHECK (date_sortie >= date_notification)
);

-- Table 5 : Suivi logistique
CREATE TABLE materiel_employe (
    id_materiel_employe  SERIAL PRIMARY KEY,
    id_employer          INTEGER NOT NULL REFERENCES employer(id_employer),
    designation          VARCHAR(150) NOT NULL,
    numero_serie         VARCHAR(100),
    date_remise          DATE NOT NULL DEFAULT CURRENT_DATE,
    date_restitution     DATE,
    etat_restitution     VARCHAR(15) CHECK (etat_restitution IN ('BON', 'ENDOMMAGE', 'PERDU')),
    montant_retenue      NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (montant_retenue >= 0),
    CONSTRAINT ck_materiel_dates CHECK (date_restitution IS NULL OR date_restitution >= date_remise)
);

-- Table 6 : Solde de Tout Compte (entête financière)
CREATE TABLE solde_tout_compte (
    id_stc                     SERIAL PRIMARY KEY,
    id_rupture                 INTEGER NOT NULL UNIQUE REFERENCES rupture_contrat(id_rupture) ON DELETE CASCADE,
    date_calcul                DATE NOT NULL DEFAULT CURRENT_DATE,
    salaire_mensuel_reference  NUMERIC(14,2) NOT NULL,
    jours_conges_restants      NUMERIC(7,2) NOT NULL DEFAULT 0,
    total_gains                NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (total_gains >= 0),
    total_retenues             NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (total_retenues >= 0),
    net_a_payer                NUMERIC(14,2) NOT NULL DEFAULT 0,
    statut                     VARCHAR(10) NOT NULL DEFAULT 'BROUILLON' CHECK (statut IN ('BROUILLON', 'VALIDE', 'PAYE')),
    id_valide_par              INTEGER REFERENCES utilisateur(id_utilisateur),
    date_validation            DATE,
    CONSTRAINT ck_stc_net CHECK (net_a_payer = total_gains - total_retenues)
);

-- Table 7 : Désactivation des accès (badge, comptes, VPN, etc.)
CREATE TABLE desactivation_acces (
    id_desactivation   SERIAL PRIMARY KEY,
    id_rupture         INTEGER NOT NULL REFERENCES rupture_contrat(id_rupture) ON DELETE CASCADE,
    type_acces         VARCHAR(30) NOT NULL
        CHECK (type_acces IN ('BADGE', 'COMPTE_APPLICATIF', 'VPN', 'EMAIL', 'TELEPHONE', 'AUTRE')),
    identifiant        VARCHAR(150),
    date_desactivation DATE,
    statut             VARCHAR(15) NOT NULL DEFAULT 'A_FAIRE'
        CHECK (statut IN ('A_FAIRE', 'FAIT', 'ECHEC')),
    effectue_par       INTEGER REFERENCES utilisateur(id_utilisateur),
    commentaire        VARCHAR(500)
);

-- Table 8 : Documents de sortie (STC, certificat de travail, attestation d'emploi)
CREATE TABLE document_sortie (
    id_document_sortie SERIAL PRIMARY KEY,
    id_rupture         INTEGER NOT NULL REFERENCES rupture_contrat(id_rupture) ON DELETE CASCADE,
    type_document      VARCHAR(30) NOT NULL
        CHECK (type_document IN ('SOLDE_TOUT_COMPTE', 'CERTIFICAT_TRAVAIL', 'ATTESTATION_EMPLOI', 'AUTRE')),
    reference          VARCHAR(100),
    fichier_url        VARCHAR(500),
    date_generation    DATE NOT NULL DEFAULT CURRENT_DATE,
    date_remise        DATE,
    genere_par         INTEGER REFERENCES utilisateur(id_utilisateur)
);

-- Archivage complet du dossier employé (le flag `rupture_contrat.archive`
-- ne gèle que la procédure de rupture, pas le dossier RH de la personne).
ALTER TABLE employer ADD COLUMN IF NOT EXISTS archive        BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE employer ADD COLUMN IF NOT EXISTS date_archivage DATE;

-- Index d'optimisation
CREATE INDEX idx_incident_emp              ON incident(id_employer);
CREATE INDEX idx_incident_document         ON incident_document(id_incident);
CREATE INDEX idx_rupture_emp               ON rupture_contrat(id_employer);
CREATE INDEX idx_materiel_emp              ON materiel_employe(id_employer);
CREATE INDEX idx_materiel_emp_date         ON materiel_employe(id_employer, date_restitution);
CREATE INDEX idx_desactivation_rupture     ON desactivation_acces(id_rupture);
CREATE INDEX idx_document_sortie_rupture   ON document_sortie(id_rupture);

COMMIT;