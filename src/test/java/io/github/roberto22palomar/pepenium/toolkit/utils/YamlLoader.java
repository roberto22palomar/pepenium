package io.github.roberto22palomar.pepenium.toolkit.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class YamlLoader {

    private YamlLoader() {
    }

    public static BrowserStackConfig load(String yamlPath) {
        try (InputStream in = Files.newInputStream(Paths.get(yamlPath))) {
            return new Yaml().loadAs(in, BrowserStackConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browserstack.yml: " + e.getMessage(), e);
        }
    }
}
