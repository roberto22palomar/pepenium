package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.DriverConfig;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseTestContractTest {

    @Test
    void baseTestUsesPerClassLifecycleAsPartOfPublicContract() {
        TestInstance annotation = BaseTest.class.getAnnotation(TestInstance.class);

        assertNotNull(annotation);
        assertEquals(TestInstance.Lifecycle.PER_CLASS, annotation.value());
        assertTrue(Modifier.isAbstract(BaseTest.class.getModifiers()));
    }

    @Test
    void baseTestProtectedFieldsAndHooksRemainAvailable() throws Exception {
        assertProtectedField("driver", WebDriver.class);
        assertProtectedField("session", DriverSession.class);

        Method getTarget = assertProtectedMethod("getTarget", TestTarget.class);
        assertTrue(Modifier.isAbstract(getTarget.getModifiers()));

        assertProtectedMethod("getDefaultProfileId", String.class);
        assertProtectedMethod("useAutomaticLifecycle", boolean.class);
        assertProtectedMethod("appiumDriver", AppiumDriver.class);
        assertProtectedMethod("step", void.class, String.class);
        assertProtectedMethod("initializeDriver", void.class, DriverConfig.class);
        assertProtectedMethod("initializeDriverForProfile", void.class, String.class);
        assertProtectedMethod("cleanupDriver", void.class);
        assertProtectedMethod("runWithConfig", void.class, DriverConfig.class, BaseTest.ThrowingRunnable.class);
        assertProtectedMethod("runWithProfile", void.class, String.class, BaseTest.ThrowingRunnable.class);

        Class<?> throwingRunnable = Class.forName(BaseTest.class.getName() + "$ThrowingRunnable");
        assertTrue(throwingRunnable.isInterface());
        assertTrue(Modifier.isProtected(throwingRunnable.getModifiers()));
    }

    @Test
    void baseTestDefaultHooksKeepCurrentSemantics() {
        ContractFixture fixture = new ContractFixture();

        assertNull(fixture.getDefaultProfileId());
        assertTrue(fixture.useAutomaticLifecycle());
    }

    private void assertProtectedField(String name, Class<?> type) throws Exception {
        Field field = BaseTest.class.getDeclaredField(name);

        assertEquals(type, field.getType());
        assertTrue(Modifier.isProtected(field.getModifiers()));
    }

    private Method assertProtectedMethod(String name, Class<?> returnType, Class<?>... parameterTypes) throws Exception {
        Method method = BaseTest.class.getDeclaredMethod(name, parameterTypes);

        assertEquals(returnType, method.getReturnType());
        assertTrue(Modifier.isProtected(method.getModifiers()));
        return method;
    }

    private static final class ContractFixture extends BaseTest {

        @Override
        protected TestTarget getTarget() {
            return TestTarget.WEB_DESKTOP;
        }
    }
}
