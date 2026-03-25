package io.github.roberto22palomar.pepenium.core.config.yaml;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public final class YamlLoader {

    private YamlLoader() {
    }

    public static BrowserStackConfig load(String yamlPath) {
        Path resolvedPath = resolvePath(yamlPath);
        try (InputStream in = Files.newInputStream(resolvedPath)) {
            return new Yaml().loadAs(in, BrowserStackConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browserstack.yml from '" + resolvedPath + "': " + e.getMessage(), e);
        }
    }

    static Path resolvePath(String yamlPath) {
        Path directPath = Paths.get(yamlPath);
        String fileName = directPath.getFileName().toString();

        List<Path> candidates = List.of(
                directPath,
                Paths.get("src", "main", "resources", "browserstackExamples", fileName),
                Paths.get("..", "pepenium-core", "src", "main", "resources", "browserstackExamples", fileName),
                directPath.resolveSibling(fileName + ".example"),
                Paths.get("src", "main", "resources", "browserstackExamples", fileName + ".example"),
                Paths.get("..", "pepenium-core", "src", "main", "resources", "browserstackExamples", fileName + ".example")
        );

        return candidates.stream()
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Could not find BrowserStack YAML. Looked for: "
                                + candidates.stream().map(Path::toString).collect(Collectors.joining(", "))
                ));
    }
}
