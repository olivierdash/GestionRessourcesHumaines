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
    date_reponse         DATE NULL, -- Peut être NULL si l'employé refuse de répondre
    contenu_reponse      VARCHAR(500) NULL,
    
    -- La sanction (remplace le besoin d'une table sanction dédiée)
    type_sanction        VARCHAR(30) NOT NULL DEFAULT 'AUCUNE'
        CHECK (type_sanction IN ('AUCUNE', 'AVERTISSEMENT', 'BLAME', 'MISE_A_PIED', 'LICENCIEMENT')),
    date_notification    DATE,
    commentaire_decision VARCHAR(500)
);

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

-- Table 5 : Suivi logistique (Une seule table pour le matériel remis/restitué)
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

-- Table 6 : Le Solde de Tout Compte (Entête financière globale)
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

-- Index d'optimisation
CREATE INDEX idx_incident_emp ON incident(id_employer);
CREATE INDEX idx_rupture_emp ON rupture_contrat(id_employer);
CREATE INDEX idx_materiel_emp ON materiel_employe(id_employer);

COMMIT;
