package house.inksoftware.systemtest.domain.config.infra.sqs;

import cloud.localstack.Localstack;
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration;
import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

import static house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher.Type.SQS;

@Slf4j
@Getter
public class SystemTestSqsLauncher implements SystemTestResourceLauncher {

    private SqsClient sqsClient;

    @Override
    public void setup() {
        Localstack.INSTANCE.startup(LocalstackDockerConfiguration.builder()
                .useSingleDockerContainer(true)
                .build());

        sqsClient = SqsClient.builder()
                .endpointOverride(URI.create(Localstack.INSTANCE.getEndpointSQS()))
                .region(Region.US_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKey", "secretKey")))
                .build();
    }

    @Override
    public void shutdown() {
        if (sqsClient != null) {
            sqsClient.close();
        }
        Localstack.INSTANCE.stop();
    }

    @Override
    public Type type() {
        return SQS;
    }

}
