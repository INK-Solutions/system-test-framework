package house.inksoftware.systemtest.configuration.infrastructure.postgres;

import house.inksoftware.systemtest.configuration.infrastructure.SystemTestResourceLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class SystemTestPostgresLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestPostgresLauncher.class);

    private static PostgreSQLContainer container;
    private final SystemTestPostgresConfiguration postgresConfiguration;

    public SystemTestPostgresLauncher(SystemTestPostgresConfiguration postgresConfiguration) {
        this.postgresConfiguration = postgresConfiguration;
    }

    @Override
    public void setup() {
        if (container == null) {
            DockerImageName myImage = DockerImageName.parse(postgresConfiguration.getImage()).asCompatibleSubstituteFor("postgres");
            container = new PostgreSQLContainer(myImage);
            container.start();
            System.setProperty("DB_URL", container.getJdbcUrl());
            System.setProperty("DB_USERNAME", container.getUsername());
            System.setProperty("DB_PASSWORD", container.getPassword());
            LOGGER.info(
                    "Starting test database... jdbc: {}, user: {}, password: {}",
                    container.getJdbcUrl(), container.getUsername(), container.getPassword()
            );

            postgresConfiguration.getInitialDataPopulation().populate();
        }
    }

    @Override
    public void shutdown() {
        if (container == null) {
            LOGGER.info("Shutting down test database...");
            container.stop();
            container.close();
        }
    }

    @Override
    public Type type() {
        return Type.DATABASE;
    }
}

