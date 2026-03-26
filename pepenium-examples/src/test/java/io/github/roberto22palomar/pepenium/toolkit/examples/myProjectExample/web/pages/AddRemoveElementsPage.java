package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
@RequiredArgsConstructor
public class AddRemoveElementsPage {

    private final By pageTitle = By.cssSelector("h3");
    private final By addElementButton = By.xpath("//button[text()='Add Element']");

    private final WebDriver driver;
    private final ActionsWeb actionsWeb;

    public void open(String baseUrl) {
        String targetUrl = baseUrl.replace("/login", "/add_remove_elements/");
        log.info("Opening add/remove elements example page");
        driver.get(targetUrl);
    }

    public void waitUntilLoaded() {
        actionsWeb.waitToBeVisible(pageTitle);
        actionsWeb.waitToBeVisible(addElementButton);
    }

    public void captureExampleState() {
        actionsWeb.takeScreenshot();
    }
}
