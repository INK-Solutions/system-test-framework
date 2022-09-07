package house.inksoftware.systemtest.domain.steps;

import com.jayway.jsonpath.JsonPath;
import lombok.Data;

@Data
public class ResponseStep {
    private final int httpCode;
    private final String body;

    public static ResponseStep from(String json) {
        int httpCode = JsonPath.parse(json).read("httpCode");
        String body = JsonPath.parse((Object) JsonPath.parse(json).read("body")).jsonString();

        return new ResponseStep(httpCode, body);
    }
}
