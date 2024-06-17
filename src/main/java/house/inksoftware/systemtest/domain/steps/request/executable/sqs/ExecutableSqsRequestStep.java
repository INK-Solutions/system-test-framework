package house.inksoftware.systemtest.domain.steps.request.executable.sqs;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.steps.request.executable.ExecutableRequestStep;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.sqs.ActualSqsResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutableSqsRequestStep implements ExecutableRequestStep {
    private final String queueName;
    private final String body;

    @Override
    public ActualResponse execute(SystemTestConfiguration config) {
        config.getSqsConfiguration().getSqsProducerService().produce(queueName, body);
        return new ActualSqsResponse();
    }
}
