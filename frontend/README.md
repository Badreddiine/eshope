# EShop — Frontend (React 18 + Vite + Tailwind)

Interface minimaliste consommant le backend Spring Boot EShop (Oracle 3 sites).

**Stack :** React 18 · Vite · Tailwind CSS · React Query · Axios · Recharts · React Router · react-hot-toast.

## Démarrer en développement

```bash
cd frontend
npm install
npm run dev          # http://localhost:3001  (Grafana occupe le 3000)
```

Le serveur Vite **proxifie** `/api` et `/auth` vers `http://localhost:8080`
(backend), donc aucune configuration CORS n'est nécessaire. Le backend doit
tourner (voir `../backend`).

## Authentification

Le backend est sécurisé par JWT : l'app redirige vers `/login` tant qu'aucun
token n'est présent. Comptes de démo (créés au démarrage du backend) :

| Utilisateur | Mot de passe | Rôle |
|-------------|--------------|------|
| `admin`     | `admin123`   | ADMIN (peut créer/éditer/supprimer) |
| `user1`     | `user123`    | USER (lecture seule) |

Le token est stocké dans `localStorage` et injecté en `Authorization: Bearer`
par l'intercepteur Axios (`src/api/axiosClient.js`). Un `401` purge le token et
renvoie vers `/login`.

## Pages

| Route | Contenu |
|-------|---------|
| `/` | Dashboard : santé des sites + stats (table + BarChart CA) + table des lignes |
| `/gestion` | Split : table des lignes (édition) + formulaire create/edit |
| `/login` | Connexion |

- **React Query** gère cache + invalidation (`['lignes']`, `['stats',…]`,
  `['health']`) après chaque mutation.
- **Toasts** sur succès/erreur d'appel de procédure stockée.
- **Colonne Site** déduite de la quantité : badge vert *Site1* (≥100), bleu
  *Site2* (<100).
- **SiteHealth** se rafraîchit toutes les 30 s (`refetchInterval`).
- **Error boundaries** autour de chaque bloc + au niveau racine.

## Build & image Docker (Nginx)

```bash
npm run build                       # génère dist/
docker build -t eshop-frontend .    # build Vite + Nginx
```

Le `Dockerfile` est multi-stage (build Node → service Nginx). `nginx.conf`
sert la SPA et relaie `/api` et `/auth` vers `http://eshop-backend:8080`.

Pour l'intégrer au `docker-compose.yml` racine (réseau `eshop-network`) :

```yaml
  eshop-frontend:
    build:
      context: ./frontend
    container_name: eshop-frontend
    ports:
      - "3001:80"          # 3000 réservé à Grafana
    depends_on:
      - eshop-backend
    networks:
      - eshop-network
```
