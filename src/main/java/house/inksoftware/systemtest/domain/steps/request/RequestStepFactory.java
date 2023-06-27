
package house.inksoftware.systemtest.domain.steps.request;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.RequestResponseFileFinder;
import house.inksoftware.systemtest.domain.utils.FileUtils;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;

import java.io.File;
import java.util.Map;

import static house.inksoftware.systemtest.domain.utils.JsonUtils.buildDynamicJson;

@Data
public class RequestStepFactory {

    public RequestStep toStep(File stepFile, SystemTestContext context) throws Exception {
        String json = buildDynamicJson(FileUtils.readFile(RequestResponseFileFinder.findRequest(stepFile)), context.getParams());
        DocumentContext documentContext = JsonPath.parse(json);

        RequestStep.RequestStepBuilder requestBuilder = RequestStep
                .builder(stepFile.getName(), json);

        if (JsonUtils.hasPath(documentContext, "callback")) {
            Map<String, String> callbackParams = documentContext.read("callback");
            requestBuilder.callbackFunction(response -> {
                callbackParams.forEach((name, path) -> {
                    Preconditions.checkState(response.has(path), "Callback for path " + path + " was requested, but path wasn't found in response: ", response);
                    context.put(name, response.read(path));
                });
            });
        }
        return requestBuilder.build();
    }


}
