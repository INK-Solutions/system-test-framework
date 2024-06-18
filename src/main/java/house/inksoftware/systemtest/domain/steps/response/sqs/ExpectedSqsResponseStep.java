package house.inksoftware.systemtest.domain.steps.response.sqs;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SqsConfiguration;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import lombok.Data;

@Data
public class ExpectedSqsResponseStep implements ExpectedResponseStep {
    private final String queueName;
    private final String expectedBody;
    private final SqsConsumerService sqsConsumerService;

    public static ExpectedSqsResponseStep from(String json, SqsConfiguration sqsConfiguration) {
        DocumentContext documentContext = JsonPath.parse(json);

        return new ExpectedSqsResponseStep(
                documentContext.read("queue"),
                JsonPath.parse((Object) documentContext.read("body")).jsonString(),
                sqsConfiguration.getSqsConsumerService()
        );
    }

    @Override
    public void assertResponseIsCorrect(ActualResponse actualResponse) {
        sqsConsumerService.find(queueName, expectedBody);
    }
}
