package house.inksoftware.systemtest.domain.config.infra.mock;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.google.common.io.Resources;
import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.postgres.SystemTestPostgresLauncher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SystemTestMockedServerLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestPostgresLauncher.class);

    private final String relativePathToConfig;
    private final Integer warmupSeconds;
    private GenericContainer container;



    @SneakyThrows
    @Override
    public void setup() {
        if (container == null) {

            String fileName = relativePathToConfig.substring(relativePathToConfig.lastIndexOf("/"));

            Map<String, String> envVariables = new HashMap<>();
            envVariables.put("MOCKSERVER_INITIALIZATION_JSON_PATH", "/config/" + fileName);

            container = new GenericContainer("mockserver/mockserver")
                    .withCopyFileToContainer(MountableFile.forClasspathResource(relativePathToConfig), "/config/" + fileName)
                    .withEnv(envVariables);

            container.setPortBindings(Arrays.asList("1080:1080/tcp"));
            container.start();
            Thread.sleep(warmupSeconds * 1000);
            LOGGER.info("Starting test service mock...");
        }
    }

    @Override
    public void shutdown() {
        if (container == null) {
            LOGGER.info("Shutting down test service mock...");
            container.stop();
        }
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

}
