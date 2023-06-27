package house.inksoftware.systemtest.domain.steps.request.executable;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;

public interface ExecutableRequestStep {
    ActualResponse execute(SystemTestConfiguration config);
}
