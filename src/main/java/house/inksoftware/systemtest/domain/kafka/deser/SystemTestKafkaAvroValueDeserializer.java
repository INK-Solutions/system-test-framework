package house.inksoftware.systemtest.domain.kafka.deser;

import house.inksoftware.systemtest.domain.config.infra.kafka.outgoing.MockedKafkaAvroValueDeserializer;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinitionHolder;
import org.apache.avro.Schema;

public class SystemTestKafkaAvroValueDeserializer extends MockedKafkaAvroValueDeserializer {

    @Override
    public Schema toSchema(String topicName) {
        return KafkaTopicDefinitionHolder
                .getTopicDefinitions()
                .stream()
                .filter(entry -> entry.getName().equals(topicName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No configuration for topic " + topicName))
                .getSchema();
    }
}
