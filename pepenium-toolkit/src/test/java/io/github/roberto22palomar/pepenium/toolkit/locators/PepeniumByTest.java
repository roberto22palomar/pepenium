package io.github.roberto22palomar.pepenium.toolkit.locators;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PepeniumByTest {

    @Test
    void accessibilityIdBuildsAppiumLocator() {
        By locator = PepeniumBy.accessibilityId("login-button");

        assertNotNull(locator);
        assertTrue(locator.toString().contains("login-button"));
    }

    @Test
    void textBuildsCrossPlatformNativeXPath() {
        By locator = PepeniumBy.text("Continue");

        String expression = locator.toString();
        assertTrue(expression.contains("@text = 'Continue'"));
        assertTrue(expression.contains("@label = 'Continue'"));
        assertTrue(expression.contains("@name = 'Continue'"));
        assertTrue(expression.contains("@value = 'Continue'"));
    }

    @Test
    void textContainsBuildsCrossPlatformNativeXPath() {
        By locator = PepeniumBy.textContains("Ready");

        String expression = locator.toString();
        assertTrue(expression.contains("contains(@text, 'Ready')"));
        assertTrue(expression.contains("contains(@label, 'Ready')"));
    }

    @Test
    void xpathLiteralEscapesSingleAndDoubleQuotes() {
        assertEquals("'Plain'", PepeniumBy.xpathLiteral("Plain"));
        assertEquals("\"Can't\"", PepeniumBy.xpathLiteral("Can't"));
        assertEquals("concat('He said \"go', \"'\", ' now\"')",
                PepeniumBy.xpathLiteral("He said \"go' now\""));
    }

    @Test
    void rejectsBlankValuesBeforeCreatingLocator() {
        assertThrows(IllegalArgumentException.class, () -> PepeniumBy.accessibilityId(" "));
        assertThrows(NullPointerException.class, () -> PepeniumBy.text(null));
    }
}
