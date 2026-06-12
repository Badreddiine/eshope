package ma.eshop.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions REST.
 *
 * <p>Traduit notamment les erreurs PL/SQL levees par les procedures stockees
 * ({@code RAISE_APPLICATION_ERROR}, violations de contrainte, FK manquante)
 * en reponses JSON exploitables par le client, plutot qu'en stacktrace 500.</p>
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Gere les erreurs de validation des bodies (@Valid).
     *
     * @param ex exception de validation
     * @return 400 Bad Request avec le detail des champs invalides
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> erreurs.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(corps(HttpStatus.BAD_REQUEST,
                "Validation echouee", erreurs.toString(), null));
    }

    /**
     * Gere les echecs d'authentification (mauvais identifiants au login).
     *
     * @param ex exception d'authentification
     * @return 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(corps(HttpStatus.UNAUTHORIZED,
                "Authentification echouee", "Identifiants invalides.", null));
    }

    /**
     * Gere les erreurs remontees par Oracle (procedures, contraintes, db links).
     *
     * @param ex      exception d'acces aux donnees
     * @param request requete a l'origine de l'erreur
     * @return 422 Unprocessable Entity avec le message Oracle
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex,
                                                                HttpServletRequest request) {
        String message = ex.getMostSpecificCause().getMessage();
        return ResponseEntity.unprocessableEntity().body(corps(HttpStatus.UNPROCESSABLE_ENTITY,
                "Erreur base de donnees", message, request.getRequestURI()));
    }

    /**
     * Construit le corps JSON standard d'erreur.
     *
     * @param status  statut HTTP
     * @param erreur  libelle court
     * @param message detail
     * @param chemin  URI concernee (peut etre {@code null})
     * @return la map serialisee en JSON
     */
    private Map<String, Object> corps(HttpStatus status, String erreur, String message, String chemin) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", erreur);
        body.put("message", message);
        if (chemin != null) {
            body.put("path", chemin);
        }
        return body;
    }
}
