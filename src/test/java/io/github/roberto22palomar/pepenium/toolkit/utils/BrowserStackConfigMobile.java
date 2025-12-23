package io.github.roberto22palomar.pepenium.toolkit.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mapea la estructura de browserstack.yml para móviles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * Sub-POJO para cada entrada bajo la clave 'platforms'.
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
