package io.github.roberto22palomar.pepenium.core.config.yaml;


import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigDesktop;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class YamlLoaderDesktop {

    private YamlLoaderDesktop() {
    }

    public static BrowserStackConfigDesktop load(String yamlPath) {
        Path resolvedPath = YamlLoader.resolvePath(yamlPath);
        try (InputStream in = Files.newInputStream(resolvedPath)) {
            return new Yaml().loadAs(in, BrowserStackConfigDesktop.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browserstack.yml from '" + resolvedPath + "': " + e.getMessage(), e);
        }
    }
}
