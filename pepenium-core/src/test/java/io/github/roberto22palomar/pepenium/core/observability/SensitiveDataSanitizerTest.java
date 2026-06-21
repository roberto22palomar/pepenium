package io.github.roberto22palomar.pepenium.core.observability;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveDataSanitizerTest {

    @Test
    void removesCredentialsFromServerUrls() throws Exception {
        URL url = new URL("https://user:access-secret@hub.example.test/wd/hub?token=query-secret");

        String sanitized = SensitiveDataSanitizer.sanitizeServerUrl(url);

        assertEquals("https://hub.example.test/wd/hub?token=***", sanitized);
        assertFalse(sanitized.contains("user"));
        assertFalse(sanitized.contains("access-secret"));
        assertFalse(sanitized.contains("query-secret"));
    }

    @Test
    void recursivelyRedactsNestedMapsListsAndText() {
        Object sanitized = SensitiveDataSanitizer.sanitizeValue("vendor:options", Map.of(
                "project", "Pepenium",
                "credentials", List.of(
                        Map.of("api_token", "nested-secret"),
                        "https://user:password@example.test/path"
                )
        ));
        String rendered = String.valueOf(sanitized);

        assertTrue(rendered.contains("api_token=***"));
        assertTrue(rendered.contains("https://***@example.test/path"));
        assertFalse(rendered.contains("nested-secret"));
        assertFalse(rendered.contains("password"));
    }

    @Test
    void redactsSecretAssignmentsInExceptionMessages() {
        String sanitized = SensitiveDataSanitizer.sanitizeText(
                "Provider rejected accessKey=abc123 and password: hidden"
        );

        assertEquals("Provider rejected accessKey=*** and password: ***", sanitized);
    }

    @Test
    void redactsAuthorizationHeadersCookiesAndCredentials() {
        Map<?, ?> sanitized = (Map<?, ?>) SensitiveDataSanitizer.sanitizeValue("headers", Map.of(
                "Authorization", "Bearer eyJhbGciOi.secret.signature",
                "Cookie", "session=private-value",
                "clientCredential", "provider-secret"
        ));

        assertEquals("***", sanitized.get("Authorization"));
        assertEquals("***", sanitized.get("Cookie"));
        assertEquals("***", sanitized.get("clientCredential"));
        assertEquals(
                "request failed with Bearer ***",
                SensitiveDataSanitizer.sanitizeText("request failed with Bearer eyJhbGciOi.secret.signature")
        );
    }
}
