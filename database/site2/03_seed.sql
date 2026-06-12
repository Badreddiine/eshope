-- =====================================================================
--  SITE2  —  Donnees de test  (UNIQUEMENT Quantite < 100)
-- =====================================================================
--  Memes dimensions (ids identiques a Site1) + commandes 2026 propres.
--  Certains clients passent des commandes sur les DEUX sites : c'est
--  exactement ce que la requete "commandes par client" (UNION ALL)
--  doit savoir recombiner.
-- =====================================================================

INSERT INTO Clients2 VALUES (1,'CLI001','TechCorp','Durand','Alice','12 rue A','Paris','France','0102030405');
INSERT INTO Clients2 VALUES (2,'CLI002','MegaStore','Martin','Bob','5 av B','Lyon','France','0203040506');
INSERT INTO Clients2 VALUES (3,'CLI003','GlobalDis','Petit','Chloe','9 bd C','Marseille','France','0304050607');

INSERT INTO Produits2 VALUES (10,1,'Clavier mecanique', 80.00,'piece',5000);
INSERT INTO Produits2 VALUES (11,1,'Souris optique',     25.00,'piece',8000);
INSERT INTO Produits2 VALUES (12,2,'Cable HDMI 2m',       9.50,'piece',20000);
INSERT INTO Produits2 VALUES (13,3,'Ecran 27 pouces',   199.00,'piece',1500);

-- Commandes 2026 propres a Site2 (ids distincts de Site1 pour eviter les collisions)
INSERT INTO Commandes2 VALUES (2000,7,1,DATE '2026-01-18',DATE '2026-01-22',8.00);
INSERT INTO Commandes2 VALUES (2001,3,2,DATE '2026-04-02',DATE '2026-04-05',6.50);
INSERT INTO Commandes2 VALUES (2002,3,1,DATE '2026-06-01',DATE '2026-06-04',5.00);
INSERT INTO Commandes2 VALUES (2003,7,3,DATE '2026-09-12',DATE '2026-09-15',7.20);

-- Lignes PETITS VOLUMES : toutes Quantite < 100
INSERT INTO LigneCommandes2 VALUES (101,2000,11,5,25.00,0.00);
INSERT INTO LigneCommandes2 VALUES (102,2000,13,2,199.00,0.05);
INSERT INTO LigneCommandes2 VALUES (103,2001,12,40,9.50,0.00);
INSERT INTO LigneCommandes2 VALUES (104,2002,10,10,80.00,0.00);
INSERT INTO LigneCommandes2 VALUES (105,2002,11,25,25.00,0.02);
INSERT INTO LigneCommandes2 VALUES (106,2003,13,1,199.00,0.00);

-- ---------------------------------------------------------------------
--  Donnees supplementaires (idempotent : inserees seulement si absentes)
--  >= 5 lignes Quantite < 100, avec dimensions FK coherentes.
-- ---------------------------------------------------------------------
-- Clients additionnels (memes ids que Site1 : dimension repliquee)
INSERT INTO Clients2
  SELECT 4,'CLI004','InfoPlus','Roux','David','3 rue D','Lille','France','0405060708' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Clients2 WHERE idclient = 4);
INSERT INTO Clients2
  SELECT 5,'CLI005','DataNet','Moreau','Emma','7 rue E','Nantes','France','0506070809' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Clients2 WHERE idclient = 5);

-- Produits additionnels
INSERT INTO Produits2
  SELECT 14,2,'Webcam HD 1080p',45.00,'piece',3000 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Produits2 WHERE idproduit = 14);
INSERT INTO Produits2
  SELECT 15,1,'Casque audio',120.00,'piece',2000 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Produits2 WHERE idproduit = 15);

-- Commandes additionnelles 2026 (ids distincts de Site1)
INSERT INTO Commandes2
  SELECT 2004,4,4,DATE '2026-06-05',DATE '2026-06-09',4.50 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 2004);
INSERT INTO Commandes2
  SELECT 2005,4,5,DATE '2026-06-11',DATE '2026-06-14',6.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 2005);

-- Lignes PETITS VOLUMES supplementaires (toutes Quantite < 100)
INSERT INTO LigneCommandes2
  SELECT 107,2004,14,12,45.00,0.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes2 WHERE idligneCommande = 107);
INSERT INTO LigneCommandes2
  SELECT 108,2004,15,3,120.00,0.05 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes2 WHERE idligneCommande = 108);
INSERT INTO LigneCommandes2
  SELECT 109,2005,10,50,80.00,0.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes2 WHERE idligneCommande = 109);
INSERT INTO LigneCommandes2
  SELECT 110,2005,11,30,25.00,0.02 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes2 WHERE idligneCommande = 110);
INSERT INTO LigneCommandes2
  SELECT 111,2005,12,99,9.50,0.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes2 WHERE idligneCommande = 111);

-- ---------------------------------------------------------------------
--  En-tetes de commande REPLIQUES depuis Site1 (dimension repliquee).
--  Indispensable : une ligne routee vers Site2 (Quantite < 100) sur une
--  commande "nee" sur Site1 exige que l'en-tete existe aussi ici, sinon
--  insertligne leve "commande inexistante". Valeurs identiques a Site1.
-- ---------------------------------------------------------------------
INSERT INTO Commandes2
  SELECT 1000,7,1,DATE '2026-01-15',DATE '2026-01-20',15.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 1000);
INSERT INTO Commandes2
  SELECT 1001,7,2,DATE '2026-02-10',DATE '2026-02-14',20.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 1001);
INSERT INTO Commandes2
  SELECT 1002,4,1,DATE '2026-03-05',DATE '2026-03-09',12.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 1002);
INSERT INTO Commandes2
  SELECT 1003,4,3,DATE '2026-05-22',DATE '2026-05-28',30.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 1003);
INSERT INTO Commandes2
  SELECT 1004,4,4,DATE '2026-06-03',DATE '2026-06-08',18.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 1004);
INSERT INTO Commandes2
  SELECT 1005,4,5,DATE '2026-06-10',DATE '2026-06-15',22.50 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes2 WHERE idcommande = 1005);

COMMIT;
