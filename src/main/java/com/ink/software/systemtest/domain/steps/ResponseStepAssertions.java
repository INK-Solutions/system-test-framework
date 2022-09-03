package com.ink.software.systemtest.domain.steps;

import com.ink.software.systemtest.domain.utils.JsonUtils;
import com.ink.software.systemtest.domain.utils.RestUtils;
import org.json.JSONException;
import org.junit.Assert;

public class ResponseStepAssertions {
    public static void assertResponseIsCorrect(RestUtils.RestResponse restResponse, ResponseStep responseStep) throws JSONException {
        JsonUtils.assertJsonEquals(responseStep.getBody(), restResponse.getBody());
        Assert.assertEquals(restResponse.getStatus().value(), responseStep.getHttpCode());
    }
}
