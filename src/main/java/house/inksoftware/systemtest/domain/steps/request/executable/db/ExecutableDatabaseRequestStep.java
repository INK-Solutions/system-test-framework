package house.inksoftware.systemtest.domain.steps.request.executable.db;

import house.inksoftware.systemtest.domain.config.SystemTestConfiguration;
import house.inksoftware.systemtest.domain.context.SystemTestContext;
import house.inksoftware.systemtest.domain.steps.request.executable.ExecutableRequestStep;
import house.inksoftware.systemtest.domain.steps.response.ActualResponse;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.sql.*;

@Builder
@Data
public class ExecutableDatabaseRequestStep implements ExecutableRequestStep {
    private final String query;
    private final String contextVariableName;
    private final SystemTestContext context;

    public ActualResponse execute(SystemTestConfiguration config) {
        makedDbCall();
        return null;
    }

    @SneakyThrows
    private void makedDbCall() {
        try (Connection connection = getConnection()) {
            CallableStatement query = connection.prepareCall(this.query);
            query.execute();
            try (ResultSet resultSet = query.getResultSet()) {
                resultSet.next();
                context.put(contextVariableName, resultSet.getString(1));
            }
        }

    }

    private static Connection getConnection() {
        Connection con = null;
        try {
            // create the connection now
            con = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

}
