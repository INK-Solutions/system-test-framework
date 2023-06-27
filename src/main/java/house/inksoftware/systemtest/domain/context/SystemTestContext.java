package house.inksoftware.systemtest.domain.context;

import java.util.HashMap;
import java.util.Map;

public class SystemTestContext {
    private final Map<String, Object> params = new HashMap<>();

    public void put(String key, Object value) {
        params.put(key, value);
    }

    public Object get(String key) {
        return params.get(key);
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
