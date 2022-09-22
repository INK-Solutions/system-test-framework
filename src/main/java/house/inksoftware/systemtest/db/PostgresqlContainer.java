package house.inksoftware.systemtest.db;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class PostgresqlContainer {
    private static PostgreSQLContainer container;

    public static PostgreSQLContainer initialise() {
        if (container == null) {
            container = new PostgreSQLContainer("postgres:11.1");
            container.start();
            System.setProperty("DB_URL", container.getJdbcUrl());
            System.setProperty("DB_USERNAME", container.getUsername());
            System.setProperty("DB_PASSWORD", container.getPassword());
            log.info("Starting test database... jdbc: {}, user: {}, password: {}", container.getJdbcUrl(), container.getUsername(), container.getPassword());
        }
        return container;
    }

    public static void shutdown() {
        if (container == null) {
            log.info("Shutting down test database...");
            container.stop();
            container.close();
        }
    }
}

