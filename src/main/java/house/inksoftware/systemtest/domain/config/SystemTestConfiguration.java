package house.inksoftware.systemtest.domain.config;

import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.kafka.KafkaBackgroundConsumerService;
import house.inksoftware.systemtest.domain.kafka.KafkaProducerService;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import lombok.Data;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.List;

@Data
public class SystemTestConfiguration {
    private RestConfiguration restConfiguration;
    private KafkaConfiguration kafkaConfiguration;

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
        private final KafkaEventProcessedCallback kafkaEventProcessedCallback;
    }


    @Data
    public static class RestConfiguration {
        private final String host;
        private final TestRestTemplate restTemplate;
        private final Integer port;
    }


}
