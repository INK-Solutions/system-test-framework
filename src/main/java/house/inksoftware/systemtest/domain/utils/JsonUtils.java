package house.inksoftware.systemtest.domain.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

public class JsonUtils {
    public static void assertJsonEquals(String expected, String actual) throws JSONException {
        if (expected.startsWith("[")) {
            JSONAssert.assertEquals(new JSONArray(expected), new JSONArray(actual), false);
        } else {
            JSONAssert.assertEquals(new JSONObject(expected), new JSONObject(actual), false);
        }
    }

    public static String buildDynamicJson(String text, Map<String, Object> values) {
        MustacheFactory factory = new DefaultMustacheFactory();
        Mustache mustache = factory.compile(new StringReader(text), UUID.randomUUID().toString());
        StringWriter writer = new StringWriter();
        try {
            mustache.execute(writer, values).flush();
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


    @Data
    public static class JsonRequestPlaceholder {
        private final String name;
        private final Object value;

        public static JsonRequestPlaceholder of(String name, Object value) {
            return new JsonRequestPlaceholder(name, value);
        }
    }

    @Data
    public static class JsonResponsePlaceholder {
        private final String logicalName;
        private final String jsonPath;
        private final Type type;

        public static JsonResponsePlaceholder ofInt(String logicalName, String jsonPath) {
            return new JsonResponsePlaceholder(logicalName, jsonPath, Type.INT);
        }

        public static JsonResponsePlaceholder ofInt(String nameAndPath) {
            return new JsonResponsePlaceholder(nameAndPath, nameAndPath, Type.INT);
        }

        public static JsonResponsePlaceholder ofString(String logicalName, String jsonPath) {
            return new JsonResponsePlaceholder(logicalName, jsonPath, Type.STRING);
        }

        public static JsonResponsePlaceholder ofString(String nameAndPath) {
            return ofString(nameAndPath, nameAndPath);
        }

        public enum Type {
            INT, STRING
        }
    }
}
