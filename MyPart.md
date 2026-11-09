## Referentiel des postes;

A completer :
PosteRepository
PosteMissionRepository

-A creer:
PosteReferentielService
PosteController

Le dossier poste dans templates
 -index.html
 -form.html
 -show.html

 ## Grille salariale

 A completer:
 GrilleSalarialeRepository

 A creer:
 GrilleSalarialeService
 GrilleSalarialController

 Le dossier grille:
 index.html
 form.html
 show.html

 ## BLOC 6
 A creer:
 Dans modele: -Regimetravail.java
 -HoraireTravail.java
 -Planning.java
 AffectaionPlanning.java

 Dans repository:
 -RegimeTravailRepository.java
 -PlanningRepository.java
 -AffectationPlanningRepository.java

 Dans service:
 -TempsTravailService.java

 Dans Controller:
 -TempstravailController.java()

 Dossier : temps-travail:
 -regimes.html
 -regime-form.html
 -plannings.html
 -plamming-form.html
 planning-detail.html

 A ajouter :
 Dans EmployeService.java:getAllEmployesList()
 Dans SecurityConfig.java:.requestMatchers("/temps-travail/**").hasAnyRole("RH", "DG")

 Dans le sidebar.html:
-le sous-menu Configuration est désormais visible pour RH et DG ;
-ajout du lien Temps de travail ;
-ajout du lien Plannings ;
-ajout des icônes correspondantes.

Creation de base sql:
 20261109-001-temps-travail.sql.
 