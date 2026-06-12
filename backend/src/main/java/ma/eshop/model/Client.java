package ma.eshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entite mappee sur la table maitre {@code CLIENTS} du SITE_GLOBAL.
 * Lecture seule.
 */
@Entity
@Table(name = "CLIENTS")
public class Client {

    /** Identifiant du client (cle primaire). */
    @Id
    @Column(name = "IDCLIENT")
    private Long idclient;

    /** Code client metier. */
    @Column(name = "CODECLIENT")
    private String codeclient;

    /** Raison sociale. */
    @Column(name = "SOCIETE")
    private String societe;

    /** Nom du contact. */
    @Column(name = "NOM")
    private String nom;

    /** Prenom du contact. */
    @Column(name = "PRENOM")
    private String prenom;

    /** Adresse postale. */
    @Column(name = "ADRESSE")
    private String adresse;

    /** Ville. */
    @Column(name = "VILLE")
    private String ville;

    /** Pays. */
    @Column(name = "PAYS")
    private String pays;

    /** Telephone. */
    @Column(name = "TEL")
    private String tel;

    /** Constructeur par defaut requis par JPA. */
    public Client() {
    }

    /** @return l'identifiant du client */
    public Long getIdclient() {
        return idclient;
    }

    /** @param idclient l'identifiant du client */
    public void setIdclient(Long idclient) {
        this.idclient = idclient;
    }

    /** @return le code client */
    public String getCodeclient() {
        return codeclient;
    }

    /** @param codeclient le code client */
    public void setCodeclient(String codeclient) {
        this.codeclient = codeclient;
    }

    /** @return la raison sociale */
    public String getSociete() {
        return societe;
    }

    /** @param societe la raison sociale */
    public void setSociete(String societe) {
        this.societe = societe;
    }

    /** @return le nom */
    public String getNom() {
        return nom;
    }

    /** @param nom le nom */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /** @return le prenom */
    public String getPrenom() {
        return prenom;
    }

    /** @param prenom le prenom */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /** @return l'adresse */
    public String getAdresse() {
        return adresse;
    }

    /** @param adresse l'adresse */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /** @return la ville */
    public String getVille() {
        return ville;
    }

    /** @param ville la ville */
    public void setVille(String ville) {
        this.ville = ville;
    }

    /** @return le pays */
    public String getPays() {
        return pays;
    }

    /** @param pays le pays */
    public void setPays(String pays) {
        this.pays = pays;
    }

    /** @return le telephone */
    public String getTel() {
        return tel;
    }

    /** @param tel le telephone */
    public void setTel(String tel) {
        this.tel = tel;
    }
}
