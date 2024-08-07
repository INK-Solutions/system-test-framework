package house.inksoftware.systemtest.domain.config.infra.sns;

import static house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Type.FIFO;
import static house.inksoftware.systemtest.domain.sns.SnsTopicDefinition.Type.STANDARD;

import house.inksoftware.systemtest.domain.sns.SnsTopicDefinition;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

public class SnsTopicFactory {

    public void create(SnsClient snsClient, List<SnsTopicDefinition> topicDefinitions) {
        topicDefinitions.forEach(topicDefinition -> create(snsClient, topicDefinition));
    }

    private void create(SnsClient snsClient, SnsTopicDefinition topicDefinition) {
        var createTopicRequestBuilder = CreateTopicRequest.builder()
            .name(topicDefinition.fullName());
        
        if (topicDefinition.getType().equals(FIFO)) {
            createTopicRequestBuilder.attributes(Map.of(
                    "FifoTopic", "true",
                    "ContentBasedDeduplication", "true"
            ));
        } else if (topicDefinition.getType().equals(STANDARD)) {
            createTopicRequestBuilder.attributes(Map.of());
        } else {
            throw new IllegalArgumentException("Not supported SNS topic type: " + topicDefinition.getType());
        }
        
        var request = createTopicRequestBuilder.build();
        var response = snsClient.createTopic(request);
    }

}
