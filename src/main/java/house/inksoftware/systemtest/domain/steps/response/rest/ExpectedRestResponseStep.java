package house.inksoftware.systemtest.domain.steps.response.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.junit.Assert;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

@Data
public class ExpectedRestResponseStep implements ExpectedResponseStep {
    private final int httpCode;
    private final JsonNode body;
    private final JsonNode verification;
    private final Set<String> verificationFieldSet;
    private final Set<String> allFieldSet;

    @SneakyThrows
    public static ExpectedRestResponseStep from(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);

        int httpCode = root.get("httpCode").asInt();
        JsonNode body = root.path("body");
        JsonNode verification = root.path("verification");

        Set<String> verificationFieldSet = new HashSet<>();
        if(!verification.isMissingNode()) {
            Iterator<JsonNode> iterator = verification.elements();
            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                verificationFieldSet.add(next.get("field").asText());
            }
        }
        Set<String> allFieldSet = new HashSet<>();
        allFieldSet = getAllFieldNames(allFieldSet, "", body);
        return new ExpectedRestResponseStep(httpCode, body, verification, verificationFieldSet, allFieldSet);
    }

    @Override
    @SneakyThrows
    public void assertResponseIsCorrect(ActualResponse response) throws JSONException {
        if(verification.isMissingNode()) {
            compareIfExactSame(response);
        } else {
            // use verification
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode actualResponseBody = objectMapper.readTree(response.body());

            compareHttpStatuses(actualResponseBody);

            actualResponseBody = actualResponseBody.path("body");

            compareUsingVerification(actualResponseBody);
            compareNormalWay(actualResponseBody);
        }

    }

    private void compareHttpStatuses(JsonNode actualResponseBody) {
        assertEquals(httpCode, actualResponseBody.get("httpCode").asInt());
    }

    private void compareNormalWay(JsonNode actualResponseBody) {
        Set<String> toBeComparedNormaly = new HashSet<>(allFieldSet);
        toBeComparedNormaly.removeAll(verificationFieldSet);

        for(String field : toBeComparedNormaly) {
            JsonNode field1 = getValueByAttributePath(body, field);
            JsonNode field2 = getValueByAttributePath(actualResponseBody, field);

            assertEquals(field1, field2);
        }
    }

    private void compareUsingVerification(JsonNode actualResponseBody) {
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

    private static Set<String> getAllFieldNames(Set<String> result, String currentField, JsonNode node) {
        if(!node.isContainerNode()) {
            result.add(currentField);
            return result;
        } else {
            if(node.isArray()) {
                // if array
                int size = node.size();
                for(int i=0; i<size; i++) {
                    JsonNode next = node.get(i);
                    result = getAllFieldNames(result, currentField+"["+i+"]", next);
                }
            } else {
                // if dict
                Iterator<String> fields = node.fieldNames();
                while(fields.hasNext()) {
                    String field = fields.next();
                    String nextField = currentField.isBlank() ? field : currentField + "." + field;
                    result = getAllFieldNames(result, nextField, node.path(field));
                }
            }
        }
        return result;
    }

    private static void compareByCosineSimilarity(JsonNode verificationStep, JsonNode field1, JsonNode field2) {
        double minSimilarityThreshold = verificationStep.path("minSimilarityThreshold").asDouble();
        StringMetric cosineSimilarity = StringMetrics.cosineSimilarity();
        float similarity = cosineSimilarity.compare(field1.asText(), field2.asText());
        assertTrue("texts are not similar ("+field1.asText()+", "+field2.asText()+"), similarity: "+String.valueOf(similarity), similarity >= minSimilarityThreshold);
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
