#!/usr/bin/env bash
# =====================================================================
#  EShop — Initialisation sequentielle de la base distribuee
# =====================================================================
#  Ordre impose par les dependances :
#    1. Demarrer les 3 conteneurs
#    2. Attendre que chaque base soit "healthy"
#    3. Charger les FRAGMENTS d'abord (site1 puis site2) : schemas,
#       procedures, donnees. Le site global en depend.
#    4. Charger le SITE GLOBAL en dernier : db links -> synonymes ->
#       triggers -> index. (Les db links exigent que site1/site2 soient
#       deja debout et que leurs tables existent.)
#    5. Executer les requetes de demonstration.
# =====================================================================
set -euo pipefail

# Le docker-compose unique est a la racine du projet (dossier parent).
COMPOSE="docker compose -f ../docker-compose.yml"
GLOBAL=oracle-global
SITE1=oracle-site1
SITE2=oracle-site2

# Connexion sqlplus a l'interieur d'un conteneur, sur la PDB XEPDB1.
# $1 = nom du conteneur, $2 = chemin du script SQL (monte dans /sql)
run_sql () {
  local container="$1"; local script="$2"
  echo "  -> [$container] $script"
  docker exec -i "$container" \
    sqlplus -S eshop/eshop@//localhost:1521/XEPDB1 "@${script}"
}

# Idem mais en tant que SYSTEM (pour les grants de privileges)
run_sql_system () {
  local container="$1"; shift
  docker exec -i "$container" \
    sqlplus -S system/oracle@//localhost:1521/XEPDB1 <<SQL
$@
EXIT;
SQL
}

# Attente active de l'etat "healthy" du healthcheck Docker
wait_healthy () {
  local container="$1"
  echo "Attente de $container ..."
  until [ "$(docker inspect -f '{{.State.Health.Status}}' "$container" 2>/dev/null)" = "healthy" ]; do
    sleep 5
    echo "   ... $container pas encore pret"
  done
  echo "   $container est PRET."
}

echo "=== 1/5  Demarrage des 3 instances Oracle XE ==="
$COMPOSE up -d

echo "=== 2/5  Attente de la disponibilite des bases ==="
wait_healthy "$SITE1"
wait_healthy "$SITE2"
wait_healthy "$GLOBAL"

echo "=== 3/5  Chargement des fragments Site1 et Site2 ==="
# L'utilisateur applicatif doit pouvoir creer des objets
run_sql_system "$SITE1"  "GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO eshop;"
run_sql_system "$SITE2"  "GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO eshop;"

run_sql "$SITE1" "/sql/01_schema.sql"
run_sql "$SITE1" "/sql/02_procedures.sql"
run_sql "$SITE1" "/sql/03_seed.sql"

run_sql "$SITE2" "/sql/01_schema.sql"
run_sql "$SITE2" "/sql/02_procedures.sql"
run_sql "$SITE2" "/sql/03_seed.sql"

echo "=== 4/5  Configuration du Site Global ==="
# Le site global a besoin du privilege CREATE DATABASE LINK
run_sql_system "$GLOBAL" "GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO eshop;
GRANT CREATE DATABASE LINK TO eshop;
GRANT CREATE SYNONYM TO eshop;"

run_sql "$GLOBAL" "/sql/01_dblinks.sql"
run_sql "$GLOBAL" "/sql/02_synonyms.sql"
run_sql "$GLOBAL" "/sql/03_triggers.sql"
run_sql "$GLOBAL" "/sql/04_indexes.sql"
run_sql "$GLOBAL" "/sql/06_api_procedures.sql"   # procedures appelees par le backend
run_sql "$GLOBAL" "/sql/07_seed_master.sql"      # dimensions + commandes maitres

echo "=== 5/5  Execution des requetes de demonstration ==="
run_sql "$GLOBAL" "/sql/05_queries.sql"

echo ""
echo "============================================================"
echo " Base distribuee EShop initialisee avec succes !"
echo "   - Site Global : localhost:1521  (eshop/eshop)"
echo "   - Site1       : localhost:1522  (gros volumes >= 100)"
echo "   - Site2       : localhost:1523  (petits volumes < 100)"
echo "============================================================"
