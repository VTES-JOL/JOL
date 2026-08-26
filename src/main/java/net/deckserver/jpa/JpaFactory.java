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
        // Explicit driver class: deployed as a real WAR in standalone Tomcat, DriverManager's
        // automatic ServiceLoader-based driver registration is unreliable (a JVM-wide singleton,
        // its one-time driver scan runs against whatever classloader first touches it) and can
        // fail with "No suitable driver" even though postgresql.jar is on the webapp classpath.
        // Setting this forces HikariCP to Class.forName() it directly instead of relying on that.
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(System.getenv().getOrDefault("JOL_DB_USER", "jol"));
        hikariConfig.setPassword(System.getenv().getOrDefault("JOL_DB_PASSWORD", ""));
        hikariConfig.setMaximumPoolSize(Integer.parseInt(System.getenv().getOrDefault("JOL_DB_POOL_SIZE", "10")));
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

    public static void initializeWithEmf(EntityManagerFactory providedEmf) {
        emf = providedEmf;
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
