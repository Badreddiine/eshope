-- =====================================================================
--  SITE_GLOBAL  —  Correctif definitif des acces au schema ESHOP
-- =====================================================================
--  Ce script resout les problemes d'acces du schema ESHOP :
--    - droits manquants (CREATE DATABASE LINK / CREATE VIEW / SELECT)
--    - synonymes PUBLIC qui masquent les vues locales de ESHOP
--    - recreation des DB Links et des vues distantes cote ESHOP
--
--  Connexion requise : SYSTEM sur l'instance oracle-global.
-- =====================================================================

-- =====================================================================
--  ETAPE 1 : Donner les droits necessaires a ESHOP
-- =====================================================================
GRANT CREATE DATABASE LINK TO eshop;
GRANT CREATE VIEW TO eshop;
GRANT SELECT ON SYSTEM.COMMANDES TO eshop;
GRANT SELECT ON SYSTEM.LIGNECOMMANDES TO eshop;
GRANT SELECT ON SYSTEM.CLIENTS TO eshop;
GRANT SELECT ON SYSTEM.PRODUITS TO eshop;
GRANT EXECUTE ON SYSTEM.INSERTLIGNE TO eshop;
GRANT EXECUTE ON SYSTEM.DELETELIGNE TO eshop;
GRANT EXECUTE ON SYSTEM.UPDATELIGNE TO eshop;

-- =====================================================================
--  ETAPE 2 : Supprimer tous les synonymes publics qui bloquent
-- =====================================================================
BEGIN
  FOR s IN (SELECT synonym_name FROM all_synonyms
            WHERE owner='PUBLIC'
            AND synonym_name IN ('COMMANDES','COMMANDES1','COMMANDES2',
                                 'LIGNECOMMANDES','LIGNECOMMANDES1','LIGNECOMMANDES2',
                                 'CLIENTS','PRODUITS','PRODUITS1','PRODUITS2'))
  LOOP
    EXECUTE IMMEDIATE 'DROP PUBLIC SYNONYM ' || s.synonym_name;
  END LOOP;
END;
/

-- =====================================================================
--  ETAPE 3 : Connecte en ESHOP — creer DB Links + vues
-- =====================================================================
CONNECT eshop/eshop@XEPDB1

-- Suppression idempotente des DB Links existants (re-jouable)
BEGIN
  EXECUTE IMMEDIATE 'DROP DATABASE LINK LINK_SITE1';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
BEGIN
  EXECUTE IMMEDIATE 'DROP DATABASE LINK LINK_SITE2';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

CREATE DATABASE LINK LINK_SITE1
  CONNECT TO system IDENTIFIED BY oracle
  USING '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=oracle-site1)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=XEPDB1)))';

CREATE DATABASE LINK LINK_SITE2
  CONNECT TO system IDENTIFIED BY oracle
  USING '(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=oracle-site2)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=XEPDB1)))';

CREATE OR REPLACE VIEW COMMANDES1 AS
  SELECT * FROM COMMANDES1@LINK_SITE1;
CREATE OR REPLACE VIEW COMMANDES2 AS
  SELECT * FROM COMMANDES2@LINK_SITE2;
CREATE OR REPLACE VIEW LIGNECOMMANDES1 AS
  SELECT * FROM LIGNECOMMANDES1@LINK_SITE1;
CREATE OR REPLACE VIEW LIGNECOMMANDES2 AS
  SELECT * FROM LIGNECOMMANDES2@LINK_SITE2;
CREATE OR REPLACE VIEW PRODUITS1 AS
  SELECT * FROM PRODUITS1@LINK_SITE1;
CREATE OR REPLACE VIEW PRODUITS2 AS
  SELECT * FROM PRODUITS2@LINK_SITE2;

-- Sequence ESHOP : genere les identifiants de ligne passes en P_IDLIGNECMD
-- au wrapper INSERTLIGNE. Demarre haut pour ne pas heurter les ids seedes
-- (1-11 sur Site1, 101-111 sur Site2).
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ligne';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
CREATE SEQUENCE seq_ligne START WITH 100000 INCREMENT BY 1 NOCACHE;

-- Procedures wrapper qui appellent les procedures distantes via DB Links
CREATE OR REPLACE PROCEDURE INSERTLIGNE(
  p_idligneCmd NUMBER, p_idcommande NUMBER, p_idproduit NUMBER,
  p_quantite NUMBER, p_remise NUMBER)
AS BEGIN
  IF p_quantite >= 100 THEN
    INSERTLIGNE@LINK_SITE1(p_idligneCmd, p_idcommande, p_idproduit, p_quantite, p_remise);
  ELSE
    INSERTLIGNE@LINK_SITE2(p_idligneCmd, p_idcommande, p_idproduit, p_quantite, p_remise);
  END IF;
END;
/

CREATE OR REPLACE PROCEDURE DELETELIGNE(p_idligneCmd NUMBER)
AS
  v_quantite NUMBER;
BEGIN
  BEGIN
    SELECT Quantite INTO v_quantite FROM LIGNECOMMANDES1 WHERE IDLIGNECOMMANDE = p_idligneCmd;
    DELETELIGNE@LINK_SITE1(p_idligneCmd);
    RETURN;
  EXCEPTION WHEN NO_DATA_FOUND THEN NULL;
  END;
  DELETELIGNE@LINK_SITE2(p_idligneCmd);
END;
/

CREATE OR REPLACE PROCEDURE UPDATELIGNE(
  p_idligneCmd NUMBER, p_idproduit NUMBER,
  p_quantite NUMBER, p_remise NUMBER)
AS
  v_old_quantite NUMBER;
  v_idcommande NUMBER;
BEGIN
  BEGIN
    SELECT Quantite, idcommande INTO v_old_quantite, v_idcommande
    FROM LIGNECOMMANDES1 WHERE IDLIGNECOMMANDE = p_idligneCmd;
    IF p_quantite >= 100 THEN
      UPDATELIGNE@LINK_SITE1(p_idligneCmd, p_idproduit, p_quantite, p_remise);
    ELSE
      -- Deplace de Site1 vers Site2
      DELETELIGNE@LINK_SITE1(p_idligneCmd);
      INSERTLIGNE@LINK_SITE2(p_idligneCmd, v_idcommande, p_idproduit, p_quantite, p_remise);
    END IF;
    RETURN;
  EXCEPTION WHEN NO_DATA_FOUND THEN NULL;
  END;
  SELECT Quantite, idcommande INTO v_old_quantite, v_idcommande
  FROM LIGNECOMMANDES2 WHERE IDLIGNECOMMANDE = p_idligneCmd;
  IF p_quantite < 100 THEN
    UPDATELIGNE@LINK_SITE2(p_idligneCmd, p_idproduit, p_quantite, p_remise);
  ELSE
    -- Deplace de Site2 vers Site1
    DELETELIGNE@LINK_SITE2(p_idligneCmd);
    INSERTLIGNE@LINK_SITE1(p_idligneCmd, v_idcommande, p_idproduit, p_quantite, p_remise);
  END IF;
END;
/

-- =====================================================================
--  ETAPE 4 : Verification
-- =====================================================================
SELECT COUNT(*) AS site1_commandes FROM COMMANDES1;
SELECT COUNT(*) AS site2_commandes FROM COMMANDES2;
SELECT COUNT(*) AS site1_lignes FROM LIGNECOMMANDES1;
SELECT COUNT(*) AS site2_lignes FROM LIGNECOMMANDES2;

COMMIT;
