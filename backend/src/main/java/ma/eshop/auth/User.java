package ma.eshop.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Compte utilisateur applicatif, persiste dans la table {@code USERS} du
 * <strong>site GLOBAL</strong> Oracle (datasource primaire).
 *
 * <p>Le mot de passe est stocke <strong>hashe en BCrypt</strong> (jamais en
 * clair). Le role est une chaine simple {@code USER} ou {@code ADMIN} ;
 * l'adaptation vers les autorites Spring Security ({@code ROLE_*}) est faite
 * par le {@link SecurityConfig#userDetailsService(UserRepository)}.</p>
 */
@Entity
@Table(name = "USERS")
public class User {

    /** Identifiant technique (sequence IDENTITY Oracle). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Nom de connexion, unique. */
    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    /** Mot de passe hashe (BCrypt). */
    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    /** Role applicatif : {@code USER} ou {@code ADMIN}. */
    @Column(name = "ROLE", nullable = false, length = 20)
    private String role;

    /** Constructeur par defaut requis par JPA. */
    public User() {
    }

    /**
     * @param username nom de connexion
     * @param password mot de passe deja hashe
     * @param role     role applicatif (USER/ADMIN)
     */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /** @return l'identifiant technique */
    public Long getId() {
        return id;
    }

    /** @param id l'identifiant technique */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return le nom de connexion */
    public String getUsername() {
        return username;
    }

    /** @param username le nom de connexion */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return le mot de passe hashe */
    public String getPassword() {
        return password;
    }

    /** @param password le mot de passe hashe */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return le role applicatif */
    public String getRole() {
        return role;
    }

    /** @param role le role applicatif */
    public void setRole(String role) {
        this.role = role;
    }
}
