package io.github.roberto22palomar.pepenium.core.config.browserstack;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Maps the structure of browserstack.yml for mobile platforms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "YAML-mapped configuration DTO intentionally exposes mutable list state."
)
public class BrowserStackConfigMobile {

    private String userName;
    private String accessKey;
    private String framework;
    private List<Platform> platforms;
    private int parallelsPerPlatform;
    private boolean browserstackLocal;
    private String buildName;
    private String projectName;

    /**
     * DTO for each entry under the {@code platforms} key.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Platform {
        private String deviceName;
        private String osVersion;
        private String browserName;
        private String deviceOrientation;
    }
}
