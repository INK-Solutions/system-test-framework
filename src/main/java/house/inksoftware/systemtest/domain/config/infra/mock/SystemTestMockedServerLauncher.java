package house.inksoftware.systemtest.domain.config.infra.mock;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.postgres.SystemTestPostgresLauncher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class SystemTestMockedServerLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestPostgresLauncher.class);

    private final String relativePathToConfig;
    private final Integer warmupSeconds;

    private final boolean reuseContainer;
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
            container.withReuse(reuseContainer);
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
