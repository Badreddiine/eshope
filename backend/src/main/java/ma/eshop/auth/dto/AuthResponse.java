package ma.eshop.auth.dto;

/**
 * Reponse renvoyee apres une authentification reussie.
 *
 * @param token     jeton JWT signe (HS256)
 * @param expiresIn duree de validite du token en millisecondes
 */
public record AuthResponse(String token, long expiresIn) {
}
