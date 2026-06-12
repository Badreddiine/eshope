package ma.eshop.dto;

import java.math.BigDecimal;

/**
 * Resultat de la statistique distribuee "chiffre d'affaires par categorie".
 *
 * @param idcateg identifiant de la categorie
 * @param caTotal chiffre d'affaires total (Site1 + Site2) sur l'annee demandee
 */
public record CaParCategorie(Long idcateg, BigDecimal caTotal) {
}
