package house.inksoftware.systemtest.domain.config.infra.db.redis;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;

public class SystemTestRedisLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestRedisLauncher.class);

    private static GenericContainer container;
    private final String image;

    public SystemTestRedisLauncher(String image) {
        this.image = image;
    }

    @Override
    public void setup() {
        if (container == null) {
            container = new GenericContainer<>(DockerImageName.parse(image));
            container.addExposedPort(6379);
            container.setPortBindings(Arrays.asList("6379:6379/tcp"));
            container.start();
            LOGGER.info("Starting redis database... ");
        }
    }

    @Override
    public void shutdown() {
        if (container == null) {
            LOGGER.info("Shutting down redis database...");
            container.stop();
            container.close();
        }
    }

    @Override
    public Type type() {
        return Type.DATABASE;
    }
}
