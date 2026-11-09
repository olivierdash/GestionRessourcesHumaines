# Suivi de ma partie du projet

## 1. Référentiel des postes

### Éléments à compléter

- `PosteRepository`
- `PosteMissionRepository`

### Éléments à créer

- `PosteReferentielService`
- `PosteController`

### Vues associées

Dossier : `sirh/src/main/resources/templates/poste/`

- `index.html`
- `form.html`
- `show.html`

---

## 2. Grille salariale

### Éléments à compléter

- `GrilleSalarialeRepository`

### Éléments à créer

- `GrilleSalarialeService`
- `GrilleSalarialController`

### Vues associées

Dossier : `sirh/src/main/resources/templates/grille/`

- `index.html`
- `form.html`
- `show.html`

---

## 3. Bloc 6 — Temps de travail et planning

### 3.1 Régimes, horaires et plannings

#### Modèles

- `RegimeTravail.java`
- `HoraireTravail.java`
- `Planning.java`
- `AffectationPlanning.java`

#### Repositories

- `RegimeTravailRepository.java`
- `PlanningRepository.java`
- `AffectationPlanningRepository.java`

#### Service

- `TempsTravailService.java`

#### Contrôleur

- `TempsTravailController.java`

#### Vues

Dossier : `sirh/src/main/resources/templates/temps-travail/`

- `regimes.html`
- `regime-form.html`
- `plannings.html`
- `planning-form.html`
- `planning-detail.html`

### 3.2 Intégration dans l’application

- Ajout de `getAllEmployesList()` dans `EmployeService.java`.
- Ajout de la règle de sécurité dans `SecurityConfig.java` :

```java
.requestMatchers("/temps-travail/**").hasAnyRole("RH", "DG")
```

- Modification de `sidebar.html` :
  - affichage du sous-menu Configuration pour les rôles RH et DG ;
  - ajout du lien « Temps de travail » ;
  - ajout du lien « Plannings » ;
  - ajout des icônes correspondantes.

### 3.3 Migration de base de données

Fichier créé :

- `sql/20261109-001-temps-travail.sql`

Cette migration crée les tables nécessaires aux régimes de travail, aux horaires, aux plannings et à leurs affectations.

---

## 4. Gestion des pointages

### 4.1 Fonctionnalités

Le module permet de :

- enregistrer l’heure d’entrée ;
- enregistrer l’heure de sortie ;
- calculer le temps de présence ;
- calculer le temps réellement travaillé ;
- détecter les retards ;
- calculer les heures supplémentaires ;
- consulter et filtrer l’historique des pointages ;
- modifier un pointage existant.

### 4.2 Modèle et repository

- `Pointage.java`
- `PointageRepository.java`

Un seul pointage est autorisé par salarié et par date.

### 4.3 Vues

Dossier : `sirh/src/main/resources/templates/temps-travail/`

- `pointage-form.html`
- `pointages.html`

### 4.4 Contrôleur et routes

Les routes sont gérées dans `TempsTravailController.java` :

```text
GET  /temps-travail/pointages
GET  /temps-travail/pointages/nouveau
GET  /temps-travail/pointages/{id}/edit
POST /temps-travail/pointages
```

### 4.5 Intégration dans le menu

Un lien « Pointages » a été ajouté dans `sidebar.html`.

### 4.6 Base de données

La table `pointage` a été ajoutée dans :

- `sql/20261109-001-temps-travail.sql`

Elle contient notamment :

- le salarié concerné ;
- la date du pointage ;
- les heures d’entrée et de sortie ;
- les minutes travaillées ;
- les minutes de pause ;
- les minutes de retard ;
- les minutes supplémentaires ;
- un commentaire éventuel.

---

## 5. Calcul du temps réellement travaillé

Le calcul tient compte du planning actif et de l’horaire correspondant au jour du pointage.

### Formules utilisées

```text
Temps de présence = Heure de sortie - Heure d’entrée

Temps réellement travaillé = Temps de présence - Pause prévue
```

### Exemple

```text
Heure d’entrée : 08:00
Heure de sortie : 17:00
Pause prévue   : 60 minutes
```

Résultat :

```text
Temps de présence            : 540 minutes
Pause déduite                : 60 minutes
Temps réellement travaillé   : 480 minutes
```

Les heures supplémentaires sont calculées à partir du temps réellement travaillé, après déduction de la pause prévue.

---

## 6. Vérification

La compilation du projet a été vérifiée avec :

```bash
mvn -q -DskipTests compile
```

La compilation est réussie.

Le test de contexte Spring nécessite toutefois l’exécution préalable de la migration SQL, car le projet utilise :

```properties
spring.jpa.hibernate.ddl-auto=validate
```
