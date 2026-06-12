package ma.eshop.controller;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controleur de sante applicatif des trois instances Oracle.
 *
 * <p>Complete {@code /actuator/health} en pingant <strong>individuellement</strong>
 * chaque datasource (global, site1, site2) : utile pour diagnostiquer quel
 * fragment est indisponible dans l'architecture distribuee.</p>
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource globalDataSource;
    private final DataSource site1DataSource;
    private final DataSource site2DataSource;

    /**
     * @param globalDataSource datasource du site global
     * @param site1DataSource  datasource du fragment Site1
     * @param site2DataSource  datasource du fragment Site2
     */
    public HealthController(
            @Qualifier("globalDataSource") DataSource globalDataSource,
            @Qualifier("site1DataSource") DataSource site1DataSource,
            @Qualifier("site2DataSource") DataSource site2DataSource) {
        this.globalDataSource = globalDataSource;
        this.site1DataSource = site1DataSource;
        this.site2DataSource = site2DataSource;
    }

    /**
     * Ping de chaque site. Renvoie 200 si les trois repondent, sinon 503.
     *
     * @return etat ("UP"/"DOWN") par site et statut global
     */
    @GetMapping("/sites")
    public ResponseEntity<Map<String, Object>> sites() {
        Map<String, Object> status = new LinkedHashMap<>();
        boolean globalUp = ping(globalDataSource);
        boolean site1Up = ping(site1DataSource);
        boolean site2Up = ping(site2DataSource);

        status.put("global", globalUp ? "UP" : "DOWN");
        status.put("site1", site1Up ? "UP" : "DOWN");
        status.put("site2", site2Up ? "UP" : "DOWN");

        boolean allUp = globalUp && site1Up && site2Up;
        status.put("global_status", allUp ? "UP" : "DEGRADED");

        return ResponseEntity
                .status(allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(status);
    }

    /**
     * Verifie la validite d'une connexion (timeout 2 s).
     *
     * @param dataSource datasource a tester
     * @return {@code true} si la connexion est valide
     */
    private boolean ping(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
