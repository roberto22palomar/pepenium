package io.github.roberto22palomar.pepenium.core.observability;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class SensitiveDataSanitizer {

    private static final Pattern URL_USER_INFO = Pattern.compile(
            "(?i)([a-z][a-z0-9+.-]*://)([^/@\\s]+)@"
    );
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile(
            "(?i)(access[_-]?key|password|secret|token|api[_-]?key)(\\s*[=:]\\s*)([^,;\\s]+)"
    );

    private SensitiveDataSanitizer() {
    }

    public static String sanitizeServerUrl(URL url) {
        if (url == null) {
            return null;
        }
        String protocol = url.getProtocol() == null ? "http" : url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath() == null ? "" : url.getPath();
        String query = url.getQuery() == null ? "" : "?" + sanitizeText(url.getQuery());
        StringBuilder value = new StringBuilder(protocol).append("://").append(host);
        if (port > 0) {
            value.append(":").append(port);
        }
        return value.append(path).append(query).toString();
    }

    public static String sanitizeText(String value) {
        if (value == null) {
            return null;
        }
        String withoutUserInfo = URL_USER_INFO.matcher(value).replaceAll("$1***@");
        return SECRET_ASSIGNMENT.matcher(withoutUserInfo).replaceAll("$1$2***");
    }

    public static Object sanitizeValue(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (isSensitiveKey(key)) {
            return "***";
        }
        if (value instanceof Map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            ((Map<?, ?>) value).forEach((nestedKey, nestedValue) -> sanitized.put(
                    String.valueOf(nestedKey),
                    sanitizeValue(String.valueOf(nestedKey), nestedValue)
            ));
            return sanitized;
        }
        if (value instanceof Iterable) {
            List<Object> sanitized = new ArrayList<>();
            for (Object item : (Iterable<?>) value) {
                sanitized.add(sanitizeValue(null, item));
            }
            return sanitized;
        }
        if (value.getClass().isArray()) {
            List<Object> sanitized = new ArrayList<>();
            for (int index = 0; index < Array.getLength(value); index++) {
                sanitized.add(sanitizeValue(null, Array.get(value, index)));
            }
            return sanitized;
        }
        return value instanceof CharSequence ? sanitizeText(String.valueOf(value)) : value;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        return normalized.contains("accesskey")
                || normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("apikey");
    }
}
