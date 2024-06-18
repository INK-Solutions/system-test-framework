package house.inksoftware.systemtest.domain.steps.request.executable.sqs;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;

public class ExecutableSqsRequestStepFactory {

    public static ExecutableSqsRequestStep create(RequestStep requestStep) throws Exception {
        return create(requestStep.getJson());
    }

    private static ExecutableSqsRequestStep create(String json) {
        DocumentContext documentContext = JsonPath.parse(json);

        String queueLabel = documentContext.read("queue");
        String body = JsonPath.parse((Object) documentContext.read("body")).jsonString();
        return new ExecutableSqsRequestStep(queueLabel, body);
    }
}
