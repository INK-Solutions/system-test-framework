package house.inksoftware.systemtest.domain.steps.response.sns;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SnsConfiguration;
import house.inksoftware.systemtest.domain.sns.SnsConsumerService;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import lombok.Data;

@Data
public class ExpectedSnsResponseStep implements ExpectedResponseStep {
    private final String topicName;
    private final String expectedBody;
    private final SnsConsumerService snsConsumerService;

    public static ExpectedSnsResponseStep from(String json, SnsConfiguration snsConfiguration) {
        DocumentContext documentContext = JsonPath.parse(json);

        return new ExpectedSnsResponseStep(
                documentContext.read("topic"),
                JsonPath.parse((Object) documentContext.read("body")).jsonString(),
                snsConfiguration.getSnsConsumerService()
        );
    }

    @Override
    public void assertResponseIsCorrect(ActualResponse actualResponse) {
        snsConsumerService.find(topicName, expectedBody);
    }
}
