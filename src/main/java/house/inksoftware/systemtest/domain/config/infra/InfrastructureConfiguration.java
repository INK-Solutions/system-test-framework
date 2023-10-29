package house.inksoftware.systemtest.domain.config.infra;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.config.infra.kafka.incoming.KafkaEventProcessedCallback;
import house.inksoftware.systemtest.domain.config.infra.rest.RestConfigurationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;

import static house.inksoftware.systemtest.domain.utils.FileUtils.readJsonStringFromFile;

@Slf4j
public class InfrastructureConfiguration {
    public void finishInfraConfig(SystemTestConfiguration systemTestConfiguration,
                                  KafkaEventProcessedCallback kafkaEventProcessedCallback,
                                  TestRestTemplate restTemplate,
                                  Integer port) throws Exception {
        if (systemTestConfiguration.hasKafka()) {
            systemTestConfiguration.getKafkaConfiguration().setKafkaEventProcessedCallback(kafkaEventProcessedCallback);
        } else if (systemTestConfiguration.hasGrpc()) {
            finishGprcConfig(systemTestConfiguration.getGrpcConfiguration(), restTemplate);
        }

        systemTestConfiguration.setRestConfiguration(RestConfigurationFactory.create(restTemplate, port));
    }

    private void finishGprcConfig(SystemTestConfiguration.GrpcConfiguration grpcConfiguration,
                                  TestRestTemplate restTemplate) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        var json = readJsonStringFromFile(grpcConfiguration.getContractsDirPath());
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode contractsArray = rootNode.path("contracts");

        if (contractsArray.isArray()) {
            for (JsonNode contract : contractsArray) {
                String jsonContract = objectMapper.writeValueAsString(contract);
                try {
                    HttpEntity<String> request = new HttpEntity<>(jsonContract);
                    restTemplate.postForObject("http://localhost:4771/add", request, String.class);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        log.info("Finished grpc configuration for endpoints");
    }
}