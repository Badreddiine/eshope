-- =====================================================================
--  SITE2  —  Procedures stockees PL/SQL (petits volumes, Quantite < 100)
-- =====================================================================
--  Memes signatures que Site1 : c'est volontaire. Le site global appelle
--  insertligne@LINK_SITE1 ou insertligne@LINK_SITE2 SANS connaitre la
--  difference — seul le predicat de routage change. Cette TRANSPARENCE
--  de fragmentation est l'objectif d'une BDD distribuee.
-- =====================================================================

CREATE OR REPLACE PROCEDURE insertligne (
  p_idligneCmd  IN NUMBER,
  p_idcommande  IN NUMBER,
  p_idproduit   IN NUMBER,
  p_quantite    IN NUMBER,
  p_remise      IN NUMBER DEFAULT 0
) AS
  v_cmd_ok   NUMBER;
  v_prix     Produits2.PrixUnitaire%TYPE;
BEGIN
  -- Invariant du site : petits volumes uniquement
  IF p_quantite >= 100 THEN
    RAISE_APPLICATION_ERROR(-20021,
      'SITE2 (petits volumes) : Quantite >= 100 refusee, doit aller sur Site1.');
  END IF;

  SELECT COUNT(*) INTO v_cmd_ok FROM Commandes2 WHERE idcommande = p_idcommande;
  IF v_cmd_ok = 0 THEN
    RAISE_APPLICATION_ERROR(-20022, 'SITE2 : commande '||p_idcommande||' inexistante.');
  END IF;

  BEGIN
    SELECT PrixUnitaire INTO v_prix FROM Produits2 WHERE idproduit = p_idproduit;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20023, 'SITE2 : produit '||p_idproduit||' inexistant.');
  END;

  INSERT INTO LigneCommandes2 (idligneCommande, idcommande, idproduit,
                               Quantite, PrixUnitaire, remise)
  VALUES (p_idligneCmd, p_idcommande, p_idproduit, p_quantite, v_prix, p_remise);
END insertligne;
/

CREATE OR REPLACE PROCEDURE deleteligne (
  p_idligneCmd IN NUMBER
) AS
  v_idcommande Commandes2.idcommande%TYPE;
  v_reste      NUMBER;
BEGIN
  SELECT idcommande INTO v_idcommande
  FROM LigneCommandes2 WHERE idligneCommande = p_idligneCmd;

  DELETE FROM LigneCommandes2 WHERE idligneCommande = p_idligneCmd;

  SELECT COUNT(*) INTO v_reste FROM LigneCommandes2 WHERE idcommande = v_idcommande;
  IF v_reste = 0 THEN
    DELETE FROM Commandes2 WHERE idcommande = v_idcommande;
  END IF;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    NULL;
END deleteligne;
/

CREATE OR REPLACE PROCEDURE updateligne (
  p_idligneCmd IN NUMBER,
  p_idproduit  IN NUMBER,
  p_quantite   IN NUMBER,
  p_remise     IN NUMBER DEFAULT 0
) AS
  v_prix Produits2.PrixUnitaire%TYPE;
BEGIN
  IF p_quantite >= 100 THEN
    RAISE_APPLICATION_ERROR(-20024,
      'SITE2 : nouvelle Quantite >= 100 — la ligne doit migrer vers Site1.');
  END IF;

  BEGIN
    SELECT PrixUnitaire INTO v_prix FROM Produits2 WHERE idproduit = p_idproduit;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20025, 'SITE2 : produit '||p_idproduit||' inexistant.');
  END;

  UPDATE LigneCommandes2
     SET idproduit    = p_idproduit,
         Quantite     = p_quantite,
         PrixUnitaire = v_prix,
         remise       = p_remise
   WHERE idligneCommande = p_idligneCmd;
END updateligne;
/

SHOW ERRORS;
