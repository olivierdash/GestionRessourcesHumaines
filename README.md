# SIRH — Système d'Information RH

Application de gestion du recrutement composée de deux interfaces :

- **SIRH interne** (Spring Boot + Thymeleaf) — gestion des demandes, offres, candidatures, contrats, onboarding
- **Site carrière public** (Vue.js) — consultation des offres et dépôt de candidature par les candidats

---

## Architecture

Monorepo avec deux applications indépendantes dans des dossiers séparés :

```
.
├── sirh/        # API REST + interface SIRH interne (Spring Boot)
│   ├── src/main/java/com/example/sirh/
│   │   ├── config/              # Sécurité, configuration web
│   │   ├── controller/          # Controllers Thymeleaf (SIRH interne)
│   │   │   └── api/v1/          # Controllers REST (site public)
│   │   ├── dto/                 # Objets de transfert (public + interne)
│   │   ├── model/               # Entités JPA
│   │   ├── repository/          # Spring Data JPA
│   │   └── service/             # Logique métier
│   └── src/main/resources/
│       ├── templates/           # Vues Thymeleaf (SIRH interne)
│       ├── static/              # CSS, JS, images
│       └── application.properties
│
└── site-public/        # Site carrière public (Vue.js)
    ├── src/
    │   ├── components/          # Composants réutilisables
    │   ├── layouts/             # PublicLayout / AuthLayout
    │   ├── modules/             # Vues par domaine (home, jobs, auth, candidate-space)
    │   ├── router/              # Vue Router
    │   ├── services/            # Appels API (fetch)
    │   ├── stores/              # Pinia (candidate, jobs, applications)
    │   └── utils/
    ├── package.json
    └── vite.config.js
```

### Communication

```
Vue.js (port 5173)
      │
      │  fetch /api/public/**
      ▼
Proxy Vite ──► Spring Boot (port 8080)
                       │
                       ▼
                  Base de données
```

Toutes les routes publiques sont préfixées par `/api/public/**` et autorisées sans authentification côté Spring Security. L'identité du candidat est transmise par le client (`idCandidat`) — l'API est stateless pour les candidats.

---

## Prérequis

| Outil | Version |
|---|---|
| JDK | 21 |
| Maven | 3.9+ (ou wrapper `./mvnw`) |
| Node.js | 20+ |
| npm | 10+ |
| Base de données | PostgreSQL  |

---

## Démarrage

### 1. Base de données

Créer la base et appliquer le schéma (migrations manuelles ou script SQL fourni) :

```sql
CREATE DATABASE sirh;
-- puis exécuter le script d'initialisation (schéma + données de référence)
```

Configurer la connexion dans `sirh-backend/src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sirh
spring.datasource.username=<user>
spring.datasource.password=<password>
```

Ce fichier est **ignoré par git** (contient des identifiants locaux).

### 2. Backend (Spring Boot)

```bash
cd sirh
./mvnw spring-boot:run
```

Démarre sur **http://localhost:8080**

- Interface SIRH interne : http://localhost:8080/login
- API publique : http://localhost:8080/api/public/

### 3. Frontend (Vue.js)

```bash
cd site-public
npm install
npm run dev
```

Démarre sur **http://localhost:5173**

Le proxy Vite redirige automatiquement `/api/**` vers `http://localhost:8080`.

---

## Parcours utilisateur — Site carrière

| Étape | URL | Accès |
|---|---|---|
| Accueil | `/` | public |
| Liste des offres | `/offres` | public |
| Détail d'une offre | `/offres/:id` | public |
| Postuler | `/offres/:id/postuler` | 🔒 compte candidat requis |
| Créer un compte | `/auth/register` | public |
| Connexion | `/auth/login` | public |
| Mes candidatures | `/mes-candidatures` | 🔒 compte candidat requis |
| Détail d'une candidature | `/mes-candidatures/:id` | 🔒 compte candidat requis |

Les pages protégées sont enveloppées dans un composant `<RequireAuth>` qui affiche un message « Connexion requise » avec boutons de connexion/inscription si le candidat n'est pas authentifié.

### Authentification candidat

- **Inscription** : email + mot de passe (hashé BCrypt) + nom + prénom
- **Session côté client** : `sessionStorage` — conservée au refresh, perdue à la fermeture de l'onglet
- **Pas de session HTTP serveur** — l'API est stateless, l'identité voyage avec chaque requête

---

## Endpoints API publique

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/public/jobs` | Liste des offres publiées |
| `GET` | `/api/public/jobs/{id}` | Détail d'une offre |
| `POST` | `/api/public/jobs/{id}/apply` | Postuler (multipart : `idCandidat` + `cv`) |
| `POST` | `/api/public/auth/register` | Créer un compte candidat |
| `POST` | `/api/public/auth/login` | Se connecter |
| `GET` | `/api/public/applications?idCandidat=` | Mes candidatures |
| `GET` | `/api/public/applications/{id}?idCandidat=` | Détail d'une candidature |

Toutes les erreurs sont renvoyées au format JSON :

```json
{ "message": "Description de l'erreur" }
```

---

## Scripts utiles

### Backend

```bash
./mvnw clean compile      # Compiler
./mvnw spring-boot:run    # Démarrer
./mvnw test               # Tests
```

### Frontend

```bash
npm run dev       # Démarrer en mode développement
npm run build     # Build de production (dossier dist/)
npm run preview   # Prévisualiser le build
npm run lint      # Linter
npm run format    # Formatter avec Prettier
```

---

## Configuration

### `.env.development` (frontend)

```env
VITE_API_BASE_URL=/api
```

Le préfixe relatif active le proxy Vite. Ne **pas** mettre `http://localhost:8080/api` — cela contournerait le proxy et provoquerait des erreurs CORS.

### `vite.config.js` — proxy

```js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
},
```


---

## Licence

Projet de cours — usage pédagogique.