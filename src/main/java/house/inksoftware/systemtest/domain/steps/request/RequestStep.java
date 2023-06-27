package house.inksoftware.systemtest.domain.steps.request;

import house.inksoftware.systemtest.domain.steps.RestResponseCallbackFunction;
import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class RequestStep {
    private final String name;
    private final String json;
    private final RestResponseCallbackFunction restResponseCallbackFunction;


    public static RequestStepBuilder builder(String name, String json) {
        return RequestStepBuilder.builder(name, json);
    }

    public static class RequestStepBuilder {
        private String name;
        private String json;
        private RestResponseCallbackFunction callbackFunction = restResponse -> { };


        public static RequestStepBuilder builder(String name, String json) {
            RequestStepBuilder builder = new RequestStepBuilder();
            builder.name = name;
            builder.json = json;

            return builder;
        }



        public RequestStepBuilder callbackFunction(RestResponseCallbackFunction callbackFunction) {
            this.callbackFunction = callbackFunction;
            return this;
        }

        public RequestStep build() {
            return new RequestStep(name, json, callbackFunction);
        }
    }


}
