# Compte rendu - Blocs 11-12

## Bloc 11 - Discipline

- Contrôleur API `DisciplineController` : liste, déclaration, demande d'explication, réponse, décision et pièces.
- Contrôleur SIRH `DisciplineController` : écran de liste, formulaire d'incident et page de détail/procédure.
- Contrôleur public `PublicDisciplineController` : accès aux demandes disciplinaires du candidat et réponse en ligne.
- Ajout du parcours candidat « Mes demandes » dans le site public.
- Renforcement des règles métier : valeurs autorisées, unicité des décisions, droits par salarié/département et audit.
- Stabilisation du service de réponse/décision : date par défaut, sanction obligatoire sans notification, contenu et statut cohérents.

## Bloc 12 - Rupture et offboarding

- Contrôleur API `OffboardingController` : ruptures, démarrage, clôture, matériel, accès, documents et solde de tout compte.
- Contrôleur SIRH `OffboardingController` : liste des ruptures, initiation, checklist et page de détail.
- Page SIRH avec solde de tout compte, matériel, désactivation des accès, documents de sortie et clôture.
- Renforcement des transitions d'offboarding : démarrage uniquement depuis `INITIEE` ou `PREAVIS_EN_COURS`.
- Clôture bloquée tant que les accès, matériels, documents et STC ne sont pas validés.

## Navigation et sécurité

- Liens « Discipline » et « Ruptures / sorties » ajoutés au menu SIRH.
- Restrictions de routes ajoutées pour les rôles RH, DG et MANAGER.
- Ajout d'une entrée « Mes demandes » dans le site public pour les candidats authentifiés.

## État des fichiers

- Les pages sont reliées aux services existants et aux données JPA.
- Les contrôleurs MCP MVC utilisent les redirections avec messages `succes`/`erreur`.
