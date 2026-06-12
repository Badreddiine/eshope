package ma.eshop.auth.dto;

/**
 * Profil de l'utilisateur connecté renvoyé par {@code GET /api/profile}.
 * Le mot de passe (hashé) n'est jamais exposé.
 *
 * @param id       identifiant technique du compte
 * @param username nom de connexion
 * @param role     rôle applicatif ({@code USER} ou {@code ADMIN})
 */
public record ProfileResponse(Long id, String username, String role) {
}
