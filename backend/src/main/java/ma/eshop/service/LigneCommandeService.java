package ma.eshop.service;

import java.math.BigDecimal;
import javax.sql.DataSource;
import ma.eshop.dto.LigneRequest;
import ma.eshop.dto.LigneUpdateRequest;
import ma.eshop.observability.MetricsConfig.ProcedureMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import java.sql.Types;

/**
 * Service de manipulation des lignes de commande.
 *
 * <p><strong>Regle d'or :</strong> toutes les ecritures passent par les
 * procedures stockees PL/SQL du site global via {@link SimpleJdbcCall}.
 * Aucun {@code INSERT/UPDATE/DELETE} JPA n'est emis. Le service ne decide
 * jamais du fragment cible : il appelle la procedure, et les triggers Oracle
 * (SYC_INSERT/DELETE/UPDATE_LIGNE) routent vers Site1 (>=100) ou Site2 (<100).</p>
 *
 * <p>Chaque appel est instrumente (latence + compteur + log JSON) via
 * {@link ProcedureMetrics}. Le site cible des metriques est <em>deduit</em> de
 * la quantite (seuil de routage 100), coherent avec la regle des triggers
 * Oracle — le backend ne decide toujours pas du routage reel.</p>
 */
@Service
public class LigneCommandeService {

    private static final Logger log = LoggerFactory.getLogger(LigneCommandeService.class);

    /** Seuil de routage : quantite >= 100 -> Site1, sinon Site2. */
    private static final int SEUIL_SITE1 = 100;

    private final ProcedureMetrics metrics;
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall insertCall;
    private final SimpleJdbcCall deleteCall;
    private final SimpleJdbcCall updateCall;

    /**
     * Prepare les trois {@link SimpleJdbcCall} sur le datasource global.
     * Les colonnes sont declarees explicitement
     * ({@code withoutProcedureColumnMetaDataAccess}) pour eviter une
     * introspection couteuse du dictionnaire Oracle a chaque appel.
     *
     * @param globalDataSource datasource du site global (procedures cibles)
     * @param metrics          porteur de metriques Micrometer
     */
    public LigneCommandeService(
            @Qualifier("globalDataSource") DataSource globalDataSource,
            JdbcTemplate jdbcTemplate,
            ProcedureMetrics metrics) {
        this.metrics = metrics;
        this.jdbcTemplate = jdbcTemplate;

        // ESHOP.INSERTLIGNE (wrapper) : (p_idligneCmd, p_idcommande, p_idproduit,
        // p_quantite, p_remise). 5 parametres IN — l'ordre des SqlParameter doit
        // suivre exactement celui de la procedure (binding positionnel car
        // withoutProcedureColumnMetaDataAccess()).
        this.insertCall = new SimpleJdbcCall(globalDataSource)
                .withProcedureName("INSERTLIGNE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_IDLIGNECMD", Types.NUMERIC),
                        new SqlParameter("P_IDCOMMANDE", Types.NUMERIC),
                        new SqlParameter("P_IDPRODUIT", Types.NUMERIC),
                        new SqlParameter("P_QUANTITE", Types.NUMERIC),
                        new SqlParameter("P_REMISE", Types.NUMERIC));

        // deleteligne(p_idligneCmd)
        this.deleteCall = new SimpleJdbcCall(globalDataSource)
                .withProcedureName("DELETELIGNE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_IDLIGNECMD", Types.NUMERIC));

        // updateligne(p_idligneCmd, p_idproduit, p_quantite, p_remise)
        this.updateCall = new SimpleJdbcCall(globalDataSource)
                .withProcedureName("UPDATELIGNE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_IDLIGNECMD", Types.NUMERIC),
                        new SqlParameter("P_IDPRODUIT", Types.NUMERIC),
                        new SqlParameter("P_QUANTITE", Types.NUMERIC),
                        new SqlParameter("P_REMISE", Types.NUMERIC));
    }

    /**
     * Cree une ligne de commande via la procedure {@code insertligne}.
     *
     * @param request donnees de la ligne (sans identifiant)
     * @return l'identifiant de ligne genere par Oracle
     */
    public Long inserer(LigneRequest request) {
        return metrics.record("insertligne", sitePour(request.quantite()), () -> {
            // L'id de ligne est genere cote Oracle (sequence ESHOP) sauf si le
            // client en fournit un explicitement. Le wrapper INSERTLIGNE l'attend
            // en premier parametre (P_IDLIGNECMD), il ne le genere pas lui-meme.
            Long idLigne = request.idligneCommande() != null
                    ? request.idligneCommande()
                    : jdbcTemplate.queryForObject("SELECT seq_ligne.NEXTVAL FROM dual", Long.class);
            log.info("INSERTLIGNE params : idLigne={}, idcommande={}, idproduit={}, quantite={}, remise={}",
                    idLigne, request.idcommande(), request.idproduit(), request.quantite(), request.remise());
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("P_IDLIGNECMD", idLigne);
            params.addValue("P_IDCOMMANDE", request.idcommande());
            params.addValue("P_IDPRODUIT", request.idproduit());
            params.addValue("P_QUANTITE", request.quantite());
            params.addValue("P_REMISE", request.remise() != null ? request.remise() : BigDecimal.ZERO);
            insertCall.execute(params);
            return idLigne;
        });
    }

    /**
     * Supprime une ligne de commande via la procedure {@code deleteligne}.
     * Le trigger Oracle propage la suppression vers le bon fragment et
     * nettoie la commande si elle devient orpheline.
     *
     * @param idLigne identifiant de la ligne a supprimer
     */
    public void supprimer(Long idLigne) {
        // La quantite (donc le site) n'est pas connue lors d'une suppression par id.
        metrics.record("deleteligne", "inconnu", () -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("P_IDLIGNECMD", idLigne);
            deleteCall.execute(params);
            return null;
        });
    }

    /**
     * Met a jour une ligne via la procedure {@code updateligne}. Si la nouvelle
     * quantite franchit le seuil 100, le trigger Oracle fait migrer la ligne
     * d'un fragment a l'autre — c'est transparent pour le backend.
     *
     * @param idLigne identifiant de la ligne a modifier
     * @param request nouvelles valeurs
     */
    public void modifier(Long idLigne, LigneUpdateRequest request) {
        metrics.record("updateligne", sitePour(request.quantite()), () -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("P_IDLIGNECMD", idLigne);
            params.addValue("P_IDPRODUIT", request.idproduit());
            params.addValue("P_QUANTITE", request.quantite());
            params.addValue("P_REMISE", request.remise() != null ? request.remise() : BigDecimal.ZERO);
            updateCall.execute(params);
            return null;
        });
    }

    /**
     * Deduit le site cible a partir de la quantite (meme regle que les triggers
     * Oracle), pour dimensionner les metriques {@code site=site1|site2}.
     *
     * @param quantite quantite de la ligne
     * @return {@code "site1"} si quantite >= 100, sinon {@code "site2"}
     */
    private static String sitePour(int quantite) {
        return quantite >= SEUIL_SITE1 ? "site1" : "site2";
    }
}
