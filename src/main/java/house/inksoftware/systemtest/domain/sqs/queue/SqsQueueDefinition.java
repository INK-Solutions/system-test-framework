package house.inksoftware.systemtest.domain.sqs.queue;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class SqsQueueDefinition {

    private final String name;
    private final Type type;

    @SneakyThrows
    public static SqsQueueDefinition create(Map<String, String> data) {
        var type = Type.fromShortName(data.get("type"));
        var name = data.get("name");
        return new SqsQueueDefinition(name, type);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        FIFO("fifo"),
        STANDARD("standard");

        private final String shortName;

        public static Type fromShortName(String shortName) {
            return Arrays.stream(Type.values())
                    .filter(entry -> entry.shortName.equals(shortName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Sqs queue type " + shortName));
        }
    }

}
