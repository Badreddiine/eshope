-- =====================================================================
--  SITE_GLOBAL  —  Index d'optimisation
-- =====================================================================
--  La requete phare "nombre de commandes par client en 2026" filtre sur
--  dateCommande (annee 2026) et regroupe par idclient. Sans index, Oracle
--  realiserait un FULL TABLE SCAN. On cree donc :
--
--   1) un index sur idclient            -> accelere le GROUP BY / jointure
--   2) un index sur dateCommande        -> accelere le filtre annuel
--   3) un index COMPOSITE (dateCommande, idclient) -> "covering index"
--      qui permet de repondre a la requete sans toucher la table (les
--      deux colonnes utiles sont dans l'index).
--
--  Ces index sont locaux a chaque base : on les cree donc aussi cote
--  fragments (Site1/Site2) ou l'agregat est reellement execute. Ici on
--  les pose sur la table maitre du site global pour les requetes locales.
-- =====================================================================

-- Index sur la cle client (jointures et regroupements)
CREATE INDEX idx_cmd_client ON Commandes(idclient);

-- Index sur la date (filtre par annee)
CREATE INDEX idx_cmd_date ON Commandes(dateCommande);

-- Index composite "couvrant" pour la requete 2026 par client
CREATE INDEX idx_cmd_date_client ON Commandes(dateCommande, idclient);

-- Mise a jour des statistiques pour que l'optimiseur (CBO) choisisse
-- effectivement ces index dans son plan d'execution.
BEGIN
  DBMS_STATS.GATHER_TABLE_STATS(USER, 'COMMANDES');
  DBMS_STATS.GATHER_TABLE_STATS(USER, 'LIGNECOMMANDES');
END;
/
