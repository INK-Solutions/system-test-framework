package house.inksoftware.systemtest.domain.steps.response;

import com.jayway.jsonpath.JsonPath;

public abstract class ActualResponse {
    public abstract String body();

    public String read(String path) {
        String result = JsonPath.parse(body()).read(path, String.class);
        if (result == null) {
            throw new IllegalArgumentException(path + " not found in " + body());
        }
        return result;
    }

    public boolean has(String path) {
        return JsonPath.parse(body()).read(path, String.class) != null;
    }
}
