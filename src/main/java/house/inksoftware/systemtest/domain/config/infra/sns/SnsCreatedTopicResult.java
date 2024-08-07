package house.inksoftware.systemtest.domain.config.infra.sns;

import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@Data
public class SnsCreatedTopicResult {
    
    private final SqsQueueDefinition sqsSubscriber;
    
}
