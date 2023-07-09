package house.inksoftware.systemtest.domain.steps.request.executable.db;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExecutableDatabaseRequestStepFactory  {
    private final String query;

    public static ExecutableDatabaseRequestStep create(RequestStep requestStep,
                                                       SystemTestContext context) throws Exception {
        DocumentContext documentContext = JsonPath.parse(requestStep.getJson());

        return new ExecutableDatabaseRequestStep(
                documentContext.read("query"),
                documentContext.read("resultVariableName"),
                context
        );
    }

}
