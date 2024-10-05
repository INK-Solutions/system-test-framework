package house.inksoftware.systemtest.domain.config.infra.sns;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsClient;

@RequiredArgsConstructor
public class SnsSubscribersConfig {
    
    private final Supplier<SqsClient> sqsClientSupplier;
    private SqsClient sqsClient;
    
    public SqsClient sqsClient() {
        if (sqsClient == null) {
            sqsClient = sqsClientSupplier.get();
        }
        return sqsClient;
    }
}
