-- =====================================================================
--  SITE1  —  Droits d'acces pour le DB Link de ESHOP
-- =====================================================================
--  Le DB Link LINK_SITE1 se connecte via SYSTEM. Ces GRANT autorisent
--  l'acces aux fragments et aux procedures de Site1 depuis le global.
-- =====================================================================
GRANT SELECT ON SYSTEM.COMMANDES1 TO PUBLIC;
GRANT SELECT ON SYSTEM.LIGNECOMMANDES1 TO PUBLIC;
GRANT SELECT ON SYSTEM.CLIENTS1 TO PUBLIC;
GRANT SELECT ON SYSTEM.PRODUITS1 TO PUBLIC;
GRANT EXECUTE ON SYSTEM.INSERTLIGNE TO PUBLIC;
GRANT EXECUTE ON SYSTEM.DELETELIGNE TO PUBLIC;
GRANT EXECUTE ON SYSTEM.UPDATELIGNE TO PUBLIC;
