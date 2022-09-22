package house.inksoftware.systemtest.configuration.infrastructure;

public interface SystemTestResourceLauncher {
    void setup();

    void shutdown();

    Type type();


    enum Type {
        DATABASE
    }
}
