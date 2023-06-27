package house.inksoftware.systemtest.domain.config.infra.kafka.outgoing;

import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;

public abstract class MockedKafkaAvroValueDeserializer extends KafkaAvroDeserializer {
    @Override
    public Object deserialize(String topic, byte[] bytes) {
        this.schemaRegistry = getMockClient(toSchema(topic));
        return super.deserialize(topic, bytes);
    }

    public abstract Schema toSchema(String topic);

    private MockSchemaRegistryClient getMockClient(final Schema schema$) {
        return new MockSchemaRegistryClient() {
            public ParsedSchema getSchemaBySubjectAndId(String subject, int id) {
                return new AvroSchema(schema$.toString());
            }
        };
    }
}
