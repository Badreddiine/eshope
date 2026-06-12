package ma.eshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Corps de requete pour la mise a jour d'une ligne ({@code PUT /api/lignes/{id}}).
 *
 * @param idproduit nouvel identifiant de produit (obligatoire)
 * @param quantite  nouvelle quantite (> 0) — peut faire migrer la ligne de
 *                  fragment, migration geree par le trigger Oracle
 * @param remise    nouveau taux de remise entre 0 et 1 (optionnel)
 */
public record LigneUpdateRequest(
        @NotNull Long idproduit,
        @NotNull @Positive Integer quantite,
        @PositiveOrZero BigDecimal remise) {
}
