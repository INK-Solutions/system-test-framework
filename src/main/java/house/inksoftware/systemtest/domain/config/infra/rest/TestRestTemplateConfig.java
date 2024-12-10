package house.inksoftware.systemtest.domain.config.infra.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@TestConfiguration
public class TestRestTemplateConfig {

    @Value("${systemtest.testRestTemplate.connection-timeout:10}")
    private long connectionTimeoutSeconds;

    @Value("${systemtest.testRestTemplate.read-timeout:10}")
    private long readTimeoutSeconds;

    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return new TestRestTemplate(
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                        .setReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
        );
    }
}
