package com.ink.software.systemtest;

import com.ink.software.systemtest.db.InitialDataPopulation;
import com.ink.software.systemtest.db.PostgresqlContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Import({InitialDataPopulation.class})
public abstract class AbstractSystemTest {
    protected TestRestTemplate restTemplate;

    private InitialDataPopulation initialDataPopulation;

    @LocalServerPort
    protected int port;

    static {
        String usecontainer = System.getenv("internal.integration.usecontainer");
        if (usecontainer==null)
            PostgresqlContainer.initialise();
    }

    @Before
    public void setup() {
        initialDataPopulation.populate();
    }

    @AfterClass
    public static void shutDown() {
        String usecontainer = System.getenv("internal.integration.usecontainer");
        if (usecontainer==null)
            PostgresqlContainer.shutdown();
    }
}
