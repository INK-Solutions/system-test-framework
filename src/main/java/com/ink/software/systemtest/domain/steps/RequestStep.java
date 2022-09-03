package com.ink.software.systemtest.domain.steps;

import com.ink.software.systemtest.domain.utils.JsonUtils;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

@Data
public class RequestStep {
    private final String url;
    private final ApiType apiType;
    private final HttpMethod httpMethod;
    private final String body;
    private final Map<String, String> headers;
    private final Attachment attachment;

    public static RequestStep from(String json, String basePath, String stepName) {
        DocumentContext documentContext = JsonPath.parse(json);

        ApiType apiType = ApiType.valueOf(documentContext.read("type"));
        String url = documentContext.read("url");
        HttpMethod httpMethod = HttpMethod.valueOf(documentContext.read("method"));
        String body = JsonPath.parse((Object) documentContext.read("body")).jsonString();
        Map<String, String> headers = toHeaders(documentContext);
        Attachment attachment = toAttachment(documentContext, basePath, stepName);

        return new RequestStep(url, apiType, httpMethod, body, headers, attachment);
    }

    private static Map<String, String> toHeaders(DocumentContext documentContext) {
        if (JsonUtils.hasPath(documentContext, "headers")) {
           return documentContext.read("headers");
        } else {
            return new HashMap<>();
        }

    }

    private static Attachment toAttachment(DocumentContext documentContext, String basePath, String stepName) {
        if (JsonUtils.hasPath(documentContext, "attachment")) {
            Object attachment = documentContext.read("attachment");
            String paramName = JsonPath.parse(attachment).read("paramName");
            String attachmentFileName = JsonPath.parse(attachment).read("fileName");
            String fullPath = basePath + stepName + "/request/attachment/" + attachmentFileName;

            return new Attachment(paramName, fullPath);
        } else {
            return null;
        }
    }

    public boolean hasAttachment() {
        return attachment != null;
    }

    public enum ApiType {
        PUBLIC(false),
        CLIENT(true),
        INTERNAL(true);

        public boolean requiresToken;

        ApiType(boolean requiresToken) {
            this.requiresToken = requiresToken;
        }
    }

    @Data
    public static class Attachment {
        private final String paramName;
        private final String filePath;
    }
}
