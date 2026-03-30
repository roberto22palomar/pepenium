package io.github.roberto22palomar.pepenium.core.config.browserstack;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mapea la estructura de browserstack.yml.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "YAML-mapped configuration DTO intentionally exposes mutable list state."
)
public class BrowserStackConfig {

    private String userName;
    private String accessKey;
    private String framework;
    private String app;
    private List<Platform> platforms;
    private int parallelsPerPlatform;
    private boolean browserstackLocal;
    private String buildName;
    private String projectName;

    /**
     * Sub-POJO para cada entrada bajo la clave 'platforms'.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Platform {
        private String platformName;
        private String deviceName;
        private String platformVersion;
    }
}
