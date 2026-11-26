# LANCEMENT DE PROJET SUR DOCKER:

## Relance le projet:
    docker compose up -d
## Et puis: 
    docker compose logs -f app
    http://localhost:8080/login
## Après modification de code: 
    docker compose up -d --build app
## Arrêter le projet:
    docker compose stop
## Accéder à la base: 
    docker compose exec  db psql -U admin -d sirh
## Voire ce qui tourne: 
    docker compose ps

## Les utilisateurs: 
## Comptes de test

| E-mail | Mot de passe | Rôle |
|---|---|---|
| `admin@sirh.com` | `admin123` | ADMIN |
| `rh@sirh.com` | `password123` | RH |
| `alice@sirh.com` | `123` | RH |
| `bob@sirh.com` | `123` | MANAGER |
| `claire@sirh.com` | `123` | DG |
| `jack@sirh.com` | `123` | FINANCE |




