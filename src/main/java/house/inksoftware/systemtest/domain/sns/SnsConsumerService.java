package house.inksoftware.systemtest.domain.sns;

import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Protocol;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Builder
public class SnsConsumerService {
    private final List<SnsTopicDefinition> topics;
    private final SqsConsumerService sqsConsumerService;

    public void find(String topicName, String body) {
        try {
            var definition = findDefinition(topicName);
            if (definition.getProtocol().equals(Protocol.SQS)) {
                sqsConsumerService.find(topicName + "__default_sqs_queue", body);
            }
        } catch (AssertionError e) {
            throw new AssertionError("It was expect that SNS topic " + topicName + " would have a message " + body);
        }
    }
    
    private SnsTopicDefinition findDefinition(String name) {
        return topics
                .stream()
                .filter(topic -> topic.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Topic " + name + " not found"));
    }
}
