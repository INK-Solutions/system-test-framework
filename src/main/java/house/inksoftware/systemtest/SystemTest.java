package house.inksoftware.systemtest;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.SystemTestExecutionService;
import house.inksoftware.systemtest.domain.SystemTestExecutionServiceFactory;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.config.infra.InfrastructureLauncher;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;
import house.inksoftware.systemtest.domain.steps.request.RequestStepFactory;
import house.inksoftware.systemtest.domain.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

@Slf4j
@TestExecutionListeners(value = {SystemTest.class}, mergeMode = MERGE_WITH_DEFAULTS)
@ActiveProfiles("systemtest")
@EmbeddedKafka
@RequiredArgsConstructor
@RunWith(SpringRunner.class)
public class SystemTest implements TestExecutionListener {
    private static final String PATH = "src/test/resources/";

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired(required = false)
    private KafkaEventProcessedCallback kafkaEventProcessedCallback;
    private InfrastructureLauncher infrastructureLauncher = new InfrastructureLauncher();

    @LocalServerPort
    protected int port;



    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        Optional<File> systemTestYaml = findSystemTestYaml();
        Assert.assertTrue(
                "File application-systemtest.yml is not found! " +
                "Please add it if it's not present or rename existing test yml file to application-systemtest.yml",
                systemTestYaml.isPresent()
        );

        Optional<File> systemTestConfFile = findSystemTestConfig();
        Assert.assertTrue(
                "File system-test-configuration.json is not found! Please add it and define your infra requirements.",
                systemTestConfFile.isPresent()
        );

        LinkedHashMap<String, Object> infrastructure = findInfraConfig(systemTestConfFile.get());
        infrastructureLauncher.launchDb(testContext, infrastructure);
    }

    @NotNull
    private static Optional<File> findSystemTestConfig() {
        return listFiles(new File(PATH), new String[] {"json"}, true)
                .stream()
                .filter(file -> file.getName().equals("system-test-configuration.json"))
                .findFirst();
    }

    @NotNull
    private static Optional<File> findSystemTestYaml() {
        return listFiles(new File(PATH), new String[] {"yml"}, true)
                .stream()
                .filter(file -> file.getName().equals("application-systemtest.yml"))
                .findFirst();
    }

    @SneakyThrows
    @Test
    public void testBusinessLogic() {
        Optional<File> systemTestConfFile = findSystemTestConfig();
        if (systemTestConfFile.isPresent()) {
            try {
                LinkedHashMap<String, Object> infrastructure = findInfraConfig(systemTestConfFile.get());

                SystemTestConfiguration config = infrastructureLauncher
                        .launchAllInfra(
                                embeddedKafkaBroker,
                                kafkaEventProcessedCallback,
                                restTemplate,
                                port,
                                infrastructure
                        );
                testBusinessLogic(config, systemTestConfFile.get());
            } finally {
                infrastructureLauncher.shutdown();
            }
        }

    }

    private static LinkedHashMap<String, Object> findInfraConfig(File systemTestConfFile) throws Exception {
        return JsonPath
                .parse(FileUtils.readFile(systemTestConfFile))
                       .read("infrastructure");
    }

    private void testBusinessLogic(SystemTestConfiguration config, File systemTestConfig) {
        Arrays.stream(systemTestConfig.getParentFile().listFiles())
              .filter(File::isDirectory)
              .forEach(testBaseFolder -> test(config, testBaseFolder));
    }

    @SneakyThrows
    private void test(SystemTestConfiguration config, File basePath) {
        log.info("Running system tests {}", basePath.getName());
        List<File> orderedSteps = orderSteps(Arrays.asList(basePath.listFiles()));
        log.info("Test {} has {} steps", basePath.getName(), orderedSteps.size());

        RequestStepFactory requestStepFactory = new RequestStepFactory();
        SystemTestContext context = new SystemTestContext();
        SystemTestExecutionService service = SystemTestExecutionServiceFactory.create(config, basePath);

        for (File stepFile : orderedSteps) {
            RequestStep step = requestStepFactory.toStep(new File(basePath.getPath() + File.separator + stepFile.getName()), context);
            service.execute(step, context);
        }
    }

    private List<File> orderSteps(List<File> steps) {
        steps
                .stream()
                .filter(File::isDirectory)
                .forEach(step -> Preconditions.checkState(toStepNumber(step).matches("\\d+"), "Step " + step + " should start with number (example: 1-register-customer), but is: " + step));

        return steps
                .stream()
                .filter(File::isDirectory)
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(toStepNumber(value))))
                .collect(Collectors.toList());
    }

    private static String toStepNumber(File file) {
        return file.getName().substring(0, file.getName().indexOf("-"));
    }


}
