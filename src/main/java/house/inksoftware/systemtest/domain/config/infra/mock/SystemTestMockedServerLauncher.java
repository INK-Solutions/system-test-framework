package house.inksoftware.systemtest.domain.config.infra.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.postgres.SystemTestPostgresLauncher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class SystemTestMockedServerLauncher implements SystemTestResourceLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestPostgresLauncher.class);

    private final TestRestTemplate restTemplate;
    private final String relativePathToConfig;
    private GenericContainer container;


    @SneakyThrows
    @Override
    public void setup() {
        if (container == null) {
            File updatedConfigFile = addStatusVerificationEndpoint(relativePathToConfig);

            Map<String, String> envVariables = new HashMap<>();
            envVariables.put("MOCKSERVER_INITIALIZATION_JSON_PATH", "/config/" + updatedConfigFile.getName());

            container = new GenericContainer("mockserver/mockserver")
                    .withCopyFileToContainer(MountableFile.forHostPath(updatedConfigFile.getPath()), "/config/" + updatedConfigFile.getName())
                    .withEnv(envVariables);

            container.setPortBindings(Arrays.asList("1080:1080/tcp"));
            container.start();
            waitUntilEndpointsAreAvailable();
            LOGGER.info("Starting test service mock...");
        }
    }

    private void waitUntilEndpointsAreAvailable() {
        try {
            await()
                    .atMost(Duration.of(50, ChronoUnit.SECONDS))
                    .pollInterval(Duration.of(1, ChronoUnit.SECONDS))
                    .until(() -> running("docker") || running("localhost"));
        } catch (Exception e) {
            throw new RuntimeException("MockServer API is not available, please check if your configuration is correct");
        }
    }

    private Boolean running(String domain) {
        String url = "http://" + domain + ":1080/api/mockserver/status";
        try {
            return restTemplate.getRestTemplate().exchange(URI.create(url), HttpMethod.GET, HttpEntity.EMPTY, String.class).getStatusCodeValue() == 200;
        } catch (Exception ex) {
            LOGGER.warn("Unable to find: {}", url);
        }
        return false;
    }

    private File addStatusVerificationEndpoint(String filePath) {
        try {
            String statusEndpoint = "/api/mockserver/status";
            String configFilePath = "src/test/resources/" + filePath;
            String jsonString = readJsonStringFromFile(configFilePath);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            JsonNode rootNode = objectMapper.readTree(jsonString);

            ObjectNode newElement = objectMapper.createObjectNode();
            ObjectNode httpRequest = objectMapper.createObjectNode();
            httpRequest.put("method", "GET");
            httpRequest.put("path", statusEndpoint);
            newElement.set("httpRequest", httpRequest);
            ObjectNode httpResponse = objectMapper.createObjectNode();
            httpResponse.put("body", "{\"status\": \"OK\"}");
            newElement.set("httpResponse", httpResponse);

            ArrayNode jsonArray = (ArrayNode) rootNode;
            jsonArray.add(newElement);

            String updatedJsonString = objectMapper.writeValueAsString(rootNode);

            return saveJsonStringToFile(updatedJsonString, configFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the JSON file: " + filePath + ", error: " + e.getMessage());
        }
    }

    private static String readJsonStringFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }

    private static File saveJsonStringToFile(String jsonString, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path newPath = Paths.get(path.getParent().toString(), "updated-mockserver-config.json");
        Files.write(newPath, jsonString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return newPath.toFile();
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
