package house.inksoftware.systemtest.domain.steps.request.executable.kafka;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.request.executable.ExecutableRequestStep;
import house.inksoftware.systemtest.domain.steps.response.kafka.ActualKafkaResponse;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExecutableKafkaRequestStep implements ExecutableRequestStep {
    private final String topicName;
    private final String body;

    public ActualResponse execute(SystemTestConfiguration config) {
        config.getKafkaConfiguration().getKafkaProducerService().produce(topicName, body);
        return new ActualKafkaResponse();
    }

}
