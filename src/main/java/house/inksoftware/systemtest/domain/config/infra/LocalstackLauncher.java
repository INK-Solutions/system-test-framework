package house.inksoftware.systemtest.domain.config.infra;

import cloud.localstack.Localstack;
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalstackLauncher {
  
    private static boolean started = false;
  
    public static void launch() {
        if (started) {
            log.info("Localstack has already launched");
            return;
        }
      
        Localstack.INSTANCE.startup(LocalstackDockerConfiguration.builder()
            .useSingleDockerContainer(true)
            .build());  
        started = true;
    }  
}
