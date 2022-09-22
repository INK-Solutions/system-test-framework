package house.inksoftware.systemtest.configuration.infrastructure.postgres;

import com.google.common.base.Preconditions;
import house.inksoftware.systemtest.configuration.infrastructure.SystemTestResourceLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import static java.util.Objects.nonNull;

public class SystemTestPostgresLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestPostgresLauncher.class);

    private final PostgreSQLContainer container;
    private final SystemTestPostgresConfiguration postgresConfiguration;

    public SystemTestPostgresLauncher(PostgreSQLContainer container, SystemTestPostgresConfiguration postgresConfiguration) {
        this.container = container;
        this.postgresConfiguration = postgresConfiguration;
    }

    @Override
    public void setup() {
        Preconditions.checkState(nonNull(container), "Container not initialized");
        postgresConfiguration.getInitialDataPopulation().populate();
    }

    @Override
    public void shutdown() {
        if (container != null) {
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

