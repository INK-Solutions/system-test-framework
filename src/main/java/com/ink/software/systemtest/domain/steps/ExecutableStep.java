package com.ink.software.systemtest.domain.steps;

import com.ink.software.systemtest.domain.utils.JsonUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ExecutableStep {
    private final String name;
    private final List<JsonUtils.JsonRequestPlaceholder> requestPlaceholder;
    private final List<JsonUtils.JsonResponsePlaceholder> responsePlaceholders;
    private final RestResponseCallbackFunction restResponseCallbackFunction;


    public static ExecutableStepBuilder builder(String name) {
        return ExecutableStepBuilder.builder(name);
    }

    public static class ExecutableStepBuilder {
        private String name;
        private List<JsonUtils.JsonRequestPlaceholder> requestPlaceholders = new ArrayList<>();
        private List<JsonUtils.JsonResponsePlaceholder> responsePlaceholders = new ArrayList<>();
        private RestResponseCallbackFunction callbackFunction = restResponse -> { };


        public static ExecutableStepBuilder builder(String name) {
            ExecutableStepBuilder builder = new ExecutableStepBuilder();
            builder.name = name;

            return builder;
        }

        public ExecutableStepBuilder requestPlaceholders(JsonUtils.JsonRequestPlaceholder... placeholders) {
            requestPlaceholders = Arrays.asList(placeholders);
            return this;
        }

        public ExecutableStepBuilder responsePlaceholders(JsonUtils.JsonResponsePlaceholder... placeholders) {
            responsePlaceholders = Arrays.asList(placeholders);
            return this;
        }

        public ExecutableStepBuilder callbackFunction(RestResponseCallbackFunction callbackFunction) {
            this.callbackFunction = callbackFunction;
            return this;
        }

        public ExecutableStep build() {
            return new ExecutableStep(name, requestPlaceholders, responsePlaceholders, callbackFunction);
        }
    }


}
