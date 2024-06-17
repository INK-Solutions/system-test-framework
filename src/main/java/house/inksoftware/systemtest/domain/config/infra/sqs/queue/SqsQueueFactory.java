package house.inksoftware.systemtest.domain.config.infra.sqs.queue;

import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.util.List;
import java.util.Map;

import static house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type.FIFO;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.CONTENT_BASED_DEDUPLICATION;
import static software.amazon.awssdk.services.sqs.model.QueueAttributeName.FIFO_QUEUE;

public class SqsQueueFactory {

    public void create(SqsClient sqsClient, List<SqsQueueDefinition> queueDefinitions) {
        queueDefinitions.forEach(queueDefinition -> create(sqsClient, queueDefinition));
    }

    private void create(SqsClient sqsClient, SqsQueueDefinition queueDefinition) {
        var createQueueRequestBuilder = CreateQueueRequest.builder();
        if (queueDefinition.getType().equals(FIFO)) {
            createQueueRequestBuilder.queueName(queueDefinition.getName() + ".fifo");
            createQueueRequestBuilder.attributes(Map.of(
                    FIFO_QUEUE, "true",
                    CONTENT_BASED_DEDUPLICATION, "true"
            ));
        } else {
            throw new IllegalArgumentException("Not supported SQS queue type: " + queueDefinition.getType());
        }
        var request = createQueueRequestBuilder.build();
        sqsClient.createQueue(request);
    }

}
