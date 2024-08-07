package house.inksoftware.systemtest.domain.config.infra.sns;

import static house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Type.FIFO;
import static house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Type.STANDARD;

import house.inksoftware.systemtest.domain.config.infra.sqs.queue.SqsQueueFactory;
import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition;
import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Protocol;
import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

public class SnsTopicFactory {

    public static List<SnsCreatedTopicResult> create(SnsClient snsClient, List<SnsTopicDefinition> topicDefinitions, SnsSubscribersConfig subscribersConfig) {
        return topicDefinitions.stream()
            .map(topic -> create(snsClient, topic, subscribersConfig))
            .toList();
    }

    private static SnsCreatedTopicResult create(SnsClient snsClient, SnsTopicDefinition topic, SnsSubscribersConfig subscribersConfig) {
        var createTopicRequestBuilder = CreateTopicRequest.builder()
            .name(topic.fullName());
        
        if (topic.getType().equals(FIFO)) {
            createTopicRequestBuilder.attributes(Map.of(
                    "FifoTopic", "true",
                    "ContentBasedDeduplication", "true"
            ));
        } else if (topic.getType().equals(STANDARD)) {
            createTopicRequestBuilder.attributes(Map.of());
        } else {
            throw new IllegalArgumentException("Not supported SNS topic type: " + topic.getType());
        }
        
        var request = createTopicRequestBuilder.build();
        var response = snsClient.createTopic(request);
        var result = SnsCreatedTopicResult.builder();
        
        if (topic.getProtocol().equals(Protocol.SQS)) {
            result.sqsSubscriber(createDefaultSqsSubscriber(snsClient, subscribersConfig, topic, response.topicArn()));
        }
        
        return result.build();
    }
    
    private static SqsQueueDefinition createDefaultSqsSubscriber(SnsClient snsClient, SnsSubscribersConfig subscribersConfig, SnsTopicDefinition topic, String topicArn) {
        var result = new SqsQueueDefinition(topic.defaultSubscriberName(), Type.fromShortName(topic.getType().getShortName()));
        
        var createdQueue = SqsQueueFactory.create(subscribersConfig.sqsClient(), result);
        var attributesRequest = GetQueueAttributesRequest.builder()
            .queueUrl(createdQueue.queueUrl())
            .attributeNames(List.of(QueueAttributeName.QUEUE_ARN))
            .build();
        String queueArn = subscribersConfig.sqsClient().getQueueAttributes(attributesRequest).attributes().get(QueueAttributeName.QUEUE_ARN);
        var subscribeRequest = buildSubscribeRequest(topicArn, queueArn, "sqs");
        snsClient.subscribe(subscribeRequest);
        
        return result;
    }

    private static SubscribeRequest buildSubscribeRequest(String topicArn, String endpointArn, String protocol) {
        return SubscribeRequest.builder()
                .protocol(protocol)
                .endpoint(endpointArn)
                .returnSubscriptionArn(true)
                .attributes(Map.of(
                    "RawMessageDelivery", "true"
                ))
                .topicArn(topicArn)
                .build();
    }
}
