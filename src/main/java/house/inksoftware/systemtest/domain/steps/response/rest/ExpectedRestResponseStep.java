package house.inksoftware.systemtest.domain.steps.response.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import house.inksoftware.systemtest.SystemTest;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;

import org.json.JSONException;
import org.junit.Assert;

import java.io.IOException;
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
        allFieldSet = getAllFieldNamesFromJSON(allFieldSet, "", body);
        return new ExpectedRestResponseStep(httpCode, body, verification, verificationFieldSet, allFieldSet);
    }

    @Override
    @SneakyThrows
    public void assertResponseIsCorrect(ActualResponse response) throws JSONException {
        if(verification.isMissingNode()) {
            compareIfExactSame(response);
        } else {
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
        Set<String> toBeComparedNormaly = new HashSet<>(allFieldsSet);
        toBeComparedNormaly.removeAll(verificationFieldsSet);

        for(String field : toBeComparedNormaly) {
            JsonNode field1 = getNodeByAttributePath(body, field);
            JsonNode field2 = getNodeByAttributePath(actualResponseBody, field);

            assertEquals(field1, field2);
        }
    }

    private void compareUsingVerification(JsonNode actualResponseBody) {
        Iterator<JsonNode> verificationSteps = verification.elements();

        while(verificationSteps.hasNext()) {
            JsonNode verificationStep = verificationSteps.next();

            JsonNode field1 = getNodeByAttributePath(body, verificationStep.path("field").asText());
            JsonNode field2 = getNodeByAttributePath(actualResponseBody, verificationStep.path("field").asText());

            String verificationType = verificationStep.path("type").asText();
            if (verificationType.equals("cosine-similarity")) {
                compareByCosineSimilarity(verificationStep, field1, field2);
            } else {
                assertEquals(field1, field2);
            }
        }
    }

    private static Set<String> getAllFieldNamesFromJSON(Set<String> result, String currentField, JsonNode node) {
        if(!node.isContainerNode()) {
            result.add(currentField);
            return result;
        } else {
            if(node.isArray()) {
                int size = node.size();
                for(int i=0; i<size; i++) {
                    JsonNode next = node.get(i);
                    result = getAllFieldNamesFromJSON(result, currentField+"["+i+"]", next);
                }
            } else {
                Iterator<String> fields = node.fieldNames();
                while(fields.hasNext()) {
                    String field = fields.next();
                    String nextField = currentField.isBlank() ? field : currentField + "." + field;
                    result = getAllFieldNamesFromJSON(result, nextField, node.path(field));
                }
            }
        }
        return result;
    }

    private static void compareByCosineSimilarity(JsonNode verificationStep, JsonNode field1, JsonNode field2) {
        double minSimilarityThreshold = verificationStep.path("minSimilarityThreshold").asDouble();

        Double[] vector1 = callAdaModel(field1.asText());
        Double[] vector2 = callAdaModel(field2.asText());

        double similarity = cosineSimilarityCalculation(vector1, vector2);

        assertTrue("texts are not similar ("+field1.asText()+", "+field2.asText()+"), similarity: "+String.valueOf(similarity), similarity >= minSimilarityThreshold);
    }

    @SneakyThrows
    private static Double[] callAdaModel(String text) {
        String apiKey = SystemTest.getApiKey();
        String url = "https://api.openai.com/v1/embeddings";
        String model = "text-embedding-ada-002";

        String json = String.format("{\"input\": \"%s\", \"model\": \"%s\"}", text, model);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-type", "application/json")
                .addHeader("Authorization","Bearer "+apiKey)
                .build();

        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            Iterator<JsonNode> iterator = jsonNode.get("data").get(0).get("embedding").elements();
            List<Double> doubles = new LinkedList<>();
            while(iterator.hasNext()) {
                JsonNode next = iterator.next();
                doubles.add(next.asDouble());
            }
            return doubles.toArray(new Double[0]);
        }
    }

    public static double cosineSimilarityCalculation(Double[] v1, Double[] v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private JsonNode getNodeByAttributePath(JsonNode body, String path) {
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
