package house.inksoftware.systemtest.domain.steps.request.executable.db;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.request.executable.ExecutableRequestStep;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.Optional;

@Builder
@Data
public class ExecutableDatabaseRequestStep implements ExecutableRequestStep {
    private final String query;
    private final Optional<String> contextVariableName;
    private final SystemTestContext context;

    public ActualResponse execute(SystemTestConfiguration config) {
        makeDbCall();
        return null;
    }

    @SneakyThrows
    private void makeDbCall() {
        try (Connection connection = getConnection()) {
            CallableStatement query = connection.prepareCall(this.query);
            query.execute();
            if (contextVariableName.isPresent()) {
                try (ResultSet resultSet = query.getResultSet()) {
                    resultSet.next();
                    context.put(contextVariableName.get(), resultSet.getString(1));
                }
            }
        }

    }

    private static Connection getConnection() {
        Connection result = null;
        try {
            result = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
