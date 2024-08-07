package house.inksoftware.systemtest.domain.config.infra.sns;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SnsConfiguration;
import house.inksoftware.systemtest.domain.sns.SnsConsumerService;
import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import java.util.List;
import software.amazon.awssdk.services.sns.SnsClient;

public class SnsConfigurationFactory {

    public static SnsConfiguration create(SnsClient snsClient,
                                          List<SnsTopicDefinition> topicDefinitions,
                                          SnsSubscribersConfig subscribersConfig) {

        var createdTopics = SnsTopicFactory.create(snsClient, topicDefinitions, subscribersConfig);
        
        var snsConsumerServiceBuilder = SnsConsumerService.builder()
            .topics(topicDefinitions);
            
        var sqsQueues = createdTopics.stream()
            .filter(topic -> topic.getSqsSubscriber() != null)
            .map(topic -> topic.getSqsSubscriber())
            .toList();
        if (!sqsQueues.isEmpty()) {
            var sqsConsumer = new SqsConsumerService(subscribersConfig.sqsClient(), sqsQueues);
            snsConsumerServiceBuilder.sqsConsumerService(sqsConsumer);
        }

        return new SnsConfiguration(
                snsClient,
                topicDefinitions,
                snsConsumerServiceBuilder.build()
        );
    }
}
