package house.inksoftware.systemtest.domain.config.infra.mock;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.postgres.SystemTestPostgresLauncher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SystemTestMockedRestServerLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestPostgresLauncher.class);

    private final String relativePathToConfig;
    private GenericContainer container;


    @SneakyThrows
    @Override
    public void setup() {
        if (container == null) {
            File updatedConfigFile = new File("src/test/resources/" + relativePathToConfig);

            Map<String, String> envVariables = new HashMap<>();
            envVariables.put("MOCKSERVER_INITIALIZATION_JSON_PATH", "/config/" + updatedConfigFile.getName());

            container = new GenericContainer("mockserver/mockserver")
                    .withCopyFileToContainer(MountableFile.forHostPath(updatedConfigFile.getPath()), "/config/" + updatedConfigFile.getName())
                    .withEnv(envVariables)
                    .waitingFor(Wait.forLogMessage(".*started on port:.*", 1));

            container.setPortBindings(Arrays.asList("1080:1080/tcp"));
            container.start();
            LOGGER.info("Starting REST service mock...");
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
