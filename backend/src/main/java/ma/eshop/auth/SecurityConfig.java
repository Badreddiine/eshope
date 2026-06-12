package ma.eshop.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de Spring Security 6 pour une API REST stateless protegee par
 * JWT.
 *
 * <p>Regles d'acces :</p>
 * <ul>
 *   <li>{@code POST /auth/**} et {@code /actuator/**} : ouverts ;</li>
 *   <li>{@code GET /api/lignes}, {@code GET /api/stats/**} : USER ou ADMIN ;</li>
 *   <li>{@code POST/PUT/DELETE /api/lignes} : ADMIN uniquement ;</li>
 *   <li>tout le reste : authentification requise.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * Chaine de filtres de securite : CSRF desactive, sessions stateless,
     * regles d'autorisation et insertion du filtre JWT.
     *
     * <p>Le {@link JwtAuthFilter} est injecte en parametre de methode (et non
     * via le constructeur) pour eviter une dependance circulaire : le filtre
     * a besoin du {@link UserDetailsService} declare ci-dessous.</p>
     *
     * @param http          builder Spring Security
     * @param jwtAuthFilter filtre d'authentification JWT
     * @return la chaine configuree
     * @throws Exception si la configuration echoue
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- Endpoints publics ---
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // --- Lecture : USER et ADMIN ---
                        .requestMatchers(HttpMethod.GET, "/api/lignes", "/api/lignes/**")
                            .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/stats/**")
                            .hasAnyRole("USER", "ADMIN")
                        // --- Mutations sur les lignes : ADMIN seulement ---
                        .requestMatchers(HttpMethod.POST, "/api/lignes", "/api/lignes/**")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/lignes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lignes/**").hasRole("ADMIN")
                        // --- Tout le reste ---
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt (force par defaut = 10).
     *
     * @return l'encodeur partage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Charge les utilisateurs depuis la table {@code USERS} (site global) et
     * mappe leur role applicatif vers une autorite {@code ROLE_*}.
     *
     * @param userRepository repository des comptes
     * @return service de chargement des utilisateurs
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(u -> org.springframework.security.core.userdetails.User.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur inconnu : " + username));
    }

    /**
     * Fournisseur d'authentification base sur le {@link UserDetailsService} et
     * l'encodeur BCrypt (verification des credentials au login).
     *
     * @param userDetailsService service de chargement des utilisateurs
     * @param passwordEncoder     encodeur BCrypt
     * @return le fournisseur configure
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Expose l'{@link AuthenticationManager} pour le {@link AuthController}.
     *
     * @param config configuration d'authentification de Spring
     * @return le gestionnaire d'authentification
     * @throws Exception si la recuperation echoue
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
