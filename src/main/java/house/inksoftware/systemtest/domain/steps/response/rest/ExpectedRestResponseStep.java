package house.inksoftware.systemtest.domain.steps.response.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Assert;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.*;

@Data
public class ExpectedRestResponseStep implements ExpectedResponseStep {
    private final int httpCode;
    private final JsonNode body;
    private final JsonNode verification;

    @SneakyThrows
    public static ExpectedRestResponseStep from(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);

        int httpCode = root.get("httpCode").asInt();
        JsonNode body = root.path("body");
        JsonNode verification = root.path("verification");

        return new ExpectedRestResponseStep(httpCode, body, verification);
    }

    @Override
    @SneakyThrows
    public void assertResponseIsCorrect(ActualResponse response) throws JSONException {
        if(verification.isMissingNode()) {
            compareIfExactSame(response);
        } else {
            // use verification
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode actualResponseBody = objectMapper.readTree(response.body()).path("body");

            Iterator<JsonNode> verificationSteps = verification.elements();

            while(verificationSteps.hasNext()) {
                JsonNode verificationStep = verificationSteps.next();

                JsonNode field1 = getValueByAttributePath(body, verificationStep.path("field").asText());
                JsonNode field2 = getValueByAttributePath(actualResponseBody, verificationStep.path("field").asText());

                String verificationType = verificationStep.path("type").asText();
                if (verificationType.equals("cosine-similarity")) {
                    compareByCosineSimilarity(verificationStep, field1, field2);
                } else {
                    assertEquals(field1, field2);
                }
            }
        }

    }

    private static void compareByCosineSimilarity(JsonNode verificationStep, JsonNode field1, JsonNode field2) {
        double minSimilarityThreshold = verificationStep.path("minSimilarityThreshold").asDouble();
        StringMetric cosineSimilarity = StringMetrics.cosineSimilarity();
        float similarity = cosineSimilarity.compare(field1.asText(), field2.asText());
        assertTrue("texts are not similar, similarity: "+String.valueOf(similarity), similarity >= minSimilarityThreshold);
    }

    private JsonNode getValueByAttributePath(JsonNode body, String path) {
        String[] attributes = path.split("\\.");
        JsonNode result = body;

        for(String attribute : attributes) {
            if(attribute.contains("[")) {
                int index = Integer.parseInt(attribute.substring(attribute.indexOf("[")+1, attribute.indexOf("]")));
                String variableName = attribute.substring(0, attribute.indexOf("["));

                result = result.path(variableName).get(index);

            } else {
                result = result.path(attribute);
            }
        }

        return result;
    }

    private void compareIfExactSame(ActualResponse response) throws JSONException {
        JsonUtils.assertJsonEquals(body.toString(), response.body());
        Assert.assertEquals(((ActualRestResponse) response).getStatusCode(), httpCode);
    }
}
