package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class LoginPage {

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By pageTitle = By.cssSelector("h2");

    private final ActionsWeb actionsWeb;

    public void waitUntilLoaded() {
        actionsWeb.waitToBeVisible(pageTitle);
        actionsWeb.waitToBeVisible(usernameInput);
        actionsWeb.waitToBeVisible(passwordInput);
        actionsWeb.waitToBeVisible(loginButton);
    }

    public void login(String username, String password) {
        log.info("Logging into The Internet example app");
        actionsWeb.type(usernameInput, username);
        actionsWeb.type(passwordInput, password);
        actionsWeb.click(loginButton);
        actionsWeb.takeScreenshot();
    }
}
