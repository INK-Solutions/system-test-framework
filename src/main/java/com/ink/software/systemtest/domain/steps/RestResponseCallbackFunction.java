package com.ink.software.systemtest.domain.steps;


import com.ink.software.systemtest.domain.utils.RestUtils;

public interface RestResponseCallbackFunction {
    void onResponseReceived(RestUtils.RestResponse restResponse);
}
