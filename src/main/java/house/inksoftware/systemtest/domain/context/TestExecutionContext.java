package house.inksoftware.systemtest.domain.context;

import com.google.common.base.Preconditions;
import lombok.Setter;

@Setter
public class TestExecutionContext {
    private String accessToken;

    public String accessToken() {
        Preconditions.checkArgument(accessToken != null, "Tried to get accessToken, but it was null");
        return accessToken;
    }

    public String internalServiceToken() {
        return "system-test-invoice-service-token";
    }

}
