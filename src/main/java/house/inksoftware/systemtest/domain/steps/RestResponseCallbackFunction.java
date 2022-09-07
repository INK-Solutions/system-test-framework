package house.inksoftware.systemtest.domain.steps;


import house.inksoftware.systemtest.domain.utils.RestUtils;

public interface RestResponseCallbackFunction {
    void onResponseReceived(RestUtils.RestResponse restResponse);
}
