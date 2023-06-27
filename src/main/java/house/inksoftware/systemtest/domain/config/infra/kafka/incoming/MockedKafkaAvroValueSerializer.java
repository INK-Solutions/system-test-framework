package house.inksoftware.systemtest.domain.config.infra.kafka.incoming;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;

public abstract class MockedKafkaAvroValueSerializer extends KafkaAvroSerializer {
    public MockedKafkaAvroValueSerializer() {
        super();
        super.schemaRegistry = new MockSchemaRegistryClient();
    }

    @Override
    public byte[] serialize(String topic, Object record) {
        SpecificRecordBase specificRecordBase = record instanceof SpecificRecordBase ? (SpecificRecordBase) record : toSpecificRecord(topic, (String) record);
        AvroSchema schema = new AvroSchema(
                AvroSchemaUtils.getSchema(record, useSchemaReflection,
                        avroReflectionAllowNull, removeJavaProperties));
        return serializeImpl(getSubjectName(topic, false, specificRecordBase, schema), record, schema);
    }

    public abstract Schema toSchema(String topic);
    public abstract SpecificRecordBase toSpecificRecord(String topic, String json);
}