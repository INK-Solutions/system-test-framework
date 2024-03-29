package house.inksoftware.systemtest.domain.steps;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RequestResponseFileFinder {
    public static File findRequest(File folder) {
        return findFile(folder, "request");
    }
    public static File findResponse(File folder) {
        return findFile(folder, "response");
    }

    private static File findFile(File folder, String name) {
        List<File> requests = Arrays.stream(folder.listFiles())
                                    .filter(entry -> entry.getName().equals(name))
                                    .map(File::listFiles)
                                    .flatMap(Arrays::stream)
                                    .collect(Collectors.toList());
        Preconditions.checkState(requests.size() == 1, "There's more than one request file under folder " + folder.getParent());

        return requests.get(0);
    }
}
