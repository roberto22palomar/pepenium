package io.github.roberto22palomar.pepenium.toolkit.utils;


import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class YamlLoaderDesktop {

    private YamlLoaderDesktop() { }

    public static BrowserStackConfigDesktop load(String yamlPath) {
        try (InputStream in = Files.newInputStream(Paths.get(yamlPath))) {
            Yaml yaml = new Yaml(new Constructor(BrowserStackConfigDesktop.class));
            return yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar browserstack.yml: " + e.getMessage(), e);
        }
    }
}
