package house.inksoftware.systemtest.domain.config.infra.sqs.queue;

import static house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type.FIFO;
import static house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type.STANDARD;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.CONTENT_BASED_DEDUPLICATION;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.FIFO_QUEUE;

import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;

public class SqsQueueFactory {

    public static void create(SqsClient sqsClient, List<SqsQueueDefinition> queueDefinitions) {
        queueDefinitions.forEach(queueDefinition -> create(sqsClient, queueDefinition));
    }

    public static CreateQueueResponse create(SqsClient sqsClient, SqsQueueDefinition queueDefinition) {
        var createQueueRequestBuilder = CreateQueueRequest.builder();
        if (queueDefinition.getType().equals(FIFO)) {
            createQueueRequestBuilder.queueName(queueDefinition.getName() + ".fifo");
            createQueueRequestBuilder.attributes(Map.of(
                    FIFO_QUEUE, "true",
                    CONTENT_BASED_DEDUPLICATION, "true"
            ));
        } else if (queueDefinition.getType().equals(STANDARD)) {
            createQueueRequestBuilder.queueName(queueDefinition.getName());
            createQueueRequestBuilder.attributes(Map.of());
        } else {
            throw new IllegalArgumentException("Not supported SQS queue type: " + queueDefinition.getType());
        }
        var request = createQueueRequestBuilder.build();
        return sqsClient.createQueue(request);
    }

}
