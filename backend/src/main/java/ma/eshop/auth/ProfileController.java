package ma.eshop.auth;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import ma.eshop.auth.dto.ProfileResponse;
import ma.eshop.auth.dto.UpdateProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints du profil de l'utilisateur <strong>connecté</strong>
 * ({@code /api/profile}).
 *
 * <p>L'identité provient du JWT : {@link Authentication#getName()} donne le nom
 * d'utilisateur (sujet du token). Ces routes tombent sous la règle
 * {@code anyRequest().authenticated()} de {@link SecurityConfig} : tout compte
 * authentifié (USER ou ADMIN) peut consulter et modifier <em>son</em> profil.</p>
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param userRepository  repository des comptes
     * @param passwordEncoder encodeur BCrypt (vérification + hashage)
     */
    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Renvoie le profil de l'utilisateur connecté.
     *
     * @param authentication contexte de sécurité (injecté par Spring)
     * @return {@code {id, username, role}}
     */
    @GetMapping
    public ResponseEntity<ProfileResponse> moi(Authentication authentication) {
        User u = utilisateurCourant(authentication);
        return ResponseEntity.ok(new ProfileResponse(u.getId(), u.getUsername(), u.getRole()));
    }

    /**
     * Met à jour le profil : nom d'utilisateur et/ou mot de passe. Le mot de
     * passe actuel est vérifié avant toute modification.
     *
     * @param authentication contexte de sécurité
     * @param request        mot de passe actuel + champs à modifier
     * @return 200 avec le profil mis à jour, 401 si mot de passe actuel faux,
     *         409 si le nouveau nom d'utilisateur est déjà pris
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> modifier(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        User u = utilisateurCourant(authentication);

        if (!passwordEncoder.matches(request.currentPassword(), u.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Mot de passe actuel incorrect."));
        }

        boolean usernameChanged = false;
        String nouveauNom = request.newUsername();
        if (nouveauNom != null && !nouveauNom.isBlank() && !nouveauNom.equals(u.getUsername())) {
            if (userRepository.existsByUsername(nouveauNom)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Nom d'utilisateur déjà utilisé."));
            }
            u.setUsername(nouveauNom);
            usernameChanged = true;
        }

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.save(u);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.getId());
        body.put("username", u.getUsername());
        body.put("role", u.getRole());
        body.put("usernameChanged", usernameChanged);
        body.put("message", usernameChanged
                ? "Profil mis à jour. Reconnecte-toi : le nom d'utilisateur a changé."
                : "Profil mis à jour.");
        return ResponseEntity.ok(body);
    }

    /**
     * Charge l'entité {@link User} correspondant au token courant.
     *
     * @param authentication contexte de sécurité
     * @return l'utilisateur connecté
     */
    private User utilisateurCourant(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable."));
    }
}
