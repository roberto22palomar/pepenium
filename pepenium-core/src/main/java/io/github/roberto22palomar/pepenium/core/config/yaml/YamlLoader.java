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
        Path localConfigDir = Paths.get(".pepenium", "browserstack");

        List<Path> candidates = List.of(
                directPath,
                localConfigDir.resolve(fileName),
                Paths.get("src", "main", "resources", "browserstackExamples", fileName),
                Paths.get("..", "pepenium-core", "src", "main", "resources", "browserstackExamples", fileName),
                directPath.resolveSibling(fileName + ".example"),
                localConfigDir.resolve(fileName + ".example"),
                Paths.get("src", "main", "resources", "browserstackExamples", fileName + ".example"),
                Paths.get("..", "pepenium-core", "src", "main", "resources", "browserstackExamples", fileName + ".example")
        );

        Path resolved = candidates.stream()
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> ConfigValidationSupport.invalid(
                        "Could not find BrowserStack YAML. Looked for: "
                                + candidates.stream().map(Path::toString).collect(Collectors.joining(", "))
                                + ". Put real BrowserStack YAML under '.pepenium/browserstack/' or pass an explicit path."
                ));

        rejectPackagedRuntimePath(resolved);
        return resolved;
    }

    private static void rejectPackagedRuntimePath(Path resolvedPath) {
        Path normalized = resolvedPath.normalize();
        Path packagedRuntimePath = Paths.get("src", "main", "resources", "browserstack.yml").normalize();
        Path nestedPackagedRuntimePath = Paths.get("..", "pepenium-core", "src", "main", "resources", "browserstack.yml")
                .normalize();

        if (endsWithPath(normalized, packagedRuntimePath) || endsWithPath(normalized, nestedPackagedRuntimePath)) {
            throw ConfigValidationSupport.invalid(
                    "Refusing to load BrowserStack YAML from '" + resolvedPath
                            + "'. Real BrowserStack credentials must live outside 'src/main/resources'. "
                            + "Use '.pepenium/browserstack/' or pass an explicit external path instead."
            );
        }
    }

    private static boolean endsWithPath(Path candidate, Path suffix) {
        int candidateCount = candidate.getNameCount();
        int suffixCount = suffix.getNameCount();
        if (suffixCount > candidateCount) {
            return false;
        }
        return candidate.subpath(candidateCount - suffixCount, candidateCount).equals(suffix);
    }
}
