package house.inksoftware.systemtest.domain.steps;


import house.inksoftware.systemtest.domain.steps.response.ActualResponse;

public interface RestResponseCallbackFunction {
    void onResponseReceived(ActualResponse restResponse);
}
