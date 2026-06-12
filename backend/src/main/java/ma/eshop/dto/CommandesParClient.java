package ma.eshop.dto;

/**
 * Resultat de la statistique "nombre de commandes par client".
 *
 * @param idclient     identifiant du client
 * @param nbCommandes  nombre de commandes de ce client sur l'annee demandee
 */
public record CommandesParClient(Long idclient, Long nbCommandes) {
}
