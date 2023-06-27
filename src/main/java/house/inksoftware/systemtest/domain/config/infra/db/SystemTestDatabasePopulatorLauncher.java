package house.inksoftware.systemtest.domain.config.infra.db;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.io.Resources;
import house.inksoftware.systemtest.domain.config.infra.SystemTestResourceLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SystemTestDatabasePopulatorLauncher implements SystemTestResourceLauncher {
    private final String migrationScriptsPath;
    private final DataSource dataSource;

    private final Map<String, String> dynamicVariables;
    private final MustacheFactory factory = new DefaultMustacheFactory();

    public SystemTestDatabasePopulatorLauncher(String migrationScriptsPath, DataSource dataSource) {
        this.migrationScriptsPath = migrationScriptsPath;
        this.dataSource = dataSource;
        dynamicVariables = new HashMap<>();
    }

    private boolean populated = false;
    @Override
    public void setup() {
        if (populated) {
            return;
        }

        try {
            File folder = new File(Resources.getResource(migrationScriptsPath).getPath());

            List<String> sqlFilePaths = orderDbScripts(folder.list());
            for (String sqlFilePath : sqlFilePaths) {
                String sql = toSql(folder, sqlFilePath);

                log.info("Executing sql {} start...", sqlFilePath);
                dataSource.getConnection().prepareCall(sql).execute();
                log.info("Executing sql {} done.", sqlFilePath);
            }
            populated = true;
        } catch (Exception e) {
            log.error("Executing initial sql failed, error: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private String toSql(File folder, String sqlFilePath) throws IOException {
        String sql = new String(Files.readAllBytes(Paths.get(folder.getPath() + File.separatorChar + sqlFilePath)));

        if (dynamicVariables.isEmpty()) {
            return sql;
        } else {
            Mustache mustache = factory.compile(new StringReader(sql), UUID.randomUUID().toString());
            try {
                StringWriter writer = new StringWriter();
                mustache.execute(writer, dynamicVariables).flush();
                return writer.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private List<String> orderDbScripts(String[] files) {
        return Arrays.stream(files)
                .filter(file -> file.endsWith(".sql"))
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.substring(0, value.indexOf("-")))))
                .collect(Collectors.toList());
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Type type() {
        return Type.DATABASE;
    }
}
