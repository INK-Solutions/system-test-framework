package house.inksoftware.systemtest.domain.config.infra;

public interface SystemTestResourceLauncher {
    void setup();

    void shutdown();

    Type type();


    enum Type {
        SERVER,
        DATABASE,
        KAFKA,
        SQS
    }
}
