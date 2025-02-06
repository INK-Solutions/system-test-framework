package house.inksoftware.systemtest.domain.steps.response.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.junit.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ExpectedRestResponseStep implements ExpectedResponseStep {
    private final int httpCode;
    private final String body;
    private final List<BodyCheck> bodyChecks;


    public static ExpectedRestResponseStep from(String json) {
        DocumentContext context = JsonPath.parse(json);
        int httpCode = context.read("httpCode");
        String body = JsonPath.parse((Object) context.read("body")).jsonString();
        List<BodyCheck> bodyChecks = parseBodyChecks(context);

        return new ExpectedRestResponseStep(httpCode, body, bodyChecks);
    }

    @Override
    @SneakyThrows
    public void assertResponseIsCorrect(ActualResponse response) {
        JsonUtils.assertJsonEquals(body, response.body());
        Assert.assertEquals(((ActualRestResponse) response).getStatusCode(), httpCode);

        if (!bodyChecks.isEmpty()) {
            validateBodyChecks(response);
        }
    }

    private static List<BodyCheck> parseBodyChecks(DocumentContext context) {
        try {
            List<Map<String, Object>> checks = context.read("bodyChecks");
            return checks.stream()
                    .map(checkMap -> BodyCheck.builder()
                            .path((String) checkMap.get("path"))
                            .type(BodyCheck.ComparisonType.valueOf((String) checkMap.get("type")))
                            .value(checkMap.get("value"))
                            .build())
                    .collect(Collectors.toList());
        } catch (PathNotFoundException e) {
            return Collections.emptyList();
        }
    }

    private void validateBodyChecks(ActualResponse response) {
        DocumentContext responseContext = JsonPath.parse(response.body());

        for (BodyCheck check : bodyChecks) {
            Object actualValue = responseContext.read(check.getPath());
            boolean result = check.validate(actualValue);

            Assert.assertTrue(
                    String.format(
                            "Body check failed for path '%s'. Expected %s %s but got: %s",
                            check.getPath(),
                            check.getType(),
                            check.getValue(),
                            actualValue
                    ),
                    result
            );
        }
    }
}
