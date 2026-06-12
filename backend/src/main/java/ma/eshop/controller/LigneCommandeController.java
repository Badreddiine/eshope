package ma.eshop.controller;

import jakarta.validation.Valid;
import java.util.Map;
import ma.eshop.dto.LigneRequest;
import ma.eshop.dto.LigneUpdateRequest;
import ma.eshop.model.LigneCommande;
import ma.eshop.repository.LigneCommandeRepository;
import ma.eshop.service.LigneCommandeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controleur REST des lignes de commande.
 *
 * <p>Separation stricte des responsabilites :</p>
 * <ul>
 *   <li>les mutations (POST/PUT/DELETE) delegate au {@link LigneCommandeService}
 *       qui appelle les procedures stockees ;</li>
 *   <li>la lecture paginee (GET) interroge directement le repository JPA
 *       (table maitre du site global).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/lignes")
public class LigneCommandeController {

    private static final Logger log = LoggerFactory.getLogger(LigneCommandeController.class);

    private final LigneCommandeService service;
    private final LigneCommandeRepository repository;

    /**
     * @param service    service d'ecriture (procedures stockees)
     * @param repository repository de lecture JPA
     */
    public LigneCommandeController(LigneCommandeService service,
                                   LigneCommandeRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    /**
     * Cree une ligne de commande. L'identifiant est genere cote Oracle et le
     * routage Site1/Site2 est decide par les triggers.
     *
     * @param request donnees de la ligne
     * @return 201 Created avec l'identifiant genere
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody LigneRequest request) {
        log.info("Création ligne : idcommande={}, idproduit={}, quantite={}",
                request.idcommande(), request.idproduit(), request.quantite());
        Long id = service.inserer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("idligneCommande", id, "message", "Ligne creee et routee par Oracle."));
    }

    /**
     * Supprime une ligne de commande via la procedure {@code deleteligne}.
     *
     * @param id identifiant de la ligne
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Met a jour une ligne via la procedure {@code updateligne}.
     *
     * @param id      identifiant de la ligne
     * @param request nouvelles valeurs
     * @return 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> modifier(@PathVariable Long id,
                                                        @Valid @RequestBody LigneUpdateRequest request) {
        service.modifier(id, request);
        return ResponseEntity.ok(Map.of("idligneCommande", id, "message", "Ligne mise a jour."));
    }

    /**
     * Liste paginee des lignes depuis la table maitre du site global.
     *
     * @param pageable pagination (defaut : 20 elements, tri par identifiant)
     * @return page de lignes de commande
     */
    @GetMapping
    public Page<LigneCommande> lister(
            @PageableDefault(size = 20, sort = "idligneCommande") Pageable pageable) {
        return repository.findAll(pageable);
    }
}
