
package house.inksoftware.systemtest.domain;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStepFactory;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class SystemTestExecutionServiceFactory {

    public static SystemTestExecutionService create(SystemTestConfiguration configuration, File basePath) throws Exception {
        return new SystemTestExecutionService(configuration, new ExpectedResponseStepFactory(configuration), basePath);
    }
}
