package house.inksoftware.systemtest.domain.config.infra.sns;

import cloud.localstack.Localstack;
import house.inksoftware.systemtest.domain.config.infra.LocalstackLauncher;
import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import java.net.URI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Slf4j
@Getter
public class SystemTestSnsLauncher implements SystemTestResourceLauncher {

    private SnsClient snsClient;

    @Override
    public void setup() {
        LocalstackLauncher.launch();

        snsClient = SnsClient.builder()
                .endpointOverride(URI.create(Localstack.INSTANCE.getEndpointSNS()))
                .region(Region.US_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("accessKey", "secretKey")))
                .build();
    }

    @Override
    public void shutdown() {
        if (snsClient != null) {
            snsClient.close();
        }
        Localstack.INSTANCE.stop();
    }

    @Override
    public Type type() {
        return Type.SNS;
    }

}
