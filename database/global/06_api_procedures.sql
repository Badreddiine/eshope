-- =====================================================================
--  SITE_GLOBAL  —  Procedures "API" appelees par le backend Spring Boot
-- =====================================================================
--  Pourquoi des procedures cote GLOBAL alors que insertligne existe deja
--  sur Site1/Site2 ?
--    Le backend doit (a) ecrire UNIQUEMENT via procedure stockee et
--    (b) laisser Oracle router vers le bon fragment. Ces deux regles ne
--    sont conciliables QUE si la procedure appelee agit sur la TABLE
--    MAITRE du site global : le DML y declenche alors les triggers
--    SYC_INSERT/DELETE/UPDATE_LIGNE qui poussent vers Site1 ou Site2.
--    Le backend ignore donc totalement la fragmentation.
--
--  Signatures alignees sur ce que le SimpleJdbcCall Java declare.
-- =====================================================================

-- Sequence pour generer les identifiants de ligne cote global
BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE seq_ligne';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/
CREATE SEQUENCE seq_ligne START WITH 100000 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
--  insertligne (GLOBAL) : insere dans la table maitre, renvoie l'id genere.
--  L'INSERT declenche SYC_INSERT_LIGNE -> routage Site1 (>=100) / Site2.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE insertligne (
  p_idcommande IN  NUMBER,
  p_idproduit  IN  NUMBER,
  p_quantite   IN  NUMBER,
  p_remise     IN  NUMBER DEFAULT 0,
  p_idligne    OUT NUMBER
) AS
  v_prix Produits.PrixUnitaire%TYPE;
  v_cmd  NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_cmd FROM Commandes WHERE idcommande = p_idcommande;
  IF v_cmd = 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'GLOBAL : commande '||p_idcommande||' inexistante.');
  END IF;

  BEGIN
    SELECT PrixUnitaire INTO v_prix FROM Produits WHERE idproduit = p_idproduit;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20002, 'GLOBAL : produit '||p_idproduit||' inexistant.');
  END;

  p_idligne := seq_ligne.NEXTVAL;
  INSERT INTO LigneCommandes (idligneCommande, idcommande, idproduit,
                              Quantite, PrixUnitaire, remise)
  VALUES (p_idligne, p_idcommande, p_idproduit, p_quantite, v_prix, p_remise);
  -- COMMIT implicite gere par la transaction JDBC du backend.
END insertligne;
/
SHOW ERRORS;

-- ---------------------------------------------------------------------
--  deleteligne (GLOBAL) : supprime de la table maitre.
--  Le DELETE declenche SYC_DELETE_LIGNE -> deleteligne@Site1/2.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE deleteligne (
  p_idligneCmd IN NUMBER
) AS
BEGIN
  DELETE FROM LigneCommandes WHERE idligneCommande = p_idligneCmd;
END deleteligne;
/
SHOW ERRORS;

-- ---------------------------------------------------------------------
--  updateligne (GLOBAL) : met a jour la table maitre.
--  L'UPDATE declenche SYC_UPDATE_LIGNE -> gere meme la migration de
--  fragment si Quantite franchit le seuil 100.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE updateligne (
  p_idligneCmd IN NUMBER,
  p_idproduit  IN NUMBER,
  p_quantite   IN NUMBER,
  p_remise     IN NUMBER DEFAULT 0
) AS
  v_prix Produits.PrixUnitaire%TYPE;
BEGIN
  BEGIN
    SELECT PrixUnitaire INTO v_prix FROM Produits WHERE idproduit = p_idproduit;
  EXCEPTION WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20003, 'GLOBAL : produit '||p_idproduit||' inexistant.');
  END;

  UPDATE LigneCommandes
     SET idproduit    = p_idproduit,
         Quantite     = p_quantite,
         PrixUnitaire = v_prix,
         remise       = p_remise
   WHERE idligneCommande = p_idligneCmd;
END updateligne;
/
SHOW ERRORS;
