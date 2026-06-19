package io.github.roberto22palomar.pepenium.smoke.web;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumPage;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@PepeniumPage
final class WebLoginPage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By FLASH = By.id("flash");

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    @FindBy(id = "flash")
    private WebElement flashMessage;

    @PepeniumInject
    private ActionsWeb actions;

    void login(String username, String password) {
        actions.type(usernameField, username);
        actions.type(passwordField, password);
        actions.click(submitButton);
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

    WebElement usernameField() {
        return usernameField;
    }

    WebElement passwordField() {
        return passwordField;
    }

    WebElement submitButton() {
        return submitButton;
    }

    WebElement flashMessage() {
        return flashMessage;
    }
}
