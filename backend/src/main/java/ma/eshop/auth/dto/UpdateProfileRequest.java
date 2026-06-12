package ma.eshop.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps de la requête de mise à jour du profil ({@code PUT /api/profile}).
 *
 * <p>Le mot de passe actuel est exigé pour autoriser tout changement. Le nouveau
 * nom d'utilisateur et le nouveau mot de passe sont optionnels : on ne modifie
 * que les champs fournis (non vides).</p>
 *
 * @param currentPassword mot de passe actuel (obligatoire, pour vérification)
 * @param newUsername     nouveau nom de connexion (optionnel)
 * @param newPassword     nouveau mot de passe (optionnel)
 */
public record UpdateProfileRequest(
        @NotBlank String currentPassword,
        String newUsername,
        String newPassword) {
}
