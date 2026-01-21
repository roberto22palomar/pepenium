package io.github.roberto22palomar.pepenium.toolkit.utils;


import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class YamlLoaderMobile {

    private YamlLoaderMobile() {
    }

    public static BrowserStackConfigMobile load(String yamlPath) {
        try (InputStream in = Files.newInputStream(Paths.get(yamlPath))) {
            return new Yaml().loadAs(in, BrowserStackConfigMobile.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browserstack.yml: " + e.getMessage(), e);
        }
    }
}
