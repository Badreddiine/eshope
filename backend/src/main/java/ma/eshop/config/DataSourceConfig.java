package ma.eshop.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration des trois sources de donnees Oracle de l'architecture
 * distribuee EShop.
 *
 * <p>Strategie :</p>
 * <ul>
 *   <li><strong>global</strong> ({@link Primary}) : porte la couche JPA
 *       (lectures) ET sert de cible aux appels de procedures stockees ;</li>
 *   <li><strong>site1</strong> / <strong>site2</strong> : sources secondaires
 *       utilisees uniquement par le health-check pour verifier la
 *       connectivite de chaque fragment.</li>
 * </ul>
 *
 * <p>Comme plusieurs {@link DataSource} coexistent, on declare explicitement
 * l'{@link EntityManagerFactory} et le {@link PlatformTransactionManager}
 * primaires, lies au datasource global, et on cantonne les repositories JPA
 * au package {@code ma.eshop.repository}.</p>
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {"ma.eshop.repository", "ma.eshop.auth"},
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager")
public class DataSourceConfig {

    /**
     * Source de donnees principale : site global (port 1521).
     * Liee aux proprietes {@code datasource.global.*} de application.yml.
     *
     * @return pool HikariCP vers le site global
     */
    @Bean
    @Primary
    @ConfigurationProperties("datasource.global")
    public HikariDataSource globalDataSource() {
        return new HikariDataSource();
    }

    /**
     * Source de donnees du fragment Site1 (gros volumes, port 1522).
     *
     * @return pool HikariCP vers Site1
     */
    @Bean
    @ConfigurationProperties("datasource.site1")
    public HikariDataSource site1DataSource() {
        return new HikariDataSource();
    }

    /**
     * Source de donnees du fragment Site2 (petits volumes, port 1523).
     *
     * @return pool HikariCP vers Site2
     */
    @Bean
    @ConfigurationProperties("datasource.site2")
    public HikariDataSource site2DataSource() {
        return new HikariDataSource();
    }

    /**
     * Fabrique d'EntityManager JPA primaire, branchee sur le datasource global
     * et scrutant les entites du package {@code ma.eshop.model}.
     *
     * @param builder          fabrique fournie par Spring Boot
     * @param globalDataSource datasource global
     * @return la fabrique d'EntityManager
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("globalDataSource") DataSource globalDataSource) {
        return builder
                .dataSource(globalDataSource)
                .packages("ma.eshop.model", "ma.eshop.auth")
                .persistenceUnit("global")
                .build();
    }

    /**
     * Gestionnaire de transactions JPA primaire, adosse a la fabrique
     * d'EntityManager du site global.
     *
     * @param entityManagerFactory fabrique d'EntityManager primaire
     * @return le gestionnaire de transactions
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
