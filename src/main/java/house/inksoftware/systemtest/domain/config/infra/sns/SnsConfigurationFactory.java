package house.inksoftware.systemtest.domain.config.infra.sns;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SnsConfiguration;
import house.inksoftware.systemtest.domain.config.infra.sqs.queue.SqsQueueFactory;
import house.inksoftware.systemtest.domain.sns.SnsConsumerService;
import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition;
import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Protocol;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition.Type;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListTopicsRequest;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

public class SnsConfigurationFactory {

    public static SnsConfiguration create(SnsClient snsClient,
                                          List<SnsTopicDefinition> topicDefinitions,
                                          SnsSubscribersConfig subscribersConfig) {

        createTopics(snsClient, topicDefinitions);
        var snsConsumerServiceBuilder = SnsConsumerService.builder()
            .topics(topicDefinitions);
            
        var hasSqs = topicDefinitions.stream()
            .filter(topic -> topic.getProtocol().equals(Protocol.SQS))
            .findFirst()
            .isPresent();
        if (hasSqs) {
            var sqsConsumer = createSqsSubscribers(snsClient, topicDefinitions, subscribersConfig);
            snsConsumerServiceBuilder.sqsConsumerService(sqsConsumer);
        }

        return new SnsConfiguration(
                snsClient,
                topicDefinitions,
                snsConsumerServiceBuilder.build()
        );
    }

    private static SqsConsumerService createSqsSubscribers(
            SnsClient snsClient, 
            List<SnsTopicDefinition> topicDefinitions,
            SnsSubscribersConfig subscribersConfig) {
                
        var sqsClient = subscribersConfig.getSqsClientSupplier().get();
        var sqsQueues = topicDefinitions.stream()
            .filter(topic -> topic.getProtocol().equals(Protocol.SQS))
            .map(topic -> subscribeToDefaultQueue(snsClient, sqsClient, topic))
            .toList();
        return new SqsConsumerService(sqsClient, sqsQueues);
    }

    private static SqsQueueDefinition subscribeToDefaultQueue(SnsClient snsClient, SqsClient sqsClient, SnsTopicDefinition topic) {
        SqsQueueDefinition result = new SqsQueueDefinition(topic.defaultSubscriberName(), Type.fromShortName(topic.getType().getShortName()));
        
        var createdQueue = SqsQueueFactory.create(sqsClient, result);
        var attributesRequest = GetQueueAttributesRequest.builder()
            .queueUrl(createdQueue.queueUrl())
            .attributeNames(List.of(QueueAttributeName.QUEUE_ARN))
            .build();
        String queueArn = sqsClient.getQueueAttributes(attributesRequest).attributes().get(QueueAttributeName.QUEUE_ARN);
        var subscribeRequest = buildSubscribeRequest(findTopicArn(topic.fullName(), snsClient), queueArn, "sqs");
        snsClient.subscribe(subscribeRequest);
        
        return result;
    }

    public static void createTopics(SnsClient snsClient, List<SnsTopicDefinition> topicDefinitions) {
        var factory = new SnsTopicFactory();
        factory.create(snsClient, topicDefinitions);
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
    
    private static String findTopicArn(String topicName, SnsClient snsClient) {
        ListTopicsRequest request = ListTopicsRequest.builder()
            .build();
        ListTopicsResponse response = snsClient.listTopics(request);
        return response.topics().stream()
            .filter(topic -> topic.topicArn().endsWith(":" + topicName))
            .findFirst()
            .map(Topic::topicArn)
            .orElseThrow(() -> new IllegalArgumentException("ARN for topic " + topicName + " not found"));
    }
}
