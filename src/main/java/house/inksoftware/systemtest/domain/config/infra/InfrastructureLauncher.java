package house.inksoftware.systemtest.domain.config.infra;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.config.infra.db.SystemTestDatabasePopulatorLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.mssql.SystemTestMsSqlLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.postgres.SystemTestPostgresLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.redis.SystemTestRedisLauncher;
import house.inksoftware.systemtest.domain.config.infra.kafka.KafkaConfigurationFactory;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.config.infra.mock.SystemTestMockedServerLauncher;
import house.inksoftware.systemtest.domain.config.infra.rest.RestConfigurationFactory;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.TestContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.nonNull;

public class InfrastructureLauncher {
    public static final Integer MOCKED_SERVER_DEFAULT_WARMUP_SECONDS = 5;
    private final List<SystemTestResourceLauncher> resources = new ArrayList<>();

    static {
        configureTestcontainers();
    }

    public void launchDb(TestContext testContext, LinkedHashMap<String, Object> config) {
        LinkedHashMap<String, String> properties = (LinkedHashMap) config.get("database");
        launchDatabase(properties.get("type"), properties.get("image"), isContainerReuseEnabled(config));

        String testDataScriptsPath = properties.get("testDataScriptsPath");
        if (testDataScriptsPath != null && testContext != null) {
            new SystemTestDatabasePopulatorLauncher(testDataScriptsPath, testContext.getApplicationContext().getBean(DataSource.class)).setup();
        }
    }

    public SystemTestConfiguration launchAllInfra(EmbeddedKafkaBroker kafkaBroker,
                                                  KafkaEventProcessedCallback kafkaEventProcessedCallback,
                                                  TestRestTemplate restTemplate,
                                                  Integer port,
                                                  LinkedHashMap<String, Object> config) {
        SystemTestConfiguration result = new SystemTestConfiguration();
        result.setRestConfiguration(RestConfigurationFactory.create(restTemplate, port));
        config.forEach((key, value) -> {
            if (key.equals("kafka")) {
                result.setKafkaConfiguration(launchKafka(kafkaBroker, kafkaEventProcessedCallback, ((JSONArray) ((LinkedHashMap) value).get("topics"))));
            } else if (key.equals("mockedServer")) {
                String path = (String) ((LinkedHashMap) value).get("path");
                Object warmupSeconds = ((LinkedHashMap) value).get("warmupSeconds");
                launchMockedServer(path, warmupSeconds == null ? MOCKED_SERVER_DEFAULT_WARMUP_SECONDS : (int) warmupSeconds, isContainerReuseEnabled(config));
            }
        });

        return result;
    }

    public void shutdown() {
        resources.forEach(SystemTestResourceLauncher::shutdown);
    }

    private void launchDatabase(String type, String image, boolean reuseContainer) {
        SystemTestResourceLauncher resourceLauncher;

        if (type.startsWith("postgres")) {
            resourceLauncher = new SystemTestPostgresLauncher(image, reuseContainer);
        } else if (type.startsWith("mssql")) {
            resourceLauncher = new SystemTestMsSqlLauncher(image, reuseContainer);
        } else if (type.startsWith("redis")) {
            resourceLauncher = new SystemTestRedisLauncher(image, reuseContainer);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }

        resources.add(resourceLauncher);

        resourceLauncher.setup();
    }

    private void launchMockedServer(String path, Integer warmupSeconds, boolean containerReuseEnabled) {
        SystemTestMockedServerLauncher mockedServerLauncher = new SystemTestMockedServerLauncher(path, warmupSeconds, containerReuseEnabled);
        resources.add(mockedServerLauncher);
        mockedServerLauncher.setup();
    }

    private SystemTestConfiguration.KafkaConfiguration launchKafka(EmbeddedKafkaBroker kafkaBroker,
                                                                   KafkaEventProcessedCallback kafkaEventProcessedCallback,
                                                                   JSONArray topics) {
        List<KafkaTopicDefinition> topicDefinitions = topics
                .stream()
                .map(entry -> KafkaTopicDefinition.create((Map<String, String>) entry))
                .collect(Collectors.toList());

        return KafkaConfigurationFactory.create(
                kafkaBroker,
                topicDefinitions,
                kafkaEventProcessedCallback
        );
    }

    private static boolean isContainerReuseEnabled(LinkedHashMap<String, Object> config) {
        LinkedHashMap<String, Object> containerProperties = (LinkedHashMap) config.get("containers");
        Boolean reuse = (Boolean) containerProperties.get("reuse");
        return nonNull(reuse) && Boolean.valueOf(reuse);
    }

    private static void configureTestcontainers() {
        try {
            String userHome = System.getProperty("user.home");
            File testcontainersProperties = Paths.get(userHome, ".testcontainers.properties").toFile();
            String enableContainerReuseConfigLine = "testcontainers.reuse.enable=true";

            String properties = FileUtils.readFileToString(testcontainersProperties, "UTF-8");

            if (!properties.contains(enableContainerReuseConfigLine)) {
                FileOutputStream fos = new FileOutputStream(testcontainersProperties, true);
                fos.write(enableContainerReuseConfigLine.getBytes());
                fos.close();
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
