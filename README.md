# EShop — Stack technique complète

Application e-commerce de démonstration bâtie sur une **base de données Oracle
distribuée sur 3 instances**, exposée par un **backend Spring Boot** sécurisé
(JWT), consommée par un **frontend React**, le tout **observé** (Prometheus /
Grafana) et **livré** par un pipeline **CI/CD** GitHub Actions.

> Principe directeur : les **écritures** passent exclusivement par des
> **procédures stockées PL/SQL** ; le **routage Site1/Site2** est décidé par des
> **triggers Oracle** (jamais par le code Java). Le backend lit via JPA et
> agrège les statistiques distribuées via des **database links**.

---

## 1. Vue d'ensemble

```
                          ┌─────────────────────────────┐
                          │   Frontend React (Nginx)     │  :3001
                          │  Vite · Tailwind · ReactQuery│
                          └──────────────┬──────────────┘
                                         │  /api, /auth  (proxy)
                                         ▼
                          ┌─────────────────────────────┐
                          │   Backend Spring Boot 3.2    │  :8080
                          │  REST · JWT · JPA · JdbcCall │
                          │  /actuator/prometheus        │
                          └───┬─────────┬─────────┬──────┘
              JPA + procédures│         │db links │
                              ▼         ▼         ▼
        ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
        │ oracle-global  │ │  oracle-site1  │ │  oracle-site2  │
        │  (maître)      │ │ gros volumes   │ │ petits volumes │
        │ db links,      │ │ Quantité ≥ 100 │ │ Quantité < 100 │
        │ triggers       │ │                │ │                │
        │  :1521         │ │  :1522         │ │  :1523         │
        └────────────────┘ └────────────────┘ └────────────────┘

   Observabilité :  Prometheus :9090  ──scrape──►  backend:8080/actuator/prometheus
                    Grafana    :3000  ◄─datasource─ Prometheus
                    cAdvisor   :8081  (métriques conteneurs)
```

---

## 2. Stack par couche

| Couche | Technologies | Rôle |
|--------|--------------|------|
| **Base de données** | Oracle XE 21c (`gvenzl/oracle-xe`) × 3 | Fragmentation horizontale, db links, triggers de routage, procédures PL/SQL |
| **Backend** | Java 17 · Spring Boot 3.2.5 · Spring Web · Spring Data JPA · HikariCP · `ojdbc11` | API REST, multi-datasource, appels de procédures via `SimpleJdbcCall` |
| **Sécurité** | Spring Security 6 · JWT (`jjwt` 0.12) · BCrypt | Authentification stateless, autorisation par rôle (USER/ADMIN) |
| **Observabilité** | Micrometer · Prometheus · Grafana · cAdvisor · Logback + `logstash-logback-encoder` | Métriques, dashboard, logs JSON structurés (MDC) |
| **Frontend** | React 18 · Vite · Tailwind CSS · React Query · Axios · Recharts · React Router · react-hot-toast | SPA : dashboard, gestion CRUD, stats, santé des sites |
| **Conteneurisation** | Docker · Docker Compose | Stack reproductible sur réseau `eshop-network` |
| **CI/CD** | GitHub Actions · Maven | Build, tests, qualité (Checkstyle, `dependency:analyze`), build image, déploiement local |

---

## 3. Base de données distribuée (`database/`)

- **3 instances Oracle XE** isolées sur le réseau `eshop-network`.
- **Fragmentation** : une ligne de commande va sur **Site1 si quantité ≥ 100**,
  sinon **Site2** — décidé par les **triggers** du site global.
- **Site global** : table maître, **database links** (`LINK_SITE1`,
  `LINK_SITE2`), synonymes, triggers de routage, procédures API
  (`insertligne` / `updateligne` / `deleteligne`).
- **Initialisation** orchestrée par `database/init.sh` (charge les fragments
  puis le site global, car les db links exigent que Site1/Site2 soient debout).

## 4. Backend Spring Boot (`backend/`)

- **Multi-datasource** (`global` `@Primary`, `site1`, `site2`) — voir
  `DataSourceConfig`.
- **Écritures** → procédures stockées (`LigneCommandeService` via
  `SimpleJdbcCall`) ; **lectures** → JPA ; **stats distribuées** → requêtes
  natives `UNION ALL` sur les db links.
- **Endpoints** :

| Méthode | URL | Accès |
|--------|-----|-------|
| `POST` | `/auth/login`, `/auth/register` | public |
| `GET` | `/api/lignes` (paginé) | USER + ADMIN |
| `POST/PUT/DELETE` | `/api/lignes`, `/api/lignes/{id}` | **ADMIN** |
| `GET` | `/api/stats/commandes-par-client`, `/api/stats/ca-par-categorie` | USER + ADMIN |
| `GET` | `/api/health/sites` | authentifié |
| `GET` | `/actuator/health`, `/actuator/prometheus` | public |

### Sécurité JWT
- Login → JWT HS256 (24 h) stocké côté client, envoyé en `Authorization: Bearer`.
- Comptes de démo créés au démarrage (`DataInitializer`) :

| Utilisateur | Mot de passe | Rôle |
|-------------|--------------|------|
| `admin` | `admin123` | ADMIN |
| `user1` | `user123` | USER |

## 5. Observabilité (services dans `docker-compose.yml`, configs dans `observability/`)

- **Prometheus** scrute `backend:8080/actuator/prometheus` (+ cAdvisor).
- **Grafana** provisionné automatiquement (datasource + dashboard `eshop.json`,
  6 panels) : JVM heap, connexions HikariCP par datasource, rate des procédures,
  latence p95, HTTP request rate, erreurs 5xx.
- **Métriques custom** (`MetricsConfig`) : `eshop_procedure_calls_total`,
  `eshop_procedure_duration_seconds`, `eshop_db_connections_active`.
- **Logs JSON** (`logback-spring.xml`) enrichis par MDC : `traceId`,
  `procedureName`, `siteName`, `durationMs`.

## 6. Frontend React (`frontend/`)

- Pages : `/` (dashboard : santé + stats + table), `/gestion` (CRUD split),
  `/login`.
- React Query (cache + invalidation), toasts sur mutations, BarChart Recharts
  pour le CA, badge Site1/Site2 déduit de la quantité, health auto-refresh 30 s.
- Servi en production via **Nginx** (proxy `/api` et `/auth` → `eshop-backend`).

## 7. CI/CD (`.github/workflows/`)

- **`ci.yml`** (push / PR `main`) : `build-and-test` (`mvn clean verify` +
  artefacts Surefire/jar) → `code-quality` (Checkstyle + `dependency:analyze`)
  → `docker-build` (build image backend).
- **`cd.yml`** (push `main`) : build jar → `docker compose up -d --build` →
  attente du healthcheck → logs des conteneurs si échec.

---

## 8. Cartographie des ports

| Service | Port hôte | Accès |
|---------|-----------|-------|
| Frontend (Nginx / Vite dev) | **3001** | http://localhost:3001 |
| Grafana | **3000** | http://localhost:3000 (admin/admin) |
| Backend Spring Boot | **8080** | http://localhost:8080 |
| Prometheus | **9090** | http://localhost:9090 |
| cAdvisor | **8081** | http://localhost:8081 |
| Oracle global / site1 / site2 | **1521 / 1522 / 1523** | JDBC |

---

## 9. Démarrage rapide

```bash
# 1) Toute la stack : 3 Oracle + backend + Prometheus/Grafana/cAdvisor
#    (le Dockerfile backend compile le jar lui-même — build multi-stage)
docker compose up -d --build
(cd database && ./init.sh)                      # initialise le SQL distribué

# 2) Frontend (en dev)
cd frontend && npm install && npm run dev       # http://localhost:3001
```

Connexion : `admin / admin123`.

---

## 10. Arborescence du projet

```
bdd/
├── docker-compose.yml              # 3 Oracle + backend (réseau eshop-network)
├── README.md                       # ce document
├── database/                       # SQL distribué + init.sh
│   ├── global/  site1/  site2/
│   └── init.sh
├── backend/                        # Spring Boot
│   ├── Dockerfile  pom.xml
│   └── src/main/java/ma/eshop/{auth,config,controller,service,repository,model,dto,observability}
├── frontend/                       # React + Vite + Nginx
│   ├── Dockerfile  nginx.conf  package.json  vite.config.js
│   └── src/{api,components,pages}
├── observability/                  # configs montées dans Prometheus/Grafana
│   ├── prometheus/prometheus.yml
│   └── grafana/{provisioning,dashboards/eshop.json}
└── .github/workflows/{ci.yml,cd.yml}
```
