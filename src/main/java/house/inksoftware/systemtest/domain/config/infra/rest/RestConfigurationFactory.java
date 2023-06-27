package house.inksoftware.systemtest.domain.config.infra.rest;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;

public class RestConfigurationFactory {
    private static final String DEFAULT_HOST = "http://127.0.0.1";

    public static SystemTestConfiguration.RestConfiguration create(TestRestTemplate restTemplate, Integer port) {
        return new SystemTestConfiguration.RestConfiguration(DEFAULT_HOST, restTemplate, port);
    }
}
