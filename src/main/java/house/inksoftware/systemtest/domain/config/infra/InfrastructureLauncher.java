package house.inksoftware.systemtest.domain.config.infra;

import com.google.common.base.Preconditions;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.GrpcConfiguration;
import house.inksoftware.systemtest.domain.config.infra.db.SystemTestDatabasePopulatorLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.mssql.SystemTestMsSqlLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.mysql.SystemTestMySqlLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.postgres.SystemTestPostgresLauncher;
import house.inksoftware.systemtest.domain.config.infra.db.redis.SystemTestRedisLauncher;
import house.inksoftware.systemtest.domain.config.infra.kafka.KafkaConfigurationFactory;
import house.inksoftware.systemtest.domain.config.infra.kafka.SystemTestKafkaLauncher;
import house.inksoftware.systemtest.domain.config.infra.mock.SystemTestMockedGrpcServerLauncher;
import house.inksoftware.systemtest.domain.config.infra.mock.SystemTestMockedRestServerLauncher;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import net.minidev.json.JSONArray;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.test.context.TestContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static house.inksoftware.systemtest.SystemTest.TEST_RESOURCES_PATH;


public class InfrastructureLauncher {
    private final List<SystemTestResourceLauncher> resources = new ArrayList<>();

    public void launchDb(TestContext testContext, LinkedHashMap<String, Object> config) {
        var databases = (JSONArray) config.get("databases");
        for (Object database : databases) {
            var dbConfig = (LinkedHashMap<String, String>) database;
            launchDatabase(dbConfig.get("type"), dbConfig.get("image"));

            String testDataScriptsPath = dbConfig.get("testDataScriptsPath");
            if (testDataScriptsPath != null && testContext != null) {
                new SystemTestDatabasePopulatorLauncher(testDataScriptsPath, testContext.getApplicationContext()
                                                                                        .getBean(DataSource.class)).setup();
            }
        }

    }

    public SystemTestConfiguration launchAllInfra(LinkedHashMap<String, Object> config) {
        SystemTestConfiguration result = new SystemTestConfiguration();
        config.forEach((key, value) -> {
            if (key.equals("kafka")) {
                var kafkaBroker = embeddedKafkaBroker();
                var topics = configureKafka(kafkaBroker, ((JSONArray) ((LinkedHashMap) value).get("topics")));
                result.setKafkaConfiguration(topics);
            } else if (key.equals("mockedServer")) {
                String path = (String) ((LinkedHashMap) value).get("path");
                launchMockedServer(path);
            } else if (key.equals("grpc")) {
                String protoDirPath = (String) ((LinkedHashMap) value).get("protoDirPath");
                Preconditions.checkState(protoDirPath != null, "Proto dir path is not defined for grpc");
                String contractsDirPath = (String) ((LinkedHashMap) value).get("contractsDirPath");
                Preconditions.checkState(contractsDirPath != null, "Contract dir path is not defined for grpc");
                launchGrpcServer(protoDirPath);
                result.setGrpcConfiguration(new GrpcConfiguration(protoDirPath, TEST_RESOURCES_PATH + contractsDirPath));
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
        } else if (type.startsWith("mysql")) {
            resourceLauncher = new SystemTestMySqlLauncher(image);
        } else if (type.startsWith("redis")) {
            resourceLauncher = new SystemTestRedisLauncher(image);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }

        resources.add(resourceLauncher);

        resourceLauncher.setup();
    }

    private void launchMockedServer(String path) {
        SystemTestMockedRestServerLauncher mockedServerLauncher = new SystemTestMockedRestServerLauncher(path);
        resources.add(mockedServerLauncher);
        mockedServerLauncher.setup();
    }


    private void launchGrpcServer(String protoDirPath) {
        SystemTestMockedGrpcServerLauncher mockedServerLauncher = new SystemTestMockedGrpcServerLauncher(protoDirPath);
        resources.add(mockedServerLauncher);
        mockedServerLauncher.setup();
    }

    private SystemTestConfiguration.KafkaConfiguration configureKafka(EmbeddedKafkaBroker kafkaBroker, JSONArray topics) {
        List<KafkaTopicDefinition> topicDefinitions = topics
                .stream()
                .map(entry -> KafkaTopicDefinition.create((Map<String, String>) entry))
                .collect(Collectors.toList());

        return KafkaConfigurationFactory.create(
                kafkaBroker,
                topicDefinitions
        );
    }

    private EmbeddedKafkaBroker embeddedKafkaBroker() {
        var kafkaLauncher = new SystemTestKafkaLauncher();
        resources.add(kafkaLauncher);
        kafkaLauncher.setup();
        return kafkaLauncher.getEmbeddedKafka();
    }
}
