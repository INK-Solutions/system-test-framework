package house.inksoftware.systemtest.domain.config.infra.mock;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class SystemTestMockedGrpcServerLauncher implements SystemTestResourceLauncher {

    private final String protoDirPath;
    private GenericContainer container;

    @SneakyThrows
    @Override
    public void setup() {
        if (container == null) {

            var protoFiles = readContracts();

            log.info("There are {} proto files.", protoFiles);
            container = new GenericContainer("tkpd/gripmock")
                    .withCopyFileToContainer(MountableFile.forHostPath(protoDirPath), "/proto")
                    .waitingFor(Wait.forLogMessage(".*Serving gRPC on.*", 1));

            container.setPortBindings(Arrays.asList("4770:4770/tcp", "4771:4771/tcp"));

            container.withCommand(protoFiles);

            container.start();
            log.info("Mocked service started!");
        }
    }

    private String[] readContracts() throws IOException {
        return Files
                .walk(Paths.get(protoDirPath))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".proto"))
                .map(entry -> "/proto/" + entry.getFileName().toString())
                .toArray(String[]::new);
    }


    @Override
    public void shutdown() {
        if (container == null) {
            log.info("Shutting down test service mock...");
            container.stop();
        }
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

}
