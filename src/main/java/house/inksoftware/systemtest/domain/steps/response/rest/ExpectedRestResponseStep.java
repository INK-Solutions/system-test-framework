package house.inksoftware.systemtest.domain.steps.response.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import java.util.stream.IntStream;

import okhttp3.*;

import static org.junit.Assert.*;

@Data
public class ExpectedRestResponseStep implements ExpectedResponseStep {
    private final int httpCode;
    private final JsonNode body;
    private final String bodyAsJson;
    private final JsonNode verification;
    private final Set<String> verificationFieldsSet;
    private final Set<String> allFieldsSet;

    @SneakyThrows
    public static ExpectedRestResponseStep from(String json) {
        var objectMapper = new ObjectMapper();
        var root = objectMapper.readTree(json);

        int httpCode = JsonPath.parse(json).read("httpCode");
        var body = root.path("body");
        var bodyAsJson = JsonPath.parse((Object) JsonPath.parse(json).read("body")).jsonString();
        var verification = root.path("verification");

        Set<String> verificationFieldSet = new HashSet<>();
        if (!verification.isMissingNode()) {
            var iterator = verification.elements();
            iterator.forEachRemaining(field -> verificationFieldSet.add(field.get("field").asText()));
        }
        Set<String> allFieldSet = new HashSet<>();
        allFieldSet = findAllFieldNamesFromJSON(allFieldSet, "", body);
        return new ExpectedRestResponseStep(httpCode, body, bodyAsJson, verification, verificationFieldSet, allFieldSet);
    }

    @Override
    @SneakyThrows
    public void assertResponseIsCorrect(ActualResponse response) {
        if (verification.isMissingNode()) {
            compareIfExactSame(response);
            return;
        }
        var objectMapper = new ObjectMapper();
        var actualResponseBody = objectMapper.readTree(response.body());

        compareHttpStatuses(response);

        compareUsingVerification(actualResponseBody);
        compareIfExactSame(actualResponseBody);

    }

    private void compareHttpStatuses(ActualResponse response) {
        assertEquals(httpCode, ((ActualRestResponse) response).getStatusCode());
    }

    private void compareIfExactSame(JsonNode actualResponseBody) {
        var fieldsToBeComparedIfIdentical = findAllFieldsToBeComparedIfIdentical();

        fieldsToBeComparedIfIdentical
                .forEach(field -> compareFieldIfIdentical(actualResponseBody, field));
    }

    private void compareFieldIfIdentical(JsonNode actualResponseBody, String field) {
        JsonNode field1 = getNodeByAttributePath(body, field);
        JsonNode field2 = getNodeByAttributePath(actualResponseBody, field);

        assertEquals(field1, field2);
    }

    @NotNull
    private HashSet<String> findAllFieldsToBeComparedIfIdentical() {
        var result = new HashSet<>(allFieldsSet);
        result.removeAll(verificationFieldsSet);

        return result;
    }

    private void compareUsingVerification(JsonNode actualResponseBody) {
        var verificationSteps = verification.elements();

        verificationSteps.forEachRemaining(verificationStep -> {

            var field1 = getNodeByAttributePath(body, verificationStep.path("field").asText());
            var field2 = getNodeByAttributePath(actualResponseBody, verificationStep.path("field").asText());

            var verificationType = verificationStep.path("type").asText();
            if (verificationType.equals("cosine-similarity")) {
                compareByCosineSimilarity(verificationStep, field1, field2);
            } else {
                assertEquals(field1, field2);
            }
        });
    }

    private static Set<String> findAllFieldNamesFromJSON(Set<String> result, String currentField, JsonNode node) {
        var isLeaf = !node.isContainerNode();

        if (isLeaf) {
            result.add(currentField);
            return result;
        }
        
        return pathFurtherNodes(result, currentField, node);
    }

    private static Set<String> pathFurtherNodes(Set<String> result, String currentField, JsonNode node) {
        var isArrayNode = node.isArray();

        if (isArrayNode) {
            return findAllFieldsFromJSONArray(result, currentField, node);
        }
        return findAllFieldsFromJSONDictionary(result, currentField, node);
    }

    private static Set<String> findAllFieldsFromJSONDictionary(Set<String> result, String currentField, JsonNode node) {
        var fields = node.fieldNames();

        fields.forEachRemaining(field -> {
            var nextField = currentField.isBlank() ? field : currentField + "." + field;
            result.addAll(findAllFieldNamesFromJSON(result, nextField, node.path(field)));
        });

        return result;
    }

    private static Set<String> findAllFieldsFromJSONArray(Set<String> result, String currentField, JsonNode node) {

        IntStream.range(0, node.size()).forEach(i -> {
            result.addAll(findAllFieldNamesFromJSON(result, currentField + "[" + i + "]", node.get(i)));
        });

        return result;
    }

    private static void compareByCosineSimilarity(JsonNode verificationStep, JsonNode field1, JsonNode field2) {
        double minSimilarityThreshold = verificationStep.path("minSimilarityThreshold").asDouble();

        var vector1 = callAdaModel(field1.asText());
        var vector2 = callAdaModel(field2.asText());

        double similarity = calculateCosineSimilarity(vector1, vector2);

        assertTrue(
                String.format("Texts are not similar ( %s, %s ), similarity: %.2f", field1.asText(), field2.asText(), similarity),
                similarity >= minSimilarityThreshold);
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
            iterator.forEachRemaining(number -> doubles.add(number.asDouble()));

            return doubles.toArray(Double[]::new);
        }
    }

    public static double calculateCosineSimilarity(Double[] vector1, Double[] vector2) {

        var dotProduct = IntStream.range(0, vector1.length)
                .mapToDouble(i -> vector1[i] * vector2[i])
                .sum();

        var norm1 = Arrays.stream(vector1)
                .mapToDouble(x -> Math.pow(x, 2))
                .sum();

        var norm2 = Arrays.stream(vector2)
                .mapToDouble(x -> Math.pow(x, 2))
                .sum();

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private JsonNode getNodeByAttributePath(JsonNode body, String path) {
        var attributes = path.split("\\.");

        if(attributes.length == 1 && attributes[0].isEmpty()) {

            return body;

        }

        var result = body;
        String currentAttribute = attributes[0];
        var isArrayIndex = currentAttribute.contains("[");
        var pathWithoutFirstAttribute = String.join(".", Arrays.copyOfRange(attributes, 1, attributes.length));

        if(isArrayIndex) {
            String currentAttributeName = currentAttribute.substring(0, currentAttribute.indexOf("["));
            int index = Integer.parseInt(currentAttribute.substring(currentAttribute.indexOf("[")+1, currentAttribute.indexOf("]")));

            return getNodeByAttributePath(result.path(currentAttributeName).get(index), pathWithoutFirstAttribute);
        }

        return getNodeByAttributePath(result.path(currentAttribute), pathWithoutFirstAttribute);
    }

    @SneakyThrows
    private void compareIfExactSame(ActualResponse response) throws JSONException {
        JsonUtils.assertJsonEquals(bodyAsJson, response.body());
        Assert.assertEquals(((ActualRestResponse) response).getStatusCode(), httpCode);
    }
}
