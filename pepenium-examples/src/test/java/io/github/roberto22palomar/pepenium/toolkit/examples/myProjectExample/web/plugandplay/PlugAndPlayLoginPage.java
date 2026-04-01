package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumPage;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Slf4j
@PepeniumPage
public class PlugAndPlayLoginPage {

    @PepeniumInject
    private ActionsWeb actionsWeb;

    @PepeniumInject
    private AssertionsWeb assertionsWeb;

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(id = "username")
    private WebElement usernameInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    public void assertLoaded() {
        assertionsWeb.assertVisible(pageTitle);
        assertionsWeb.assertVisible(usernameInput);
        assertionsWeb.assertVisible(passwordInput);
        assertionsWeb.assertVisible(loginButton);
        assertionsWeb.assertTextEquals(pageTitle, "Login Page");
    }

    public void login(String username, String password) {
        log.info("Logging into The Internet example app with the plug-and-play page");
        actionsWeb.type(usernameInput, username);
        actionsWeb.type(passwordInput, password);
        actionsWeb.click(loginButton);
        actionsWeb.takeScreenshot();
    }
}
