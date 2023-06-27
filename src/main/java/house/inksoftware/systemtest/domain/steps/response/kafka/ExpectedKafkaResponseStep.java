package house.inksoftware.systemtest.domain.steps.response.kafka;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.kafka.KafkaBackgroundConsumerService;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import lombok.Data;

@Data
public class ExpectedKafkaResponseStep implements ExpectedResponseStep {
    private final String topicName;
    private final String expectedBody;
    private final KafkaBackgroundConsumerService kafkaBackgroundConsumerService;

    public static ExpectedKafkaResponseStep from(String json, SystemTestConfiguration.KafkaConfiguration kafkaConfiguration) {
        DocumentContext documentContext = JsonPath.parse(json);

        return new ExpectedKafkaResponseStep(
                documentContext.read("topic"),
                JsonPath.parse((Object) documentContext.read("body")).jsonString(),
                kafkaConfiguration.getKafkaBackgroundConsumerService()
        );
    }


    @Override
    public void assertResponseIsCorrect(ActualResponse actualResponse) {
        kafkaBackgroundConsumerService.find(topicName, expectedBody);
    }
}
