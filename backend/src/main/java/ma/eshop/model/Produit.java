package ma.eshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entite mappee sur la table maitre {@code PRODUITS} du SITE_GLOBAL.
 * Lecture seule. Porte la categorie ({@code idcateg}) utilisee par la
 * statistique de chiffre d'affaires par categorie.
 */
@Entity
@Table(name = "PRODUITS")
public class Produit {

    /** Identifiant du produit (cle primaire). */
    @Id
    @Column(name = "IDPRODUIT")
    private Long idproduit;

    /** Identifiant de la categorie du produit. */
    @Column(name = "IDCATEG")
    private Long idcateg;

    /** Libelle du produit. */
    @Column(name = "DESIGNATION")
    private String designation;

    /** Prix unitaire catalogue. */
    @Column(name = "PRIXUNITAIRE")
    private BigDecimal prixUnitaire;

    /** Unite de quantite (piece, lot, ...). */
    @Column(name = "UNITEQTE")
    private String uniteQte;

    /** Quantite en stock. */
    @Column(name = "QTESTOCK")
    private Integer qteStock;

    /** Constructeur par defaut requis par JPA. */
    public Produit() {
    }

    /** @return l'identifiant du produit */
    public Long getIdproduit() {
        return idproduit;
    }

    /** @param idproduit l'identifiant du produit */
    public void setIdproduit(Long idproduit) {
        this.idproduit = idproduit;
    }

    /** @return l'identifiant de la categorie */
    public Long getIdcateg() {
        return idcateg;
    }

    /** @param idcateg l'identifiant de la categorie */
    public void setIdcateg(Long idcateg) {
        this.idcateg = idcateg;
    }

    /** @return la designation */
    public String getDesignation() {
        return designation;
    }

    /** @param designation la designation */
    public void setDesignation(String designation) {
        this.designation = designation;
    }

    /** @return le prix unitaire */
    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    /** @param prixUnitaire le prix unitaire */
    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    /** @return l'unite de quantite */
    public String getUniteQte() {
        return uniteQte;
    }

    /** @param uniteQte l'unite de quantite */
    public void setUniteQte(String uniteQte) {
        this.uniteQte = uniteQte;
    }

    /** @return la quantite en stock */
    public Integer getQteStock() {
        return qteStock;
    }

    /** @param qteStock la quantite en stock */
    public void setQteStock(Integer qteStock) {
        this.qteStock = qteStock;
    }
}
