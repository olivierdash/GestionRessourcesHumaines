-- ============================================================
-- 1. UTILISATEURS ET RÔLES
-- ============================================================

CREATE TABLE roles (
    id_role SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE   -- 'MANAGER', 'RH', 'FINANCE', 'DG'
);

CREATE TABLE utilisateur (
    id_utilisateur SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    motDePasse VARCHAR(255) NOT NULL,
    id_role INT NOT NULL,
    FOREIGN KEY (id_role) REFERENCES roles(id_role)
);

CREATE TABLE departement (
    id_departement SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    id_manager INT NULL,
    FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur)
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
    cv_url VARCHAR(500)                    -- simple : 1 CV par candidat
);

CREATE TABLE etat_demande (
    id_etat_demande SERIAL PRIMARY KEY,
    intitule VARCHAR(100) NOT NULL UNIQUE  -- 'BROUILLON', 'VALIDE_RH', 'VALIDE_FINANCE', 'VALIDE_DG'
);

-- ============================================================
-- 3. STRUCTURE DE L'ENTREPRISE
-- ============================================================

CREATE TABLE poste (
    id_poste SERIAL PRIMARY KEY,
    id_departement INT NOT NULL,
    date_effet DATE,
    intitule VARCHAR(255) NOT NULL,
    lieu VARCHAR(255),
    duree_hebdo INT,
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

CREATE TABLE poste_mission (
    id_mission SERIAL PRIMARY KEY,
    id_poste INT NOT NULL,
    titre VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste) ON DELETE CASCADE
);

CREATE TABLE grille_salarial (
    id_grille SERIAL PRIMARY KEY,
    id_poste INT NOT NULL,
    id_departement INT NOT NULL,
    salaire_min DECIMAL(10,2) NOT NULL,
    salaire_max DECIMAL(10,2) NOT NULL,
    salaire_moyen DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

-- ============================================================
-- 4. BLOC 1 : DEMANDE ET WORKFLOW
-- ============================================================

CREATE TABLE demande_recrutement (
    id_demande_recrutement SERIAL PRIMARY KEY,
    id_manager INT NOT NULL,
    id_poste INT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_souhaitee DATE,                   -- date d'entrée souhaitée (Gestion.md)
    effectif INT NOT NULL,
    motif TEXT,
    statut VARCHAR(50) NOT NULL DEFAULT 'BROUILLON', -- dénormalisé pour filtrer vite
    FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

CREATE TABLE historique_demande_recrutement (
    id_historique_demande_recrutement SERIAL PRIMARY KEY,
    id_demande_recrutement INT NOT NULL,
    id_etat_demande INT NOT NULL,
    id_validateur INT NOT NULL,
    date_changement DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement) ON DELETE CASCADE,
    FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur)
);

-- ============================================================
-- 5. BLOC 2 : RECRUTEMENT ET ÉVALUATION  ← TON BLOC
-- ============================================================

CREATE TABLE candidature (
    id_candidature SERIAL PRIMARY KEY,
    id_candidat INT NOT NULL,
    id_demande_recrutement INT NOT NULL,
    date_depot_candidature DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut_candidature VARCHAR(50) NOT NULL DEFAULT 'RECU',
    -- 'RECU', 'EN_EVALUATION', 'RETENU', 'REFUSE'
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    CONSTRAINT uk_candidat_demande UNIQUE (id_candidat, id_demande_recrutement)
);

CREATE TABLE evaluation (
    id_evaluation SERIAL PRIMARY KEY,
    id_candidature INT NOT NULL,
    id_evaluateur INT NOT NULL,                    -- qui a évalué (RH, Manager, etc.)
    type_evaluation VARCHAR(50) NOT NULL,          -- 'TEST_TECHNIQUE', 'ENTRETIEN_RH', 'ENTRETIEN_MANAGER'
    evaluation TEXT,                               -- compte-rendu
    note DECIMAL(5,2),
    date_evaluation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature) ON DELETE CASCADE,
    FOREIGN KEY (id_evaluateur) REFERENCES utilisateur(id_utilisateur)
);

-- ============================================================
-- 6. BLOC 3 : PROPOSITION ET VALIDATION
-- ============================================================

CREATE TABLE proposition_embauche (
    id_proposition SERIAL PRIMARY KEY,
    id_candidature INT NOT NULL,
    salaire_propose DECIMAL(10,2) NOT NULL,
    salaire_final DECIMAL(10,2),
    date_proposition DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME NOT NULL,
    statut_proposition VARCHAR(50) NOT NULL,       -- 'EN_ATTENTE', 'ACCEPTE', 'REFUSE', 'EXPIRE'
    date_decision DATETIME,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE validation_proposition (
    id_validation_proposition SERIAL PRIMARY KEY,
    id_proposition INT NOT NULL,
    id_validateur INT NOT NULL,
    departement_role VARCHAR(100),
    decision VARCHAR(50) NOT NULL,                 -- 'APPROUVE', 'REFUSE'
    date_validation_proposition DATETIME DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id_proposition) ON DELETE CASCADE,
    FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur)
);

-- ============================================================
-- 7. BLOC 4 : EMBAUCHE ET CONTRAT
-- ============================================================

CREATE TABLE employer (
    id_employer SERIAL PRIMARY KEY,
    id_candidature INT UNIQUE NOT NULL,
    matricule VARCHAR(50) UNIQUE NOT NULL,
    hiring_date DATE NOT NULL,
    status_employer VARCHAR(50) NOT NULL,          -- 'ACTIF', 'INACTIF', 'SORTI'
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE contract (
    id_contract SERIAL PRIMARY KEY,
    id_employer INT NOT NULL,
    id_poste INT NOT NULL,
    type_contrat VARCHAR(50) NOT NULL,             -- 'CE', 'CDD', 'CDI', 'STAGE'
    date_debut DATE NOT NULL,
    date_fin DATE,
    salaire_mensuel DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,                   -- 'BROUILLON', 'SIGNE', 'ACTIF', 'TERMINE'
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer) ON DELETE CASCADE,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

-- ============================================================
-- 8. NOTIFICATIONS
-- ============================================================

CREATE TABLE notification (
    id_notification SERIAL PRIMARY KEY,
    id_destinataire_candidat INT NULL,
    id_destinataire_utilisateur INT NULL,
    type_notification VARCHAR(50) NOT NULL,
    message VARCHAR(255) NOT NULL,
    lien_action VARCHAR(255),
    est_lu BOOLEAN DEFAULT FALSE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_destinataire_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_destinataire_utilisateur) REFERENCES utilisateur(id_utilisateur)
);