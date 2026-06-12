package ma.eshop.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre de correlation des logs : attribue un {@code traceId} a chaque requete
 * HTTP et le place dans le MDC, de sorte que toutes les lignes de log JSON
 * emises pendant le traitement (y compris les appels de procedures, qui ajoutent
 * {@code procedureName}, {@code siteName}, {@code durationMs}) partagent le meme
 * identifiant de trace.
 *
 * <p>L'encodage JSON et les niveaux de log (INFO global, DEBUG sur
 * {@code ma.eshop.service}) sont definis dans {@code logback-spring.xml}.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingConfig extends OncePerRequestFilter {

    /** Cle MDC du correlateur de trace. */
    public static final String TRACE_ID = "traceId";

    /** En-tete HTTP permettant de propager un traceId fourni par l'appelant. */
    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(TRACE_ID, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
