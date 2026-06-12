package ma.eshop.repository;

import java.util.List;
import ma.eshop.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository JPA en lecture seule sur la table maitre {@code COMMANDES}.
 */
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    /**
     * Statistique <strong>distribuee</strong> : nombre de commandes par client
     * pour une annee donnee.
     *
     * <p>Les commandes 2026 etant reparties sur les deux fragments, on
     * reconstitue la relation globale par {@code UNION ALL} des synonymes
     * {@code COMMANDES1@LINK_SITE1} et {@code COMMANDES2@LINK_SITE2} avant de
     * compter par client. Requete native (db links non exprimables en JPQL).</p>
     *
     * @param annee annee de filtrage (ex. 2026)
     * @return lignes {@code [idclient, nb_commandes]}
     */
    @Query(value =
            "SELECT idclient, COUNT(*) AS nb_commandes FROM ( "
          + "  SELECT idclient, idcommande FROM COMMANDES1 "
          + "  WHERE EXTRACT(YEAR FROM dateCommande) = :annee "
          + "  UNION ALL "
          + "  SELECT idclient, idcommande FROM COMMANDES2 "
          + "  WHERE EXTRACT(YEAR FROM dateCommande) = :annee "
          + ") GROUP BY idclient ORDER BY idclient",
            nativeQuery = true)
    List<Object[]> countCommandesParClient(@Param("annee") int annee);
}
