package ma.eshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entite mappee sur la table maitre {@code COMMANDES} du SITE_GLOBAL.
 * Lecture seule (les en-tetes de commande sont geres par les scripts SQL).
 */
@Entity
@Table(name = "COMMANDES")
public class Commande {

    /** Identifiant de la commande (cle primaire). */
    @Id
    @Column(name = "IDCOMMANDE")
    private Long idcommande;

    /** Identifiant de l'employe ayant saisi la commande. */
    @Column(name = "IDEMPLOYE")
    private Long idemploye;

    /** Reference vers le client (FK logique). */
    @Column(name = "IDCLIENT")
    private Long idclient;

    /** Date de la commande. */
    @Column(name = "DATECOMMANDE")
    private LocalDate dateCommande;

    /** Date de livraison prevue. */
    @Column(name = "DATELIVRAISON")
    private LocalDate dateLivraison;

    /** Frais de port. */
    @Column(name = "PORT")
    private BigDecimal port;

    /** Constructeur par defaut requis par JPA. */
    public Commande() {
    }

    /** @return l'identifiant de la commande */
    public Long getIdcommande() {
        return idcommande;
    }

    /** @param idcommande l'identifiant de la commande */
    public void setIdcommande(Long idcommande) {
        this.idcommande = idcommande;
    }

    /** @return l'identifiant de l'employe */
    public Long getIdemploye() {
        return idemploye;
    }

    /** @param idemploye l'identifiant de l'employe */
    public void setIdemploye(Long idemploye) {
        this.idemploye = idemploye;
    }

    /** @return l'identifiant du client */
    public Long getIdclient() {
        return idclient;
    }

    /** @param idclient l'identifiant du client */
    public void setIdclient(Long idclient) {
        this.idclient = idclient;
    }

    /** @return la date de commande */
    public LocalDate getDateCommande() {
        return dateCommande;
    }

    /** @param dateCommande la date de commande */
    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    /** @return la date de livraison */
    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    /** @param dateLivraison la date de livraison */
    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    /** @return les frais de port */
    public BigDecimal getPort() {
        return port;
    }

    /** @param port les frais de port */
    public void setPort(BigDecimal port) {
        this.port = port;
    }
}
