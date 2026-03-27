package io.github.roberto22palomar.pepenium.core.config.yaml;


import io.github.roberto22palomar.pepenium.core.config.browserstack.BrowserStackConfigMobile;
import io.github.roberto22palomar.pepenium.core.config.validation.ConfigValidationSupport;
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
            BrowserStackConfigMobile config = new Yaml().loadAs(in, BrowserStackConfigMobile.class);
            return ConfigValidationSupport.validateBrowserStackMobileWebConfig(config, resolvedPath.toString());
        } catch (Exception e) {
            throw ConfigValidationSupport.invalid(
                    "Failed to load BrowserStack YAML from '" + resolvedPath + "': " + e.getMessage(), e);
        }
    }
}
