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

        var fullQueueName = sqsQueueDefinition.getName() + "." + sqsQueueDefinition.getType().getShortName();
        var queueUrl = findUrl(fullQueueName);

        var sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageGroupId(UUID.randomUUID().toString())
                .messageDeduplicationId(UUID.randomUUID().toString())
                .build();
        var response = sqsClient.sendMessage(sendMessageRequest);
        log.info("Produced message: {} to queue: {}, url: {}", message, fullQueueName, queueUrl);
    }

    private String findUrl(String queueName) {
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
        return getQueueUrlResponse.queueUrl();
    }
}
