# EShop — Démarrage & tests

Guide pas-à-pas pour lancer toute la stack puis la vérifier. À exécuter depuis
la **racine du projet** (`bdd/`).

> **Windows** : Docker et `curl.exe` fonctionnent dans PowerShell. Le script
> `database/init.sh` est en **bash** → lance-le via **Git Bash** ou **WSL**.

---

## 0. Prérequis

- Docker Desktop (avec Docker Compose v2)
- Java 17 + Maven 3.9+ (uniquement pour `mvn test` / dev local — le build
  Docker compile le backend tout seul)
- Node 20+ (pour le frontend en dev)
- `curl` (fourni avec Windows 11)

Vérifier :

```bash
docker --version
docker compose version
java -version
mvn -version
node --version
```

---

## 1. Démarrer la base + le backend

```bash
# a) Lancer toute la stack : 3 Oracle + backend + Prometheus/Grafana/cAdvisor
#    Le Dockerfile backend est multi-stage : il compile le jar lui-même,
#    aucun "mvn package" préalable n'est nécessaire.
docker compose up -d --build

# b) Vérifier que les conteneurs montent (oracle = "healthy" après ~1-3 min)
docker compose ps
```

Attendre que `oracle-global`, `oracle-site1`, `oracle-site2` soient `healthy`.

```bash
# c) Initialiser le SQL distribué (schémas, db links, triggers, procédures, seed)
#    -> Git Bash / WSL sous Windows
cd database
./init.sh
cd ..
```

✅ À ce stade : backend sur **http://localhost:8080**, base distribuée prête.
La table `USERS` et les comptes de démo sont créés automatiquement par le
backend au démarrage.

---

## 2. Observabilité (déjà incluse)

Prometheus, Grafana et cAdvisor font partie du **même** `docker-compose.yml` :
ils sont donc **déjà démarrés** par l'étape 1 (`docker compose up -d`).

- Prometheus : **http://localhost:9090**
- Grafana : **http://localhost:3000** (admin / admin)
- cAdvisor : **http://localhost:8081**

---

## 3. Démarrer le frontend

```bash
cd frontend
npm install
npm run dev          # http://localhost:3001
```

Ouvre **http://localhost:3001** → page de login → `admin / admin123`.

---

## 4. Tester le backend (curl)

> Sous PowerShell, remplace `curl` par `curl.exe` pour éviter l'alias
> `Invoke-WebRequest`.

### 4.1 Santé (public)

```bash
curl http://localhost:8080/actuator/health
# -> {"status":"UP", ...}
```

### 4.2 Login → récupérer un JWT

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
# -> {"token":"eyJhbGciOiJIUzI1NiJ9...","expiresIn":86400000}
```

Copie le `token`, puis exporte-le (Git Bash / Linux) :

```bash
TOKEN="colle_le_token_ici"
```

### 4.3 Accès refusé sans token (401 attendu)

```bash
curl -i http://localhost:8080/api/lignes
# -> HTTP/1.1 401
```

### 4.4 Lister les lignes (USER ou ADMIN)

```bash
curl http://localhost:8080/api/lignes -H "Authorization: Bearer $TOKEN"
# -> page JSON { content:[...], totalElements, totalPages, ... }
```

### 4.5 Créer une ligne (ADMIN) — déclenche la procédure + le routage

```bash
# quantité 150 -> routée vers Site1 par les triggers Oracle
curl -X POST http://localhost:8080/api/lignes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"idcommande\":1000,\"idproduit\":10,\"quantite\":150,\"remise\":0.05}"
# -> 201 {"idligneCommande":<id>,"message":"Ligne creee et routee par Oracle."}
```

### 4.6 Modifier / supprimer (ADMIN)

```bash
curl -X PUT http://localhost:8080/api/lignes/<id> \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"idproduit\":10,\"quantite\":80,\"remise\":0}"        # passe < 100 -> migre Site2

curl -X DELETE http://localhost:8080/api/lignes/<id> \
  -H "Authorization: Bearer $TOKEN" -i                          # -> 204
```

### 4.7 Vérifier le contrôle de rôle (USER interdit en écriture)

```bash
# Login en user1 puis POST -> doit renvoyer 403
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d "{\"username\":\"user1\",\"password\":\"user123\"}"
# récupère son token, puis :
curl -i -X POST http://localhost:8080/api/lignes -H "Authorization: Bearer <token_user1>" \
  -H "Content-Type: application/json" \
  -d "{\"idcommande\":1000,\"idproduit\":10,\"quantite\":150,\"remise\":0}"
# -> HTTP/1.1 403 Forbidden
```

### 4.8 Statistiques distribuées (UNION ALL via db links)

```bash
curl "http://localhost:8080/api/stats/commandes-par-client?annee=2026" -H "Authorization: Bearer $TOKEN"
curl "http://localhost:8080/api/stats/ca-par-categorie?annee=2026"      -H "Authorization: Bearer $TOKEN"
```

### 4.9 Santé des 3 sites

```bash
curl http://localhost:8080/api/health/sites -H "Authorization: Bearer $TOKEN"
# -> {"global":"UP","site1":"UP","site2":"UP","global_status":"UP"}
```

---

## 5. Tester l'observabilité

### 5.1 Métriques exposées par le backend

```bash
curl http://localhost:8080/actuator/prometheus | grep eshop_procedure
# -> eshop_procedure_calls_total{procedure="insertligne",site="site1",...}
#    eshop_procedure_duration_seconds_bucket{...}
#    eshop_db_connections_active{datasource="global"} ...
```

### 5.2 Prometheus

- Ouvre **http://localhost:9090/targets** → la cible `eshop-backend`
  (`backend:8080`) doit être **UP**.
- Teste une requête dans **http://localhost:9090/graph** :
  `rate(eshop_procedure_calls_total[5m])`

### 5.3 Grafana

- **http://localhost:3000** (admin / admin)
- Dashboard **« EShop — Observabilité »** (dossier *EShop*) : 6 panels
  (JVM heap, connexions HikariCP, rate procédures, latence p95, HTTP rate,
  erreurs 5xx). Génère du trafic via les curl du §4 pour voir les courbes bouger.

---

## 6. Tester le frontend

1. http://localhost:3001 → login `admin / admin123`
2. **Tableau de bord** : badges de santé des 3 sites, stats (table + BarChart
   du CA), table paginée des lignes.
3. **Gestion** : créer une ligne (quantité ≥ 100 → badge **Site1** vert ;
   < 100 → **Site2** bleu), éditer, supprimer → un **toast** confirme chaque
   appel de procédure ; la table se rafraîchit automatiquement (React Query).

---

## 7. Tests automatisés du backend

```bash
cd backend
mvn test          # tests unitaires (Mockito) du LigneCommandeService
```

---

## 8. Arrêt / nettoyage

```bash
# Arrêter tout (Oracle + backend + observabilité)
docker compose down

# Tout supprimer, y compris les volumes (réinitialisation complète)
docker compose down -v
```

---

## 9. Dépannage rapide

| Symptôme | Cause probable | Solution |
|----------|----------------|----------|
| Backend redémarre en boucle | Oracle pas encore `healthy` | Attendre, vérifier `docker compose ps` puis `docker compose logs eshop-backend` |
| `401` sur `/api/**` | Token manquant/expiré | Refaire un `/auth/login` |
| `403` sur POST/PUT/DELETE | Connecté en `user1` (USER) | Se connecter en `admin` (ADMIN) |
| Tables `LIGNECOMMANDES` introuvables | `init.sh` non exécuté | Lancer `database/init.sh` (Git Bash/WSL) |
| Cible Prometheus `backend` DOWN | Backend non démarré / alias réseau | Vérifier `docker compose ps` et le réseau `eshop-network` |
| Port 3000 occupé | Conflit Grafana/Frontend | Frontend déjà déplacé sur **3001** |
| `init.sh` ne s'exécute pas (Windows) | Script bash | Utiliser **Git Bash** ou **WSL** |
