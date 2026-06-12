package ma.eshop.repository;

import java.util.List;
import ma.eshop.model.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository JPA <strong>en lecture seule</strong> sur la table maitre
 * {@code LIGNECOMMANDES} du site global.
 *
 * <p>Aucune methode d'ecriture n'est exposee : les mutations passent par les
 * procedures stockees. On herite seulement des lectures de
 * {@link JpaRepository} (notamment la pagination via {@code findAll(Pageable)}).</p>
 */
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    /**
     * Statistique <strong>distribuee</strong> : chiffre d'affaires par categorie
     * pour une annee donnee.
     *
     * <p>Cette requete ne peut PAS s'exprimer en JPQL : elle combine deux
     * fragments distants via les synonymes/database links
     * ({@code LIGNECOMMANDES1@LINK_SITE1}, {@code LIGNECOMMANDES2@LINK_SITE2})
     * par un {@code UNION ALL}. On utilise donc une requete native qui
     * s'execute sur le datasource global, lequel possede les db links.</p>
     *
     * <p>CA d'une ligne = {@code Quantite * PrixUnitaire * (1 - remise)}.
     * Le {@code SUM} etant distributif, on additionne les contributions
     * locales de chaque site (l'agregation est poussee cote distant).</p>
     *
     * @param annee annee de filtrage (ex. 2026)
     * @return lignes {@code [idcateg, ca_total]}
     */
    @Query(value =
            "SELECT idcateg, SUM(ca_partiel) AS ca_total FROM ( "
          + "  SELECT p.idcateg AS idcateg, "
          + "         SUM(l.Quantite * l.PrixUnitaire * (1 - NVL(l.remise,0))) AS ca_partiel "
          + "  FROM LIGNECOMMANDES1 l "
          + "  JOIN PRODUITS1  p ON p.idproduit  = l.idproduit "
          + "  JOIN COMMANDES1 c ON c.idcommande = l.idcommande "
          + "  WHERE EXTRACT(YEAR FROM c.dateCommande) = :annee "
          + "  GROUP BY p.idcateg "
          + "  UNION ALL "
          + "  SELECT p.idcateg AS idcateg, "
          + "         SUM(l.Quantite * l.PrixUnitaire * (1 - NVL(l.remise,0))) AS ca_partiel "
          + "  FROM LIGNECOMMANDES2 l "
          + "  JOIN PRODUITS2  p ON p.idproduit  = l.idproduit "
          + "  JOIN COMMANDES2 c ON c.idcommande = l.idcommande "
          + "  WHERE EXTRACT(YEAR FROM c.dateCommande) = :annee "
          + "  GROUP BY p.idcateg "
          + ") GROUP BY idcateg ORDER BY idcateg",
            nativeQuery = true)
    List<Object[]> chiffreAffaireParCategorie(@Param("annee") int annee);
}
