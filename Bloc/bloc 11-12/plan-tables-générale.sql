-- Reconstruction complète du schéma SIRH
-- Exécution : psql -v ON_ERROR_STOP=1 -U <user> -d sirh -f 01_schema_sirh.sql
-- ATTENTION : table.sql commence par DROP SCHEMA public CASCADE (toutes les données sont perdues).

-- ======================================================================
-- FICHIER : table.sql (base)
-- ======================================================================
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
-- ============================================================
-- 1. NETTOYAGE COMPLET (Dans l'ordre inverse des dépendances)
-- ============================================================
DROP TABLE IF EXISTS journal_audit CASCADE;
DROP TABLE IF EXISTS coordonnees_employe CASCADE;
DROP TABLE IF EXISTS pointage CASCADE;
DROP TABLE IF EXISTS affectation_planning CASCADE;
DROP TABLE IF EXISTS planning CASCADE;
DROP TABLE IF EXISTS horaire_travail CASCADE;
DROP TABLE IF EXISTS regime_travail CASCADE;
DROP TABLE IF EXISTS document CASCADE;
DROP TABLE IF EXISTS onboarding_tache CASCADE;
DROP TABLE IF EXISTS onboarding CASCADE;
DROP TABLE IF EXISTS affectation CASCADE;
DROP TABLE IF EXISTS historique_type_contrat CASCADE;
DROP TABLE IF EXISTS contract CASCADE;
DROP TABLE IF EXISTS employer CASCADE;
DROP TABLE IF EXISTS proposition_message CASCADE;
DROP TABLE IF EXISTS validation_proposition CASCADE;
DROP TABLE IF EXISTS proposition_embauche CASCADE;
DROP TABLE IF EXISTS evaluation CASCADE;
DROP TABLE IF EXISTS candidature CASCADE;
DROP TABLE IF EXISTS historique_candidat_identifiants CASCADE;
DROP TABLE IF EXISTS offre_emploi CASCADE;
DROP TABLE IF EXISTS candidat CASCADE;
DROP TABLE IF EXISTS historique_demande_recrutement CASCADE;
DROP TABLE IF EXISTS demande_recrutement CASCADE;
DROP TABLE IF EXISTS poste_mission CASCADE;
DROP TABLE IF EXISTS grille_salarial CASCADE;
DROP TABLE IF EXISTS poste CASCADE;
DROP TABLE IF EXISTS lieu CASCADE;
DROP TABLE IF EXISTS departement CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
-- Tables de référence
DROP TABLE IF EXISTS statut_demande_recrutement CASCADE;
DROP TABLE IF EXISTS statut_offre_emploie CASCADE;
DROP TABLE IF EXISTS statut_candidature CASCADE;
DROP TABLE IF EXISTS statut_proposition CASCADE;
DROP TABLE IF EXISTS statut_contrat CASCADE;
DROP TABLE IF EXISTS statut_tache_onboarding CASCADE;
DROP TABLE IF EXISTS statut_employe CASCADE;
DROP TABLE IF EXISTS etat_demande CASCADE;
DROP TABLE IF EXISTS motif_demande CASCADE;
DROP TABLE IF EXISTS type_document CASCADE;
DROP TABLE IF EXISTS type_evaluation CASCADE;
DROP TABLE IF EXISTS type_contrat CASCADE;
DROP TABLE IF EXISTS type_notification CASCADE;

-- ============================================================
-- 2. TABLES DE RÉFÉRENCE (STATUTS & TYPES)
-- ============================================================
CREATE TABLE type_notification (id_type_notification SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE type_evaluation (id_type_evaluation SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE type_contrat (id_type_contrat SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE, duree_max_mois INTEGER);
CREATE TABLE statut_candidature (id_statut_candidature SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_proposition (id_statut_proposition SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_offre_emploie (id_statut_offre_emploie SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE etat_demande (id_etat_demande SERIAL PRIMARY KEY, intitule VARCHAR(100) NOT NULL UNIQUE);
CREATE TABLE motif_demande (id_motif_demande SERIAL PRIMARY KEY, libelle VARCHAR(100) NOT NULL UNIQUE);
CREATE TABLE statut_contrat (id_statut_contrat SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_tache_onboarding (id_statut_tache_onboarding SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE type_document (id_type_document SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_employe (id SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE lieu (id_lieu INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, libelle VARCHAR(150) NOT NULL UNIQUE);

-- Gardée temporairement pour éviter les crashs Hibernate (ancien code)
CREATE TABLE statut_demande_recrutement (id_statut_demande_recrutement SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);

-- ============================================================
-- 3. UTILISATEURS ET STRUCTURE
-- ============================================================
CREATE TABLE roles (id_role SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);

CREATE TABLE utilisateur (
    id_utilisateur SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    id_role INTEGER NOT NULL,
    CONSTRAINT fk_utilisateur_role FOREIGN KEY (id_role) REFERENCES roles(id_role)
);

CREATE TABLE departement (
    id_departement SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    id_manager INTEGER NULL,
    CONSTRAINT fk_departement_manager FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE poste (
    id_poste SERIAL PRIMARY KEY,
    id_departement INTEGER NOT NULL,
    date_effet DATE,
    intitule VARCHAR(255) NOT NULL,
    id_lieu INTEGER,
    duree_hebdo INTEGER,
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement),
    FOREIGN KEY (id_lieu) REFERENCES lieu(id_lieu)
);

CREATE TABLE poste_mission (
    id_mission SERIAL PRIMARY KEY,
    id_poste INTEGER NOT NULL,
    titre VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste) ON DELETE CASCADE
);

-- Salaire_moyen retiré (script du 8 nov)
CREATE TABLE grille_salarial (
    id_grille SERIAL PRIMARY KEY,
    id_poste INTEGER NOT NULL,
    id_departement INTEGER NOT NULL,
    salaire_min NUMERIC(10,2) NOT NULL,
    salaire_max NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

-- ============================================================
-- 4. BLOC 1 : DEMANDE ET WORKFLOW
-- ============================================================
CREATE TABLE demande_recrutement (
    id_demande_recrutement SERIAL PRIMARY KEY,
    id_manager INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_souhaitee DATE,
    effectif INTEGER NOT NULL,
    id_etat_demande INTEGER NOT NULL,
    id_motif_demande INTEGER NOT NULL,
    id_type_contrat INTEGER NOT NULL,
    commentaires TEXT,
    id_statut_demande_recrutement INTEGER NULL, -- Gardé pour compatibilité Hibernate
    FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    FOREIGN KEY (id_motif_demande) REFERENCES motif_demande(id_motif_demande),
    FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id_type_contrat),
    FOREIGN KEY (id_statut_demande_recrutement) REFERENCES statut_demande_recrutement(id_statut_demande_recrutement)
);

CREATE TABLE historique_demande_recrutement (
    id_historique_demande_recrutement SERIAL PRIMARY KEY,
    id_demande_recrutement INTEGER NOT NULL,
    id_etat_demande INTEGER NOT NULL,
    id_validateur INTEGER NOT NULL,
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaires TEXT,
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement) ON DELETE CASCADE,
    FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur)
);

-- ============================================================
-- 5. BLOC 2 : RECRUTEMENT ET ÉVALUATION
-- ============================================================
CREATE TABLE candidat (
    id_candidat SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(255) UNIQUE NOT NULL,
    cv_url VARCHAR(500),
    mot_de_passe VARCHAR(255)
);

CREATE TABLE historique_candidat_identifiants (
    id_historique SERIAL PRIMARY KEY,
    id_candidat INTEGER NOT NULL,
    date_changement TIMESTAMP NOT NULL DEFAULT NOW(),
    ancien_nom VARCHAR(255),
    ancien_prenom VARCHAR(255),
    ancien_email VARCHAR(255),
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat)
);

CREATE TABLE offre_emploi (
    id_offre SERIAL PRIMARY KEY,
    id_demande_recrutement INT NOT NULL,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_publication TIMESTAMP,
    date_cloture TIMESTAMP,
    id_statut_offre_emploie INT NOT NULL,
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    FOREIGN KEY (id_statut_offre_emploie) REFERENCES statut_offre_emploie(id_statut_offre_emploie),
    CONSTRAINT uk_offre_demande UNIQUE (id_demande_recrutement)
);

CREATE TABLE candidature (
    id_candidature SERIAL PRIMARY KEY,
    id_candidat INTEGER NOT NULL,
    id_demande_recrutement INTEGER NOT NULL,
    date_depot_candidature TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_statut_candidature INT NOT NULL,
    id_offre INT NOT NULL,
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    CONSTRAINT uk_candidat_demande UNIQUE (id_candidat, id_demande_recrutement),
    FOREIGN KEY (id_offre) REFERENCES offre_emploi(id_offre),
    FOREIGN KEY (id_statut_candidature) REFERENCES statut_candidature(id_statut_candidature)
);

CREATE TABLE evaluation (
    id_evaluation SERIAL PRIMARY KEY,
    id_candidature INTEGER NOT NULL,
    id_evaluateur INTEGER NOT NULL,
    id_type_evaluation INT NOT NULL,
    evaluation TEXT,
    note NUMERIC(5,2),
    date_evaluation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature) ON DELETE CASCADE,
    FOREIGN KEY (id_evaluateur) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_type_evaluation) REFERENCES type_evaluation(id_type_evaluation)
);

-- ============================================================
-- 6. BLOC 3 : PROPOSITION ET VALIDATION
-- ============================================================
CREATE TABLE proposition_embauche (
    id_proposition SERIAL PRIMARY KEY,
    id_candidature INTEGER NOT NULL,
    salaire_propose NUMERIC(10,2) NOT NULL,
    salaire_final NUMERIC(10,2),
    date_proposition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP NOT NULL,
    id_statut_proposition INT NOT NULL,
    date_decision TIMESTAMP,
    remarque TEXT,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature),
    FOREIGN KEY (id_statut_proposition) REFERENCES statut_proposition(id_statut_proposition)
);

CREATE TABLE proposition_message (
    id_message INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_proposition INTEGER NOT NULL,
    id_candidat INTEGER,
    id_utilisateur INTEGER,
    contenu TEXT,
    salaire_propose NUMERIC(12,2),
    date_creation TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id_proposition) ON DELETE CASCADE,
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur),
    CONSTRAINT chk_msg_auteur CHECK ((id_candidat IS NOT NULL AND id_utilisateur IS NULL) OR (id_candidat IS NULL AND id_utilisateur IS NOT NULL))
);

CREATE TABLE validation_proposition (
    id_validation_proposition SERIAL PRIMARY KEY,
    id_proposition INTEGER NOT NULL,
    id_validateur INTEGER NOT NULL,
    id_departement INT NOT NULL,
    decision VARCHAR(50) NOT NULL,
    date_validation_proposition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id_proposition) ON DELETE CASCADE,
    FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

-- ============================================================
-- 7. BLOC 4 & 5 : EMPLOYÉ, CONTRATS, PLANNING, DOSSIER SALARIÉ
-- ============================================================
CREATE TABLE employer (
    id_employer SERIAL PRIMARY KEY,
    id_candidature INTEGER UNIQUE NOT NULL,
    matricule VARCHAR(50) UNIQUE,
    hiring_date DATE NOT NULL,
    status_employer VARCHAR(50), -- Rendu nullable
    statut_employe_id INT NOT NULL,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature),
    FOREIGN KEY (statut_employe_id) REFERENCES statut_employe(id)
);

-- Trigger Matricule
CREATE SEQUENCE IF NOT EXISTS matricule_seq START 1;
CREATE OR REPLACE FUNCTION set_matricule_on_insert() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.matricule IS NULL THEN
        NEW.matricule := 'EMP-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || LPAD(nextval('matricule_seq')::text, 4, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_set_matricule BEFORE INSERT ON employer FOR EACH ROW EXECUTE FUNCTION set_matricule_on_insert();

CREATE TABLE contract (
    id_contract SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    id_type_contrat INTEGER NOT NULL,
    id_statut_contrat INTEGER NOT NULL, -- Remplace l'ancien "status"
    date_debut DATE NOT NULL,
    date_fin DATE,
    salaire_mensuel NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id_type_contrat),
    FOREIGN KEY (id_statut_contrat) REFERENCES statut_contrat(id_statut_contrat)
);

CREATE TABLE historique_type_contrat (
    id_historique INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_type_contrat INTEGER NOT NULL,
    id_utilisateur INTEGER NOT NULL,
    date_modification TIMESTAMP NOT NULL DEFAULT NOW(),
    ancienne_duree INTEGER,
    nouvelle_duree INTEGER,
    FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id_type_contrat),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE affectation (
    id_affectation SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

CREATE TABLE onboarding (
    id_onboarding SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL UNIQUE,
    date_debut DATE DEFAULT CURRENT_DATE,
    date_fin DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer)
);

CREATE TABLE onboarding_tache (
    id_tache SERIAL PRIMARY KEY,
    id_onboarding INTEGER NOT NULL,
    titre VARCHAR(255) NOT NULL,
    id_responsable INTEGER NOT NULL,
    date_echeance DATE,
    est_obligatoire BOOLEAN NOT NULL DEFAULT TRUE,
    id_statut_tache_onboarding INTEGER NOT NULL,
    FOREIGN KEY (id_onboarding) REFERENCES onboarding(id_onboarding) ON DELETE CASCADE,
    FOREIGN KEY (id_responsable) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_statut_tache_onboarding) REFERENCES statut_tache_onboarding(id_statut_tache_onboarding)
);

-- (BLOC 5) : GESTION DES DOCUMENTS ET VALIDITÉ
CREATE TABLE document (
    id_document SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_type_document INTEGER NOT NULL,
    id_tache INTEGER NULL,
    nom_fichier VARCHAR(255) NOT NULL,
    fichier_url VARCHAR(500) NOT NULL,
    date_depot TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATE, 
    statut_document VARCHAR(50) DEFAULT 'VALIDE', 
    id_depose_par INTEGER NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer),
    FOREIGN KEY (id_type_document) REFERENCES type_document(id_type_document),
    FOREIGN KEY (id_tache) REFERENCES onboarding_tache(id_tache),
    FOREIGN KEY (id_depose_par) REFERENCES utilisateur(id_utilisateur)
);

-- (BLOC 5) : COORDONNÉES
CREATE TABLE coordonnees_employe (
    id_coordonnees SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL UNIQUE,
    adresse VARCHAR(255),
    code_postal VARCHAR(20),
    ville VARCHAR(100),
    pays VARCHAR(100),
    situation_matrimoniale VARCHAR(50),
    nombre_enfants INTEGER DEFAULT 0,
    telephone_personnel VARCHAR(20),
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE
);

-- (BLOC 5) : JOURNAL D'AUDIT
CREATE TABLE journal_audit (
    id_audit SERIAL PRIMARY KEY,
    id_utilisateur INTEGER NOT NULL,
    action VARCHAR(255) NOT NULL,
    entite_cible VARCHAR(100) NOT NULL,
    id_entite_cible INTEGER NOT NULL,
    details TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- Planning (Coéquipier)
CREATE TABLE regime_travail (
    id_regime_travail SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    heures_hebdomadaires INTEGER NOT NULL CHECK (heures_hebdomadaires > 0),
    jours_hebdomadaires INTEGER NOT NULL CHECK (jours_hebdomadaires BETWEEN 1 AND 7),
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE horaire_travail (
    id_horaire_travail SERIAL PRIMARY KEY,
    id_regime_travail INTEGER NOT NULL REFERENCES regime_travail(id_regime_travail) ON DELETE CASCADE,
    jour_semaine VARCHAR(12) NOT NULL,
    heure_debut TIME,
    heure_fin TIME,
    pause_minutes INTEGER NOT NULL DEFAULT 0 CHECK (pause_minutes >= 0),
    CONSTRAINT uq_horaire_regime_jour UNIQUE (id_regime_travail, jour_semaine)
);

CREATE TABLE planning (
    id_planning SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    id_regime_travail INTEGER NOT NULL REFERENCES regime_travail(id_regime_travail),
    date_debut DATE NOT NULL,
    date_fin DATE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_planning_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE TABLE affectation_planning (
    id_affectation_planning SERIAL PRIMARY KEY,
    id_planning INTEGER NOT NULL REFERENCES planning(id_planning) ON DELETE CASCADE,
    id_employer INTEGER NOT NULL REFERENCES employer(id_employer),
    date_debut DATE NOT NULL,
    date_fin DATE,
    CONSTRAINT ck_affectation_planning_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE TABLE pointage (
    id_pointage SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL REFERENCES employer(id_employer),
    date_pointage DATE NOT NULL,
    heure_entree TIME,
    heure_sortie TIME,
    minutes_travaillees INTEGER NOT NULL DEFAULT 0 CHECK (minutes_travaillees >= 0),
    minutes_pause INTEGER NOT NULL DEFAULT 0 CHECK (minutes_pause >= 0),
    minutes_retard INTEGER NOT NULL DEFAULT 0 CHECK (minutes_retard >= 0),
    minutes_supplementaires INTEGER NOT NULL DEFAULT 0 CHECK (minutes_supplementaires >= 0),
    commentaire VARCHAR(500),
    CONSTRAINT uq_pointage_employe_date UNIQUE (id_employer, date_pointage),
    CONSTRAINT ck_pointage_heures CHECK (heure_entree IS NULL OR heure_sortie IS NULL OR heure_sortie > heure_entree)
);

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================
CREATE TABLE notification (
    id_notification SERIAL PRIMARY KEY,
    id_destinataire_candidat INTEGER NULL,
    id_destinataire_utilisateur INTEGER NULL,
    id_type_notification INTEGER NOT NULL,
    message VARCHAR(255) NOT NULL,
    lien_action VARCHAR(255),
    est_lu BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_destinataire_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_destinataire_utilisateur) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_type_notification) REFERENCES type_notification(id_type_notification)
);

-- ======================================================================
-- FICHIER : 20261112-002-horaireTravailModif.sql
-- ======================================================================
-- 1. Supprimer les anciennes contraintes
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS horaire_travail_pause_minutes_check;
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_jour; -- Le vrai nom d'origine
ALTER TABLE horaire_travail DROP CONSTRAINT IF EXISTS uq_horaire_regime_unite;

-- 2. Renommer et convertir (on suppose que la colonne d'origine s'appelait jour_semaine)
-- Si votre colonne s'appelle déjà 'unite' (entier), passez directement à la ligne 3.
ALTER TABLE horaire_travail RENAME COLUMN jour_semaine TO dayweek;

ALTER TABLE horaire_travail ALTER COLUMN dayweek TYPE INTEGER USING (
    CASE dayweek
        WHEN 'MONDAY' THEN 1
        WHEN 'TUESDAY' THEN 2
        WHEN 'WEDNESDAY' THEN 3
        WHEN 'THURSDAY' THEN 4
        WHEN 'FRIDAY' THEN 5
        ELSE 1
    END
);

-- 3. Ajouter les nouvelles contraintes (Check et Unicité)
ALTER TABLE horaire_travail ADD CONSTRAINT ck_horaire_dayweek CHECK (dayweek BETWEEN 1 AND 5);
ALTER TABLE horaire_travail ADD CONSTRAINT uq_horaire_regime_dayweek UNIQUE (id_regime_travail, dayweek);

-- ======================================================================
-- FICHIER : 20261112-003-planningModif.sql
-- ======================================================================
ALTER TABLE planning DROP CONSTRAINT IF EXISTS ck_planning_dates;
ALTER TABLE affectation_planning DROP CONSTRAINT IF EXISTS ck_affectation_planning_dates;

-- ======================================================================
-- FICHIER : 20261112-004-jourFerieCreation.sql
-- ======================================================================
CREATE TABLE IF NOT EXISTS jour_ferie (
    id_jour_ferie  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jour           INTEGER NOT NULL,
    mois           INTEGER NOT NULL,
    annee          INTEGER, -- NULL pour les fêtes fixes annuelles
    libelle        VARCHAR(25) NOT NULL
);

-- Index partiel pour les fêtes fixes (ex: 01/01 tous les ans)
CREATE UNIQUE INDEX IF NOT EXISTS uq_jour_ferie_fixe
    ON jour_ferie(jour, mois)
    WHERE annee IS NULL;

-- Index partiel pour les fêtes mobiles spécifiques à une année (ex: Aïd 2025)
CREATE UNIQUE INDEX IF NOT EXISTS uq_jour_ferie_mobile
    ON jour_ferie(jour, mois, annee)
    WHERE annee IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_jour_ferie_annee
    ON jour_ferie(annee);

-- ======================================================================
-- FICHIER : 20261112-005-pointageModif.sql
-- ======================================================================
-- 1. Supprimer les colonnes de calcul (désormais calculées à la volée par PointageService)
ALTER TABLE pointage
    DROP COLUMN IF EXISTS minutes_travaillees,
    DROP COLUMN IF EXISTS minutes_pause,
    DROP COLUMN IF EXISTS minutes_retard,
    DROP COLUMN IF EXISTS minutes_supplementaires;

-- 2. Supprimer l'ancienne contrainte de vérification des heures
ALTER TABLE pointage DROP CONSTRAINT IF EXISTS ck_pointage_heures;
ALTER TABLE pointage ADD COLUMN IF NOT EXISTS saisi_par INTEGER;


DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_pointage_saisi_par') THEN
        ALTER TABLE pointage
            ADD CONSTRAINT fk_pointage_saisi_par
            FOREIGN KEY (saisi_par) REFERENCES utilisateur(id_utilisateur);
    END IF;
END $$;

-- 5. Conversion de SERIAL vers IDENTITY (pour coller au standard SQL et à l'entité Java)
ALTER TABLE pointage ALTER COLUMN id_pointage DROP DEFAULT;
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='pointage' AND column_name='id_pointage' AND is_identity='YES'
    ) THEN
        ALTER TABLE pointage ALTER COLUMN id_pointage ADD GENERATED ALWAYS AS IDENTITY;
    END IF;
END $$;

-- 6. Remplacer l'ancien index par le nouvel index combiné
DROP INDEX IF EXISTS idx_pointage_date;

CREATE INDEX IF NOT EXISTS idx_pointage_employe_date
    ON pointage(id_employer, date_pointage);

-- 7. (Optionnel) Nettoyage du script d'origine qui ajoutait minutes_pause
-- Si le script original a été exécuté, la colonne a déjà été supprimée à l'étape 1, 
-- donc cette ligne n'est plus nécessaire.

-- ======================================================================
-- FICHIER : 2026-11-13.sql
-- ADAPTATION : 1re instruction protégée (la colonne jour_semaine a déjà été
-- renommée en dayweek et convertie en INTEGER par 002 ; sinon erreur "column does not exist").
-- ======================================================================
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='horaire_travail' AND column_name='jour_semaine') THEN
        ALTER TABLE horaire_travail ALTER COLUMN jour_semaine TYPE INTEGER USING jour_semaine::INTEGER;
    END IF;
END $$;

-- ajoute de table type de compte 
CREATE TABLE IF NOT EXISTS type_decompte (
    id_type_decompte  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code              VARCHAR(20) NOT NULL UNIQUE,
    libelle           VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO type_decompte (code, libelle) VALUES
('HEURES', 'Décompte en heures'),
('JOURS', 'Décompte en jours');


ALTER TABLE regime_travail
    ADD COLUMN IF NOT EXISTS code                          VARCHAR(30),
    ADD COLUMN IF NOT EXISTS type_decompte_id              INTEGER,
    ADD COLUMN IF NOT EXISTS jours_annuels_reference       INTEGER,
    ADD COLUMN IF NOT EXISTS pointage_obligatoire          BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS seuil_heures_supplementaires  DECIMAL(3,1),
    ADD COLUMN IF NOT EXISTS duree_max_hebdomadaire        INTEGER,
    ADD COLUMN IF NOT EXISTS duree_max_journaliere         INTEGER;

ALTER TABLE regime_travail ALTER COLUMN heures_hebdomadaires TYPE DECIMAL(3,1);
ALTER TABLE regime_travail DROP CONSTRAINT IF EXISTS regime_travail_heures_hebdomadaires_check;
ALTER TABLE regime_travail DROP CONSTRAINT IF EXISTS regime_travail_jours_hebdomadaires_check;

UPDATE regime_travail
SET code = 'R_' || id_regime_travail
WHERE code IS NULL;

UPDATE regime_travail
SET type_decompte_id = (SELECT id_type_decompte FROM type_decompte WHERE code = 'HEURES')
WHERE type_decompte_id IS NULL;

ALTER TABLE regime_travail ALTER COLUMN code SET NOT NULL;
ALTER TABLE regime_travail ALTER COLUMN type_decompte_id SET NOT NULL;


ALTER TABLE regime_travail DROP COLUMN jours_hebdomadaires;


ALTER TABLE regime_travail ALTER COLUMN heures_hebdomadaires DROP NOT NULL;


CREATE UNIQUE INDEX IF NOT EXISTS uq_regime_travail_code
    ON regime_travail(code);

-- 2.7 FK (idempotent)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_regime_type_decompte') THEN
        ALTER TABLE regime_travail
            ADD CONSTRAINT fk_regime_type_decompte
            FOREIGN KEY (type_decompte_id) REFERENCES type_decompte(id_type_decompte);
    END IF;
END $$;

-- ======================================================================
-- FICHIER : 2026-11-18.sql
-- ADAPTATION : ADD COLUMN IF NOT EXISTS (table.sql contient déjà ces 2 colonnes).
-- ======================================================================
ALTER TABLE document ADD COLUMN IF NOT EXISTS date_expiration DATE;
ALTER TABLE document ADD COLUMN IF NOT EXISTS statut_document VARCHAR(50) DEFAULT 'VALIDE';

-- ======================================================================
-- FICHIER : 20261119-001-module-conges.sql (crée absence, demande_conge, solde_conge, ...)
-- ======================================================================
-- =====================================================================
-- Module Congés / Absences
-- Script unique, idempotent pour les créations.
-- À exécuter sur la base SIRH après sauvegarde.
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Lien utilisateur <-> employé et rôle EMPLOYE
-- ---------------------------------------------------------------------

ALTER TABLE utilisateur
    ADD COLUMN IF NOT EXISTS id_employe INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_utilisateur_employe'
    ) THEN
        ALTER TABLE utilisateur
            ADD CONSTRAINT fk_utilisateur_employe
            FOREIGN KEY (id_employe) REFERENCES employer(id_employer);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_utilisateur_employe
    ON utilisateur(id_employe)
    WHERE id_employe IS NOT NULL;

INSERT INTO roles(libelle)
VALUES ('EMPLOYE')
ON CONFLICT (libelle) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Types de congé / absence
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS type_conge (
    id_type_conge               INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    code                        VARCHAR(30) NOT NULL UNIQUE,
    libelle                     VARCHAR(100) NOT NULL,
    description                 VARCHAR(500),
    categorie                   VARCHAR(20) NOT NULL,
    mode_decompte               VARCHAR(20) NOT NULL,
    paye                        BOOLEAN NOT NULL DEFAULT TRUE,
    consomme_solde              BOOLEAN NOT NULL DEFAULT TRUE,
    justificatif_requis         BOOLEAN NOT NULL DEFAULT FALSE,
    delai_justificatif_jours    INTEGER,
    duree_max_jours             NUMERIC(5,2),
    report_autorise             BOOLEAN NOT NULL DEFAULT FALSE,
    plafond_report_jours        NUMERIC(5,2),
    demande_employe_autorisee   BOOLEAN NOT NULL DEFAULT TRUE,
    actif                       BOOLEAN NOT NULL DEFAULT TRUE,
    ordre_affichage             INTEGER NOT NULL DEFAULT 0,
    couleur                     VARCHAR(7),
    date_creation               TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_type_conge_categorie
        CHECK (categorie IN ('CONGE', 'ABSENCE')),
    CONSTRAINT ck_type_conge_mode
        CHECK (mode_decompte IN ('OUVRES', 'CALENDAIRES')),
    CONSTRAINT ck_type_conge_delai
        CHECK (delai_justificatif_jours IS NULL OR delai_justificatif_jours >= 0),
    CONSTRAINT ck_type_conge_duree
        CHECK (duree_max_jours IS NULL OR duree_max_jours > 0),
    CONSTRAINT ck_type_conge_plafond
        CHECK (plafond_report_jours IS NULL OR plafond_report_jours >= 0)
);

-- ---------------------------------------------------------------------
-- 3. Règles d'acquisition (barèmes)
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS droit_conge (
    id_droit_conge              INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_type_conge               INTEGER NOT NULL REFERENCES type_conge(id_type_conge),
    id_type_contrat             INTEGER REFERENCES type_contrat(id_type_contrat),
    anciennete_min_mois         INTEGER NOT NULL DEFAULT 0,
    anciennete_max_mois         INTEGER,
    mode_acquisition            VARCHAR(20) NOT NULL DEFAULT 'ANNUEL',
    jours_par_an                NUMERIC(5,2) NOT NULL,
    prorata_annee_embauche      BOOLEAN NOT NULL DEFAULT TRUE,
    actif                       BOOLEAN NOT NULL DEFAULT TRUE,
    description                 VARCHAR(500),
    date_creation               TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_droit_conge_anciennete
        CHECK (anciennete_min_mois >= 0
               AND (anciennete_max_mois IS NULL OR anciennete_max_mois >= anciennete_min_mois)),
    CONSTRAINT ck_droit_conge_mode
        CHECK (mode_acquisition IN ('ANNUEL', 'MENSUEL')),
    CONSTRAINT ck_droit_conge_jours
        CHECK (jours_par_an > 0)
);

CREATE INDEX IF NOT EXISTS idx_droit_conge_type
    ON droit_conge(id_type_conge, actif);

-- ---------------------------------------------------------------------
-- 4. Soldes individuels
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS solde_conge (
    id_solde_conge              INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_employer                 INTEGER NOT NULL REFERENCES employer(id_employer) ON DELETE CASCADE,
    id_type_conge               INTEGER NOT NULL REFERENCES type_conge(id_type_conge),
    annee                       INTEGER NOT NULL,
    taux_annuel                 NUMERIC(5,2) NOT NULL DEFAULT 0,
    objectif_annuel             NUMERIC(7,2) NOT NULL DEFAULT 0,
    mode_acquisition            VARCHAR(20) NOT NULL DEFAULT 'ANNUEL',
    mois_debut_acquisition      INTEGER NOT NULL DEFAULT 1,
    jours_acquis                NUMERIC(7,2) NOT NULL DEFAULT 0,
    jours_reportes              NUMERIC(7,2) NOT NULL DEFAULT 0,
    jours_ajustes               NUMERIC(7,2) NOT NULL DEFAULT 0,
    jours_utilises              NUMERIC(7,2) NOT NULL DEFAULT 0,
    jours_en_attente            NUMERIC(7,2) NOT NULL DEFAULT 0,
    dernier_mois_acquis         INTEGER NOT NULL DEFAULT 0,
    annee_cloturee              BOOLEAN NOT NULL DEFAULT FALSE,
    report_initialise           BOOLEAN NOT NULL DEFAULT FALSE,
    version                     BIGINT NOT NULL DEFAULT 0,
    date_creation               TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_solde_conge_employe_type_annee
        UNIQUE (id_employer, id_type_conge, annee),
    CONSTRAINT ck_solde_conge_annee
        CHECK (annee BETWEEN 2000 AND 2100),
    CONSTRAINT ck_solde_conge_mois
        CHECK (mois_debut_acquisition BETWEEN 1 AND 12
               AND dernier_mois_acquis BETWEEN 0 AND 12),
    CONSTRAINT ck_solde_conge_mode
        CHECK (mode_acquisition IN ('ANNUEL', 'MENSUEL')),
    CONSTRAINT ck_solde_conge_valeurs
        CHECK (jours_acquis >= 0
               AND jours_reportes >= 0
               AND jours_utilises >= 0
               AND jours_en_attente >= 0)
);

-- Compatibilité avec une table déjà créée par une version antérieure du script
ALTER TABLE solde_conge
    ADD COLUMN IF NOT EXISTS objectif_annuel NUMERIC(7,2) NOT NULL DEFAULT 0;
ALTER TABLE solde_conge
    ADD COLUMN IF NOT EXISTS mode_acquisition VARCHAR(20) NOT NULL DEFAULT 'ANNUEL';

CREATE INDEX IF NOT EXISTS idx_solde_conge_annee
    ON solde_conge(annee);

-- ---------------------------------------------------------------------
-- 5. Demandes de congé
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS demande_conge (
    id_demande_conge            INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    reference                   VARCHAR(30) UNIQUE,
    id_employer                 INTEGER NOT NULL REFERENCES employer(id_employer),
    id_type_conge               INTEGER NOT NULL REFERENCES type_conge(id_type_conge),
    id_utilisateur_createur     INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    date_debut                  DATE NOT NULL,
    date_fin                    DATE NOT NULL,
    demi_jour_debut             VARCHAR(10),
    demi_jour_fin               VARCHAR(10),
    nombre_jours                NUMERIC(7,2) NOT NULL,
    solde_requis                BOOLEAN NOT NULL DEFAULT TRUE,
    motif                       VARCHAR(500),
    statut                      VARCHAR(30) NOT NULL,
    manager_requis              BOOLEAN NOT NULL DEFAULT TRUE,
    id_manager_attendu          INTEGER REFERENCES utilisateur(id_utilisateur),
    id_validateur_manager       INTEGER REFERENCES utilisateur(id_utilisateur),
    date_validation_manager     TIMESTAMP,
    commentaire_manager         VARCHAR(500),
    id_validateur_rh            INTEGER REFERENCES utilisateur(id_utilisateur),
    date_validation_rh          TIMESTAMP,
    commentaire_rh              VARCHAR(500),
    date_limite_justificatif    DATE,
    date_soumission             TIMESTAMP,
    date_creation               TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                    TIMESTAMP NOT NULL DEFAULT now(),
    version                     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_demande_conge_dates CHECK (date_fin >= date_debut),
    CONSTRAINT ck_demande_conge_jours CHECK (nombre_jours > 0),
    CONSTRAINT ck_demande_conge_demi
        CHECK ((demi_jour_debut IS NULL OR demi_jour_debut IN ('MATIN', 'APRES_MIDI'))
               AND (demi_jour_fin IS NULL OR demi_jour_fin IN ('MATIN', 'APRES_MIDI')))
);

CREATE INDEX IF NOT EXISTS idx_demande_conge_employe_dates
    ON demande_conge(id_employer, date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_demande_conge_statut
    ON demande_conge(statut);
CREATE INDEX IF NOT EXISTS idx_demande_conge_manager
    ON demande_conge(id_manager_attendu, statut);

-- ---------------------------------------------------------------------
-- 6. Historique des demandes
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS historique_demande_conge (
    id_historique_demande_conge INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_demande_conge            INTEGER NOT NULL REFERENCES demande_conge(id_demande_conge) ON DELETE CASCADE,
    statut_avant                VARCHAR(30),
    statut_apres                VARCHAR(30) NOT NULL,
    id_utilisateur              INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    commentaire                 VARCHAR(500),
    date_changement             TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_historique_demande_conge
    ON historique_demande_conge(id_demande_conge, date_changement);

-- ---------------------------------------------------------------------
-- 7. Mouvements de solde
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS mouvement_solde_conge (
    id_mouvement_solde          INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_solde_conge              INTEGER NOT NULL REFERENCES solde_conge(id_solde_conge) ON DELETE CASCADE,
    type_mouvement              VARCHAR(30) NOT NULL,
    jours                       NUMERIC(7,2) NOT NULL,
    id_demande_conge            INTEGER REFERENCES demande_conge(id_demande_conge),
    id_utilisateur              INTEGER REFERENCES utilisateur(id_utilisateur),
    motif                       VARCHAR(500),
    date_mouvement              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_mouvement_solde_type
        CHECK (type_mouvement IN (
            'ACQUISITION', 'REPORT', 'AJUSTEMENT', 'RESERVATION',
            'CONSOMMATION', 'LIBERATION', 'ANNULATION_CONSOMMATION'
        ))
);

CREATE INDEX IF NOT EXISTS idx_mouvement_solde
    ON mouvement_solde_conge(id_solde_conge, date_mouvement);
CREATE INDEX IF NOT EXISTS idx_mouvement_demande
    ON mouvement_solde_conge(id_demande_conge);

-- ---------------------------------------------------------------------
-- 8. Absences
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS absence (
    id_absence                  INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_employer                 INTEGER NOT NULL REFERENCES employer(id_employer),
    id_type_conge               INTEGER NOT NULL REFERENCES type_conge(id_type_conge),
    id_demande_conge            INTEGER UNIQUE REFERENCES demande_conge(id_demande_conge),
    source                      VARCHAR(20) NOT NULL,
    date_debut                  DATE NOT NULL,
    date_fin                    DATE NOT NULL,
    demi_jour_debut             VARCHAR(10),
    demi_jour_fin               VARCHAR(10),
    nombre_jours                NUMERIC(7,2) NOT NULL,
    statut                      VARCHAR(20) NOT NULL,
    motif                       VARCHAR(500),
    id_utilisateur_createur     INTEGER REFERENCES utilisateur(id_utilisateur),
    date_limite_justificatif    DATE,
    date_creation               TIMESTAMP NOT NULL DEFAULT now(),
    date_maj                    TIMESTAMP NOT NULL DEFAULT now(),
    version                     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_absence_dates CHECK (date_fin >= date_debut),
    CONSTRAINT ck_absence_jours CHECK (nombre_jours > 0),
    CONSTRAINT ck_absence_source
        CHECK (source IN ('DEMANDE_CONGE', 'SAISIE_MANUELLE', 'POINTAGE')),
    CONSTRAINT ck_absence_statut
        CHECK (statut IN ('DECLAREE', 'JUSTIFIEE', 'NON_JUSTIFIEE', 'ANNULEE'))
);

CREATE INDEX IF NOT EXISTS idx_absence_employe_dates
    ON absence(id_employer, date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_absence_statut
    ON absence(statut);

-- ---------------------------------------------------------------------
-- 9. Justificatifs
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS justificatif_conge (
    id_justificatif             INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_demande_conge            INTEGER REFERENCES demande_conge(id_demande_conge) ON DELETE CASCADE,
    id_absence                  INTEGER REFERENCES absence(id_absence) ON DELETE CASCADE,
    nom_fichier                 VARCHAR(255) NOT NULL,
    chemin_stockage             VARCHAR(500) NOT NULL,
    type_mime                   VARCHAR(100),
    taille_octets               BIGINT,
    id_utilisateur_depot        INTEGER NOT NULL REFERENCES utilisateur(id_utilisateur),
    date_depot                  TIMESTAMP NOT NULL DEFAULT now(),
    statut                      VARCHAR(20) NOT NULL DEFAULT 'DEPOSE',
    commentaire                 VARCHAR(500),
    id_validateur               INTEGER REFERENCES utilisateur(id_utilisateur),
    date_validation             TIMESTAMP,
    CONSTRAINT ck_justificatif_parent
        CHECK (num_nonnulls(id_demande_conge, id_absence) = 1),
    CONSTRAINT ck_justificatif_statut
        CHECK (statut IN ('DEPOSE', 'VALIDE', 'REFUSE'))
);

CREATE INDEX IF NOT EXISTS idx_justificatif_demande
    ON justificatif_conge(id_demande_conge);
CREATE INDEX IF NOT EXISTS idx_justificatif_absence
    ON justificatif_conge(id_absence);

-- ---------------------------------------------------------------------
-- 10. Journal de paie
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS variable_paie (
    id_variable_paie            INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    id_employer                 INTEGER NOT NULL REFERENCES employer(id_employer),
    id_demande_conge            INTEGER REFERENCES demande_conge(id_demande_conge),
    id_absence                  INTEGER REFERENCES absence(id_absence),
    code                        VARCHAR(30) NOT NULL,
    libelle                     VARCHAR(255),
    periode                     DATE NOT NULL,
    date_debut                  DATE NOT NULL,
    date_fin                    DATE NOT NULL,
    quantite                    NUMERIC(7,2) NOT NULL,
    unite                       VARCHAR(10) NOT NULL DEFAULT 'JOUR',
    statut                      VARCHAR(20) NOT NULL DEFAULT 'A_CONSOMMER',
    date_creation               TIMESTAMP NOT NULL DEFAULT now(),
    date_consommation           TIMESTAMP,
    commentaire                 VARCHAR(500),
    CONSTRAINT ck_variable_paie_parent
        CHECK (num_nonnulls(id_demande_conge, id_absence) = 1),
    CONSTRAINT ck_variable_paie_quantite CHECK (quantite > 0),
    CONSTRAINT ck_variable_paie_statut
        CHECK (statut IN ('A_CONSOMMER', 'CONSOMME', 'ANNULE'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_variable_paie_demande
    ON variable_paie(id_demande_conge, periode, code)
    WHERE id_demande_conge IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_variable_paie_absence
    ON variable_paie(id_absence, periode, code)
    WHERE id_absence IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_variable_paie_periode
    ON variable_paie(periode, statut);
CREATE INDEX IF NOT EXISTS idx_variable_paie_employe
    ON variable_paie(id_employer, periode);

-- ---------------------------------------------------------------------
-- 11. Seeds minimaux (aucun barème légal)
-- ---------------------------------------------------------------------

INSERT INTO type_conge
    (code, libelle, description, categorie, mode_decompte, paye, consomme_solde,
     justificatif_requis, delai_justificatif_jours, duree_max_jours,
     report_autorise, plafond_report_jours, demande_employe_autorisee,
     actif, ordre_affichage, couleur)
VALUES
    ('CONGE_PAYE', 'Congé payé', 'Congé annuel payé', 'CONGE', 'OUVRES',
     TRUE, TRUE, FALSE, NULL, NULL, FALSE, NULL, TRUE, TRUE, 10, '#2563eb'),
    ('CONGE_SANS_SOLDE', 'Congé sans solde', 'Congé sans rémunération', 'CONGE', 'OUVRES',
     FALSE, FALSE, FALSE, NULL, NULL, FALSE, NULL, FALSE, TRUE, 20, '#64748b'),
    ('ABSENCE_JUSTIFIEE', 'Absence justifiée', 'Absence couverte par un justificatif', 'ABSENCE', 'OUVRES',
     TRUE, FALSE, TRUE, 5, NULL, FALSE, NULL, FALSE, TRUE, 30, '#059669'),
    ('ABSENCE_NON_JUSTIFIEE', 'Absence non justifiée', 'Absence sans justificatif valable', 'ABSENCE', 'OUVRES',
     FALSE, FALSE, FALSE, NULL, NULL, FALSE, NULL, FALSE, TRUE, 40, '#dc2626')
ON CONFLICT (code) DO NOTHING;

COMMIT;
