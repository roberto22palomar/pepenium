package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.actions.ActionsWeb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
@RequiredArgsConstructor
public class CheckboxesPage {

    private final By pageTitle = By.cssSelector("h3");
    private final By firstCheckbox = By.xpath("(//*[@id='checkboxes']//input[@type='checkbox'])[1]");
    private final By secondCheckbox = By.xpath("(//*[@id='checkboxes']//input[@type='checkbox'])[2]");

    private final WebDriver driver;
    private final ActionsWeb actionsWeb;

    public void open(String baseUrl) {
        String targetUrl = baseUrl.replace("/login", "/checkboxes");
        log.info("Opening checkboxes example page");
        driver.get(targetUrl);
    }

    public void waitUntilLoaded() {
        actionsWeb.waitToBeVisible(pageTitle);
        actionsWeb.waitToBeVisible(firstCheckbox);
        actionsWeb.waitToBeVisible(secondCheckbox);
    }

    public boolean isFirstCheckboxSelected() {
        return driver.findElement(firstCheckbox).isSelected();
    }

    public boolean isSecondCheckboxSelected() {
        return driver.findElement(secondCheckbox).isSelected();
    }
}
