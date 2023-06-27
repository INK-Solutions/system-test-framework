package house.inksoftware.systemtest.domain.kafka.deser;

import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.MockedKafkaAvroValueSerializer;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinition;
import house.inksoftware.systemtest.domain.kafka.topic.KafkaTopicDefinitionHolder;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

public class SystemTestKafkaAvroValueSerializer extends MockedKafkaAvroValueSerializer {

    @Override
    public Schema toSchema(String topicName) {
        return find(topicName).getSchema();
    }

    private KafkaTopicDefinition find(String topicName) {
        return KafkaTopicDefinitionHolder
                .getTopicDefinitions()
                .stream()
                .filter(entry -> entry.getName().equals(topicName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No configuration for topic " + topicName));
    }

    @Override
    public SpecificRecordBase toSpecificRecord(String topic, String json) {
        return new JsonAvroConverter()
                .convertToSpecificRecord(json.getBytes(), find(topic).getClazz(), toSchema(topic));
    }
}
