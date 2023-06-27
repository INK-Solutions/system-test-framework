package house.inksoftware.systemtest.domain.steps.response.rest;

import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.junit.Assert;

@Data
public class ExpectedRestResponseStep implements ExpectedResponseStep {
    private final int httpCode;
    private final String body;

    public static ExpectedRestResponseStep from(String json) {
        int httpCode = JsonPath.parse(json).read("httpCode");
        String body = JsonPath.parse((Object) JsonPath.parse(json).read("body")).jsonString();

        return new ExpectedRestResponseStep(httpCode, body);
    }

    @Override
    public void assertResponseIsCorrect(ActualResponse response) throws JSONException {
        JsonUtils.assertJsonEquals(body, response.body());
        Assert.assertEquals(((ActualRestResponse) response).getStatusCode().value(), httpCode);
    }
}
