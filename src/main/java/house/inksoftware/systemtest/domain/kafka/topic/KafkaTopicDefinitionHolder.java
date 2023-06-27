package house.inksoftware.systemtest.domain.kafka.topic;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KafkaTopicDefinitionHolder {
    private static List<KafkaTopicDefinition> topicDefinitions;

    public static List<KafkaTopicDefinition> getTopicDefinitions() {
        return topicDefinitions;
    }

    public static void setTopicDefinitions(List<KafkaTopicDefinition> topicDefinitions) {
        KafkaTopicDefinitionHolder.topicDefinitions = topicDefinitions;
    }
}
