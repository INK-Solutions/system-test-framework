package house.inksoftware.systemtest.domain.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void assertJsonEquals(String expected, String actual) throws JSONException {
        String normalizedExpected = normalizeNumbers(expected);
        String normalizedActual = normalizeNumbers(actual);
        if (normalizedExpected.startsWith("[")) {
            JSONAssert.assertEquals(new JSONArray(normalizedExpected), new JSONArray(normalizedActual), false);
        } else {
            JSONAssert.assertEquals(new JSONObject(normalizedExpected), new JSONObject(normalizedActual), false);
        }
    }

    private static String normalizeNumbers(String json) {
        try {
            return OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }

    public static boolean isEqual(String expected, String actual) {
        try {
            assertJsonEquals(expected, actual);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String buildDynamicJson(String text, Map<String, Object> values) {
        MustacheFactory factory = new DefaultMustacheFactory();
        Mustache mustache = factory.compile(new StringReader(text), UUID.randomUUID().toString());
        StringWriter writer = new StringWriter();
        try {
            FailOnMissingKeyMap map = new FailOnMissingKeyMap();
            map.putAll(values);
            mustache.execute(writer, map).flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }


    public static boolean hasPath(DocumentContext context, String key) {
        try {
            context.read(key);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }


    public static class FailOnMissingKeyMap extends HashMap<String, Object> {
        @Override
        public boolean containsKey(Object key) {
            if (!super.containsKey(key)) {
                missingKey(key);
            }
            return super.containsKey(key);
        }

        @Override
        public Object get(Object key) {
            if (!super.containsKey(key)) {
                missingKey(key);
            }
            return super.get(key);
        }

        private void missingKey(Object key) {
            throw new IllegalStateException("Missing required placeholder: " + key + ". Make sure you store it in callback context for one of previous calls. More info: https://github.com/INK-Solutions/system-test-framework#callbacks");
        }
    }
}
