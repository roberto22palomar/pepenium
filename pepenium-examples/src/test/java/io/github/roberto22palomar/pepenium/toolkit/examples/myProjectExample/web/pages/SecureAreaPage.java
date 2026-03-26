package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
@RequiredArgsConstructor
public class SecureAreaPage {

    private final By secureAreaTitle = By.cssSelector("h2");
    private final By flashMessage = By.id("flash");
    private final By logoutButton = By.cssSelector("a.button.secondary.radius");

    private final ActionsWeb actionsWeb;

    public void waitUntilLoaded() {
        actionsWeb.waitToBeVisible(secureAreaTitle);
        actionsWeb.waitToBeVisible(flashMessage);
        actionsWeb.waitToBeVisible(logoutButton);
    }
}
