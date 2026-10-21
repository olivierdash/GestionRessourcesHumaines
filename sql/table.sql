-- ============================================================
-- 1. UTILISATEURS ET RÔLES
-- ============================================================
 DROP TABLE IF EXISTS candidat                       CASCADE
 DROP TABLE IF EXISTS candidature                    CASCADE
 DROP TABLE IF EXISTS contract                       CASCADE
 DROP TABLE IF EXISTS demande_recrutement            CASCADE
 DROP TABLE IF EXISTS departement                    CASCADE
 DROP TABLE IF EXISTS employer                       CASCADE
 DROP TABLE IF EXISTS etat_demande                   CASCADE
 DROP TABLE IF EXISTS evaluation                     CASCADE
 DROP TABLE IF EXISTS grille_salarial                CASCADE
 DROP TABLE IF EXISTS historique_demande_recrutement CASCADE
 DROP TABLE IF EXISTS notification                   CASCADE
 DROP TABLE IF EXISTS poste                          CASCADE
 DROP TABLE IF EXISTS poste_mission                  CASCADE
 DROP TABLE IF EXISTS proposition_embauche           CASCADE
 DROP TABLE IF EXISTS roles                          CASCADE
 DROP TABLE IF EXISTS utilisateur                    CASCADE
 DROP TABLE IF EXISTS validation_proposition         CASCADE

CREATE TABLE roles (
    id_role SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);

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

-- ============================================================
-- 2. CANDIDATS ET ÉTATS
-- ============================================================
CREATE TABLE candidat (
    id_candidat SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(255) UNIQUE NOT NULL,
    cv_url VARCHAR(500)
);

CREATE TABLE etat_demande (
    id_etat_demande SERIAL PRIMARY KEY,
    intitule VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================================
-- 3. STRUCTURE DE L'ENTREPRISE
-- ============================================================
CREATE TABLE poste (
    id_poste SERIAL PRIMARY KEY,
    id_departement INTEGER NOT NULL,
    date_effet DATE,
    intitule VARCHAR(255) NOT NULL,
    lieu VARCHAR(255),
    duree_hebdo INTEGER,
    CONSTRAINT fk_poste_departement FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

CREATE TABLE poste_mission (
    id_mission SERIAL PRIMARY KEY,
    id_poste INTEGER NOT NULL,
    titre VARCHAR(255) NOT NULL,
    CONSTRAINT fk_mission_poste FOREIGN KEY (id_poste) REFERENCES poste(id_poste) ON DELETE CASCADE
);

CREATE TABLE grille_salarial (
    id_grille SERIAL PRIMARY KEY,
    id_poste INTEGER NOT NULL,
    id_departement INTEGER NOT NULL,
    salaire_min NUMERIC(10,2) NOT NULL,
    salaire_max NUMERIC(10,2) NOT NULL,
    salaire_moyen NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_grille_poste FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    CONSTRAINT fk_grille_departement FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
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
    motif TEXT,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    CONSTRAINT fk_demande_manager FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur),
    CONSTRAINT fk_demande_poste FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

CREATE TABLE historique_demande_recrutement (
    id_historique_demande_recrutement SERIAL PRIMARY KEY,
    id_demande_recrutement INTEGER NOT NULL,
    id_etat_demande INTEGER NOT NULL,
    id_validateur INTEGER NOT NULL,
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_demande FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement) ON DELETE CASCADE,
    CONSTRAINT fk_hist_etat FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    CONSTRAINT fk_hist_validateur FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur)
);

-- ============================================================
-- 5. BLOC 2 : RECRUTEMENT ET ÉVALUATION
-- ============================================================
CREATE TABLE candidature (
    id_candidature SERIAL PRIMARY KEY,
    id_candidat INTEGER NOT NULL,
    id_demande_recrutement INTEGER NOT NULL,
    date_depot_candidature TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut_candidature VARCHAR(50) NOT NULL DEFAULT 'RECU',
    CONSTRAINT fk_candidature_candidat FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    CONSTRAINT fk_candidature_demande FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    CONSTRAINT uk_candidat_demande UNIQUE (id_candidat, id_demande_recrutement)
);

CREATE TABLE evaluation (
    id_evaluation SERIAL PRIMARY KEY,
    id_candidature INTEGER NOT NULL,
    id_evaluateur INTEGER NOT NULL,
    type_evaluation VARCHAR(50) NOT NULL,
    evaluation TEXT,
    note NUMERIC(5,2),
    date_evaluation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_candidature FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature) ON DELETE CASCADE,
    CONSTRAINT fk_eval_evaluateur FOREIGN KEY (id_evaluateur) REFERENCES utilisateur(id_utilisateur)
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
    statut_proposition VARCHAR(50) NOT NULL,
    date_decision TIMESTAMP,
    CONSTRAINT fk_prop_candidature FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE validation_proposition (
    id_validation_proposition SERIAL PRIMARY KEY,
    id_proposition INTEGER NOT NULL,
    id_validateur INTEGER NOT NULL,
    departement_role VARCHAR(100),
    decision VARCHAR(50) NOT NULL,
    date_validation_proposition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    CONSTRAINT fk_valprop_proposition FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id_proposition) ON DELETE CASCADE,
    CONSTRAINT fk_valprop_validateur FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur)
);

-- ============================================================
-- 7. BLOC 4 : EMBAUCHE ET CONTRAT
-- ============================================================
CREATE TABLE employer (
    id_employer SERIAL PRIMARY KEY,
    id_candidature INTEGER UNIQUE NOT NULL,
    matricule VARCHAR(50) UNIQUE NOT NULL,
    hiring_date DATE NOT NULL,
    status_employer VARCHAR(50) NOT NULL,
    CONSTRAINT fk_employer_candidature FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE contract (
    id_contract SERIAL PRIMARY KEY,
    id_employer INTEGER NOT NULL,
    id_poste INTEGER NOT NULL,
    type_contrat VARCHAR(50) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    salaire_mensuel NUMERIC(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_contract_employer FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE,
    CONSTRAINT fk_contract_poste FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================
CREATE TABLE notification (
    id_notification SERIAL PRIMARY KEY,
    id_destinataire_candidat INTEGER NULL,
    id_destinataire_utilisateur INTEGER NULL,
    type_notification VARCHAR(50) NOT NULL,
    message VARCHAR(255) NOT NULL,
    lien_action VARCHAR(255),
    est_lu BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_candidat FOREIGN KEY (id_destinataire_candidat) REFERENCES candidat(id_candidat),
    CONSTRAINT fk_notif_utilisateur FOREIGN KEY (id_destinataire_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE offre_emploi (
    id_offre SERIAL PRIMARY KEY,
    id_demande_recrutement INT NOT NULL,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_publication TIMESTAMP,
    date_cloture TIMESTAMP,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    CONSTRAINT uk_offre_demande UNIQUE (id_demande_recrutement)
);

ALTER TABLE candidature ADD COLUMN id_offre INT;
ALTER TABLE candidature ADD CONSTRAINT fk_candidature_offre FOREIGN KEY (id_offre) REFERENCES offre_emploi(id_offre);