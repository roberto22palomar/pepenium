package io.github.roberto22palomar.pepenium.toolkit.examples.myProjectExample.web.plugandplay;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumPage;
import io.github.roberto22palomar.pepenium.toolkit.assertions.AssertionsWeb;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@PepeniumPage
public class PlugAndPlaySecureAreaPage {

    @PepeniumInject
    private AssertionsWeb assertionsWeb;

    @FindBy(id = "flash")
    private WebElement flashMessage;

    @FindBy(css = "a.button.secondary.radius")
    private WebElement logoutButton;

    public void assertSuccessfulLogin() {
        assertionsWeb.assertVisible(logoutButton);
        assertionsWeb.assertTextContains(flashMessage, "You logged into a secure area!");
        assertionsWeb.assertUrlContains("/secure");
    }
}
