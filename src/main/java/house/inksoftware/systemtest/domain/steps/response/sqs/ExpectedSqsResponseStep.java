package house.inksoftware.systemtest.domain.steps.response.sqs;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SqsConfiguration;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class ExpectedSqsResponseStep implements ExpectedResponseStep {
    private final Map<String, List<String>> queueMessages;
    private final SqsConsumerService sqsConsumerService;

    public static ExpectedSqsResponseStep from(String json, SqsConfiguration sqsConfiguration) {
        DocumentContext documentContext = JsonPath.parse(json);
        Map<String, List<String>> queueMessages = new HashMap<>();

        List<Map<String, Object>> queues = documentContext.read("$.queues", List.class);

        for (Map<String, Object> queue : queues) {
            String queueName = (String) queue.get("name");
            List<Object> bodies = (List<Object>) queue.get("body");

            var parsedBodies = bodies.stream()
                    .map(body -> JsonPath.parse(body).jsonString())
                    .toList();

            queueMessages.put(queueName, parsedBodies);
        }

        return new ExpectedSqsResponseStep(
                queueMessages,
                sqsConfiguration.getSqsConsumerService()
        );
    }

    @Override
    public void assertResponseIsCorrect(ActualResponse actualResponse) {
        queueMessages.forEach(sqsConsumerService::find
        );
    }
}