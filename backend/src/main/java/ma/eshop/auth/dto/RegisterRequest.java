package ma.eshop.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Corps de la requete d'inscription {@code POST /auth/register}.
 *
 * @param username nom de connexion (unique)
 * @param password mot de passe en clair (hashe avant stockage)
 * @param role     role applicatif : {@code USER} ou {@code ADMIN}
 */
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @Pattern(regexp = "USER|ADMIN", message = "role doit valoir USER ou ADMIN") String role) {
}
