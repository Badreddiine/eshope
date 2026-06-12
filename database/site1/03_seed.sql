-- =====================================================================
--  SITE1  —  Donnees de test  (UNIQUEMENT Quantite >= 100)
-- =====================================================================
--  Les clients/produits/commandes sont volontairement les memes "ids"
--  que sur Site2 (tables repliquees) : seul le fragment LigneCommandes
--  differe. Les dates sont en 2026 pour alimenter les requetes annuelles.
-- =====================================================================

-- Clients (dimension repliquee)
INSERT INTO Clients1 VALUES (1,'CLI001','TechCorp','Durand','Alice','12 rue A','Paris','France','0102030405');
INSERT INTO Clients1 VALUES (2,'CLI002','MegaStore','Martin','Bob','5 av B','Lyon','France','0203040506');
INSERT INTO Clients1 VALUES (3,'CLI003','GlobalDis','Petit','Chloe','9 bd C','Marseille','France','0304050607');

-- Produits (dimension repliquee, idcateg = categorie)
INSERT INTO Produits1 VALUES (10,1,'Clavier mecanique', 80.00,'piece',5000);
INSERT INTO Produits1 VALUES (11,1,'Souris optique',     25.00,'piece',8000);
INSERT INTO Produits1 VALUES (12,2,'Cable HDMI 2m',       9.50,'piece',20000);
INSERT INTO Produits1 VALUES (13,3,'Ecran 27 pouces',   199.00,'piece',1500);

-- Commandes 2026 (en-tetes)
INSERT INTO Commandes1 VALUES (1000,7,1,DATE '2026-01-15',DATE '2026-01-20',15.00);
INSERT INTO Commandes1 VALUES (1001,7,2,DATE '2026-02-10',DATE '2026-02-14',20.00);
INSERT INTO Commandes1 VALUES (1002,4,1,DATE '2026-03-05',DATE '2026-03-09',12.00);
INSERT INTO Commandes1 VALUES (1003,4,3,DATE '2026-05-22',DATE '2026-05-28',30.00);

-- Lignes GROS VOLUMES : toutes Quantite >= 100
INSERT INTO LigneCommandes1 VALUES (1,1000,10,150,80.00,0.05);
INSERT INTO LigneCommandes1 VALUES (2,1000,11,300,25.00,0.10);
INSERT INTO LigneCommandes1 VALUES (3,1001,12,500,9.50,0.00);
INSERT INTO LigneCommandes1 VALUES (4,1002,13,100,199.00,0.02);
INSERT INTO LigneCommandes1 VALUES (5,1003,10,250,80.00,0.08);
INSERT INTO LigneCommandes1 VALUES (6,1003,12,1000,9.50,0.15);

-- ---------------------------------------------------------------------
--  Donnees supplementaires (idempotent : inserees seulement si absentes)
--  >= 5 lignes Quantite >= 100, avec dimensions FK coherentes.
-- ---------------------------------------------------------------------
-- Clients additionnels (dimension repliquee)
INSERT INTO Clients1
  SELECT 4,'CLI004','InfoPlus','Roux','David','3 rue D','Lille','France','0405060708' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Clients1 WHERE idclient = 4);
INSERT INTO Clients1
  SELECT 5,'CLI005','DataNet','Moreau','Emma','7 rue E','Nantes','France','0506070809' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Clients1 WHERE idclient = 5);

-- Produits additionnels
INSERT INTO Produits1
  SELECT 14,2,'Webcam HD 1080p',45.00,'piece',3000 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Produits1 WHERE idproduit = 14);
INSERT INTO Produits1
  SELECT 15,1,'Casque audio',120.00,'piece',2000 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Produits1 WHERE idproduit = 15);

-- Commandes additionnelles 2026
INSERT INTO Commandes1
  SELECT 1004,4,4,DATE '2026-06-03',DATE '2026-06-08',18.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 1004);
INSERT INTO Commandes1
  SELECT 1005,4,5,DATE '2026-06-10',DATE '2026-06-15',22.50 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 1005);

-- Lignes GROS VOLUMES supplementaires (toutes Quantite >= 100)
INSERT INTO LigneCommandes1
  SELECT 7,1004,14,120,45.00,0.05 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes1 WHERE idligneCommande = 7);
INSERT INTO LigneCommandes1
  SELECT 8,1004,15,200,120.00,0.10 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes1 WHERE idligneCommande = 8);
INSERT INTO LigneCommandes1
  SELECT 9,1005,10,150,80.00,0.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes1 WHERE idligneCommande = 9);
INSERT INTO LigneCommandes1
  SELECT 10,1005,11,500,25.00,0.12 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes1 WHERE idligneCommande = 10);
INSERT INTO LigneCommandes1
  SELECT 11,1005,13,100,199.00,0.03 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM LigneCommandes1 WHERE idligneCommande = 11);

-- ---------------------------------------------------------------------
--  En-tetes de commande REPLIQUES depuis Site2 (dimension repliquee).
--  Indispensable : une ligne routee vers Site1 (Quantite >= 100) sur une
--  commande "nee" sur Site2 exige que l'en-tete existe aussi ici, sinon
--  insertligne leve "commande inexistante". Valeurs identiques a Site2.
-- ---------------------------------------------------------------------
INSERT INTO Commandes1
  SELECT 2000,7,1,DATE '2026-01-18',DATE '2026-01-22',8.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 2000);
INSERT INTO Commandes1
  SELECT 2001,3,2,DATE '2026-04-02',DATE '2026-04-05',6.50 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 2001);
INSERT INTO Commandes1
  SELECT 2002,3,1,DATE '2026-06-01',DATE '2026-06-04',5.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 2002);
INSERT INTO Commandes1
  SELECT 2003,7,3,DATE '2026-09-12',DATE '2026-09-15',7.20 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 2003);
INSERT INTO Commandes1
  SELECT 2004,4,4,DATE '2026-06-05',DATE '2026-06-09',4.50 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 2004);
INSERT INTO Commandes1
  SELECT 2005,4,5,DATE '2026-06-11',DATE '2026-06-14',6.00 FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM Commandes1 WHERE idcommande = 2005);

COMMIT;
