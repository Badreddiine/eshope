package ma.eshop.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository JPA des comptes utilisateurs (table {@code USERS}, site global).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son nom de connexion.
     *
     * @param username nom de connexion
     * @return l'utilisateur s'il existe
     */
    Optional<User> findByUsername(String username);

    /**
     * @param username nom de connexion
     * @return {@code true} si un compte porte deja ce nom
     */
    boolean existsByUsername(String username);
}
