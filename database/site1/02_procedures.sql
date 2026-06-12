-- =====================================================================
--  SITE1  —  Procedures stockees PL/SQL de manipulation des lignes
-- =====================================================================
--  Ces procedures sont APPELEES A DISTANCE par les triggers du site
--  global (ex : insertligne@LINK_SITE1). Elles encapsulent toute la
--  logique d'integrite referentielle cote site, ce qui evite au site
--  global de connaitre la structure interne du fragment.
--
--  Regle metier propre a Site1 : Quantite >= 100 (impose aussi par le
--  CHECK de la table — la procedure le valide pour renvoyer une erreur
--  parlante plutot qu'une violation de contrainte brute).
-- =====================================================================

-- ---------------------------------------------------------------------
--  insertligne : insere une ligne APRES verification des FK
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE insertligne (
  p_idligneCmd  IN NUMBER,
  p_idcommande  IN NUMBER,
  p_idproduit   IN NUMBER,
  p_quantite    IN NUMBER,
  p_remise      IN NUMBER DEFAULT 0
) AS
  v_cmd_ok   NUMBER;
  v_prod_ok  NUMBER;
  v_prix     Produits1.PrixUnitaire%TYPE;
BEGIN
  -- Garde-fou : respecter l'invariant de fragmentation du site
  IF p_quantite < 100 THEN
    RAISE_APPLICATION_ERROR(-20011,
      'SITE1 (gros volumes) : Quantite < 100 refusee, doit aller sur Site2.');
  END IF;

  -- Verification FK : la commande parente doit exister
  SELECT COUNT(*) INTO v_cmd_ok FROM Commandes1 WHERE idcommande = p_idcommande;
  IF v_cmd_ok = 0 THEN
    RAISE_APPLICATION_ERROR(-20012, 'SITE1 : commande '||p_idcommande||' inexistante.');
  END IF;

  -- Verification FK : le produit doit exister + on recupere son prix
  BEGIN
    SELECT PrixUnitaire INTO v_prix FROM Produits1 WHERE idproduit = p_idproduit;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20013, 'SITE1 : produit '||p_idproduit||' inexistant.');
  END;

  INSERT INTO LigneCommandes1 (idligneCommande, idcommande, idproduit,
                               Quantite, PrixUnitaire, remise)
  VALUES (p_idligneCmd, p_idcommande, p_idproduit, p_quantite, v_prix, p_remise);
END insertligne;
/

-- ---------------------------------------------------------------------
--  deleteligne : supprime la ligne puis nettoie les commandes orphelines
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE deleteligne (
  p_idligneCmd IN NUMBER
) AS
  v_idcommande Commandes1.idcommande%TYPE;
  v_reste      NUMBER;
BEGIN
  -- On memorise la commande parente avant suppression
  SELECT idcommande INTO v_idcommande
  FROM LigneCommandes1 WHERE idligneCommande = p_idligneCmd;

  DELETE FROM LigneCommandes1 WHERE idligneCommande = p_idligneCmd;

  -- Nettoyage : si la commande n'a plus aucune ligne, on la supprime
  SELECT COUNT(*) INTO v_reste FROM LigneCommandes1 WHERE idcommande = v_idcommande;
  IF v_reste = 0 THEN
    DELETE FROM Commandes1 WHERE idcommande = v_idcommande;
  END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    NULL; -- la ligne n'existait pas sur ce site : on ignore silencieusement
END deleteligne;
/

-- ---------------------------------------------------------------------
--  updateligne : met a jour une ligne avec re-validation de la FK produit
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE updateligne (
  p_idligneCmd IN NUMBER,
  p_idproduit  IN NUMBER,
  p_quantite   IN NUMBER,
  p_remise     IN NUMBER DEFAULT 0
) AS
  v_prix Produits1.PrixUnitaire%TYPE;
BEGIN
  IF p_quantite < 100 THEN
    RAISE_APPLICATION_ERROR(-20014,
      'SITE1 : nouvelle Quantite < 100 — la ligne doit migrer vers Site2.');
  END IF;

  -- Re-validation de la FK produit (elle peut changer lors d'un update)
  BEGIN
    SELECT PrixUnitaire INTO v_prix FROM Produits1 WHERE idproduit = p_idproduit;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20015, 'SITE1 : produit '||p_idproduit||' inexistant.');
  END;

  UPDATE LigneCommandes1
     SET idproduit    = p_idproduit,
         Quantite     = p_quantite,
         PrixUnitaire = v_prix,
         remise       = p_remise
   WHERE idligneCommande = p_idligneCmd;
END updateligne;
/

SHOW ERRORS;
