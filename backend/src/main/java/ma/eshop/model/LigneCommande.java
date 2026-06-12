package ma.eshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entite mappee sur la table maitre {@code LIGNECOMMANDES} du SITE_GLOBAL.
 *
 * <p>Utilisee en <strong>lecture seule</strong> : aucune ecriture JPA n'est
 * faite sur cette entite. Toute creation/modification/suppression passe par
 * les procedures stockees PL/SQL (voir
 * {@code ma.eshop.service.LigneCommandeService}). La fragmentation
 * Site1/Site2 est invisible ici : la table maitre globale reflete la vue
 * unifiee.</p>
 */
@Entity
@Table(name = "LIGNECOMMANDES")
public class LigneCommande {

    /** Identifiant de la ligne de commande (cle primaire). */
    @Id
    @Column(name = "IDLIGNECOMMANDE")
    private Long idligneCommande;

    /** Reference vers la commande parente. */
    @Column(name = "IDCOMMANDE")
    private Long idcommande;

    /** Reference vers le produit commande. */
    @Column(name = "IDPRODUIT")
    private Long idproduit;

    /** Quantite commandee (>=100 -> Site1, <100 -> Site2). */
    @Column(name = "QUANTITE")
    private Integer quantite;

    /** Prix unitaire applique (recopie depuis le produit par la procedure). */
    @Column(name = "PRIXUNITAIRE")
    private BigDecimal prixUnitaire;

    /** Remise appliquee (taux entre 0 et 1). */
    @Column(name = "REMISE")
    private BigDecimal remise;

    /** Constructeur par defaut requis par JPA. */
    public LigneCommande() {
    }

    /** @return l'identifiant de la ligne */
    public Long getIdligneCommande() {
        return idligneCommande;
    }

    /** @param idligneCommande l'identifiant de la ligne */
    public void setIdligneCommande(Long idligneCommande) {
        this.idligneCommande = idligneCommande;
    }

    /** @return l'identifiant de la commande parente */
    public Long getIdcommande() {
        return idcommande;
    }

    /** @param idcommande l'identifiant de la commande parente */
    public void setIdcommande(Long idcommande) {
        this.idcommande = idcommande;
    }

    /** @return l'identifiant du produit */
    public Long getIdproduit() {
        return idproduit;
    }

    /** @param idproduit l'identifiant du produit */
    public void setIdproduit(Long idproduit) {
        this.idproduit = idproduit;
    }

    /** @return la quantite commandee */
    public Integer getQuantite() {
        return quantite;
    }

    /** @param quantite la quantite commandee */
    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    /** @return le prix unitaire applique */
    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    /** @param prixUnitaire le prix unitaire applique */
    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    /** @return la remise appliquee */
    public BigDecimal getRemise() {
        return remise;
    }

    /** @param remise la remise appliquee */
    public void setRemise(BigDecimal remise) {
        this.remise = remise;
    }
}
