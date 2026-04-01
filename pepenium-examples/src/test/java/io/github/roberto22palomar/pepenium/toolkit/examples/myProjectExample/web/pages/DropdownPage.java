package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@Slf4j
public class DropdownPage {

    private final By pageTitle = By.cssSelector("h3");
    private final By dropdown = By.id("dropdown");

    @PepeniumInject
    private WebDriver driver;

    @PepeniumInject
    private ActionsWeb actionsWeb;

    @PepeniumInject
    private AssertionsWeb assertionsWeb;

    public void open(String baseUrl) {
        String dropdownUrl = baseUrl.replace("/login", "/dropdown");
        log.info("Opening dropdown example page");
        driver.get(dropdownUrl);
    }

    public void waitUntilLoaded() {
        actionsWeb.waitToBeVisible(pageTitle);
        actionsWeb.waitToBeVisible(dropdown);
    }

    public void assertLoaded() {
        assertionsWeb.assertTextContains(pageTitle, "Dropdown List");
        assertionsWeb.assertVisible(dropdown);
    }

    public void selectByVisibleText(String visibleText) {
        WebElement dropdownElement = driver.findElement(dropdown);
        new Select(dropdownElement).selectByVisibleText(visibleText);
        actionsWeb.takeScreenshot();
    }

    public void assertSelectedOptionContains(String expectedText) {
        assertionsWeb.assertTextContains(dropdown, expectedText);
    }
}
