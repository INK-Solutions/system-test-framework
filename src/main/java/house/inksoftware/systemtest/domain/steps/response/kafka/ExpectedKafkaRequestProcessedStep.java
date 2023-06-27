package house.inksoftware.systemtest.domain.steps.response.kafka;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.kafka.KafkaBackgroundConsumerService;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import lombok.Data;

@Data
public class ExpectedKafkaRequestProcessedStep implements ExpectedResponseStep {
    private final String kafkaRequestId;
    private final KafkaEventProcessedCallback kafkaEventProcessedCallback;

    public static ExpectedKafkaRequestProcessedStep from(String json, SystemTestConfiguration.KafkaConfiguration kafkaConfiguration) {
        DocumentContext documentContext = JsonPath.parse(json);

        return new ExpectedKafkaRequestProcessedStep(documentContext.read("kafkaRequestId"), kafkaConfiguration.getKafkaEventProcessedCallback());
    }


    @Override
    public void assertResponseIsCorrect(ActualResponse actualResponse) {
        kafkaEventProcessedCallback.awaitUntilEventIsProcessed(kafkaRequestId);
    }

}
