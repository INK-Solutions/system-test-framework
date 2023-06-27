package house.inksoftware.systemtest.domain.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Resources;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class TestUtils {
    public static String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T fromJson(String json, Class<T> type) {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .readValue(json, type);
    }

    public static String readFileContent(String filePath) throws Exception {
        return Resources.toString(Resources.getResource( filePath), StandardCharsets.UTF_8);
    }

    public static File getFile(String filePath) {
        return new File(Resources.getResource(filePath).getPath());
    }
}
