package io.github.roberto22palomar.pepenium.toolkit.myProjectExample.web.pages;

import io.github.roberto22palomar.pepenium.toolkit.utils.ActionsWeb;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@RequiredArgsConstructor
@Slf4j
public class NavigationTabsPage {

    // ============================================================
    // Tabs (generic example) - XPath only
    // ============================================================
    private final By tabOne = By.xpath("//*[@data-testid='TABS-ONE']");
    private final By tabTwo = By.xpath("//*[@data-testid='TABS-TWO']");
    private final By tabThree = By.xpath("//*[@data-testid='TABS-THREE']");
    private final By tabFour = By.xpath("//*[@data-testid='TABS-FOUR']");
    private final By tabFive = By.xpath("//*[@data-testid='TABS-FIVE']");
    private final By tabSix = By.xpath("//*[@data-testid='TABS-SIX']");
    private final By tabSeven = By.xpath("//*[@data-testid='TABS-SEVEN']");

    private final ActionsWeb actionsWeb;

    public enum Tab {
        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN
    }

    public void open(Tab tab) {
        By locator = resolve(tab);
        log.info("Opening navigation tab: {}", tab);
        actionsWeb.esperarVisible(locator);
        actionsWeb.hacerClick(locator);
        actionsWeb.hacerCapturaPantalla();
    }

    private By resolve(Tab tab) {
        switch (tab) {
            case ONE:
                return tabOne;
            case TWO:
                return tabTwo;
            case THREE:
                return tabThree;
            case FOUR:
                return tabFour;
            case FIVE:
                return tabFive;
            case SIX:
                return tabSix;
            case SEVEN:
                return tabSeven;
            default:
                throw new IllegalArgumentException("Unsupported tab: " + tab);
        }
    }
}
