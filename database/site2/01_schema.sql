-- =====================================================================
--  SITE2  —  Fragment "PETITS VOLUMES"
--  Contient les LigneCommandes dont la Quantite < 100
-- =====================================================================
--  Choix de fragmentation (complementaire de Site1) :
--    Site2 = { l ∈ LigneCommandes | l.Quantite < 100 }
--    Union disjointe et complete : Site1 ∪ Site2 = LigneCommandes,
--    Site1 ∩ Site2 = ∅  (le predicat Quantite>=100 / Quantite<100
--    garantit la completude et la non-redondance du decoupage).
--    Les dimensions (Clients/Produits/Commandes) sont repliquees.
-- =====================================================================

BEGIN
  FOR t IN (SELECT table_name FROM user_tables
            WHERE table_name IN ('LIGNECOMMANDES2','COMMANDES2','PRODUITS2','CLIENTS2')) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE '||t.table_name||' CASCADE CONSTRAINTS';
  END LOOP;
END;
/

CREATE TABLE Clients2 (
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

CREATE TABLE Produits2 (
  idproduit     NUMBER        PRIMARY KEY,
  idcateg       NUMBER,
  Designation   VARCHAR2(150),
  PrixUnitaire  NUMBER(10,2),
  UniteQte      VARCHAR2(30),
  QteStock      NUMBER
);

CREATE TABLE Commandes2 (
  idcommande    NUMBER        PRIMARY KEY,
  idemploye     NUMBER,
  idclient      NUMBER,
  dateCommande  DATE,
  dateLivraison DATE,
  port          NUMBER(10,2),
  CONSTRAINT fk_cmd2_client FOREIGN KEY (idclient) REFERENCES Clients2(idclient)
);

-- LigneCommandes2 : invariant de fragmentation Quantite < 100 impose par CHECK
CREATE TABLE LigneCommandes2 (
  idligneCommande NUMBER       PRIMARY KEY,
  idcommande      NUMBER,
  idproduit       NUMBER,
  Quantite        NUMBER       NOT NULL,
  PrixUnitaire    NUMBER(10,2),
  remise          NUMBER(4,3)  DEFAULT 0,
  CONSTRAINT fk_lc2_cmd  FOREIGN KEY (idcommande) REFERENCES Commandes2(idcommande),
  CONSTRAINT fk_lc2_prod FOREIGN KEY (idproduit)  REFERENCES Produits2(idproduit),
  CONSTRAINT chk_lc2_qte CHECK (Quantite < 100)
);

CREATE INDEX idx_lc2_cmd  ON LigneCommandes2(idcommande);
CREATE INDEX idx_lc2_prod ON LigneCommandes2(idproduit);

COMMIT;
