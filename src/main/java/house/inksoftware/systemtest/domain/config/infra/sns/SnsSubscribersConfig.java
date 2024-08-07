package house.inksoftware.systemtest.domain.config.infra.sns;

import java.util.function.Supplier;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsClient;

@Data
@RequiredArgsConstructor
public class SnsSubscribersConfig {
    private final Supplier<SqsClient> sqsClientSupplier;
}
