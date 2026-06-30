package net.deckserver.jpa;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public final class JpaFactory {

    private static final Logger logger = LoggerFactory.getLogger(JpaFactory.class);
    private static volatile EntityManagerFactory emf;
    private static volatile HikariDataSource dataSource;

    private JpaFactory() {}

    public static void initialize() {
        String url = System.getenv().getOrDefault("JOL_DB_URL",
                System.getProperty("jol.db.url", "jdbc:postgresql://localhost:5432/jol"));

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(System.getenv().getOrDefault("JOL_DB_USER", "jol"));
        hikariConfig.setPassword(System.getenv().getOrDefault("JOL_DB_PASSWORD", ""));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setPoolName("jol-pool");
        dataSource = new HikariDataSource(hikariConfig);

        runMigrations(dataSource);

        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.nonJtaDataSource", dataSource);
        props.put("hibernate.hbm2ddl.auto", "validate");
        emf = Persistence.createEntityManagerFactory("jol-pu", props);

        logger.info("JPA initialized against {}", url);
    }

    private static void runMigrations(DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        logger.info("Flyway migrations applied");
    }

    public static EntityManager createEntityManager() {
        if (emf == null) throw new IllegalStateException("JPA not initialized");
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        logger.info("JPA shut down");
    }
}
