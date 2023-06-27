package house.inksoftware.systemtest.domain.steps.request.executable.rest;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.request.executable.ExecutableRequestStep;
import house.inksoftware.systemtest.domain.steps.response.rest.ActualRestResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class ExecutableRestRequestStep implements ExecutableRequestStep {
    private final String path;
    private final HttpMethod httpMethod;
    private final String body;
    private final Map<String, String> headers;
    private final FormData formData;

    public ActualResponse execute(SystemTestConfiguration config) {
        return makeApiCall(config);
    }

    private ActualRestResponse makeApiCall(SystemTestConfiguration config) {
        String fullPath = config.getRestConfiguration().getHost() + ":" + config.getRestConfiguration().getPort() + "/" + this.path;

        ResponseEntity<String> response = config
                .getRestConfiguration()
                .getRestTemplate()
                .exchange(fullPath, getHttpMethod(), toEntity(), String.class);

        return new ActualRestResponse(response.getStatusCode(), response.getBody() == null ? "{}" : response.getBody());
    }

    private HttpEntity<Object> toEntity() {
        HttpHeaders headers = new HttpHeaders();
        this.headers.forEach(headers::set);

        if (hasFormData()) {
            LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();

            if (formData.getAttachment() != null) {
                parameters.add(
                        formData.getAttachment().getParamName(),
                        new ClassPathResource(formData.getAttachment().getFilePath())
                );
            }

            formData.getParams().forEach(param -> parameters.add(param.getKey(), param.getValue()));

            return new HttpEntity<>(parameters, headers);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new HttpEntity<>(body.equals("{}") ? null : body, headers);
        }
    }

    public boolean hasFormData() {
        return formData != null;
    }

    @Data
    public static class FormData {
        private final Attachment attachment;
        private final List<FormParam> params;

        @Data
        public static class FormParam {
            private final String key;
            private final Object value;
        }
    }

    @Data
    public static class Attachment {
        private final String paramName;
        private final String filePath;
    }
}
