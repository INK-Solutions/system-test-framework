package house.inksoftware.systemtest.domain.kafka;

import house.inksoftware.systemtest.domain.utils.JsonUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.awaitility.Awaitility.await;

@Slf4j
@RequiredArgsConstructor
public class KafkaBackgroundConsumerService {
    private final Executor executor = Executors.newFixedThreadPool(4);

    private final Map<String, List<ConsumedRecord>> fetchedRecords = new HashMap<>();

    private final Consumer<String, Object> consumer;

    public void initiate() {
        executor.execute(() -> {
            while (true) {
                try {
                    ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(consumer, 1000);
                    records
                            .forEach(record -> {
                                log.info("Read record {} from topic {}", record.value().toString(), record.topic());
                                List<ConsumedRecord> consumedRecords = find(record.topic());
                                consumedRecords.add(ConsumedRecord.record(record.value().toString()));
                                fetchedRecords.put(record.topic(), consumedRecords);
                            });
                } catch (IllegalStateException e) {
                    // ignore
                }
            }
        });
    }

    private List<ConsumedRecord> find(String key) {
        return fetchedRecords.get(key) == null ? new ArrayList<>() : fetchedRecords.get(key);
    }

    public void find(String topicName, String body) {
        try {
            await()
                    .atMost(Duration.ofSeconds(30))
                    .with()
                    .pollInterval(Duration.ofSeconds(2))
                    .until(() -> {
                        Optional<ConsumedRecord> matchingRecord = find(topicName)
                                .stream()
                                .filter(e -> !e.readByTest && JsonUtils.isEqual(body, e.body))
                                .findAny();

                        if (matchingRecord.isPresent()) {
                            matchingRecord.get().readByTest = true;
                            return true;
                        } else {
                            return false;
                        }
                    });
        } catch (ConditionTimeoutException exception) {
            throw new IllegalStateException("It was expected that there would be an event outputed to " + topicName + ", but it didn't happen");
        }
    }

    @Data
    public static class ConsumedRecord {
        private String body;
        private boolean readByTest;

        public static ConsumedRecord record(String body) {
            ConsumedRecord result = new ConsumedRecord();
            result.setBody(body);
            return result;
        }
    }
}
