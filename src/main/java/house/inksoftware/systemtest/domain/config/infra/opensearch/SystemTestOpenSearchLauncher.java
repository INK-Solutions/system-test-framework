package house.inksoftware.systemtest.domain.config.infra.opensearch;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import lombok.SneakyThrows;
import org.opensearch.testcontainers.OpensearchContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;

import java.net.URL;

public class SystemTestOpenSearchLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestOpenSearchLauncher.class);

    private static OpensearchContainer container;
    private final String image;

    public SystemTestOpenSearchLauncher(String image) {
        this.image = image;
    }

    @SneakyThrows
    @Override
    public void setup() {
        if (container == null) {
            container = new OpensearchContainer<>(DockerImageName.parse(image));

            container.start();
            URL url = new URL(container.getHttpHostAddress());

            // Getting the host and port
            System.setProperty("OPEN_SEARCH_HOST", url.getHost());
            System.setProperty("OPEN_SEARCH_PORT", String.valueOf(url.getPort()));
            System.setProperty("OPEN_SEARCH_USERNAME", container.getUsername());
            System.setProperty("OPEN_SEARCH_PASSWORD", container.getPassword());
            LOGGER.info(
                    "Open search started... host address: {}, user: {}, password: {}",
                    container.getHttpHostAddress(), container.getUsername(), container.getPassword()
            );
        }
    }

    @Override
    public void shutdown() {
        if (container != null) {
            LOGGER.info("Shutting down open search...");
            container.stop();
            container.close();
        }
    }

    @Override
    public Type type() {
        return Type.DATABASE;
    }
}
