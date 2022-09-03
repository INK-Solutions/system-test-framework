package com.ink.software.systemtest.db;

import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class InitialDataPopulation {
    private final String migrationScriptsPath;
    private final DataSource dataSource;

    private boolean populated = false;

    public void populate() {
        if (populated) {
            return;
        }

        try {
            File folder = new File(Resources.getResource(migrationScriptsPath).getPath());

            List<String> sqlFilePaths = orderDbScripts(folder.list());
            for (String sqlFilePath : sqlFilePaths) {
                String sql = new String(Files.readAllBytes(Paths.get(folder.getPath() + File.separatorChar + sqlFilePath)));
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

    private List<String> orderDbScripts(String[] files) {
        return Arrays.stream(files)
                .filter(file -> file.endsWith(".sql"))
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.substring(0, value.indexOf("-")))))
                .collect(Collectors.toList());
    }
}
