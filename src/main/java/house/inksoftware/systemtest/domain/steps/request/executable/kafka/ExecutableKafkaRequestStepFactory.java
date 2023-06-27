
package house.inksoftware.systemtest.domain.steps.request.executable.kafka;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;

public class ExecutableKafkaRequestStepFactory {

    public static ExecutableKafkaRequestStep create(RequestStep requestStep) throws Exception {
        return create(requestStep.getJson());
    }

    private static ExecutableKafkaRequestStep create(String json) {
        DocumentContext documentContext = JsonPath.parse(json);

        String topicLabel = documentContext.read("topic");
        String body = JsonPath.parse((Object) documentContext.read("body")).jsonString();
        return new ExecutableKafkaRequestStep(topicLabel, body);
    }

}
