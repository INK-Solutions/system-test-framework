package house.inksoftware.systemtest.domain.config.infra.kafka;

import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import static house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher.Type.KAFKA;

public class SystemTestKafkaLauncher implements SystemTestResourceLauncher {
    private EmbeddedKafkaBroker embeddedKafka;

    @Override
    public void setup() {
        embeddedKafka = new EmbeddedKafkaBroker(1, true, 1);
        embeddedKafka.kafkaPorts(9092);
        embeddedKafka.afterPropertiesSet();
        System.setProperty("spring.embedded.kafka.brokers", embeddedKafka.getBrokersAsString());
    }

    @Override
    public void shutdown() {
        if (embeddedKafka != null) {
            embeddedKafka.destroy();
        }
        System.clearProperty("spring.embedded.kafka.brokers");
    }

    @Override
    public Type type() {
        return KAFKA;
    }

    public EmbeddedKafkaBroker getEmbeddedKafka() {
        return embeddedKafka;
    }
}
