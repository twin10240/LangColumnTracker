package loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class XmlFileLoader {

    private final String folderPath;

    public XmlFileLoader(String folderPath) {
        this.folderPath = folderPath;
    }

    // 하위 폴더 포함 탐색
    public List<Path> loadAll() throws IOException {
        return Files.walk(Paths.get(folderPath))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".xml"))
            .collect(Collectors.toList());
    }

    // 현재 폴더만 탐색 (하위 폴더 제외)
    public List<Path> loadShallow() throws IOException {
        return Files.list(Paths.get(folderPath))
            .filter(p -> p.toString().endsWith(".xml"))
            .collect(Collectors.toList());
    }

}