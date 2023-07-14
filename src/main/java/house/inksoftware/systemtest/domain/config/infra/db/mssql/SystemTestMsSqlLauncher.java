package house.inksoftware.systemtest.domain.config.infra.db.mssql;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

public class SystemTestMsSqlLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestMsSqlLauncher.class);

    private static MSSQLServerContainer container;
    private final String image;

    public SystemTestMsSqlLauncher(String image) {
        this.image = image;
    }

    @Override
    public void setup() {
        if (container == null) {
            DockerImageName myImage = DockerImageName.parse(image).asCompatibleSubstituteFor("mcr.microsoft.com/mssql/server");
            container = new MSSQLServerContainer(myImage)
                    .acceptLicense();
            container.start();
            System.setProperty("DB_URL", container.getJdbcUrl());
            System.setProperty("DB_USERNAME", container.getUsername());
            System.setProperty("DB_PASSWORD", container.getPassword());
            LOGGER.info(
                    "Starting test database... jdbc: {}, user: {}, password: {}",
                    container.getJdbcUrl(), container.getUsername(), container.getPassword()
            );
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

