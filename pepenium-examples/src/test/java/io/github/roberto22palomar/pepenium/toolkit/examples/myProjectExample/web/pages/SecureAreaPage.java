package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class SecureAreaPage {

    private final By secureAreaTitle = By.cssSelector("h2");
    private final By flashMessage = By.id("flash");
    private final By logoutButton = By.cssSelector("a.button.secondary.radius");

    @PepeniumInject
    private ActionsWeb actionsWeb;

    @PepeniumInject
    private AssertionsWeb assertionsWeb;

    public void waitUntilLoaded() {
        actionsWeb.waitToBeVisible(secureAreaTitle);
        actionsWeb.waitToBeVisible(flashMessage);
        actionsWeb.waitToBeVisible(logoutButton);
    }

    public void assertSuccessfulLoginState() {
        assertionsWeb.assertTextContains(flashMessage, "You logged into a secure area!");
        assertionsWeb.assertVisible(logoutButton);
        assertionsWeb.assertUrlContains("/secure");
    }
}
