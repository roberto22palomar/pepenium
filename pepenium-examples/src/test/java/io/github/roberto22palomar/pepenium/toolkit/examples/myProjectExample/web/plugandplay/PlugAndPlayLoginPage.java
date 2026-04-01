package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumPage;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Slf4j
@PepeniumPage
public class PlugAndPlayLoginPage {

    @PepeniumInject
    private ActionsWeb actionsWeb;

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(id = "username")
    private WebElement usernameInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    public WebElement pageTitle() {
        return pageTitle;
    }

    public WebElement usernameInput() {
        return usernameInput;
    }

    public WebElement passwordInput() {
        return passwordInput;
    }

    public WebElement loginButton() {
        return loginButton;
    }

    public void login(String username, String password) {
        log.info("Logging into The Internet example app with the plug-and-play page");
        actionsWeb.type(usernameInput, username);
        actionsWeb.type(passwordInput, password);
        actionsWeb.click(loginButton);
        actionsWeb.takeScreenshot();
    }
}
