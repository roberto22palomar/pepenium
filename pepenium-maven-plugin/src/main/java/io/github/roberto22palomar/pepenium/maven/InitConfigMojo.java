package io.github.roberto22palomar.pepenium.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates a safe starter {@code pepenium.yml} without overwriting consumer configuration by default.
 */
@Mojo(name = "init-config", threadSafe = true)
public final class InitConfigMojo extends AbstractMojo {

    private static final String SCHEMA_URL = "https://raw.githubusercontent.com/roberto22palomar/pepenium/"
            + "main/docs/schema/pepenium.schema.json";
    private static final Map<String, String> TEMPLATES = createTemplates();

    @Parameter(property = "pepenium.config", defaultValue = "${project.basedir}/pepenium.yml")
    private File configFile;

    @Parameter(property = "pepenium.init.template", defaultValue = "local-web")
    private String template = "local-web";

    @Parameter(property = "pepenium.init.force", defaultValue = "false")
    private boolean force;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (configFile == null) {
            throw new MojoExecutionException("Pepenium configuration path was not provided.");
        }
        String templateId = template == null ? "local-web" : template.trim();
        String content = TEMPLATES.get(templateId);
        if (content == null) {
            throw new MojoFailureException("Unknown Pepenium configuration template '" + templateId
                    + "'. Supported templates: " + String.join(", ", TEMPLATES.keySet()));
        }

        Path output = configFile.toPath().toAbsolutePath().normalize();
        try {
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (force) {
                Files.writeString(output, content, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.writeString(output, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
            getLog().info("Created Pepenium configuration: " + output + " (template " + templateId + ")");
        } catch (java.nio.file.FileAlreadyExistsException error) {
            throw new MojoFailureException("Pepenium configuration already exists: " + output
                    + ". Use -Dpepenium.init.force=true only when replacement is intentional.", error);
        } catch (IOException error) {
            throw new MojoExecutionException("Could not create Pepenium configuration: " + output, error);
        }
    }

    void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    void setTemplate(String template) {
        this.template = template;
    }

    void setForce(boolean force) {
        this.force = force;
    }

    private static Map<String, String> createTemplates() {
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("local-web", localWeb());
        templates.put("local-android", localAndroid());
        templates.put("browserstack-web", browserStackWeb());
        return templates;
    }

    private static String commonHeader(String defaultProfile) {
        return "# yaml-language-server: $schema=" + SCHEMA_URL + "\n"
                + "schemaVersion: 1\n"
                + "defaultProfile: " + defaultProfile + "\n\n"
                + "reporting:\n"
                + "  directory: target/pepenium-reports\n"
                + "  screenshotPath: target/pepenium-screenshots\n\n"
                + "logging:\n"
                + "  detailed: false\n"
                + "  stepLimit: 20\n\n"
                + "timeouts:\n"
                + "  action: 10s\n"
                + "  longAction: 30s\n"
                + "  assertion: 10s\n\n";
    }

    private static String localWeb() {
        return commonHeader("local-web")
                + "baseUrl: https://example.com\n\n"
                + "profiles:\n"
                + "  local-web:\n"
                + "    browser:\n"
                + "      headless: true\n"
                + "      acceptInsecureCerts: false\n"
                + "      pageLoadStrategy: normal\n";
    }

    private static String localAndroid() {
        return commonHeader("local-android")
                + "profiles:\n"
                + "  local-android:\n"
                + "    serverUrl: http://127.0.0.1:4723\n"
                + "    device:\n"
                + "      udid: ${ANDROID_UDID}\n"
                + "      name: ${ANDROID_DEVICE_NAME}\n"
                + "    app:\n"
                + "      path: ${APP_PATH}\n"
                + "    capabilities:\n"
                + "      autoGrantPermissions: true\n"
                + "      noReset: false\n";
    }

    private static String browserStackWeb() {
        return commonHeader("browserstack-windows-web")
                + "baseUrl: https://example.com\n\n"
                + "# BrowserStack credentials and platform selection remain in its provider YAML.\n"
                + "profiles:\n"
                + "  browserstack-windows-web:\n"
                + "    capabilities:\n"
                + "      acceptInsecureCerts: false\n";
    }
}
