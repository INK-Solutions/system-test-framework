
package house.inksoftware.systemtest.domain.steps.response.rest;

import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActualRestResponse extends ActualResponse {
    private final int statusCode;
    private final String body;

    public ActualRestResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    @Override
    public String body() {
        return body;
    }
}
