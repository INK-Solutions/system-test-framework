package house.inksoftware.systemtest.domain.steps.response;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.steps.response.kafka.ExpectedKafkaRequestProcessedStep;
import house.inksoftware.systemtest.domain.steps.response.kafka.ExpectedKafkaResponseStep;
import house.inksoftware.systemtest.domain.steps.response.rest.ExpectedRestResponseStep;
import house.inksoftware.systemtest.domain.steps.response.sqs.ExpectedSqsResponseStep;
import house.inksoftware.systemtest.domain.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExpectedResponseStepFactory {
    private final SystemTestConfiguration systemTestConfiguration;

    @SneakyThrows
    public List<ExpectedResponseStep> create(File basePath, String stepName) {
        String responsesFolder = basePath.getAbsolutePath() + File.separator + stepName + File.separator + "response";
        File folder = new File(responsesFolder);

        if (!folder.exists()) {
            return new ArrayList<>();
        } else {
            return FileUtils
                    .listFiles(responsesFolder)
                    .stream()
                    .map(fileName -> createStep(responsesFolder + File.separator + fileName))
                    .collect(Collectors.toList());
        }
    }

    @SneakyThrows
    private ExpectedResponseStep createStep(String fullPath) {
        String json = FileUtils.readFileContent(fullPath);
        if (fullPath.endsWith("rest-response.json")) {
            return ExpectedRestResponseStep.from(json);
        } else if (fullPath.endsWith("event.json")) {
            return ExpectedKafkaResponseStep.from(json, systemTestConfiguration.getKafkaConfiguration());
        } else if (fullPath.endsWith("kafka-request-processed.json")) {
            return ExpectedKafkaRequestProcessedStep.from(json, systemTestConfiguration.getKafkaConfiguration());
        } else if (fullPath.endsWith("sqs-response.json")) {
            return ExpectedSqsResponseStep.from(json, systemTestConfiguration.getSqsConfiguration());
        } else {
            throw new IllegalArgumentException("Expected response file should be named: event.json or rest-response.json");
        }
    }
}
