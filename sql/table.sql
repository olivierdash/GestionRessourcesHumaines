-- ============================================================
-- 1. NETTOYAGE COMPLET
-- ============================================================
DROP TABLE IF EXISTS document CASCADE;
DROP TABLE IF EXISTS onboarding_tache CASCADE;
DROP TABLE IF EXISTS onboarding CASCADE;
DROP TABLE IF EXISTS affectation CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS contract CASCADE;
DROP TABLE IF EXISTS employer CASCADE;
DROP TABLE IF EXISTS validation_proposition CASCADE;
DROP TABLE IF EXISTS proposition_embauche CASCADE;
DROP TABLE IF EXISTS evaluation CASCADE;
DROP TABLE IF EXISTS candidature CASCADE;
DROP TABLE IF EXISTS offre_emploi CASCADE;
DROP TABLE IF EXISTS historique_demande_recrutement CASCADE;
DROP TABLE IF EXISTS demande_recrutement CASCADE;
DROP TABLE IF EXISTS grille_salarial CASCADE;
DROP TABLE IF EXISTS poste_mission CASCADE;
DROP TABLE IF EXISTS poste CASCADE;
DROP TABLE IF EXISTS departement CASCADE;
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS candidat CASCADE;
DROP TABLE IF EXISTS etat_demande CASCADE;
DROP TABLE IF EXISTS motif_demande CASCADE;
DROP TABLE IF EXISTS statut_offre_emploie CASCADE;
DROP TABLE IF EXISTS statut_candidature CASCADE;
DROP TABLE IF EXISTS statut_proposition CASCADE;
DROP TABLE IF EXISTS statut_contrat CASCADE;
DROP TABLE IF EXISTS statut_tache_onboarding CASCADE;
DROP TABLE IF EXISTS type_evaluation CASCADE;
DROP TABLE IF EXISTS type_contrat CASCADE;
DROP TABLE IF EXISTS type_notification CASCADE;
DROP TABLE IF EXISTS type_document CASCADE;

-- ============================================================
-- 2. TABLES DE RÉFÉRENCE (STATUTS & TYPES)
-- ============================================================
CREATE TABLE type_notification (id_type_notification SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE type_evaluation (id_type_evaluation SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE type_contrat (id_type_contrat SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_candidature (id_statut_candidature SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_proposition (id_statut_proposition SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_offre_emploie (id_statut_offre_emploie SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE etat_demande (id_etat_demande SERIAL PRIMARY KEY, intitule VARCHAR(100) NOT NULL UNIQUE);
CREATE TABLE motif_demande (id_motif_demande SERIAL PRIMARY KEY, libelle VARCHAR(100) NOT NULL UNIQUE);
CREATE TABLE statut_contrat (id_statut_contrat SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE statut_tache_onboarding (id_statut_tache_onboarding SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE type_document (id_type_document SERIAL PRIMARY KEY, libelle VARCHAR(50) NOT NULL UNIQUE);

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
    lieu VARCHAR(255),
    duree_hebdo INTEGER,
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

CREATE TABLE poste_mission (
    id_mission SERIAL PRIMARY KEY,
    id_poste INTEGER NOT NULL,
    titre VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste) ON DELETE CASCADE
);

CREATE TABLE grille_salarial (
    id_grille SERIAL PRIMARY KEY,
    id_poste INTEGER NOT NULL,
    id_departement INTEGER NOT NULL,
    salaire_min NUMERIC(10,2) NOT NULL,
    salaire_max NUMERIC(10,2) NOT NULL,
    salaire_moyen NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

-- ============================================================
-- 4. BLOC 1 : DEMANDE ET WORKFLOW (MIS À JOUR)
-- ============================================================
CREATE TABLE demande_recrutement (
    id_demande_recrutement SERIAL PRIMARY KEY,
    id_manager INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_souhaitee DATE,
    effectif INTEGER NOT NULL,
    id_etat_demande INTEGER NOT NULL, -- Remplacé par le coéquipier
    id_motif_demande INTEGER NOT NULL, -- Remplacé par le coéquipier
    commentaires TEXT,
    FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    FOREIGN KEY (id_motif_demande) REFERENCES motif_demande(id_motif_demande)
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
    cv_url VARCHAR(500)
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
    remarque TEXT, -- Ajout coéquipier
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature),
    FOREIGN KEY (id_statut_proposition) REFERENCES statut_proposition(id_statut_proposition)
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
-- 7. BLOC 4 : EMBAUCHE, CONTRAT ET ONBOARDING
-- ============================================================
CREATE TABLE employer (
    id_employer SERIAL PRIMARY KEY,
    id_candidature INTEGER UNIQUE NOT NULL,
    matricule VARCHAR(50) UNIQUE NOT NULL,
    hiring_date DATE NOT NULL,
    status_employer VARCHAR(50) NOT NULL,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE contract (
    id_contract SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    id_type_contrat INTEGER NOT NULL,
    id_statut_contrat INTEGER NOT NULL, -- Remplace "status"
    date_debut DATE NOT NULL,
    date_fin DATE,
    salaire_mensuel NUMERIC(10,2) NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_type_contrat) REFERENCES type_contrat(id_type_contrat),
    FOREIGN KEY (id_statut_contrat) REFERENCES statut_contrat(id_statut_contrat)
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

CREATE TABLE document (
    id_document SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_type_document INTEGER NOT NULL,
    id_tache INTEGER NULL,
    nom_fichier VARCHAR(255) NOT NULL,
    fichier_url VARCHAR(500) NOT NULL,
    date_depot TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_depose_par INTEGER NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer),
    FOREIGN KEY (id_type_document) REFERENCES type_document(id_type_document),
    FOREIGN KEY (id_tache) REFERENCES onboarding_tache(id_tache),
    FOREIGN KEY (id_depose_par) REFERENCES utilisateur(id_utilisateur)
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