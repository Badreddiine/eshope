package ma.eshop.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps de la requete de connexion {@code POST /auth/login}.
 *
 * @param username nom de connexion
 * @param password mot de passe en clair (verifie contre le hash BCrypt)
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
