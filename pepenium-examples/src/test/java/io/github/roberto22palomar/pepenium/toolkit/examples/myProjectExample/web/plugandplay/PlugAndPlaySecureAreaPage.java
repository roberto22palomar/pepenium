package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@PepeniumPage
public class PlugAndPlaySecureAreaPage {

    @FindBy(id = "flash")
    private WebElement flashMessage;

    @FindBy(css = "a.button.secondary.radius")
    private WebElement logoutButton;

    public WebElement flashMessage() {
        return flashMessage;
    }

    public WebElement logoutButton() {
        return logoutButton;
    }
}
