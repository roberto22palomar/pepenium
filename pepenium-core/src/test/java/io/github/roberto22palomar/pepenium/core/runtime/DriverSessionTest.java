package io.github.roberto22palomar.pepenium.core.runtime;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.github.roberto22palomar.pepenium.core.execution.DriverRequest;
import io.github.roberto22palomar.pepenium.core.execution.DriverType;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DriverSessionTest {

    @Test
    void closeIsIdempotent() {
        WebDriver driver = mock(WebDriver.class);
        AppiumDriverLocalService service = mock(AppiumDriverLocalService.class);
        DriverSession session = new DriverSession(driver, request(service));

        session.close();
        session.close();

        verify(driver, times(1)).quit();
        verify(service, times(1)).stop();
    }

    @Test
    void stopsOwnedServiceWhenDriverQuitFails() {
        WebDriver driver = mock(WebDriver.class);
        AppiumDriverLocalService service = mock(AppiumDriverLocalService.class);
        IllegalStateException failure = new IllegalStateException("quit failed");
        doThrow(failure).when(driver).quit();
        DriverSession session = new DriverSession(driver, request(service));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, session::close);

        assertSame(failure, thrown);
        verify(service).stop();
    }

    private DriverRequest request(AppiumDriverLocalService service) {
        return DriverRequest.builder()
                .driverType(DriverType.ANDROID_APPIUM)
                .description("owned service")
                .ownedService(service)
                .build();
    }
}
