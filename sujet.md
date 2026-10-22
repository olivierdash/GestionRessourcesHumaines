# Conception d'un logiciel de gestion RH

Du recrutement à la rupture du contrat : concevoir un SIRH autour du cycle de vie du salarié

Besoin → Recrutement → Embauche → Vie du salarié → Évolution → Rupture

---

## Objectifs pédagogiques

Comprendre les processus RH avant de concevoir les fonctionnalités du logiciel.

Transformer un processus métier en règles de gestion, données, workflows et interfaces.

Concevoir un SIRH (Système d'information des ressources humaines) couvrant tout le cycle de vie du salarié.

Éviter une conception limitée à de simples écrans CRUD.

Faire évoluer un même cas pratique jusqu'à l'architecture et au MVP.

---

## Bloc 1 — Expression du besoin en personnel

Un manager exprime un besoin : création ou remplacement d'un poste.

Informations : poste, département, effectif, motif, budget, date souhaitée.

Workflow : Manager → RH → Finance -> Direction → ouverture du recrutement.
Direction -> RH

Objets métier : Département, Poste, DemandeRecrutement, Validation.

Point de conception : un POSTE existe indépendamment de l'EMPLOYÉ.

---

## Bloc 2 — Recrutement

Transformer une demande validée en offre d'emploi.

Publier l'offre et recevoir plusieurs candidatures.

Présélectionner, organiser les entretiens et les tests.

Conserver les évaluations et la décision finale.

Objets : OffreEmploi, Candidat, Candidature, Entretien, ÉvaluationCandidat.

---

## Bloc 3 — Sélection et embauche

Le candidat retenu reçoit une proposition d'embauche.

Après acceptation : création du dossier salarié et du matricule.

Créer le contrat, le poste et l'affectation.

Point de conception : CANDIDAT ≠ EMPLOYÉ.

Une personne peut avoir plusieurs candidatures avant de devenir salariée.

---

## Bloc 4 — Contrat et onboarding

Un salarié peut avoir plusieurs contrats successifs : CDD, renouvellement, CDI...

Ne pas écraser l'historique en stockant seulement employe.type_contrat.

Onboarding : contrat signé, badge, compte, matériel, formation, inscription paie.

Utiliser une checklist et un workflow d'intégration.

Objets : Contrat, Affectation, Onboarding, OnboardingTache, Document.

---

## Architecture technique possible

### Couche applicative

- Front Web / Mobile
- API REST
- Services métier RH
- Moteur de workflow
- Authentification

### Données & intégration

- PostgreSQL / MySQL
- GED / stockage documents
- Notifications
- Audit & logs
- Reporting / BI
