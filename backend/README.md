# EShop — Backend Spring Boot (base distribuee Oracle 3 sites)

Backend REST minimaliste connecté aux **3 instances Oracle XE**, protégé par
**authentification JWT** (Spring Security 6). Toute la stack (3 bases +
backend) est décrite dans le **`docker-compose.yml` unique** à la racine du
projet.

## Principes d'architecture

| Operation | Mecanisme | Datasource |
|-----------|-----------|------------|
| Ecritures (POST/PUT/DELETE lignes) | Procedures stockees PL/SQL via `SimpleJdbcCall` | global |
| Routage Site1/Site2 | Triggers Oracle `SYC_*` (pas de logique Java) | Oracle |
| Lectures simples | JPA / repository | global |
| Stats distribuees | Requetes natives `UNION ALL` via database links | global |

> Les procedures `insertligne/deleteligne/updateligne` appelees par le backend
> sont celles du **site global** (`database/global/06_api_procedures.sql`) :
> elles agissent sur la table maitre, ce qui declenche les triggers de routage.

## Prerequis

- Docker + Docker Compose (recommandé), ou Java 17 + Maven 3.9+ pour un run local.

## Lancer (tout en Docker — recommandé)

Depuis la **racine du projet** (un seul `docker-compose.yml`) :

```bash
# 1) Démarrer oracle-global/site1/site2 + eshop-backend (+ observabilité)
#    Le Dockerfile backend est multi-stage : il compile le jar lui-même.
docker compose up -d --build

# 2) Charger le SQL distribué (fragments puis site global)
(cd database && ./init.sh)
```

> Les pipelines CI/CD (`.github/workflows/ci.yml` et `cd.yml`) automatisent
> build, tests, qualité et démarrage de la stack.

> `init.sh` ne fait que charger le SQL distribué (schémas, db links, triggers,
> seed). La table `USERS` et les comptes de démo sont créés automatiquement par
> le backend au démarrage (`DataInitializer`).

## Lancer (backend en local, hors Docker)

```bash
mvn spring-boot:run
# ou
mvn clean package && java -jar target/eshop-backend-1.0.0.jar
```

L'application ecoute sur `http://localhost:8080`.

## Authentification

Comptes de démonstration créés au démarrage :

| Utilisateur | Mot de passe | Rôle  |
|-------------|--------------|-------|
| `admin`     | `admin123`   | ADMIN |
| `user1`     | `user123`    | USER  |

Règles d'accès :

- `POST` / `PUT` / `DELETE /api/lignes` → **ADMIN** uniquement
- `GET /api/lignes`, `GET /api/stats/**` → **USER** et **ADMIN**
- `POST /auth/**`, `GET /actuator/**` → publics

```bash
# 1) Login -> récupère un JWT
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

# 2) Appel authentifié
curl http://localhost:8080/api/lignes -H "Authorization: Bearer $TOKEN"
```

## Endpoints

| Methode | URL | Description |
|---------|-----|-------------|
| POST | `/auth/login` | Connexion — body `{username, password}` → `{token, expiresIn}` |
| POST | `/auth/register` | Inscription — body `{username, password, role}` |
| POST | `/api/lignes` | Cree une ligne — body `{idcommande, idproduit, quantite, remise}` |
| DELETE | `/api/lignes/{id}` | Supprime une ligne |
| PUT | `/api/lignes/{id}` | Modifie — body `{idproduit, quantite, remise}` |
| GET | `/api/lignes` | Liste paginee (site global) |
| GET | `/api/stats/commandes-par-client?annee=2026` | Commandes/client (distribue) |
| GET | `/api/stats/ca-par-categorie?annee=2026` | CA/categorie (distribue) |
| GET | `/api/health/sites` | Ping individuel des 3 sites |
| GET | `/actuator/health` | Sante globale |
| GET | `/actuator/metrics` | Metriques (dont `eshop.procedure.*`) |
| GET | `/actuator/prometheus` | Export Prometheus |

## Exemple

```bash
# Cree une ligne gros volume (Quantite=150 -> routee vers Site1 par Oracle)
curl -X POST http://localhost:8080/api/lignes \
  -H "Content-Type: application/json" \
  -d '{"idcommande":1000,"idproduit":10,"quantite":150,"remise":0.05}'
```
