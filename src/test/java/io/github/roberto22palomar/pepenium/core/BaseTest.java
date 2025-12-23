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

    /**
     * Cada subclase devolverá AndroidConfigAWS, IOSConfigAWS, etc.
     */
    protected abstract DriverConfig getConfig();

    @BeforeAll
    void setupSuite() throws Exception {
        DriverConfig cfg = getConfig();
        service = cfg.startService();
        driver = cfg.createDriver(service);
        System.out.println("[CP] " + System.getProperty("java.class.path"));
    }

    @AfterAll
    void teardownSuite() {
        if (driver != null) {
            driver.quit();
        }
        if (service != null) {
            service.stop();
        }
    }
}
