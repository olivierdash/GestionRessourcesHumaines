# ma partie j'ai utilisé ces tables :
J'ai demandé à chat de me faire un remake de tout les tables avec vos modif et il m'a fabriqué ces tables et j'ai demander la structure de la table et la rêgle de gestion dans ce truc.  
```sql
-- (BLOC 5) : GESTION DES DOCUMENTS ET VALIDITÉ (ajout de date d'expiration et de status de document pour l'état)
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

-- (BLOC 5) : COORDONNÉES (à corriger par aristide dans le cas où )
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

-- Planning (en théorie s'il a fini ) (aristide)
CREATE TABLE regime_travail (
    id_regime_travail SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    heures_hebdomadaires INTEGER NOT NULL CHECK (heures_hebdomadaires > 0),
    jours_hebdomadaires INTEGER NOT NULL CHECK (jours_hebdomadaires BETWEEN 1 AND 7),
    actif BOOLEAN NOT NULL DEFAULT TRUE
);
```

Insérer ensuite les donnés de test dans insertionTest.sql après avoir insérer les tables.sql et test.sql 

**Model :**  
- JournalAudit.java
- CoordonneesEmploye.java
- Document.java (ajout de colonne date d'expiration et de statut_document)

**Repository :**  
- CoordonneesEmployeRepository
- JournalAuditRepository
- DocumentRepository


**Service :**  
- AuditService.java (pour enregistrer les actions effectuer sur tel ou tel dossier)  


**Controler :**
- DossierSalarierControlleur.java
- SecurityConfig.java (ajout de la route url dans selon le rôle )
```java
.requestMatchers("/dossier/**").hasAnyRole("RH", "ADMIN","DG")
```



### Règle de gestion :  
**Règle 1 : Traçabilité(Le Journal d'Audit) :**  
> "Toute action sensible (consultation d'un dossier, modification de coordonnées, ajout d'un document) doit être enregistrée avec l'identité de l'auteur, la date et l'heure."  
**Pourquoi ?**  
En cas de fuite de données ou de litige RH, on doit savoir qui a regardé le dossier de qui, et quand.



### **Règle 2 : Contrôle de validité des documents :**  
> "Un document administratif a une durée de vie légale. Le système doit alerter les RH lorsqu'un document est expiré ou arrive à expiration."  

**Pourquoi ?**  
Une carte d'identité périmée ou un titre de séjour expiré rend le salarié illégal au travail. Les RH doivent anticiper le renouvellement.  


### **Règle 3 : Habilitations strictes (Confidentialité) :**  
> "Le dossier salarié contient des données confidentielles (salaire, situation familiale, etc.). Seuls les RH, l'Admin et le DG peuvent y accéder. Un manager ne voit que ses équipes, et un employé ne voit que son propre profil."  

**Pourquoi ?**  
Protection des données personnelles (RGPD) et de la vie privée.



**Règle 4 : Unicité du dossier central (Référentiel) :**  
> "Un employé possède un seul et unique dossier administratif centralisé. Les coordonnées personnelles (adresse, situation matrimoniale) sont uniques à cet employé."  

**Pourquoi ?**  
Éviter les doublons d'adresses ou de fiches dans la base de données.  



**Règle 5 : Intégrité des modifications :**  
> "Lorsqu'un RH modifie une donnée du dossier salarié, l'ancienne valeur ne doit pas être perdue sans laisser de trace."  

**Pourquoi ?**  
Si un RH change l'adresse par erreur, on doit pouvoir savoir ce qu'il y avait avant.
