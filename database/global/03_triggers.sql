-- =====================================================================
--  SITE_GLOBAL  —  Tables maitres + Triggers de synchronisation
-- =====================================================================
--  Le site global expose une table LigneCommandes "logique" (vue unifiee
--  materialisee en table maitre) sur laquelle l'application travaille
--  comme si la base n'etait PAS distribuee. Les triggers interceptent
--  chaque DML et ROUTENT physiquement la ligne vers le bon fragment :
--      Quantite >= 100  -> Site1   (via insertligne@LINK_SITE1)
--      Quantite <  100  -> Site2   (via insertligne@LINK_SITE2)
--  C'est la TRANSPARENCE DE FRAGMENTATION : un seul point d'entree,
--  routage automatique invisible pour le client.
--
--  Remarque pedagogique : dans un deploiement reel, ces tables maitres
--  pourraient n'etre que des en-tetes (Clients/Commandes/Produits
--  repliques). On les cree ici pour que les triggers et les index aient
--  un objet local sur lequel s'appuyer.
-- =====================================================================

-- ---------- Tables maitres locales du site global ----------
BEGIN
  FOR t IN (SELECT table_name FROM user_tables
            WHERE table_name IN ('LIGNECOMMANDES','COMMANDES','PRODUITS','CLIENTS')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE '||t.table_name||' CASCADE CONSTRAINTS';
  END LOOP;
END;
/

CREATE TABLE Clients (
  idclient NUMBER PRIMARY KEY, Codeclient VARCHAR2(20), Societe VARCHAR2(100),
  Nom VARCHAR2(50), Prenom VARCHAR2(50), Adresse VARCHAR2(200),
  Ville VARCHAR2(50), Pays VARCHAR2(50), Tel VARCHAR2(30)
);

CREATE TABLE Produits (
  idproduit NUMBER PRIMARY KEY, idcateg NUMBER, Designation VARCHAR2(150),
  PrixUnitaire NUMBER(10,2), UniteQte VARCHAR2(30), QteStock NUMBER
);

CREATE TABLE Commandes (
  idcommande NUMBER PRIMARY KEY, idemploye NUMBER, idclient NUMBER,
  dateCommande DATE, dateLivraison DATE, port NUMBER(10,2),
  CONSTRAINT fk_cmd_client FOREIGN KEY (idclient) REFERENCES Clients(idclient)
);

-- Table maitre : pas de CHECK sur Quantite ici, car elle accueille
-- AUSSI BIEN les gros que les petits volumes avant routage.
CREATE TABLE LigneCommandes (
  idligneCommande NUMBER PRIMARY KEY, idcommande NUMBER, idproduit NUMBER,
  Quantite NUMBER NOT NULL, PrixUnitaire NUMBER(10,2), remise NUMBER(4,3) DEFAULT 0,
  CONSTRAINT fk_lc_cmd  FOREIGN KEY (idcommande) REFERENCES Commandes(idcommande),
  CONSTRAINT fk_lc_prod FOREIGN KEY (idproduit)  REFERENCES Produits(idproduit)
);

-- =====================================================================
--  Trigger 1 : SYC_INSERT_LIGNE
--  Apres insertion dans la table maitre, on pousse la ligne vers le
--  fragment adequat selon le predicat de fragmentation.
-- =====================================================================
CREATE OR REPLACE TRIGGER SYC_INSERT_LIGNE
AFTER INSERT ON LigneCommandes
FOR EACH ROW
BEGIN
  IF :NEW.Quantite >= 100 THEN
    -- Gros volume -> Site1
    insertligne@LINK_SITE1(:NEW.idligneCommande, :NEW.idcommande,
                           :NEW.idproduit, :NEW.Quantite, :NEW.remise);
  ELSE
    -- Petit volume -> Site2
    insertligne@LINK_SITE2(:NEW.idligneCommande, :NEW.idcommande,
                           :NEW.idproduit, :NEW.Quantite, :NEW.remise);
  END IF;
END;
/
SHOW ERRORS;

-- =====================================================================
--  Trigger 2 : SYC_DELETE_LIGNE
--  On utilise :OLD.Quantite (la valeur supprimee) pour savoir sur quel
--  site la ligne residait, puis on appelle deleteligne du bon fragment.
-- =====================================================================
CREATE OR REPLACE TRIGGER SYC_DELETE_LIGNE
AFTER DELETE ON LigneCommandes
FOR EACH ROW
BEGIN
  IF :OLD.Quantite >= 100 THEN
    deleteligne@LINK_SITE1(:OLD.idligneCommande);
  ELSE
    deleteligne@LINK_SITE2(:OLD.idligneCommande);
  END IF;
END;
/
SHOW ERRORS;

-- =====================================================================
--  Trigger 3 : SYC_UPDATE_LIGNE
--  Cas delicat : un UPDATE peut faire CHANGER DE FRAGMENT une ligne
--  (ex : Quantite passe de 50 a 200 -> elle doit migrer Site2 -> Site1).
--  On gere donc 3 scenarios :
--    a) reste sur Site1    : updateligne@LINK_SITE1
--    b) reste sur Site2    : updateligne@LINK_SITE2
--    c) change de fragment : delete sur l'ancien + insert sur le nouveau
-- =====================================================================
CREATE OR REPLACE TRIGGER SYC_UPDATE_LIGNE
AFTER UPDATE ON LigneCommandes
FOR EACH ROW
DECLARE
  v_old_site1 BOOLEAN := (:OLD.Quantite >= 100);
  v_new_site1 BOOLEAN := (:NEW.Quantite >= 100);
BEGIN
  IF v_old_site1 AND v_new_site1 THEN
    -- a) reste sur Site1
    updateligne@LINK_SITE1(:NEW.idligneCommande, :NEW.idproduit,
                           :NEW.Quantite, :NEW.remise);
  ELSIF (NOT v_old_site1) AND (NOT v_new_site1) THEN
    -- b) reste sur Site2
    updateligne@LINK_SITE2(:NEW.idligneCommande, :NEW.idproduit,
                           :NEW.Quantite, :NEW.remise);
  ELSIF v_old_site1 AND (NOT v_new_site1) THEN
    -- c) migration Site1 -> Site2
    deleteligne@LINK_SITE1(:OLD.idligneCommande);
    insertligne@LINK_SITE2(:NEW.idligneCommande, :NEW.idcommande,
                           :NEW.idproduit, :NEW.Quantite, :NEW.remise);
  ELSE
    -- c') migration Site2 -> Site1
    deleteligne@LINK_SITE2(:OLD.idligneCommande);
    insertligne@LINK_SITE1(:NEW.idligneCommande, :NEW.idcommande,
                           :NEW.idproduit, :NEW.Quantite, :NEW.remise);
  END IF;
END;
/
SHOW ERRORS;
