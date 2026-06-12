-- =====================================================================
--  SITE_GLOBAL  —  Donnees maitres (dimensions + commandes)
-- =====================================================================
--  Les fragments Site1/Site2 ont leurs propres donnees (seed des parties
--  precedentes). La table MAITRE du site global, elle, sert de point
--  d'entree aux ECRITURES via l'API : pour qu'un INSERT de ligne reussisse
--  (FK commande + produit), le global doit connaitre clients, produits et
--  commandes. On replique donc ici les dimensions et quelques en-tetes de
--  commande sur lesquels le backend pourra rattacher de nouvelles lignes.
-- =====================================================================

-- Dimensions (memes ids que sur les fragments)
INSERT INTO Clients VALUES (1,'CLI001','TechCorp','Durand','Alice','12 rue A','Paris','France','0102030405');
INSERT INTO Clients VALUES (2,'CLI002','MegaStore','Martin','Bob','5 av B','Lyon','France','0203040506');
INSERT INTO Clients VALUES (3,'CLI003','GlobalDis','Petit','Chloe','9 bd C','Marseille','France','0304050607');

INSERT INTO Produits VALUES (10,1,'Clavier mecanique', 80.00,'piece',5000);
INSERT INTO Produits VALUES (11,1,'Souris optique',     25.00,'piece',8000);
INSERT INTO Produits VALUES (12,2,'Cable HDMI 2m',       9.50,'piece',20000);
INSERT INTO Produits VALUES (13,3,'Ecran 27 pouces',   199.00,'piece',1500);

-- En-tetes de commande disponibles pour de nouvelles lignes via l'API.
INSERT INTO Commandes VALUES (1000,7,1,DATE '2026-01-15',DATE '2026-01-20',15.00);
INSERT INTO Commandes VALUES (1001,7,2,DATE '2026-02-10',DATE '2026-02-14',20.00);
INSERT INTO Commandes VALUES (2000,7,1,DATE '2026-01-18',DATE '2026-01-22', 8.00);

COMMIT;
