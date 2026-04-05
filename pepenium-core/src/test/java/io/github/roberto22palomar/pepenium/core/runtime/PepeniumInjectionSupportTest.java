package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.AppiumDriver;
import io.github.roberto22palomar.pepenium.core.execution.TestTarget;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PepeniumInjectionSupportTest {

    @Test
    void injectsDriversHelpersPagesFlowsAndStepRecorderFromTheRuntime() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        WebDriver driver = mock(WebDriver.class);
        DriverSession session = mock(DriverSession.class);

        when(runtime.getDriver()).thenReturn(driver);
        when(runtime.getSession()).thenReturn(session);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        PlugAndPlayFixture fixture = new PlugAndPlayFixture();
        injector.injectInto(fixture);

        assertSame(driver, fixture.driver);
        assertSame(session, fixture.session);
        assertNotNull(fixture.page);
        assertNotNull(fixture.flow);
        assertNotNull(fixture.steps);
        assertNotNull(fixture.page.loginButton);
        assertSame(fixture.page, fixture.flow.page);
        assertSame(fixture.driver, fixture.flow.driver);
    }

    @Test
    void resolvesDirectTypesIncludingAppiumDriverAndCachesComplexComponents() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        WebDriver driver = mock(WebDriver.class);
        DriverSession session = mock(DriverSession.class);
        @SuppressWarnings("unchecked")
        AppiumDriver appiumDriver = mock(AppiumDriver.class);

        when(runtime.getDriver()).thenReturn(driver);
        when(runtime.getSession()).thenReturn(session);
        when(runtime.getAppiumDriver()).thenReturn(appiumDriver);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        Object resolvedWebDriver = injector.resolve(WebDriver.class);
        Object resolvedSession = injector.resolve(DriverSession.class);
        Object resolvedAppiumDriver = injector.resolve(AppiumDriver.class);
        Object resolvedFlow = injector.resolve(LoginFlow.class);

        assertSame(driver, resolvedWebDriver);
        assertSame(session, resolvedSession);
        assertSame(appiumDriver, resolvedAppiumDriver);
        assertSame(resolvedFlow, injector.resolve(LoginFlow.class));
        assertTrue(PepeniumInjectionSupport.isDirectlySupported(PepeniumSteps.class));
    }

    @Test
    void reportsMissingLifecycleForDriverDependentInjection() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        when(runtime.getDriver()).thenReturn(null);
        when(runtime.getSession()).thenReturn(null);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> injector.injectInto(new PlugAndPlayFixture())
        );

        assertTrue(error.getMessage().contains("no active Pepenium session exists"));
    }

    @Test
    void detectsCircularDependencies() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        WebDriver driver = mock(WebDriver.class);
        DriverSession session = mock(DriverSession.class);
        when(runtime.getDriver()).thenReturn(driver);
        when(runtime.getSession()).thenReturn(session);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> injector.resolve(CircularA.class)
        );

        assertTrue(error.getMessage().contains("Circular Pepenium injection dependency detected"));
    }

    @Test
    void lenientInjectionLeavesDriverBoundFieldsNullUntilTheRuntimeIsReady() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        when(runtime.getDriver()).thenReturn(null);
        when(runtime.getSession()).thenReturn(null);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        PlugAndPlayFixture fixture = new PlugAndPlayFixture();
        injector.injectInto(fixture, false);

        assertNull(fixture.driver);
        assertNull(fixture.session);
        assertNull(fixture.page);
        assertNull(fixture.flow);
        assertNotNull(fixture.steps);
    }

    @Test
    void multipleConstructorsRequireAnExplicitInjectedConstructor() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        WebDriver driver = mock(WebDriver.class);
        DriverSession session = mock(DriverSession.class);
        when(runtime.getDriver()).thenReturn(driver);
        when(runtime.getSession()).thenReturn(session);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> injector.resolve(AmbiguousConstructors.class)
        );

        assertTrue(error.getMessage().contains("multiple constructors"));
        assertTrue(error.getMessage().contains("@PepeniumInject"));
    }

    @Test
    void annotatedConstructorWinsWhenMultipleConstructorsExist() {
        PepeniumRuntime runtime = mock(PepeniumRuntime.class);
        WebDriver driver = mock(WebDriver.class);
        DriverSession session = mock(DriverSession.class);
        when(runtime.getDriver()).thenReturn(driver);
        when(runtime.getSession()).thenReturn(session);

        PepeniumInjectionSupport injector = new PepeniumInjectionSupport(
                runtime,
                fixtureConfig(),
                new PepeniumInjectionSupport.CacheState()
        );

        ExplicitConstructorComponent component = (ExplicitConstructorComponent) injector.resolve(ExplicitConstructorComponent.class);

        assertSame(driver, component.driver);
    }

    private PepeniumTest fixtureConfig() {
        return AnnotationDrivenFixture.class.getAnnotation(PepeniumTest.class);
    }

    @PepeniumTest(target = TestTarget.WEB_DESKTOP)
    private static final class AnnotationDrivenFixture {
    }

    private static final class PlugAndPlayFixture {
        @PepeniumInject
        private WebDriver driver;

        @PepeniumInject
        private DriverSession session;

        @PepeniumInject
        private LoginPage page;

        @PepeniumInject
        private LoginFlow flow;

        @PepeniumInject
        private PepeniumSteps steps;
    }

    @PepeniumPage
    private static final class LoginPage {
        @FindBy(id = "login-button")
        private WebElement loginButton;

        @SuppressWarnings("unused")
        private final By usernameInput = By.id("username");
    }

    private static final class LoginFlow {
        private final LoginPage page;
        private final WebDriver driver;

        private LoginFlow(LoginPage page, WebDriver driver) {
            this.page = page;
            this.driver = driver;
        }
    }

    private static final class CircularA {
        private CircularA(CircularB b) {
        }
    }

    private static final class CircularB {
        private CircularB(CircularA a) {
        }
    }

    private static final class AmbiguousConstructors {
        private AmbiguousConstructors() {
        }

        private AmbiguousConstructors(WebDriver driver) {
        }
    }

    private static final class ExplicitConstructorComponent {
        private final WebDriver driver;

        private ExplicitConstructorComponent() {
            this.driver = null;
        }

        @PepeniumInject
        private ExplicitConstructorComponent(WebDriver driver) {
            this.driver = driver;
        }
    }
}
