package io.github.roberto22palomar.pepenium.core;

import org.apache.logging.log4j.ThreadContext;

import java.util.Objects;

public final class LoggingContext {

    private LoggingContext() {
    }

    public static void setSessionContext(DriverRequest request) {
        put("target", request.getTarget());
        put("targetShort", abbreviate(request.getTarget()));
        put("profile", request.getExecutionProfileId());
        put("driverType", request.getDriverType());
        put("driverShort", abbreviate(request.getDriverType()));
    }

    public static void setSessionId(String sessionId) {
        put("sessionId", sessionId);
        if (sessionId != null && !sessionId.isBlank()) {
            ThreadContext.put("sessionShort", shorten(sessionId));
        }
    }

    public static void clearSessionId() {
        ThreadContext.remove("sessionId");
        ThreadContext.remove("sessionShort");
    }

    public static void clearAll() {
        ThreadContext.clearMap();
    }

    private static void put(String key, Object value) {
        if (Objects.nonNull(value)) {
            ThreadContext.put(key, String.valueOf(value));
        }
    }

    private static String abbreviate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof TestTarget) {
            switch ((TestTarget) value) {
                case ANDROID_NATIVE:
                    return "android";
                case ANDROID_WEB:
                    return "android-web";
                case IOS_NATIVE:
                    return "ios";
                case IOS_WEB:
                    return "ios-web";
                case WEB_DESKTOP:
                    return "web";
                default:
                    return String.valueOf(value).toLowerCase();
            }
        }
        if (value instanceof DriverType) {
            switch ((DriverType) value) {
                case ANDROID_APPIUM:
                    return "appium-android";
                case IOS_APPIUM:
                    return "appium-ios";
                case REMOTE_WEB:
                    return "remote-web";
                case LOCAL_CHROME:
                    return "chrome";
                default:
                    return String.valueOf(value).toLowerCase();
            }
        }
        return String.valueOf(value);
    }

    private static String shorten(String sessionId) {
        return sessionId.length() <= 8 ? sessionId : sessionId.substring(0, 8);
    }
}
