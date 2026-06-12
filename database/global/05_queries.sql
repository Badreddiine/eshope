-- =====================================================================
--  SITE_GLOBAL  —  Requetes distribuees de demonstration
-- =====================================================================
--  Toutes ces requetes s'appuient sur les SYNONYMES distants definis
--  dans 02_synonyms.sql. L'optimiseur distribue d'Oracle decompose
--  automatiquement le travail : il pousse les filtres/agregats vers
--  chaque site distant ("predicate pushing") et ne rapatrie que les
--  resultats partiels — ce qui minimise le trafic reseau.
-- =====================================================================

SET LINESIZE 200
SET PAGESIZE 100

-- ---------------------------------------------------------------------
--  REQUETE 1 : Nombre de commandes par client en 2026
--  Les commandes 2026 sont reparties sur les deux fragments. On
--  reconstitue la relation globale par UNION ALL des deux sites, puis
--  on compte les commandes DISTINCTES par client.
--  (UNION ALL et non UNION : les ids de commandes sont disjoints entre
--   sites, donc pas de doublon possible -> on evite un tri couteux.)
-- ---------------------------------------------------------------------
PROMPT === Requete 1 : commandes par client en 2026 ===
SELECT idclient, COUNT(*) AS nb_commandes
FROM (
    SELECT idclient, idcommande
    FROM   COMMANDES1
    WHERE  EXTRACT(YEAR FROM dateCommande) = 2026
    UNION ALL
    SELECT idclient, idcommande
    FROM   COMMANDES2
    WHERE  EXTRACT(YEAR FROM dateCommande) = 2026
)
GROUP BY idclient
ORDER BY idclient;

-- ---------------------------------------------------------------------
--  REQUETE 2 : Plan d'execution de la requete 1 (EXPLAIN PLAN)
--  EXPLAIN PLAN remplit la table PLAN_TABLE sans executer la requete.
--  DBMS_XPLAN.DISPLAY formate ce plan : on y verra les operations
--  "REMOTE" prouvant qu'Oracle delegue le filtrage a chaque site.
-- ---------------------------------------------------------------------
PROMPT === Requete 2 : EXPLAIN PLAN de la requete 1 ===
EXPLAIN PLAN FOR
SELECT idclient, COUNT(*) AS nb_commandes
FROM (
    SELECT idclient, idcommande FROM COMMANDES1
    WHERE  EXTRACT(YEAR FROM dateCommande) = 2026
    UNION ALL
    SELECT idclient, idcommande FROM COMMANDES2
    WHERE  EXTRACT(YEAR FROM dateCommande) = 2026
)
GROUP BY idclient;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- ---------------------------------------------------------------------
--  REQUETE 3 : Chiffre d'affaires par categorie en 2026 (distribuee)
--  Le CA d'une ligne = Quantite * PrixUnitaire * (1 - remise).
--  Le CA total par categorie est la SOMME des contributions des deux
--  fragments. Chaque sous-requete joint, EN LOCAL sur son site, ses
--  LigneCommandes avec Produits (pour idcateg) et Commandes (pour
--  l'annee). On additionne ensuite les CA partiels par categorie.
--
--  -> C'est l'illustration directe du theoreme : une agregation
--     distributive (SUM) sur une union de fragments disjoints =
--     somme des agregations locales. Le reseau ne transporte que
--     quelques lignes (CA par categorie), pas les lignes de detail.
-- ---------------------------------------------------------------------
PROMPT === Requete 3 : CA par categorie 2026 (Site1 + Site2) ===
SELECT idcateg, SUM(ca_partiel) AS ca_total_2026
FROM (
    -- Contribution Site1 (gros volumes)
    SELECT p.idcateg,
           SUM(l.Quantite * l.PrixUnitaire * (1 - NVL(l.remise,0))) AS ca_partiel
    FROM   LIGNECOMMANDES1 l
    JOIN   PRODUITS1  p ON p.idproduit  = l.idproduit
    JOIN   COMMANDES1 c ON c.idcommande = l.idcommande
    WHERE  EXTRACT(YEAR FROM c.dateCommande) = 2026
    GROUP  BY p.idcateg
    UNION ALL
    -- Contribution Site2 (petits volumes)
    SELECT p.idcateg,
           SUM(l.Quantite * l.PrixUnitaire * (1 - NVL(l.remise,0))) AS ca_partiel
    FROM   LIGNECOMMANDES2 l
    JOIN   PRODUITS2  p ON p.idproduit  = l.idproduit
    JOIN   COMMANDES2 c ON c.idcommande = l.idcommande
    WHERE  EXTRACT(YEAR FROM c.dateCommande) = 2026
    GROUP  BY p.idcateg
)
GROUP BY idcateg
ORDER BY idcateg;
