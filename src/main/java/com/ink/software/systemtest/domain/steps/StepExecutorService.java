package com.ink.software.systemtest.domain.steps;

import com.ink.software.systemtest.domain.context.TestExecutionContext;
import com.ink.software.systemtest.domain.utils.JsonUtils;
import com.ink.software.systemtest.domain.utils.RestUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ink.software.systemtest.domain.steps.ResponseStepAssertions.assertResponseIsCorrect;
import static com.ink.software.systemtest.domain.utils.FileUtils.readFileContent;
import static com.ink.software.systemtest.domain.utils.JsonUtils.buildDynamicJson;


@Data
public class StepExecutorService {
    private static final Logger log = LoggerFactory.getLogger(StepExecutorService.class);

    private final String baseFolder;
    private final int port;
    private final TestExecutionContext testExecutionContext;
    private final TestRestTemplate restTemplate;

    @SneakyThrows
    public void execute(ExecutableStep executableStep) {
        log.info("Executing: step: {} ", executableStep.getName());
        RequestStep requestStep = toRequestStep(executableStep.getName(), executableStep.getRequestPlaceholder());
        RestUtils.RestResponse actualResponse = callApi(requestStep);
        ResponseStep responseStep = toResponseStep(executableStep.getName(), actualResponse, executableStep.getResponsePlaceholders());
        log.info("Expected response: {}, actual response: {}", responseStep.getBody(), actualResponse.getBody());
        assertResponseIsCorrect(actualResponse, responseStep);
        executableStep.getRestResponseCallbackFunction().onResponseReceived(actualResponse);

        log.info("Step: {} executed", executableStep.getName());
    }

    private RequestStep toRequestStep(String stepName, List<JsonUtils.JsonRequestPlaceholder> placeholders) throws Exception {
        Map<String, Object> params = new HashMap<>();
        placeholders.forEach(placeholder -> params.put(placeholder.getName(), placeholder.getValue()));
        String templatedContent = readFileContent(baseFolder + stepName + "/" + "request");

        return RequestStep.from(buildDynamicJson(templatedContent, params), baseFolder, stepName);
    }

    private ResponseStep toResponseStep(String step, RestUtils.RestResponse restResponse, List<JsonUtils.JsonResponsePlaceholder> placeholders) throws Exception {
        Map<String, Object> params = new HashMap<>();
        placeholders.forEach(placeholder -> params.put(placeholder.getLogicalName(), restResponse.read(placeholder.getJsonPath())));
        String templatedContent = readFileContent(baseFolder + step + "/" + "response");

        return ResponseStep.from(buildDynamicJson(templatedContent, params));
    }

    public RestUtils.RestResponse callApi(RequestStep requestStep) {
        String url = "http://127.0.0.1:" + port + requestStep.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (requestStep.getApiType() == RequestStep.ApiType.CLIENT) {
            headers.setBearerAuth(testExecutionContext.accessToken());
        } else if (requestStep.getApiType() == RequestStep.ApiType.INTERNAL) {
            headers.set("Authorization", testExecutionContext.internalServiceToken());
        }

        requestStep.getHeaders().forEach(headers::set);

        if (requestStep.hasAttachment()) {
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        }

        return RestUtils.makeApiCall(url, requestStep, restTemplate, headers);
    }
}
