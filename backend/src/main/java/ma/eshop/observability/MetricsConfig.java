package ma.eshop.observability;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Observabilite des appels de procedures stockees et des pools de connexions.
 *
 * <p>Metriques exposees via {@code /actuator/prometheus} :</p>
 * <ul>
 *   <li>{@code eshop.procedure.calls{procedure, site}} — compteur d'appels
 *       reussis, dimensionne par procedure et par site cible ;</li>
 *   <li>{@code eshop.procedure.duration{procedure}} — chronometre (histogramme
 *       de percentiles active pour permettre un p95 cote Prometheus) ;</li>
 *   <li>{@code eshop.procedure.errors} — compteur global d'echecs ;</li>
 *   <li>{@code eshop.db.connections.active{datasource}} — jauge des connexions
 *       HikariCP actives par datasource (global/site1/site2).</li>
 * </ul>
 */
@Configuration
public class MetricsConfig {

    /**
     * Porteur d'instrumentation des procedures stockees.
     *
     * @param registry registre Micrometer fourni par Spring Boot
     * @return le bean d'instrumentation
     */
    @Bean
    public ProcedureMetrics procedureMetrics(MeterRegistry registry) {
        return new ProcedureMetrics(registry);
    }

    /**
     * Active l'histogramme de percentiles pour le timer des procedures, afin
     * que Prometheus expose les buckets {@code eshop_procedure_duration_seconds_bucket}
     * necessaires a {@code histogram_quantile(0.95, ...)}.
     *
     * <p>Les beans {@link MeterFilter} sont appliques automatiquement par
     * Spring Boot a tous les {@link MeterRegistry}.</p>
     *
     * @return le filtre de configuration des statistiques de distribution
     */
    @Bean
    public MeterFilter procedureDurationHistogram() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id,
                                                         DistributionStatisticConfig config) {
                if ("eshop.procedure.duration".equals(id.getName())) {
                    return DistributionStatisticConfig.builder()
                            .percentilesHistogram(true)
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }

    /**
     * Enregistre une jauge du nombre de connexions HikariCP actives pour chacun
     * des trois datasources. Les beans {@link MeterBinder} sont lies au registre
     * automatiquement par Spring Boot.
     *
     * @param global datasource du site global
     * @param site1  datasource du fragment Site1
     * @param site2  datasource du fragment Site2
     * @return le binder qui declare les trois jauges
     */
    @Bean
    public MeterBinder hikariConnectionGauges(
            @Qualifier("globalDataSource") HikariDataSource global,
            @Qualifier("site1DataSource") HikariDataSource site1,
            @Qualifier("site2DataSource") HikariDataSource site2) {
        return registry -> {
            registerActiveConnections(registry, global, "global");
            registerActiveConnections(registry, site1, "site1");
            registerActiveConnections(registry, site2, "site2");
        };
    }

    private static void registerActiveConnections(MeterRegistry registry,
                                                  HikariDataSource dataSource, String name) {
        Gauge.builder("eshop.db.connections.active", dataSource,
                        ds -> ds.getHikariPoolMXBean() != null
                                ? ds.getHikariPoolMXBean().getActiveConnections()
                                : 0)
                .description("Connexions HikariCP actives")
                .tag("datasource", name)
                .register(registry);
    }

    /**
     * Instrumentation centralisee d'un appel de procedure stockee : chronometre,
     * compteurs (succes/erreur) et journalisation structuree (MDC).
     */
    public static class ProcedureMetrics {

        private static final Logger log = LoggerFactory.getLogger(ProcedureMetrics.class);

        private final MeterRegistry registry;
        private final Counter errorCounter;

        /**
         * @param registry registre Micrometer
         */
        public ProcedureMetrics(MeterRegistry registry) {
            this.registry = registry;
            this.errorCounter = Counter.builder("eshop.procedure.errors")
                    .description("Nombre d'echecs d'appel aux procedures stockees")
                    .register(registry);
        }

        /**
         * Execute l'action en la chronometrant ; en cas de succes, incremente le
         * compteur d'appels {@code (procedure, site)} et journalise une ligne JSON
         * enrichie par MDC ({@code procedureName}, {@code siteName},
         * {@code durationMs}). En cas d'echec, incremente le compteur d'erreurs.
         *
         * @param procedure nom de la procedure (insertligne/deleteligne/updateligne)
         * @param site      site cible (site1/site2, ou "inconnu" si non determinable)
         * @param action    appel JDBC a executer
         * @param <T>       type de retour
         * @return la valeur produite par l'action
         */
        public <T> T record(String procedure, String site, Supplier<T> action) {
            long start = System.nanoTime();
            MDC.put("procedureName", procedure);
            MDC.put("siteName", site);
            try {
                T result = action.get();
                long elapsedNanos = System.nanoTime() - start;
                registry.timer("eshop.procedure.duration", "procedure", procedure)
                        .record(Duration.ofNanos(elapsedNanos));
                registry.counter("eshop.procedure.calls", "procedure", procedure, "site", site)
                        .increment();
                long ms = elapsedNanos / 1_000_000;
                MDC.put("durationMs", String.valueOf(ms));
                log.info("Procedure {} executee (site={}, duree={} ms)", procedure, site, ms);
                return result;
            } catch (RuntimeException ex) {
                errorCounter.increment();
                log.error("Echec de la procedure {} (site={})", procedure, site, ex);
                throw ex;
            } finally {
                MDC.remove("procedureName");
                MDC.remove("siteName");
                MDC.remove("durationMs");
            }
        }
    }
}
