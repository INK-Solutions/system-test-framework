package house.inksoftware.systemtest.domain.config;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.kafka.KafkaBackgroundConsumerService;
import house.inksoftware.systemtest.domain.kafka.KafkaProducerService;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import house.inksoftware.systemtest.domain.sns.SnsConsumerService;
import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import house.inksoftware.systemtest.domain.sqs.SqsProducerService;
import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import java.util.List;
import lombok.Data;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Data
public class SystemTestConfiguration {
    private RestConfiguration restConfiguration;
    private KafkaConfiguration kafkaConfiguration;
    private GrpcConfiguration grpcConfiguration;
    private SqsConfiguration sqsConfiguration;
    private SnsConfiguration snsConfiguration;
    private List<SystemTestResourceLauncher> resources;
    
    @Data
    public static class SnsConfiguration {
        private final SnsClient snsClient;
        private final List<SnsTopicDefinition> snsTopics;
        private final SnsConsumerService snsConsumerService;
    }

    @Data
    public static class SqsConfiguration {
        private final SqsClient sqsClient;
        private final List<SqsQueueDefinition> sqsQueues;
        private final SqsProducerService sqsProducerService;
        private final SqsConsumerService sqsConsumerService;
    }

    @Data
    public static class KafkaTopic {
        private final String label;
        private final String topicName;
    }

    @Data
    public static class KafkaConfiguration {
        private final EmbeddedKafkaBroker broker;
        private final List<KafkaTopicDefinition> kafkaTopics;
        private final KafkaBackgroundConsumerService kafkaBackgroundConsumerService;
        private final KafkaProducerService kafkaProducerService;
        private KafkaEventProcessedCallback kafkaEventProcessedCallback;
    }


    @Data
    public static class RestConfiguration {
        private final String host;
        private final TestRestTemplate restTemplate;
        private final Integer port;
    }


    @Data
    public static class GrpcConfiguration {
        private final String protoDirPath;
        private final String contractsDirPath;
    }

    public boolean hasKafka() {
        return kafkaConfiguration != null;
    }

    public boolean hasGrpc() {
        return grpcConfiguration != null;
    }
}
