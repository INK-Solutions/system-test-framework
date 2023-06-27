
package house.inksoftware.systemtest.domain.steps.request.executable.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;
import house.inksoftware.systemtest.domain.steps.request.executable.rest.ExecutableRestRequestStep.Attachment;
import house.inksoftware.systemtest.domain.steps.request.executable.rest.ExecutableRestRequestStep.FormData;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import net.minidev.json.JSONArray;
import org.springframework.http.HttpMethod;

import java.util.*;

import static com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType.APPLICATION_JSON;

public class ExecutableRestRequestStepFactory {

    public static ExecutableRestRequestStep create(String baseFolder, RequestStep requestStep) throws Exception {
        return create(requestStep.getJson(), baseFolder, requestStep.getName());
    }

    private static ExecutableRestRequestStep create(String json, String basePath, String stepName) {
        DocumentContext documentContext = JsonPath.parse(json);

        String url = documentContext.read("url");
        HttpMethod httpMethod = HttpMethod.valueOf(documentContext.read("method"));
        Map<String, String> headers = toHeaders(documentContext);
        String body = toBody(documentContext, headers);
        FormData formData = toFormData(documentContext, basePath, stepName);

        return new ExecutableRestRequestStep(url, httpMethod, body, headers, formData);
    }

    private static String toBody(DocumentContext documentContext, Map<String, String> headers) {
        String contentType = headers.getOrDefault("Content-Type", APPLICATION_JSON.getMimeType());
        if (contentType.equals(APPLICATION_JSON.getMimeType())) {
            return JsonPath.parse((Object) documentContext.read("body")).jsonString();
        } else {
            return documentContext.read("body");
        }
    }


    private static Map<String, String> toHeaders(DocumentContext documentContext) {
        if (JsonUtils.hasPath(documentContext, "headers")) {
            return documentContext.read("headers");
        } else {
            return new HashMap<>();
        }

    }

    private static FormData toFormData(DocumentContext documentContext, String basePath, String stepName) {
        if (JsonUtils.hasPath(documentContext, "form")) {
            List<FormData.FormParam> formParams = new ArrayList<>();
            LinkedHashMap<String, String> params = (LinkedHashMap<String, String>) ((JSONArray) documentContext.read("form.params")).get(0);
            params.forEach((key, value) -> formParams.add(new FormData.FormParam(key, value)));

            return new FormData(toAttachment(documentContext, basePath, stepName), formParams);
        } else {
            return null;
        }
    }

    private static Attachment toAttachment(DocumentContext documentContext, String basePath, String stepName) {
        if (JsonUtils.hasPath(documentContext, "form.attachment")) {
            Object attachment = documentContext.read("form.attachment");
            String paramName = JsonPath.parse(attachment).read("name");
            String attachmentFileName = JsonPath.parse(attachment).read("file");
            String fullPath = basePath + stepName + "/request/attachment/" + attachmentFileName;

            return new Attachment(paramName, fullPath);
        } else {
            return null;
        }
    }
}
