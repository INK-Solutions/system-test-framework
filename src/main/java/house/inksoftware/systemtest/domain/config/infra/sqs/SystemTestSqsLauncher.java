package house.inksoftware.systemtest.domain.config.infra.sqs;

import static house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher.Type.SQS;

import cloud.localstack.Localstack;
import house.inksoftware.systemtest.domain.config.infra.LocalstackLauncher;
import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import java.net.URI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Getter
public class SystemTestSqsLauncher implements SystemTestResourceLauncher {

    private SqsClient sqsClient;

    @Override
    public void setup() {
        LocalstackLauncher.launch();

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
