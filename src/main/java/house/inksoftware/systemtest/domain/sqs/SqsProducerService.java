package house.inksoftware.systemtest.domain.sqs;

import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.UUID;

import static house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type.FIFO;
import static house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type.STANDARD;

@Slf4j
@RequiredArgsConstructor
public class SqsProducerService {
    private final SqsClient sqsClient;
    private final List<SqsQueueDefinition> queues;

    public void produce(String name, String message) {
        log.info("Producing message: {} to queue: {}", message, name);
        SqsQueueDefinition sqsQueueDefinition = queues
                .stream()
                .filter(queue -> queue.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Queue " + name + " not found"));

        var fullQueueName = toQueueName(sqsQueueDefinition);
        var queueUrl = findUrl(fullQueueName);

        var sendMessageRequest = buildRequestFor(sqsQueueDefinition, queueUrl, message);
        sqsClient.sendMessage(sendMessageRequest);
        log.info("Produced message: {} to queue: {}, url: {}", message, fullQueueName, queueUrl);
    }

    private SendMessageRequest buildRequestFor(SqsQueueDefinition sqsQueueDefinition, String queueUrl, String message) {
        var builder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message);

        if (sqsQueueDefinition.getType().equals(FIFO)) {
            builder.messageGroupId(UUID.randomUUID().toString())
                    .messageDeduplicationId(UUID.randomUUID().toString());
        } else if (!sqsQueueDefinition.getType().equals(STANDARD)) {
            throw new IllegalArgumentException("Unknown queue type: " + sqsQueueDefinition.getType());
        }

        return builder.build();
    }

    private String toQueueName(SqsQueueDefinition sqsQueueDefinition) {
        return switch (sqsQueueDefinition.getType()) {
            case FIFO -> sqsQueueDefinition.getName() + ".fifo";
            case STANDARD -> sqsQueueDefinition.getName();
            default -> throw new IllegalArgumentException("Unknown queue type: " + sqsQueueDefinition.getType());
        };
    }

    private String findUrl(String queueName) {
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
        return getQueueUrlResponse.queueUrl();
    }
}
