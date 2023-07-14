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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.TestContext;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfrastructureLauncher {
    public static final Integer MOCKED_SERVER_DEFAULT_WARMUP_SECONDS = 5;
    private final List<SystemTestResourceLauncher> resources = new ArrayList<>();

    public void launchDb(TestContext testContext, LinkedHashMap<String, Object> config) {
        LinkedHashMap<String, String> properties = (LinkedHashMap) config.get("database");
        launchDatabase(properties.get("type"), properties.get("image"));

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
                launchMockedServer(path, warmupSeconds == null ? MOCKED_SERVER_DEFAULT_WARMUP_SECONDS : (int) warmupSeconds);
            }
        });

        return result;
    }

    public void shutdown() {
        resources.forEach(SystemTestResourceLauncher::shutdown);
    }

    private void launchDatabase(String type, String image) {
        SystemTestResourceLauncher resourceLauncher;

        if (type.startsWith("postgres")) {
            resourceLauncher = new SystemTestPostgresLauncher(image);
        } else if (type.startsWith("mssql")) {
            resourceLauncher = new SystemTestMsSqlLauncher(image);
        } else if (type.startsWith("redis")) {
            resourceLauncher = new SystemTestRedisLauncher(image);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }

        resources.add(resourceLauncher);

        resourceLauncher.setup();
    }

    private void launchMockedServer(String path, Integer warmupSeconds) {
        SystemTestMockedServerLauncher mockedServerLauncher = new SystemTestMockedServerLauncher(path, warmupSeconds);
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
}
