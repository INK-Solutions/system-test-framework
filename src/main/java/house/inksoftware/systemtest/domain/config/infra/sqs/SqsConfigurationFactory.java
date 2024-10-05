package house.inksoftware.systemtest.domain.config.infra.sqs;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration.SqsConfiguration;
import house.inksoftware.systemtest.domain.config.infra.sqs.queue.SqsQueueFactory;
import house.inksoftware.systemtest.domain.sqs.SqsConsumerService;
import house.inksoftware.systemtest.domain.sqs.SqsProducerService;
import house.inksoftware.systemtest.domain.sqs.queue.SqsQueueDefinition;
import java.util.List;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsConfigurationFactory {

    public static SqsConfiguration create(SqsClient sqsClient,
                                          List<SqsQueueDefinition> queueDefinitions) {

        SqsQueueFactory.create(sqsClient, queueDefinitions);
        SqsProducerService sqsProducerService = new SqsProducerService(sqsClient, queueDefinitions);
        SqsConsumerService sqsConsumerService = new SqsConsumerService(sqsClient, queueDefinitions);

        return new SqsConfiguration(
                sqsClient,
                queueDefinitions,
                sqsProducerService,
                sqsConsumerService
        );
    }

}
