
package house.inksoftware.systemtest.domain.steps.response.rest;

import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class ActualRestResponse extends ActualResponse {
    private final HttpStatus statusCode;
    private final String body;

    public ActualRestResponse(HttpStatus statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    @Override
    public String body() {
        return body;
    }
}
