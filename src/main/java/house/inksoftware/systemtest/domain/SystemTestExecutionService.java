package house.inksoftware.systemtest.domain;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import house.inksoftware.systemtest.domain.steps.request.executable.ExecutableRequestStepFactory;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStep;
import house.inksoftware.systemtest.domain.steps.response.ExpectedResponseStepFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public class SystemTestExecutionService {
    private static final Logger log = LoggerFactory.getLogger(SystemTestExecutionService.class);

    private final SystemTestConfiguration systemTestConfiguration;
    private final ExpectedResponseStepFactory expectedResponseStepFactory;
    private final File basePath;


    @SneakyThrows
    public void execute(RequestStep requestStep, SystemTestContext context) {
        log.info("Executing step: {} ", requestStep.getName());

        ActualResponse actualResponse = ExecutableRequestStepFactory
                .create(basePath.getAbsolutePath(), requestStep, context)
                .execute(systemTestConfiguration);

        List<ExpectedResponseStep> expectedRestResponseSteps = expectedResponseStepFactory.create(basePath, requestStep.getName());
        for (ExpectedResponseStep step : expectedRestResponseSteps) {
            try {
                step.assertResponseIsCorrect(actualResponse);
            } catch (JSONException e) {
                throw new IllegalStateException("In test: " + basePath.getName() + ", step: " + requestStep.getName() + " json didn't match: " + ExceptionUtils.getStackTrace(e));
            }
        }

        requestStep.getRestResponseCallbackFunction().onResponseReceived(actualResponse);

        log.info("Step: {} executed", requestStep.getName());
    }
}
