package io.github.roberto22palomar.pepenium.smoke.contract;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import io.github.roberto22palomar.pepenium.core.runtime.BaseTest;
import org.junit.jupiter.api.Test;

class BaseTestContractSmokeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.WEB_DESKTOP;
    }

    @Test
    void protectedBaseTestContractCompilesForExternalConsumers() {
        Object currentDriver = driver;
        Object currentSession = session;
        String defaultProfileId = getDefaultProfileId();
        boolean automaticLifecycle = useAutomaticLifecycle();
        AppiumDriver mobileDriver = appiumDriver();
        DriverConfig noopConfig = () -> null;
        ThrowingRunnable initializeWithConfig = () -> initializeDriver(noopConfig);
        ThrowingRunnable initializeWithProfile = () -> initializeDriverForProfile("local-web");
        ThrowingRunnable cleanup = this::cleanupDriver;
        ThrowingRunnable runWithConfigHook = () -> runWithConfig(noopConfig, () -> step("inner step"));
        ThrowingRunnable runWithProfileHook = () -> runWithProfile("local-web", () -> step("inner step"));

        step("compile smoke");
    }
}
