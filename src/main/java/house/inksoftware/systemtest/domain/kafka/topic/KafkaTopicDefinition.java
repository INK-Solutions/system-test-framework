package house.inksoftware.systemtest.domain.kafka.topic;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.avro.Schema;

import java.util.Arrays;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class KafkaTopicDefinition {

    private final String name;
    private final Class clazz;
    private final Schema schema;
    private final Direction direction;

    @SneakyThrows
    public static KafkaTopicDefinition create(Map<String, String> data) {
        Class clazz = Class.forName((data).get("schema"));
        Schema schema = (Schema) clazz.getField("SCHEMA$").get(Schema.class);

        return new KafkaTopicDefinition(
                data.get("name"),
                clazz,
                schema,
                Direction.findByShortName(data.get("direction"))
        );
    }

    @RequiredArgsConstructor
    public enum Direction {
        READ("read"),
        PUBLISH("publish");

        private final String shortName;


        public static Direction findByShortName(String shortName) {
            return Arrays.stream(Direction.values())
                    .filter(entry -> entry.shortName.equals(shortName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Direction with name " + shortName + " not found. " +
                            "Please make sure that your direction for kafka topics is either read/publish"));
        }

    }
}
