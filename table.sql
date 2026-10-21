-- 1. TABLES DES UTILISATEURS ET RÔLES
CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    nom_prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL, -- Sera hashé par Spring Security (ex: BCrypt)
    role VARCHAR(50) NOT NULL -- 'MANAGER', 'RH', 'FINANCE', 'DG'
);

-- (Optionnel) Si tu veux garder le département du manager
CREATE TABLE departement (
    id_departement INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    id_manager INT NULL, -- Lien vers l'utilisateur qui est manager
    FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE candidat (
    id_candidat INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(255) UNIQUE NOT NULL,
    cv_url VARCHAR(500)
);

CREATE TABLE etat_demande (
    id_etat_demande INT AUTO_INCREMENT PRIMARY KEY,
    intitule VARCHAR(100) NOT NULL -- 'BROUILLON', 'VALIDE_RH', 'VALIDE_FINANCE', 'VALIDE_DG'
);

-- 2. STRUCTURE DE L'ENTREPRISE
CREATE TABLE poste (
    id_poste INT AUTO_INCREMENT PRIMARY KEY,
    id_departement INT NOT NULL,
    date_effet DATE,
    intitule VARCHAR(255) NOT NULL,
    lieu VARCHAR(255),
    duree_hebdo INT,
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

CREATE TABLE poste_mission (
    id_mission INT AUTO_INCREMENT PRIMARY KEY,
    id_poste INT NOT NULL,
    titre VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

CREATE TABLE grille_salarial (
    id_grille INT AUTO_INCREMENT PRIMARY KEY,
    id_poste INT NOT NULL,
    id_departement INT NOT NULL,
    salaire_min DECIMAL(10,2) NOT NULL,
    salaire_max DECIMAL(10,2) NOT NULL,
    salaire_moyen DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste),
    FOREIGN KEY (id_departement) REFERENCES departement(id_departement)
);

-- 3. BLOC 1 : DEMANDE ET WORKFLOW
CREATE TABLE demande_recrutement (
    id_demande_recrutement INT AUTO_INCREMENT PRIMARY KEY,
    id_manager INT NOT NULL, -- Lié à utilisateur (role MANAGER)
    id_poste INT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    effectif INT NOT NULL,
    motif TEXT,
    FOREIGN KEY (id_manager) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

CREATE TABLE historique_demande_recrutement (
    id_historique_demande_recrutement INT AUTO_INCREMENT PRIMARY KEY,
    id_demande_recrutement INT NOT NULL,
    id_etat_demande INT NOT NULL,
    id_validateur INT NOT NULL, -- <-- Lié à utilisateur (peut être RH, Finance ou DG)
    date_changement DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur)
);

-- 4. BLOC 2 & 3 : RECRUTEMENT ET PROPOSITION
CREATE TABLE candidature (
    id_candidature INT AUTO_INCREMENT PRIMARY KEY,
    id_candidat INT NOT NULL,
    id_demande_recrutement INT NOT NULL,
    date_depot_candidature DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut_candidature VARCHAR(50) NOT NULL, 
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement)
);

CREATE TABLE evaluation (
    id_evaluation INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT NOT NULL,
    evaluation VARCHAR(255), 
    note DECIMAL(5,2), 
    date_evaluation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE proposition_embauche (
    id_proposition INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT NOT NULL,
    salaire_propose DECIMAL(10,2) NOT NULL,
    salaire_final DECIMAL(10,2), 
    date_proposition DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME NOT NULL,
    statut_proposition VARCHAR(50) NOT NULL, 
    date_decision DATETIME,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE validation_proposition (
    id_validation_proposition INT AUTO_INCREMENT PRIMARY KEY,
    id_proposition INT NOT NULL,
    id_validateur INT NOT NULL, -- <-- Lié à utilisateur (DG ou Finance)
    departement_role VARCHAR(100),
    decision VARCHAR(50) NOT NULL, 
    date_validation_proposition DATETIME DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id_proposition),
    FOREIGN KEY (id_validateur) REFERENCES utilisateur(id_utilisateur) 
);

-- 5. BLOC 3 & 4 : EMBAUCHE ET CONTRAT
CREATE TABLE employer (
    id_employer INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT UNIQUE NOT NULL, 
    matricule VARCHAR(50) UNIQUE NOT NULL, 
    hiring_date DATE NOT NULL,
    status_employer VARCHAR(50) NOT NULL, 
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE contract (
    id_contract INT AUTO_INCREMENT PRIMARY KEY,
    id_employer INT NOT NULL,
    id_poste INT NOT NULL,
    type_contrat VARCHAR(50) NOT NULL, 
    date_debut DATE NOT NULL,
    date_fin DATE, 
    salaire_mensuel DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL, 
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

-- 6. NOTIFICATIONS
CREATE TABLE notification (
    id_notification INT AUTO_INCREMENT PRIMARY KEY,
    id_destinataire_candidat INT NULL,   -- Si notif pour candidat
    id_destinataire_utilisateur INT NULL, -- Si notif pour RH/Finance/DG/Manager
    type_notification VARCHAR(50) NOT NULL, 
    message VARCHAR(255) NOT NULL,
    lien_action VARCHAR(255),
    est_lu BOOLEAN DEFAULT FALSE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_destinataire_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_destinataire_utilisateur) REFERENCES utilisateur(id_utilisateur)
);