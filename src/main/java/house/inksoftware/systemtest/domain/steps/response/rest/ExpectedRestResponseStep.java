package house.inksoftware.systemtest.domain.steps.response.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.json.JSONException;
import org.junit.Assert;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.search.similarities.ClassicSimilarity;

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
        allFieldSet = getAllFieldNamesFromJSON(allFieldSet, "", body);
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
                // if array
                int size = node.size();
                for(int i=0; i<size; i++) {
                    JsonNode next = node.get(i);
                    result = getAllFieldNamesFromJSON(result, currentField+"["+i+"]", next);
                }
            } else {
                // if dict
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

        double similarity = cosineSimilarityCalculation(field1.asText(), field2.asText());

        assertTrue("texts are not similar ("+field1.asText()+", "+field2.asText()+"), similarity: "+String.valueOf(similarity), similarity >= minSimilarityThreshold);
    }

    @SneakyThrows
    private static double cosineSimilarityCalculation(String s1, String s2) {
        Directory directory = new ByteBuffersDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, config);

        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        fieldType.setTokenized(true);
        fieldType.setStored(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStoreTermVectorOffsets(true);

        Document document1 = new Document();
        document1.add(new Field("s1", s1, fieldType));
        writer.addDocument(document1);

        Document document2 = new Document();
        document2.add(new Field("s2", s2, fieldType));
        writer.addDocument(document2);

        writer.close();

        DirectoryReader reader = DirectoryReader.open(directory);
        ClassicSimilarity similarity = new ClassicSimilarity();

        Terms terms1 = reader.getTermVectors(0).terms("s1");
        Terms terms2 = reader.getTermVectors(1).terms("s2");

        if (terms1 == null || terms2 == null) {
            return 0.0; // One of the documents has no terms
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        TermsEnum termsEnum1 = terms1.iterator();
        while (termsEnum1.next() != null) {
            String term = termsEnum1.term().utf8ToString();
            int freq1 = (int) termsEnum1.docFreq();
            int freq2 = 0;

            TermsEnum termsEnum2 = terms2.iterator();
            while (termsEnum2.next() != null) {
                if (term.equals(termsEnum2.term().utf8ToString())) {
                    freq2 = (int) termsEnum2.docFreq();
                    break;
                }
            }

            dotProduct += freq1 * freq2;
            magnitude1 += freq1 * freq1;
            magnitude2 += freq2 * freq2;
        }

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }

        // Calculate cosine similarity
        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
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
