package house.inksoftware.systemtest.domain.kafka;

import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.MockedKafkaAvroValueSerializer;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private final Producer<String, Object> producer;
    private final MockedKafkaAvroValueSerializer serializer;
    private final List<KafkaTopicDefinition> topics;

    public void produce(String name, String json) {
        log.info("Producing message to topic: {}", name);
        KafkaTopicDefinition kafkaTopic = topics
                .stream()
                .filter(topic -> topic.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Topic with label " + name + " not found"));

        producer.send(new ProducerRecord<>(kafkaTopic.getName(), "key", serializer.toSpecificRecord(kafkaTopic.getName(), json)));

        log.info("Produced message to topic: {}", name);
    }
}
