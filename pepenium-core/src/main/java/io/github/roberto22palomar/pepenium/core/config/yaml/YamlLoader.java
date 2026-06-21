package io.github.roberto22palomar.pepenium.core.config.yaml;

import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfig;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
import io.github.roberto22palomar.pepenium.core.observability.SensitiveDataSanitizer;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class YamlLoader {

    private YamlLoader() {
    }

    public static BrowserStackConfig load(String yamlPath) {
        Path resolvedPath = resolvePath(yamlPath);
        try (InputStream in = Files.newInputStream(resolvedPath)) {
            BrowserStackConfig config = loadAs(in, BrowserStackConfig.class);
            return ConfigValidationSupport.validateBrowserStackAppConfig(config, resolvedPath.toString());
        } catch (Exception e) {
            throw loadFailure(resolvedPath, e);
        }
    }

    static <T> T loadAs(InputStream input, Class<T> type) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(50);
        options.setNestingDepthLimit(50);
        options.setCodePointLimit(3 * 1024 * 1024);
        return new Yaml(new Constructor(type, options)).loadAs(input, type);
    }

    static IllegalStateException loadFailure(Path path, Exception error) {
        return ConfigValidationSupport.invalid(
                "Failed to load BrowserStack YAML from '" + path + "': "
                        + SensitiveDataSanitizer.sanitizeText(error.getMessage()),
                error
        );
    }

    static Path resolvePath(String yamlPath) {
        String normalizedYamlPath = requireYamlPath(yamlPath);
        Path directPath = Paths.get(normalizedYamlPath);
        rejectPackagedRuntimeIntent(directPath);
        Path fileNamePath = directPath.getFileName();
        String fileName = fileNamePath == null ? normalizedYamlPath : fileNamePath.toString();
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

    private static String requireYamlPath(String yamlPath) {
        Objects.requireNonNull(yamlPath, "yamlPath must not be null");
        String normalizedYamlPath = yamlPath.trim();
        if (normalizedYamlPath.isEmpty()) {
            throw ConfigValidationSupport.invalid("BrowserStack YAML path must not be blank");
        }
        return normalizedYamlPath;
    }

    private static void rejectPackagedRuntimeIntent(Path requestedPath) {
        Path normalized = requestedPath.normalize();
        Path packagedRuntimePath = Paths.get("src", "main", "resources", "browserstack.yml").normalize();
        Path nestedPackagedRuntimePath = Paths.get("..", "pepenium-core", "src", "main", "resources", "browserstack.yml")
                .normalize();

        if (endsWithPath(normalized, packagedRuntimePath) || endsWithPath(normalized, nestedPackagedRuntimePath)) {
            throw ConfigValidationSupport.invalid(
                    "Refusing to load BrowserStack YAML from '" + requestedPath
                            + "'. Real BrowserStack credentials must live outside 'src/main/resources'. "
                            + "Use '.pepenium/browserstack/' or pass an explicit external path instead."
            );
        }
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
