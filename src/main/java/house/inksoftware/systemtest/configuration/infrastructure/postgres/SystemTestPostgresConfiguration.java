package house.inksoftware.systemtest.configuration.infrastructure.postgres;

import house.inksoftware.systemtest.db.InitialDataPopulation;
import lombok.Data;

@Data
public class SystemTestPostgresConfiguration {
    private final String image;
    private final InitialDataPopulation initialDataPopulation;
}
