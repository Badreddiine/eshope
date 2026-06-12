package ma.eshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree de l'application backend EShop.
 *
 * <p>Cette application expose une API REST au-dessus d'une base de donnees
 * Oracle <strong>distribuee sur 3 instances</strong> :</p>
 * <ul>
 *   <li>les <em>ecritures</em> de lignes de commande passent exclusivement par
 *       les procedures stockees PL/SQL du site global (qui declenchent le
 *       routage Site1/Site2 via triggers Oracle) ;</li>
 *   <li>les <em>lectures</em> simples utilisent JPA sur le site global ;</li>
 *   <li>les <em>statistiques distribuees</em> s'appuient sur les database links
 *       (requetes UNION ALL sur les fragments).</li>
 * </ul>
 */
@SpringBootApplication
public class EshopBackendApplication {

    /**
     * Demarre le contexte Spring Boot.
     *
     * @param args arguments de ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        SpringApplication.run(EshopBackendApplication.class, args);
    }
}
