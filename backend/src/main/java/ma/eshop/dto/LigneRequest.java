package ma.eshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Corps de requete pour la creation d'une ligne de commande
 * ({@code POST /api/lignes}).
 *
 * <p>L'identifiant de ligne est normalement <strong>genere cote serveur</strong>
 * (sequence ESHOP {@code seq_ligne}) : {@code idligneCommande} reste donc
 * {@code null} pour une creation classique. Le champ existe pour aligner le
 * corps JSON sur la signature du wrapper {@code ESHOP.INSERTLIGNE}
 * (P_IDLIGNECMD, P_IDCOMMANDE, P_IDPRODUIT, P_QUANTITE, P_REMISE) et permettre,
 * le cas echeant, d'imposer un id. Le routage Site1/Site2 depend de
 * {@code quantite} et est decide par Oracle, jamais par le backend.</p>
 *
 * @param idligneCommande identifiant de ligne impose (optionnel ; sinon genere)
 * @param idcommande      identifiant de la commande parente (obligatoire)
 * @param idproduit       identifiant du produit (obligatoire)
 * @param quantite        quantite commandee (> 0)
 * @param remise          taux de remise entre 0 et 1 (optionnel, defaut 0)
 */
public record LigneRequest(
        Long idligneCommande,
        @NotNull Long idcommande,
        @NotNull Long idproduit,
        @NotNull @Positive Integer quantite,
        @PositiveOrZero BigDecimal remise) {
}
