package ma.eshop.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service de gestion des jetons JWT (signature et verification HS256).
 *
 * <p>La cle secrete et la duree de validite sont externalisees dans
 * {@code application.yml} ({@code jwt.secret}, {@code jwt.expiration}).
 * La cle doit faire au moins 256 bits (32 octets) pour HS256.</p>
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    /**
     * @param secret           cle secrete (>= 256 bits) ; acceptee en clair ou
     *                         en Base64
     * @param expirationMillis duree de validite du token en millisecondes
     */
    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expirationMillis) {
        this.signingKey = buildKey(secret);
        this.expirationMillis = expirationMillis;
    }

    /**
     * Genere un JWT signe HS256 pour l'utilisateur, avec son role en claim.
     *
     * @param user utilisateur authentifie
     * @return le token compact
     */
    public String generateToken(UserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .toList())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur (subject) du token.
     *
     * @param token jeton JWT
     * @return le nom d'utilisateur
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Verifie que le token correspond a l'utilisateur et n'est pas expire.
     *
     * @param token       jeton JWT
     * @param userDetails utilisateur de reference
     * @return {@code true} si le token est valide
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** @return la duree de validite configuree (ms) */
    public long getExpirationMillis() {
        return expirationMillis;
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    /**
     * Construit la cle de signature. Tente d'abord un decodage Base64 ; si la
     * chaine n'est pas du Base64 valide, retombe sur les octets UTF-8 bruts.
     */
    private static SecretKey buildKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
