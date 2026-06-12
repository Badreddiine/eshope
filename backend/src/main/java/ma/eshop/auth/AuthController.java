package ma.eshop.auth;

import jakarta.validation.Valid;
import java.util.Map;
import ma.eshop.auth.dto.AuthResponse;
import ma.eshop.auth.dto.LoginRequest;
import ma.eshop.auth.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints d'authentification : connexion (delivrance d'un JWT) et inscription
 * d'un nouveau compte.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param authenticationManager gestionnaire d'authentification Spring
     * @param userDetailsService    chargement des utilisateurs
     * @param jwtService            service de generation de JWT
     * @param userRepository        repository des comptes
     * @param passwordEncoder       encodeur BCrypt
     */
    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authentifie un utilisateur et renvoie un JWT.
     *
     * @param request identifiants de connexion
     * @return {@code {token, expiresIn}}
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, jwtService.getExpirationMillis()));
    }

    /**
     * Cree un nouveau compte (mot de passe hashe en BCrypt).
     *
     * @param request donnees du compte
     * @return 201 si cree, 409 si le nom est deja pris
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Nom d'utilisateur deja utilise."));
        }
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role());
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("username", user.getUsername(), "role", user.getRole(),
                        "message", "Compte cree."));
    }
}
