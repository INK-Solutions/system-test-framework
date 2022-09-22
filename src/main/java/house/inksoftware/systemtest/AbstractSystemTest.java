package house.inksoftware.systemtest;

import house.inksoftware.systemtest.configuration.infrastructure.SystemTestResourceLauncher;
import house.inksoftware.systemtest.db.InitialDataPopulation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@Import({InitialDataPopulation.class})
public abstract class AbstractSystemTest {
    private TestRestTemplate restTemplate;

    @LocalServerPort
    protected int port;

    @Before
    public void setup() {
        List<SystemTestResourceLauncher> launchers = resourceLaunchers();
        launchers.forEach(SystemTestResourceLauncher::setup);
    }

    @AfterClass
    public void shutdown() {
        resourceLaunchers()
                .forEach(SystemTestResourceLauncher::shutdown);
    }

    public abstract List<SystemTestResourceLauncher> resourceLaunchers();
}
