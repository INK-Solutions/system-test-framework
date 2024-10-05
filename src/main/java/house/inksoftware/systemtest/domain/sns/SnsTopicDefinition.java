package house.inksoftware.systemtest.domain.sns;

import java.util.Arrays;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Data
@RequiredArgsConstructor
public class SnsTopicDefinition {

    private final String name;
    private final Type type;
    private final Protocol protocol;
    
    public String fullName() {
        return type.equals(Type.FIFO) ? name + ".fifo" : name;
    }
    
    public String defaultSubscriberName() {
        return name + "__default_topic_subscriber";
    }

    @SneakyThrows
    public static SnsTopicDefinition create(Map<String, String> data) {
        var type = Type.fromShortName(data.get("type"));
        var name = data.get("name");
        var protocol = Protocol.fromShortName(data.get("protocol"));
        return new SnsTopicDefinition(name, type, protocol);
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
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Sns topic type " + shortName));
        }
    }    
    
    @Getter
    @RequiredArgsConstructor
    public enum Protocol {
        SQS("sqs");

        private final String shortName;

        public static Protocol fromShortName(String shortName) {
            return Arrays.stream(Protocol.values())
                    .filter(entry -> entry.shortName.equals(shortName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Sns protocol " + shortName));
        }
    }

}
