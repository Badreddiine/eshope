-- =====================================================================
--  SITE_GLOBAL  —  Database Links vers Site1 et Site2
-- =====================================================================
--  Un DATABASE LINK est un objet de schema qui ouvre une session SQL*Net
--  vers une base distante. C'est le mecanisme natif d'Oracle pour la
--  TRANSPARENCE DE LOCALISATION : une fois le lien cree, on adresse une
--  table distante avec la syntaxe  table@nom_du_lien.
--
--  Connexion : l'utilisateur applicatif est "eshop" sur chaque site.
--  Le service PDB par defaut de l'image Oracle XE 21c est XEPDB1.
--  Les hotes "oracle-site1" / "oracle-site2" sont les noms de services
--  Docker resolus par le DNS interne du reseau bridge "eshop-network".
--  Le listener interne de chaque conteneur ecoute sur 1521.
-- =====================================================================

-- Suppression idempotente (re-jouable)
BEGIN
  FOR l IN (SELECT db_link FROM user_db_links
            WHERE db_link IN ('LINK_SITE1','LINK_SITE2')) LOOP
    EXECUTE IMMEDIATE 'DROP DATABASE LINK '||l.db_link;
  END LOOP;
END;
/

-- Lien vers Site1 (gros volumes, port interne 1521 / externe 1522)
CREATE DATABASE LINK LINK_SITE1
  CONNECT TO eshop IDENTIFIED BY eshop
  USING '//oracle-site1:1521/XEPDB1';

-- Lien vers Site2 (petits volumes, port interne 1521 / externe 1523)
CREATE DATABASE LINK LINK_SITE2
  CONNECT TO eshop IDENTIFIED BY eshop
  USING '//oracle-site2:1521/XEPDB1';

-- Test de connectivite (doit renvoyer 'X' depuis chaque site distant)
SELECT 'site1 OK' AS test FROM dual@LINK_SITE1;
SELECT 'site2 OK' AS test FROM dual@LINK_SITE2;
