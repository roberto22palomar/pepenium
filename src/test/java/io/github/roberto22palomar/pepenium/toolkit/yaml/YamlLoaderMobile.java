package io.github.roberto22palomar.pepenium.toolkit.yaml;


import io.github.roberto22palomar.pepenium.toolkit.browserstack.BrowserStackConfigMobile;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class YamlLoaderMobile {

    private YamlLoaderMobile() {
    }

    public static BrowserStackConfigMobile load(String yamlPath) {
        Path resolvedPath = YamlLoader.resolvePath(yamlPath);
        try (InputStream in = Files.newInputStream(resolvedPath)) {
            return new Yaml().loadAs(in, BrowserStackConfigMobile.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browserstack.yml from '" + resolvedPath + "': " + e.getMessage(), e);
        }
    }
}
