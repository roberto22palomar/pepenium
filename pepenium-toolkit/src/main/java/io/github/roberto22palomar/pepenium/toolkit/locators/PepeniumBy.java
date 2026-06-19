package io.github.roberto22palomar.pepenium.toolkit.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

import java.util.Objects;

/**
 * Locator helpers tuned for Pepenium tests.
 */
public final class PepeniumBy {

    private PepeniumBy() {
    }

    public static By accessibilityId(String accessibilityId) {
        return AppiumBy.accessibilityId(requireText("accessibilityId", accessibilityId));
    }

    public static By androidResourceId(String resourceId) {
        return By.id(requireText("resourceId", resourceId));
    }

    public static By androidUiAutomator(String expression) {
        return AppiumBy.androidUIAutomator(requireText("expression", expression));
    }

    public static By iosClassChain(String classChain) {
        return AppiumBy.iOSClassChain(requireText("classChain", classChain));
    }

    public static By iosPredicate(String predicate) {
        return AppiumBy.iOSNsPredicateString(requireText("predicate", predicate));
    }

    public static By text(String text) {
        String literal = xpathLiteral(requireText("text", text));
        return By.xpath("//*["
                + "@text = " + literal
                + " or @label = " + literal
                + " or @name = " + literal
                + " or @value = " + literal
                + " or normalize-space(.) = " + literal
                + "]");
    }

    public static By textContains(String textFragment) {
        String literal = xpathLiteral(requireText("textFragment", textFragment));
        return By.xpath("//*["
                + "contains(@text, " + literal + ")"
                + " or contains(@label, " + literal + ")"
                + " or contains(@name, " + literal + ")"
                + " or contains(@value, " + literal + ")"
                + " or contains(normalize-space(.), " + literal + ")"
                + "]");
    }

    static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        StringBuilder literal = new StringBuilder("concat(");
        boolean appendComma = false;
        int segmentStart = 0;
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) == '\'') {
                appendComma = appendQuotedSegment(literal, appendComma, value.substring(segmentStart, index));
                appendComma = appendRawSegment(literal, appendComma, "\"'\"");
                segmentStart = index + 1;
            }
        }
        appendQuotedSegment(literal, appendComma, value.substring(segmentStart));
        return literal.append(')').toString();
    }

    private static boolean appendQuotedSegment(StringBuilder literal, boolean appendComma, String segment) {
        if (segment.isEmpty()) {
            return appendComma;
        }
        if (appendComma) {
            literal.append(", ");
        }
        literal.append('\'').append(segment).append('\'');
        return true;
    }

    private static boolean appendRawSegment(StringBuilder literal, boolean appendComma, String segment) {
        if (appendComma) {
            literal.append(", ");
        }
        literal.append(segment);
        return true;
    }

    private static String requireText(String name, String value) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
