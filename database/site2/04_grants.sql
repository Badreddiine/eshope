-- =====================================================================
--  SITE2  —  Droits d'acces pour le DB Link de ESHOP
-- =====================================================================
--  Le DB Link LINK_SITE2 se connecte via SYSTEM. Ces GRANT autorisent
--  l'acces aux fragments et aux procedures de Site2 depuis le global.
-- =====================================================================
GRANT SELECT ON SYSTEM.COMMANDES2 TO PUBLIC;
GRANT SELECT ON SYSTEM.LIGNECOMMANDES2 TO PUBLIC;
GRANT SELECT ON SYSTEM.CLIENTS2 TO PUBLIC;
GRANT SELECT ON SYSTEM.PRODUITS2 TO PUBLIC;
GRANT EXECUTE ON SYSTEM.INSERTLIGNE TO PUBLIC;
GRANT EXECUTE ON SYSTEM.DELETELIGNE TO PUBLIC;
GRANT EXECUTE ON SYSTEM.UPDATELIGNE TO PUBLIC;
