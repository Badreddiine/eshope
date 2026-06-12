-- =====================================================================
--  SITE1  —  Fragment "GROS VOLUMES"
--  Contient les LigneCommandes dont la Quantite >= 100
-- =====================================================================
--  Choix de fragmentation :
--    On applique une FRAGMENTATION HORIZONTALE DERIVEE.
--    Le predicat de fragmentation porte sur LigneCommandes.Quantite.
--    Site1 = { l ∈ LigneCommandes | l.Quantite >= 100 }
--    Les tables Clients/Commandes/Produits sont repliquees sur les deux
--    sites (fragmentation par reference) afin que les jointures locales
--    restent possibles sans aller-retour reseau.
--
--  Convention de nommage : toutes les tables portent le suffixe "1"
--  pour identifier sans ambiguite le site d'origine lors des requetes
--  distribuees (ex : CLIENTS1@LINK_SITE1).
-- =====================================================================

-- Nettoyage idempotent (utile pour re-jouer le script en developpement)
BEGIN
  FOR t IN (SELECT table_name FROM user_tables
            WHERE table_name IN ('LIGNECOMMANDES1','COMMANDES1','PRODUITS1','CLIENTS1')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE '||t.table_name||' CASCADE CONSTRAINTS';
  END LOOP;
END;
/

-- ---------------------------------------------------------------------
--  Clients1  (table repliquee — dimension)
-- ---------------------------------------------------------------------
CREATE TABLE Clients1 (
  idclient    NUMBER        PRIMARY KEY,
  Codeclient  VARCHAR2(20),
  Societe     VARCHAR2(100),
  Nom         VARCHAR2(50),
  Prenom      VARCHAR2(50),
  Adresse     VARCHAR2(200),
  Ville       VARCHAR2(50),
  Pays        VARCHAR2(50),
  Tel         VARCHAR2(30)
);

-- ---------------------------------------------------------------------
--  Produits1  (table repliquee — dimension, porte la categorie)
-- ---------------------------------------------------------------------
CREATE TABLE Produits1 (
  idproduit     NUMBER        PRIMARY KEY,
  idcateg       NUMBER,
  Designation   VARCHAR2(150),
  PrixUnitaire  NUMBER(10,2),
  UniteQte      VARCHAR2(30),
  QteStock      NUMBER
);

-- ---------------------------------------------------------------------
--  Commandes1  (table repliquee — en-tete de commande)
-- ---------------------------------------------------------------------
CREATE TABLE Commandes1 (
  idcommande    NUMBER        PRIMARY KEY,
  idemploye     NUMBER,
  idclient      NUMBER,
  dateCommande  DATE,
  dateLivraison DATE,
  port          NUMBER(10,2),
  CONSTRAINT fk_cmd1_client FOREIGN KEY (idclient) REFERENCES Clients1(idclient)
);

-- ---------------------------------------------------------------------
--  LigneCommandes1  (TABLE FRAGMENTEE — coeur du site "gros volumes")
--  Contrainte CHECK : garantit l'invariant de fragmentation Quantite>=100.
--  C'est cette contrainte qui materialise le predicat de fragmentation
--  et empeche une ligne "petit volume" d'atterrir par erreur sur Site1.
-- ---------------------------------------------------------------------
CREATE TABLE LigneCommandes1 (
  idligneCommande NUMBER       PRIMARY KEY,
  idcommande      NUMBER,
  idproduit       NUMBER,
  Quantite        NUMBER       NOT NULL,
  PrixUnitaire    NUMBER(10,2),
  remise          NUMBER(4,3)  DEFAULT 0,
  CONSTRAINT fk_lc1_cmd  FOREIGN KEY (idcommande) REFERENCES Commandes1(idcommande),
  CONSTRAINT fk_lc1_prod FOREIGN KEY (idproduit)  REFERENCES Produits1(idproduit),
  CONSTRAINT chk_lc1_qte CHECK (Quantite >= 100)
);

-- Index local pour accelerer les jointures et les agregats locaux
CREATE INDEX idx_lc1_cmd  ON LigneCommandes1(idcommande);
CREATE INDEX idx_lc1_prod ON LigneCommandes1(idproduit);

COMMIT;
