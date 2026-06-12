package ma.eshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import ma.eshop.dto.LigneRequest;
import ma.eshop.dto.LigneUpdateRequest;
import ma.eshop.observability.MetricsConfig.ProcedureMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * Tests unitaires du {@link LigneCommandeService}.
 *
 * <p>Le service construit lui-meme ses trois {@link SimpleJdbcCall} dans son
 * constructeur. On intercepte donc ces instanciations avec
 * {@link Mockito#mockConstruction(Class, org.mockito.MockedConstruction.MockInitializer)}
 * (mock maker "inline" actif par defaut en Mockito 5) : chaque appel
 * {@code new SimpleJdbcCall(...)} renvoie un mock dont les methodes de
 * configuration (builder fluent) se renvoient elles-memes, et dont
 * {@code execute(...)} renvoie une reponse simulee.</p>
 *
 * <p>Les metriques utilisent un vrai {@link SimpleMeterRegistry} : inutile de
 * mocker {@link io.micrometer.core.instrument.Timer}/{@code Counter}, et le
 * {@code Timer.record(Supplier)} execute reellement l'appel JDBC simule.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LigneCommandeServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private final ProcedureMetrics metrics = new ProcedureMetrics(new SimpleMeterRegistry());

    /** Configure chaque SimpleJdbcCall mocke : builder fluent + execute simule. */
    private MockedConstruction<SimpleJdbcCall> mockJdbcCalls(Map<String, Object> executeResult) {
        return mockConstruction(SimpleJdbcCall.class, (mock, context) -> {
            when(mock.withProcedureName(any())).thenReturn(mock);
            when(mock.withoutProcedureColumnMetaDataAccess()).thenReturn(mock);
            when(mock.declareParameters(any(SqlParameter[].class))).thenReturn(mock);
            when(mock.execute(any(SqlParameterSource.class))).thenReturn(executeResult);
        });
    }

    @Test
    void inserer_appelleInsertligne_etRetourneIdGenereParOracle() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockJdbcCalls(Map.of())) {
            // L'id de ligne provient de la sequence ESHOP (seq_ligne.NEXTVAL).
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(42L);

            LigneCommandeService service = new LigneCommandeService(dataSource, jdbcTemplate, metrics);
            Long id = service.inserer(new LigneRequest(null, 1000L, 10L, 150, new BigDecimal("0.05")));

            assertThat(id).isEqualTo(42L);

            // Les 3 SimpleJdbcCall sont construits dans l'ordre insert/delete/update
            SimpleJdbcCall insertCall = mocked.constructed().get(0);
            ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(insertCall).execute(params.capture());
            MapSqlParameterSource captured = params.getValue();
            assertThat(captured.getValue("P_IDLIGNECMD")).isEqualTo(42L);
            assertThat(captured.getValue("P_IDCOMMANDE")).isEqualTo(1000L);
            assertThat(captured.getValue("P_IDPRODUIT")).isEqualTo(10L);
            assertThat(captured.getValue("P_QUANTITE")).isEqualTo(150);
            assertThat(captured.getValue("P_REMISE")).isEqualTo(new BigDecimal("0.05"));
        }
    }

    @Test
    void inserer_remiseNull_envoieZero() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockJdbcCalls(Map.of())) {
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(7L);

            LigneCommandeService service = new LigneCommandeService(dataSource, jdbcTemplate, metrics);
            service.inserer(new LigneRequest(null, 1L, 2L, 50, null));

            ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(mocked.constructed().get(0)).execute(params.capture());
            assertThat(params.getValue().getValue("P_REMISE")).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Test
    void supprimer_appelleDeleteligneAvecId() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockJdbcCalls(Map.of())) {

            LigneCommandeService service = new LigneCommandeService(dataSource, jdbcTemplate, metrics);
            service.supprimer(99L);

            SimpleJdbcCall deleteCall = mocked.constructed().get(1);
            ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(deleteCall).execute(params.capture());
            assertThat(params.getValue().getValue("P_IDLIGNECMD")).isEqualTo(99L);
        }
    }

    @Test
    void modifier_appelleUpdateligneAvecNouvellesValeurs() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockJdbcCalls(Map.of())) {

            LigneCommandeService service = new LigneCommandeService(dataSource, jdbcTemplate, metrics);
            service.modifier(5L, new LigneUpdateRequest(20L, 80, new BigDecimal("0.10")));

            SimpleJdbcCall updateCall = mocked.constructed().get(2);
            ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
            verify(updateCall).execute(params.capture());
            MapSqlParameterSource captured = params.getValue();
            assertThat(captured.getValue("P_IDLIGNECMD")).isEqualTo(5L);
            assertThat(captured.getValue("P_IDPRODUIT")).isEqualTo(20L);
            assertThat(captured.getValue("P_QUANTITE")).isEqualTo(80);
            assertThat(captured.getValue("P_REMISE")).isEqualTo(new BigDecimal("0.10"));
        }
    }

    @Test
    void troisCallsSontPreparesAuDemarrage() {
        try (MockedConstruction<SimpleJdbcCall> mocked = mockJdbcCalls(Map.of())) {
            new LigneCommandeService(dataSource, jdbcTemplate, metrics);

            List<SimpleJdbcCall> calls = mocked.constructed();
            assertThat(calls).hasSize(3);
        }
    }
}
