package io.github.roberto22palomar.pepenium.core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    protected AppiumDriver driver;
    private AppiumDriverLocalService service;

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Cada subclase devolvera AndroidConfigAWS, IOSConfigAWS, etc.
     */
    protected abstract DriverConfig getConfig();

    protected boolean useAutomaticLifecycle() {
        return true;
    }

    protected void initializeDriver(DriverConfig cfg) throws Exception {
        service = cfg.startService();
        driver = cfg.createDriver(service);
        System.out.println("[CP] " + System.getProperty("java.class.path"));
    }

    protected void cleanupDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        if (service != null) {
            service.stop();
            service = null;
        }
    }

    protected void runWithConfig(DriverConfig cfg, ThrowingRunnable testBody) throws Exception {
        initializeDriver(cfg);
        try {
            testBody.run();
        } finally {
            cleanupDriver();
        }
    }

    @BeforeAll
    void setupSuite() throws Exception {
        if (useAutomaticLifecycle()) {
            initializeDriver(getConfig());
        }
    }

    @AfterAll
    void teardownSuite() {
        if (useAutomaticLifecycle()) {
            cleanupDriver();
        }
    }
}
