package io.github.roberto22palomar.pepenium.toolkit.utils;


import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class YamlLoaderDesktop {

    private YamlLoaderDesktop() {
    }

    public static BrowserStackConfigDesktop load(String yamlPath) {
        try (InputStream in = Files.newInputStream(Paths.get(yamlPath))) {
            return new Yaml().loadAs(in, BrowserStackConfigDesktop.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browserstack.yml: " + e.getMessage(), e);
        }
    }
}
