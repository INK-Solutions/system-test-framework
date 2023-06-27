package house.inksoftware.systemtest.domain.config.infra.kafka;

import org.apache.avro.Schema;

public class AvroSchemaWithClass {
    private final Schema schema;
    private final Class clazz;

    public AvroSchemaWithClass(Schema schema, Class clazz) {
        this.schema = schema;
        this.clazz = clazz;
    }

    public Schema getSchema() {
        return schema;
    }

    public Class getClazz() {
        return clazz;
    }
}
