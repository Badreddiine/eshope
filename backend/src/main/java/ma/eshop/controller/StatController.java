package ma.eshop.controller;

import java.math.BigDecimal;
import java.util.List;
import ma.eshop.dto.CaParCategorie;
import ma.eshop.dto.CommandesParClient;
import ma.eshop.repository.CommandeRepository;
import ma.eshop.repository.LigneCommandeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controleur des statistiques <strong>distribuees</strong>.
 *
 * <p>Ces endpoints declenchent des requetes qui agregent les deux fragments
 * (Site1 + Site2) via les database links du site global. Le backend ne fait
 * que transformer les lignes brutes {@code Object[]} en DTO typés.</p>
 */
@RestController
@RequestMapping("/api/stats")
public class StatController {

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    /**
     * @param commandeRepository      repository des commandes (stat par client)
     * @param ligneCommandeRepository repository des lignes (stat CA par categorie)
     */
    public StatController(CommandeRepository commandeRepository,
                          LigneCommandeRepository ligneCommandeRepository) {
        this.commandeRepository = commandeRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
    }

    /**
     * Nombre de commandes par client pour une annee (UNION ALL des deux sites).
     *
     * @param annee annee de filtrage (defaut 2026)
     * @return liste {@code (idclient, nbCommandes)}
     */
    @GetMapping("/commandes-par-client")
    public List<CommandesParClient> commandesParClient(
            @RequestParam(defaultValue = "2026") int annee) {
        return commandeRepository.countCommandesParClient(annee).stream()
                .map(row -> new CommandesParClient(
                        toLong(row[0]),
                        toLong(row[1])))
                .toList();
    }

    /**
     * Chiffre d'affaires par categorie pour une annee : somme distribuee des
     * contributions de Site1 et Site2.
     *
     * @param annee annee de filtrage (defaut 2026)
     * @return liste {@code (idcateg, caTotal)}
     */
    @GetMapping("/ca-par-categorie")
    public List<CaParCategorie> caParCategorie(
            @RequestParam(defaultValue = "2026") int annee) {
        return ligneCommandeRepository.chiffreAffaireParCategorie(annee).stream()
                .map(row -> new CaParCategorie(
                        toLong(row[0]),
                        toBigDecimal(row[1])))
                .toList();
    }

    /**
     * Convertit une valeur numerique Oracle (souvent {@link BigDecimal}) en Long.
     *
     * @param value valeur brute
     * @return la valeur en Long, ou {@code null}
     */
    private static Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    /**
     * Convertit une valeur numerique Oracle en {@link BigDecimal}.
     *
     * @param value valeur brute
     * @return la valeur en BigDecimal, ou {@code null}
     */
    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        return (value instanceof BigDecimal bd) ? bd : new BigDecimal(value.toString());
    }
}
