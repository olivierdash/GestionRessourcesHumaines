# CAHIER DES CHARGES TECHNIQUE : WORKFLOW DU SIRH
Projet : Système d'Information de Gestion des Ressources Humaines (SIRH)  
- Architecture : Spring Boot, PostgreSQL/MySQL, Thymeleaf  
- Rôle du document : Spécifications fonctionnelles et règles métier  

## Les Acteurs du Système
Manager : Exprime le besoin et évalue les candidats.  

RH (Superuser) : Gère le recrutement, les propositions et les contrats.  

Finance (Superuser) : Valide le budget global du recrutement.  

DG (Superuser) : Valide les embauches hors budget ou les négociations salariales.  

Candidat : Postule, passe les tests et reçoit les propositions.  

## Workflow Global (Le Cycle de Vie)
### BLOC 1 : Expression du Besoin (Demande de recrutement)
**Objectif : Valider l'ouverture d'un poste.**  
1. Création : Le Manager crée une demande_recrutement (liée au poste, avec effectif et motif). L'état initial est BROUILLON.
2. Validation RH : Le RH vérifie la cohérence et ajoute l'état VALIDE_RH dans historique_demande_recrutement.
3. Validation Finance : La Finance vérifie si les fonds sont disponibles (via la grille_salarial). Si oui, état VALIDE_FINANCE.
4. Validation DG : La DG donne le feu vert final. État VALIDE_DG.
5. Règle : Tant que l'état n'est pas VALIDE_DG, aucune candidature ne peut être reçue.

### BLOC 2 : Recrutement & Sélection
**Objectif : Trier les candidats.**  
1. Dépôt : Les candidats postulent. Création d'une ligne dans candidature (statut RECU).
2. Évaluation : Le RH fait passer des tests. Il enregistre les notes dans la table evaluation.
3. Validation des tests : Si le candidat réussit, la candidature passe au statut TEST_VALIDE.
4. Règle métier : Le système bloque le passage à l'étape suivante si le nombre de candidats avec le statut TEST_VALIDE est inférieur à 2.

### BLOC 3 : Proposition d'Embauche & Négociation (Cœur du système)
**Objectif : Faire une offre, gérer les négociations et les refus en cascade.**  
1. Création de la proposition : Le RH sélectionne le Candidat 1 et crée une proposition_embauche avec un salaire_propose et une date_expiration (+15 jours). Le statut est EN_ATTENTE.
2. Notification Candidat : Une notification lui est envoyée pour l'informer de l'offre.
3. Réponse du Candidat (3 scénarios possibles) :
    * Scénario A (Acceptation directe) : Le statut passe à ACCEPTEE, la date_decision est remplie. -> Passage au Bloc 4.
    * Scénario B (Négociation) : Le candidat demande un salaire_final plus élevé.  

4. Règle d'or (Validation Automatique) : Le système compare salaire_final avec la grille_salarial (salaire_min et salaire_max).  
_**Si le salaire est DANS la fourchette :** La proposition passe automatiquement à ACCEPTEE. Une notification est envoyée au RH pour dire que c'est validé._  
_**Si le salaire est HORS fourchette (trop cher) :** Le statut passe à EN_ATTENTE_DG. Une notification est envoyée à la DG pour une validation manuelle (ajout d'une ligne dans validation_proposition)._

5. Scénario C (Refus ou Expiration) : Le candidat refuse ou la date_expiration est dépassée. Le statut passe à REFUSEE ou EXPIREE.  

6. Règle du Candidat 2 : Le système cherche automatiquement la candidature suivante (TEST_VALIDE) et génère une nouvelle proposition_embauche. Si aucun autre candidat, le processus s'arrête et la demande repasse en recrutement.


### BLOC 4 : Embauche & Contrat
**Objectif : Intégrer le salarié.**  
1. Création Employé : Dès que la proposition est ACCEPTEE, le système génère un matricule unique et crée une ligne dans employer.
2. Création Contrat : Création d'une ligne dans contract (type CDD/CDI/ESSAI, dates, salaire). Statut BROUILLON puis SIGNE.
3. Onboarding : Déclenchement des tâches (badge, compte, matériel).

### Système de Notifications
Toutes les actions importantes génèrent une ligne dans la table notification (cloche 🔔 sur l'interface).  
**Pour le Superuser (RH/Finance/DG) :**  
- "Nouvelle demande de recrutement à valider."
- "Le candidat a accepté la proposition."
- "La négociation a été validée automatiquement (dans la fourchette)."
- "Action requise : le candidat demande un salaire hors grille."  
**Pour le Candidat :**  
- "Vous avez reçu une proposition d'embauche (Date d'expiration : JJ/MM/AAAA)."
- "Votre contrat est prêt à être signé."

### Tâches Automatiques (Cron Jobs / Spring Scheduler)
- Vérification des expirations : Tous les jours à minuit, le système vérifie les proposition_embauche. Si date_expiration < Date_du_jour et que le statut est EN_ATTENTE, il passe le statut à EXPIREE et déclenche la règle du Candidat 2.

- Calcul des coûts : Quand la Finance valide, le système calcule le coût total réel (salaire_moyen * 12 + charges) pour s'assurer que le budget global du département n'est pas dépassé.