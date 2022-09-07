package house.inksoftware.systemtest.domain.utils;

import house.inksoftware.systemtest.domain.steps.RequestStep;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.util.List;

@Component
public class RestUtils {
    private static final String EMPTY_JSON = "{}";

    public static RestResponse makeApiCall(String url,
                                           RequestStep requestStep,
                                           TestRestTemplate restTemplate,
                                           HttpHeaders headers) {
        HttpEntity<Object> entity = toEntity(requestStep, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, requestStep.getHttpMethod(), entity, String.class);
        return new RestResponse(response.getStatusCode(), response.getBody() == null ? EMPTY_JSON : response.getBody());
    }

    private static HttpEntity<Object> toEntity(RequestStep requestStep, HttpHeaders headers) {
        if (requestStep.hasAttachment()) {
            LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
            parameters.add(
                    requestStep.getAttachment().getParamName(),
                    new ClassPathResource(requestStep.getAttachment().getFilePath())
            );

            return new HttpEntity<>(parameters, headers);
        } else {
            return new HttpEntity<>(requestStep.getBody(), headers);
        }
    }

    @Data
    public static class RestResponse {
        private final HttpStatus status;
        private final String body;

        public Integer readInt(String path) {
            Integer result = JsonPath.parse(body).read(path, Integer.class);
            if (result == null) {
                throw new IllegalArgumentException(path + " not found in " + body);
            }
            return result;
        }

        public Long readLong(String path) {
            Long result = JsonPath.parse(body).read(path, Long.class);
            if (result == null) {
                throw new IllegalArgumentException(path + " not found in " + body);
            }
            return result;
        }

        public String read(String path) {
            String result = JsonPath.parse(body).read(path, String.class);
            if (result == null) {
                throw new IllegalArgumentException(path + " not found in " + body);
            }
            return result;
        }


        public BigDecimal readBigDecimal(String path) {
            BigDecimal result = JsonPath.parse(body).read(path, BigDecimal.class);
            if (result == null) {
                throw new IllegalArgumentException(path + " not found in " + body);
            }
            return result;
        }

        public Long readListElement(String path, Integer index) {
            List<Integer> result = JsonPath.parse(body).read(path, List.class);
            if (result == null) {
                throw new IllegalArgumentException(path + " not found in " + body);
            }
            return Long.valueOf(result.get(index));
        }
    }
}
