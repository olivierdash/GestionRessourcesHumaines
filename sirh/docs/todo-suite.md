# A faire (suite)

## bloc 4 : contrat et onboarding (MAJ)

- Un salarie peut avoir plusieurs contrats successifs:CDD, renouvellement, CDI...
- Ne pas ecraser l'historique en stockant seulement employe.type_contrat.
- Onboarding: contrat signe, badge, compte, materiel, formation, inscription paie.
- Utiliser une checklist et un workflow d'integration.
- Objets: Contrat, Affectation, Onboarding, OnboardingTache, Document.

## bloc 5 : dossier administratif du salarie

- Centraliser identite, coordonnees, documents et situation professionelle.
- Conserver contrats, affectations, salaire et historique de carriere.
- Gerer les documents administratifs et leur validite.
- Prevoir confidentialite, habilitations et journal d'audit.
- Le dossier salarie devient le referentiel central du SIRH.

## bloc 6 : Temps de travail et pointage

- Definir horaires, planning et regime de travail.
- Enregistrer entrees, sorties, retards et heures supplementaires.
- Calculer le temps reelement travaille.
- Detecter les anomalies de pointage.
- Les donnees de temps alimentent les absences et les variales de paie.

## bloc 7 : conges et absences

- Demande de conge -> controle du solde -> validatioin manager/RH.
- Gerer droits, soldes, types de conges, absences et justificatifs.
- Controller chevauchement et disponibilite du solde.
- Mettre a jour planning et variable de paie.
- objets : TypeConge, Droitconge, SoldeConge, DemandeConge, Absence.