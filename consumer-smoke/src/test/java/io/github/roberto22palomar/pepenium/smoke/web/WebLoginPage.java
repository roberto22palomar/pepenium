package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import org.openqa.selenium.By;

final class WebLoginPage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By FLASH = By.id("flash");

    private final ActionsWeb actions;
    private final AssertionsWeb assertions;

    WebLoginPage(ActionsWeb actions, AssertionsWeb assertions) {
        this.actions = actions;
        this.assertions = assertions;
    }

    void waitUntilLoaded() {
        assertions.assertVisible(USERNAME);
        assertions.assertVisible(PASSWORD);
        assertions.assertVisible(SUBMIT);
    }

    void login(String username, String password) {
        actions.type(USERNAME, username);
        actions.type(PASSWORD, password);
        actions.click(SUBMIT);
    }

    void assertSuccessMessageVisible() {
        assertions.assertVisible(FLASH);
        assertions.assertTextContains(FLASH, "You logged into a secure area!");
    }
}
