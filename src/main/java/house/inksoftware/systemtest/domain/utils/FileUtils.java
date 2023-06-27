package house.inksoftware.systemtest.domain.utils;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {
    public static String readFileContent(final String fullPath) throws Exception {
        return Resources.toString(new File(fullPath).toURI().toURL(), StandardCharsets.UTF_8);
    }

    public static List<String> listFiles(final String fullFolderPath) {
        File folder = new File(fullFolderPath);
        Preconditions.checkArgument(folder.isDirectory(), fullFolderPath + " is not a directory");

        return Arrays.stream(folder.listFiles(File::isFile))
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static String readFile(File file) throws Exception {
        return Resources.toString(file.toURI().toURL(), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static File toFile(String path) {
        return new File(URLDecoder.decode(Resources.getResource(path).toURI().toURL().getPath()));
    }
}
