package house.inksoftware.systemtest.domain.steps.request.executable;

import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.RequestResponseFileFinder;
import house.inksoftware.systemtest.domain.steps.request.RequestStep;
import house.inksoftware.systemtest.domain.steps.request.executable.db.ExecutableDatabaseRequestStepFactory;
import house.inksoftware.systemtest.domain.steps.request.executable.kafka.ExecutableKafkaRequestStepFactory;
import house.inksoftware.systemtest.domain.steps.request.executable.rest.ExecutableRestRequestStepFactory;

import java.io.File;

public class ExecutableRequestStepFactory {

    public static ExecutableRequestStep create(String baseFolder,
                                               RequestStep requestStep,
                                               SystemTestContext context) throws Exception {
        File requestFile = RequestResponseFileFinder.findRequest(new File(baseFolder + File.separator + requestStep.getName()));
        if (requestFile.getName().equals("rest-request.json")) {
            return ExecutableRestRequestStepFactory.create(baseFolder, requestStep);
        } else if (requestFile.getName().equals("kafka-request.json")) {
            return ExecutableKafkaRequestStepFactory.create(requestStep);
        } else if (requestFile.getName().equals("db-request.json")) {
            return ExecutableDatabaseRequestStepFactory.create(requestStep, context);
        } else {
            throw new IllegalArgumentException("Unknown step file name: " + requestFile.getName() + ". Allowed requests names are: rest-request.json, kafka-request.json, db-request.json");
        }
    }
}
