package house.inksoftware.systemtest.domain.steps.response.sqs;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SqsConfiguration;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import lombok.Data;

import java.util.List;

@Data
public class ExpectedSqsResponseStep implements ExpectedResponseStep {
    private final String queueName;
    private final List<String> expectedBodies;
    private final SqsConsumerService sqsConsumerService;

    public static ExpectedSqsResponseStep from(String json, SqsConfiguration sqsConfiguration) {
        DocumentContext documentContext = JsonPath.parse(json);
        var bodies = documentContext.read("body", List.class);

        var parsedBodies = bodies.stream()
                .map(body -> JsonPath.parse(body).jsonString())
                .toList();

        return new ExpectedSqsResponseStep(
                documentContext.read("queue"),
                parsedBodies,
                sqsConfiguration.getSqsConsumerService()
        );
    }
    @Override
    public void assertResponseIsCorrect(ActualResponse actualResponse) {
        sqsConsumerService.find(queueName, expectedBodies);
    }
}