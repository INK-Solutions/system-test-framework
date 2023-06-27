package house.inksoftware.systemtest.domain.config.infra.kafka.incoming;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;

public class KafkaEventProcessedCallback {
    private final List<String> processedEventIds = new ArrayList<>();

    public void handle(String id) {
        processedEventIds.add(id);
    }

    public void awaitUntilEventIsProcessed(String id) {
        await()
                .atMost(Duration.ofSeconds(30))
                .with()
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> processedEventIds.contains(id));
    }
}
