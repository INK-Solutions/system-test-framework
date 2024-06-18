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

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SqsConsumerService {
    private final Integer MAX_MESSAGES = 10;
    private final Integer WAIT_TIME = 10;
    private final SqsClient sqsClient;
    private final List<SqsQueueDefinition> queues;


    public void find(String queueName, String body) {
        var definition = findDefinition(queueName);
        var fullQueueName = definition.getName() + "." + definition.getType().getShortName();
        var url = findUrl(fullQueueName);
        var messages = poll(url);
        var result = messages.stream()
                .filter(message -> JsonUtils.isEqual(body, message.body()))
                .findAny()
                .orElseThrow(() -> new AssertionError("It was expect that queue " + queueName + " would have a message " + body));
        delete(url, result.receiptHandle());
    }

    private SqsQueueDefinition findDefinition(String name) {
        return queues
                .stream()
                .filter(queue -> queue.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Queue " + name + " not found"));
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
