package house.inksoftware.systemtest.domain.sqs;

import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class SqsConsumerService {
    private final Integer MAX_MESSAGES = 10;
    private final Integer WAIT_TIME = 10;
    private final SqsClient sqsClient;
    private final List<SqsQueueDefinition> queues;

    public void find(String queueName, List<String> bodies) {
        log.info("Finding messages with bodies: {} in queue: {}", bodies, queueName);

        var definition = findDefinition(queueName);
        var fullQueueName = toQueueName(definition);
        var url = findUrl(fullQueueName);
        var messages = poll(url);

        var remainingMessages = new ArrayList<>(messages);
        var matchedMessages = new ArrayList<Message>();

        for (String expectedBody : bodies) {
            var matchingMessage = remainingMessages.stream()
                    .filter(message -> JsonUtils.isEqual(expectedBody, message.body()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(String.format(
                            "Expected message not found in queue %s: %s%nAvailable messages: %s",
                            queueName,
                            expectedBody,
                            remainingMessages.stream().map(Message::body).collect(Collectors.toList())
                    )));

            matchedMessages.add(matchingMessage);
            remainingMessages.remove(matchingMessage);
        }

        matchedMessages.forEach(message -> delete(url, message.receiptHandle()));
    }

    private SqsQueueDefinition findDefinition(String name) {
        return queues
                .stream()
                .filter(queue -> queue.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Queue " + name + " not found"));
    }

    private String toQueueName(SqsQueueDefinition sqsQueueDefinition) {
        return switch (sqsQueueDefinition.getType()) {
            case FIFO -> sqsQueueDefinition.getName() + ".fifo";
            case STANDARD -> sqsQueueDefinition.getName();
            default -> throw new IllegalArgumentException("Unknown queue type: " + sqsQueueDefinition.getType());
        };
    }

    private List<Message> poll(String queueUrl) {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(MAX_MESSAGES)
                .waitTimeSeconds(WAIT_TIME)
                .build();

        return sqsClient.receiveMessage(receiveMessageRequest).messages();
    }

    private void delete(String queueUrl, String receiptHandle) {
        sqsClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(receiptHandle));
    }

    private String findUrl(String queueName) {
        GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();
        GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
        return getQueueUrlResponse.queueUrl();
    }

}
