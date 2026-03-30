package io.github.roberto22palomar.pepenium.core.config.yaml;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
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
            BrowserStackConfig config = new Yaml().loadAs(in, BrowserStackConfig.class);
            return ConfigValidationSupport.validateBrowserStackAppConfig(config, resolvedPath.toString());
        } catch (Exception e) {
            throw ConfigValidationSupport.invalid(
                    "Failed to load BrowserStack YAML from '" + resolvedPath + "': " + e.getMessage(), e);
        }
    }

    static Path resolvePath(String yamlPath) {
        Path directPath = Paths.get(yamlPath);
        Path fileNamePath = directPath.getFileName();
        String fileName = fileNamePath == null ? yamlPath : fileNamePath.toString();

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
                .orElseThrow(() -> ConfigValidationSupport.invalid(
                        "Could not find BrowserStack YAML. Looked for: "
                                + candidates.stream().map(Path::toString).collect(Collectors.joining(", "))
                ));
    }
}
