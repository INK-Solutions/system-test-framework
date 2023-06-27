package house.inksoftware.systemtest.domain.steps.response;

import org.json.JSONException;

public interface ExpectedResponseStep {
    void assertResponseIsCorrect(ActualResponse actualResponse) throws JSONException;

}
