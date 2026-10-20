CREATE TABLE superuser(
    id_validateur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50),
    statut VARCHAR(50)
);

CREATE TABLE manager (
    id_manager INT AUTO_INCREMENT PRIMARY KEY,
    nom_prenom VARCHAR(255) NOT NULL
);

CREATE TABLE departement (
    id_departement INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL
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
    intitule VARCHAR(100) NOT NULL -- Ex: 'BROUILLON', 'VALIDE_RH', 'VALIDE_FINANCE', 'VALIDE_DG'
);

-- 2. Tables structurelles de l'entreprise
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

-- 3. Bloc 1 : Demande de recrutement et Workflow
CREATE TABLE demande_recrutement (
    id_demande_recrutement INT AUTO_INCREMENT PRIMARY KEY,
    id_manager INT NOT NULL,
    id_poste INT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    effectif INT NOT NULL,
    motif TEXT,
    FOREIGN KEY (id_manager) REFERENCES manager(id_manager),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

CREATE TABLE historique_demande_recrutement (
    id_historique_demande_recrutement INT AUTO_INCREMENT PRIMARY KEY,
    id_demande_recrutement INT NOT NULL,
    id_etat_demande INT NOT NULL,
    id_validateur INT NOT NULL,
    date_changement DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement),
    FOREIGN KEY (id_etat_demande) REFERENCES etat_demande(id_etat_demande),
    FOREIGN KEY (id_validateur) REFERENCES superuser(id_validateur)
);

-- 4. Bloc 2 & 3 : Recrutement, Sélection et Proposition
CREATE TABLE candidature (
    id_candidature INT AUTO_INCREMENT PRIMARY KEY,
    id_candidat INT NOT NULL,
    id_demande_recrutement INT NOT NULL,
    date_depot_candidature DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut_candidature VARCHAR(50) NOT NULL, -- Ex: 'RECU', 'TEST_VALIDE', 'RETENU', 'REFUSE'
    FOREIGN KEY (id_candidat) REFERENCES candidat(id_candidat),
    FOREIGN KEY (id_demande_recrutement) REFERENCES demande_recrutement(id_demande_recrutement)
);

CREATE TABLE evaluation (
    id_evaluation INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT NOT NULL,
    evaluation VARCHAR(255), -- Ex: 'Entretien RH', 'Test technique'
    note DECIMAL(5,2), -- Ex: 15.5
    date_evaluation DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE proposition_embauche (
    id_proposition INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT NOT NULL,
    salaire_propose DECIMAL(10,2) NOT NULL,
    salaire_final DECIMAL(10,2), -- Le résultat de la négociation
    date_proposition DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME NOT NULL,
    statut_proposition VARCHAR(50) NOT NULL, -- 'EN_ATTENTE', 'NEGOCIATION', 'ACCEPTEE', 'REFUSEE', 'EXPIREE'
    date_decision DATETIME,
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE validation_proposition (
    id_validation_proposition INT AUTO_INCREMENT PRIMARY KEY,
    id_proposition INT NOT NULL,
    id_validateur INT NOT NULL,
    departement_role VARCHAR(100),
    decision VARCHAR(50) NOT NULL, -- 'APPROUVE', 'REJETE'
    date_validation_proposition DATETIME DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_proposition) REFERENCES proposition_embauche(id_proposition),
    FOREIGN KEY (id_validateur) REFERENCES superuser(id_validateur) 
);

-- 5. Fin du Bloc 3 & Bloc 4 : Embauche et Contrat
CREATE TABLE employer (
    id_employer INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT UNIQUE NOT NULL, -- Unique car 1 candidature = 1 employé
    matricule VARCHAR(50) UNIQUE NOT NULL, -- Ex: 'EMP-2025-001'
    hiring_date DATE NOT NULL,
    status_employer VARCHAR(50) NOT NULL, -- 'ACTIF', 'SUSPENDU', 'SORTI'
    FOREIGN KEY (id_candidature) REFERENCES candidature(id_candidature)
);

CREATE TABLE contract (
    id_contract INT AUTO_INCREMENT PRIMARY KEY,
    id_employer INT NOT NULL,
    id_poste INT NOT NULL,
    type_contrat VARCHAR(50) NOT NULL, -- 'CDD', 'CDI', 'ESSAI', 'AVENANT'
    date_debut DATE NOT NULL,
    date_fin DATE, -- NULL si CDI
    salaire_mensuel DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- 'BROUILLON', 'SIGNE', 'ACTIF', 'TERMINE'
    FOREIGN KEY (id_employer) REFERENCES employer(id_employer),
    FOREIGN KEY (id_poste) REFERENCES poste(id_poste)
);

