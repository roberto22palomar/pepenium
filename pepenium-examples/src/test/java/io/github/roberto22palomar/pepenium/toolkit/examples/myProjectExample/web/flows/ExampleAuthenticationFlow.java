package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.flows;

import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.AddRemoveElementsPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.CheckboxesPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.DropdownPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.LoginPage;
import io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages.SecureAreaPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ExampleAuthenticationFlow {

    private static final By LOGIN_TITLE = By.cssSelector("h2");
    private static final By FLASH_MESSAGE = By.id("flash");
    private static final By LOGOUT_BUTTON = By.cssSelector("a.button.secondary.radius");
    private static final By DROPDOWN_TITLE = By.cssSelector("h3");
    private static final By DROPDOWN = By.id("dropdown");

    private final LoginPage loginPage;
    private final SecureAreaPage secureAreaPage;
    private final DropdownPage dropdownPage;
    private final CheckboxesPage checkboxesPage;
    private final AddRemoveElementsPage addRemoveElementsPage;
    private final AssertionsWeb assertionsWeb;
    private final Consumer<String> stepRecorder;

    public void runSuccessfulLoginAndDropdownSelection(String baseUrl, String username, String password) {
        log.info("=== Starting ExampleAuthenticationFlow ===");

        stepRecorder.accept("Open login page and validate credentials form");
        loginPage.waitUntilLoaded();
        assertionsWeb.assertTextEquals(LOGIN_TITLE, "Login Page");

        stepRecorder.accept("Login with valid credentials");
        loginPage.login(username, password);

        stepRecorder.accept("Verify secure area after successful login");
        secureAreaPage.waitUntilLoaded();
        assertionsWeb.assertTextContains(FLASH_MESSAGE, "You logged into a secure area!");
        assertionsWeb.assertVisible(LOGOUT_BUTTON);
        assertionsWeb.assertUrlContains("/secure");

        stepRecorder.accept("Navigate to dropdown example");
        dropdownPage.open(baseUrl);
        dropdownPage.waitUntilLoaded();
        assertionsWeb.assertTextContains(DROPDOWN_TITLE, "Dropdown List");
        assertionsWeb.assertVisible(DROPDOWN);

        stepRecorder.accept("Select option 2 from dropdown");
        dropdownPage.selectByVisibleText("Option 2");

        stepRecorder.accept("Verify selected dropdown option");
        assertionsWeb.assertTextContains(DROPDOWN, "Option 2");

        stepRecorder.accept("Navigate to checkboxes example");
        checkboxesPage.open(baseUrl);
        checkboxesPage.waitUntilLoaded();

        stepRecorder.accept("Verify default checkbox states");
        if (checkboxesPage.isFirstCheckboxSelected()) {
            throw new AssertionError("Web assertion failed: first checkbox should be unchecked by default");
        }
        if (!checkboxesPage.isSecondCheckboxSelected()) {
            throw new AssertionError("Web assertion failed: second checkbox should remain selected in the example page");
        }

        stepRecorder.accept("Navigate to add-remove elements example");
        addRemoveElementsPage.open(baseUrl);
        addRemoveElementsPage.waitUntilLoaded();

        stepRecorder.accept("Capture add-remove elements example state");
        addRemoveElementsPage.captureExampleState();

        log.info("=== Finished ExampleAuthenticationFlow ===");
    }
}
