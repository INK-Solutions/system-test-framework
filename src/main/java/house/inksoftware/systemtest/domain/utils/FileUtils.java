package house.inksoftware.systemtest.domain.utils;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String readFileContentFromDir(final String fullFolderPath) throws Exception {
        URL resource = Resources.getResource(fullFolderPath);
        File folder = new File(resource.getPath());
        Preconditions.checkArgument(folder.isDirectory(), fullFolderPath + " is not a directory");
        File[] files = folder.listFiles(File::isFile);
        Preconditions.checkArgument(files.length == 1, fullFolderPath + " has  "+ files.length + " files.");

        return Resources.toString(new File(files[0].getPath()).toURI().toURL(), StandardCharsets.UTF_8);
    }

    public static String readFileContent(String filePath) throws Exception {
        return Resources.toString(Resources.getResource(filePath), StandardCharsets.UTF_8);
    }
}
