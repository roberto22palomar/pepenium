package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import org.openqa.selenium.By;

final class WebLoginPage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By FLASH = By.id("flash");

    @PepeniumInject
    private ActionsWeb actions;

    void login(String username, String password) {
        actions.type(USERNAME, username);
        actions.type(PASSWORD, password);
        actions.click(SUBMIT);
    }

    By username() {
        return USERNAME;
    }

    By password() {
        return PASSWORD;
    }

    By submit() {
        return SUBMIT;
    }

    By flash() {
        return FLASH;
    }
}
