package house.inksoftware.systemtest.domain.steps;

import house.inksoftware.systemtest.domain.utils.JsonUtils;
import house.inksoftware.systemtest.domain.utils.RestUtils;
import org.json.JSONException;
import org.junit.Assert;

public class ResponseStepAssertions {
    public static void assertResponseIsCorrect(RestUtils.RestResponse restResponse, ResponseStep responseStep) throws JSONException {
        JsonUtils.assertJsonEquals(responseStep.getBody(), restResponse.getBody());
        Assert.assertEquals(restResponse.getStatus().value(), responseStep.getHttpCode());
    }
}
