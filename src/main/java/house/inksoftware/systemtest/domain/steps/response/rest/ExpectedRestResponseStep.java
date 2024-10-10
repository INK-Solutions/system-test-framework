package house.inksoftware.systemtest.domain.steps.response.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import house.inksoftware.systemtest.SystemTest;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.Assert;

import java.util.*;

import okhttp3.*;

import static org.junit.Assert.*;

@Data
public class ExpectedRestResponseStep implements ExpectedResponseStep {
    private final int httpCode;
    private final JsonNode body;
    private final JsonNode verification;
    private final Set<String> verificationFieldsSet;
    private final Set<String> allFieldsSet;

    @SneakyThrows
    public static ExpectedRestResponseStep from(String json) {
        var objectMapper = new ObjectMapper();
        var root = objectMapper.readTree(json);

        var httpCode = root.get("httpCode").asInt();
        var body = root.path("body");
        var verification = root.path("verification");

        Set<String> verificationFieldSet = new HashSet<>();
        if (!verification.isMissingNode()) {
            var iterator = verification.elements();
            while (iterator.hasNext()) {
                var next = iterator.next();
                verificationFieldSet.add(next.get("field").asText());
            }
        }
        Set<String> allFieldSet = new HashSet<>();
        allFieldSet = findAllFieldNamesFromJSON(allFieldSet, "", body);
        return new ExpectedRestResponseStep(httpCode, body, verification, verificationFieldSet, allFieldSet);
    }

    @Override
    @SneakyThrows
    public void assertResponseIsCorrect(ActualResponse response) {
        if (verification.isMissingNode()) {
            compareIfExactSame(response);
        } else {
            var objectMapper = new ObjectMapper();
            var actualResponseBody = objectMapper.readTree(response.body());

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
        var fieldsToBeComparedNormally = findAllFieldsToBeComparedNormally();

        fieldsToBeComparedNormally
                .forEach(field -> compareFieldNormally(actualResponseBody, field));
    }

    private void compareFieldNormally(JsonNode actualResponseBody, String field) {
        JsonNode field1 = getNodeByAttributePath(body, field);
        JsonNode field2 = getNodeByAttributePath(actualResponseBody, field);

        assertEquals(field1, field2);
    }

    @NotNull
    private HashSet<String> findAllFieldsToBeComparedNormally() {
        var result = new HashSet<>(allFieldsSet);
        result.removeAll(verificationFieldsSet);

        return result;
    }

    private void compareUsingVerification(JsonNode actualResponseBody) {
        var verificationSteps = verification.elements();

        while (verificationSteps.hasNext()) {
            var verificationStep = verificationSteps.next();

            var field1 = getNodeByAttributePath(body, verificationStep.path("field").asText());
            var field2 = getNodeByAttributePath(actualResponseBody, verificationStep.path("field").asText());

            var verificationType = verificationStep.path("type").asText();
            if (verificationType.equals("cosine-similarity")) {
                compareByCosineSimilarity(verificationStep, field1, field2);
            } else {
                assertEquals(field1, field2);
            }
        }
    }

    private static Set<String> findAllFieldNamesFromJSON(Set<String> result, String currentField, JsonNode node) {
        var isLeaf = !node.isContainerNode();

        if (isLeaf) {
            result.add(currentField);
            return result;
        } else {
            result = pathFurtherNodes(result, currentField, node);
        }
        return result;
    }

    private static Set<String> pathFurtherNodes(Set<String> result, String currentField, JsonNode node) {
        var isArrayNode = node.isArray();

        if (isArrayNode) {
            result = findAllFieldsFromJSONArray(result, currentField, node);
        } else {
            result = findAllFieldsFromJSONDictionary(result, currentField, node);
        }
        return result;
    }

    private static Set<String> findAllFieldsFromJSONDictionary(Set<String> result, String currentField, JsonNode node) {
        var fields = node.fieldNames();
        while(fields.hasNext()) {
            var field = fields.next();
            var nextField = currentField.isBlank() ? field : currentField + "." + field;
            result = findAllFieldNamesFromJSON(result, nextField, node.path(field));
        }
        return result;
    }

    private static Set<String> findAllFieldsFromJSONArray(Set<String> result, String currentField, JsonNode node) {
        int size = node.size();

        for (int i=0; i<size; i++) {
            var next = node.get(i);
            result = findAllFieldNamesFromJSON(result, currentField +"["+i+"]", next);
        }

        return result;
    }

    private static void compareByCosineSimilarity(JsonNode verificationStep, JsonNode field1, JsonNode field2) {
        double minSimilarityThreshold = verificationStep.path("minSimilarityThreshold").asDouble();

        var vector1 = callAdaModel(field1.asText());
        var vector2 = callAdaModel(field2.asText());

        double similarity = cosineSimilarityCalculation(vector1, vector2);

        assertTrue("texts are not similar ("+field1.asText()+", "+field2.asText()+"), similarity: "+ similarity, similarity >= minSimilarityThreshold);
    }

    @SneakyThrows
    private static Double[] callAdaModel(String text) {
        String apiKey = SystemTest.getApiKey();
        String url = "https://api.openai.com/v1/embeddings";
        String model = "text-embedding-ada-002";

        String json = String.format("{\"input\": \"%s\", \"model\": \"%s\"}", text, model);
        var body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        var request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-type", "application/json")
                .addHeader("Authorization","Bearer "+apiKey)
                .build();

        var client = new OkHttpClient();
        try (var response = client.newCall(request).execute()) {
            assertNotNull(response.body());
            var responseBody = response.body().string();

            var objectMapper = new ObjectMapper();
            var jsonNode = objectMapper.readTree(responseBody);
            List<Double> doubles = new LinkedList<>();

            var iterator = jsonNode.get("data").get(0).get("embedding").elements();
            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                doubles.add(next.asDouble());
            }

            return doubles.toArray(Double[]::new);
        }
    }

    public static double cosineSimilarityCalculation(Double[] vector1, Double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private JsonNode getNodeByAttributePath(JsonNode body, String path) {
        var attributes = path.split("\\.");
        var result = body;

        for(String attribute : attributes) {
            var isArrayIndexed = attribute.contains("[");
            if (isArrayIndexed) {
                result = pathArrayIndex(attribute, result);
            } else {
                result = pathNormalAttribute(attribute, result);
            }
        }

        return result;
    }

    private static JsonNode pathNormalAttribute(String attribute, JsonNode result) {
        result = result.path(attribute);

        return result;
    }

    private static JsonNode pathArrayIndex(String attribute, JsonNode result) {
        var index = Integer.parseInt(attribute.substring(attribute.indexOf("[")+1, attribute.indexOf("]")));
        var variableName = attribute.substring(0, attribute.indexOf("["));

        result = result.path(variableName).get(index);

        return result;
    }

    private void compareIfExactSame(ActualResponse response) throws JSONException {
        JsonUtils.assertJsonEquals(body.toString(), response.body());
        Assert.assertEquals(((ActualRestResponse) response).getStatusCode(), httpCode);
    }
}
