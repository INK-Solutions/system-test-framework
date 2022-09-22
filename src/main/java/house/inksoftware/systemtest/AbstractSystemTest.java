package house.inksoftware.systemtest;

import house.inksoftware.systemtest.configuration.infrastructure.SystemTestResourceLauncher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.Objects.isNull;

@RunWith(SpringRunner.class)
public abstract class AbstractSystemTest {
    private static List<SystemTestResourceLauncher> systemTestResourceLaunchers;

    private TestRestTemplate restTemplate;

    @LocalServerPort
    protected int port;

    @Before
    public void setup() {
        if (isNull(systemTestResourceLaunchers)) {
            systemTestResourceLaunchers = resourceLaunchers();
        }
        systemTestResourceLaunchers.forEach(SystemTestResourceLauncher::setup);
    }

    @AfterClass
    public static void shutdown() {
        systemTestResourceLaunchers
                .forEach(SystemTestResourceLauncher::shutdown);
    }

    public abstract List<SystemTestResourceLauncher> resourceLaunchers();
}
