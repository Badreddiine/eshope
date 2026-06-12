-- =====================================================================
--  SITE_GLOBAL  —  Synonymes sur les tables distantes
-- =====================================================================
--  Les synonymes apportent la TRANSPARENCE DE NOMMAGE : on ecrit
--  "CLIENTS1" au lieu de "CLIENTS1@LINK_SITE1". Si demain le fragment
--  Site1 demenage sur un autre serveur, on ne modifie QUE le synonyme
--  (et le db link) — toutes les requetes du site global restent
--  inchangees. C'est la cle de l'INDEPENDANCE A LA LOCALISATION.
-- =====================================================================

-- ----- Tables du fragment Site1 -----
CREATE OR REPLACE SYNONYM CLIENTS1        FOR CLIENTS1@LINK_SITE1;
CREATE OR REPLACE SYNONYM PRODUITS1       FOR PRODUITS1@LINK_SITE1;
CREATE OR REPLACE SYNONYM COMMANDES1      FOR COMMANDES1@LINK_SITE1;
CREATE OR REPLACE SYNONYM LIGNECOMMANDES1 FOR LIGNECOMMANDES1@LINK_SITE1;

-- ----- Tables du fragment Site2 -----
CREATE OR REPLACE SYNONYM CLIENTS2        FOR CLIENTS2@LINK_SITE2;
CREATE OR REPLACE SYNONYM PRODUITS2       FOR PRODUITS2@LINK_SITE2;
CREATE OR REPLACE SYNONYM COMMANDES2      FOR COMMANDES2@LINK_SITE2;
CREATE OR REPLACE SYNONYM LIGNECOMMANDES2 FOR LIGNECOMMANDES2@LINK_SITE2;

-- Verification
SELECT COUNT(*) AS lignes_site1 FROM LIGNECOMMANDES1;
SELECT COUNT(*) AS lignes_site2 FROM LIGNECOMMANDES2;
