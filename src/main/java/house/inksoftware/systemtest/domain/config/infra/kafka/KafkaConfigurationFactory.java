
package house.inksoftware.systemtest.domain.config.infra.kafka;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.MockedKafkaAvroValueSerializer;
import house.inksoftware.systemtest.domain.config.infra.kafka.outgoing.MockedKafkaAvroValueDeserializer;
import house.inksoftware.systemtest.domain.kafka.KafkaBackgroundConsumerService;
import house.inksoftware.systemtest.domain.kafka.KafkaProducerService;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition.Direction;
import house.inksoftware.systemtest.domain.kafka.deser.SystemTestKafkaAvroValueDeserializer;
import house.inksoftware.systemtest.domain.kafka.deser.SystemTestKafkaAvroValueSerializer;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinitionHolder;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KafkaConfigurationFactory {
    @SneakyThrows
    public static SystemTestConfiguration.KafkaConfiguration create(EmbeddedKafkaBroker embeddedKafkaBroker,
                                                                    List<KafkaTopicDefinition> topicDefinitions,
                                                                    KafkaEventProcessedCallback kafkaEventProcessedCallback) {

        KafkaTopicDefinitionHolder.setTopicDefinitions(topicDefinitions);

        List<KafkaTopicDefinition> appReadTopics = filter(topicDefinitions, Direction.READ);
        List<KafkaTopicDefinition> appPublishTopics = filter(topicDefinitions, Direction.PUBLISH);


        Consumer<String, Object> consumer = createConsumer(appPublishTopics, embeddedKafkaBroker, new SystemTestKafkaAvroValueDeserializer());
        KafkaBackgroundConsumerService kafkaBackgroundConsumerService = new KafkaBackgroundConsumerService(consumer);
        kafkaBackgroundConsumerService.initiate();

        SystemTestKafkaAvroValueSerializer serializer = new SystemTestKafkaAvroValueSerializer();
        KafkaProducerService kafkaProducerService = new KafkaProducerService(createProducer(embeddedKafkaBroker, serializer), serializer, appReadTopics);

        return new SystemTestConfiguration.KafkaConfiguration(
                embeddedKafkaBroker,
                appPublishTopics,
                kafkaBackgroundConsumerService,
                kafkaProducerService,
                kafkaEventProcessedCallback
        );
    }

    private static List<KafkaTopicDefinition> filter(List<KafkaTopicDefinition> topicDefinitions, Direction direction) {
        return topicDefinitions
                .stream()
                .filter(topic -> topic.getDirection() == direction)
                .collect(Collectors.toList());
    }


    public static Consumer<String, Object> createConsumer(List<KafkaTopicDefinition> topics,
                                                          EmbeddedKafkaBroker embeddedKafkaBroker,
                                                          MockedKafkaAvroValueDeserializer deserializer) {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configs.put("schema.registry.url", "not-used");

        Consumer<String, Object> consumer = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), deserializer).createConsumer();
        consumer.subscribe(topics.stream().map(KafkaTopicDefinition::getName).collect(Collectors.toList()));


        return consumer;
    }

    public static Producer<String, Object> createProducer(EmbeddedKafkaBroker embeddedKafkaBroker,
                                                                  MockedKafkaAvroValueSerializer serializer) {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, new StringSerializer());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer.getClass());
        configs.put("schema.registry.url", "not-used");

        return new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), serializer).createProducer();
    }
}
