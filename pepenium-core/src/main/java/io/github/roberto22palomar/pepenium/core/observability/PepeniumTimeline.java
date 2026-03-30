package io.github.roberto22palomar.pepenium.core.observability;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PepeniumTimeline {

    private static final ThreadLocal<List<Event>> EVENTS = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Instant> STARTED_AT = new ThreadLocal<>();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private PepeniumTimeline() {
    }

    public static void beginTest() {
        EVENTS.set(new ArrayList<>());
        STARTED_AT.set(Instant.now());
    }

    public static void recordStep(String message) {
        record(EventType.STEP, EventStatus.INFO, message, null);
    }

    public static void recordAction(String message) {
        record(EventType.ACTION, EventStatus.INFO, message, null);
    }

    public static void recordWait(String message) {
        record(EventType.WAIT, EventStatus.INFO, message, null);
    }

    public static void recordAssertionPassed(String message) {
        record(EventType.ASSERT, EventStatus.PASSED, message, null);
    }

    public static void recordAssertionFailed(String message) {
        record(EventType.ASSERT, EventStatus.FAILED, message, null);
    }

    public static void recordScreenshot(String message, String screenshotPath) {
        record(EventType.SCREENSHOT, EventStatus.INFO, message, screenshotPath);
    }

    public static void recordError(String message) {
        record(EventType.ERROR, EventStatus.FAILED, message, null);
    }

    public static Snapshot snapshot() {
        return new Snapshot(new ArrayList<>(EVENTS.get()), STARTED_AT.get());
    }

    public static void clear() {
        EVENTS.remove();
        STARTED_AT.remove();
    }

    private static void record(EventType type, EventStatus status, String message, String screenshotPath) {
        if (message == null || message.isBlank()) {
            return;
        }
        ensureStarted();
        EVENTS.get().add(new Event(
                LocalTime.now().format(TIME_FORMAT),
                type,
                status,
                message.trim(),
                screenshotPath
        ));
    }

    private static void ensureStarted() {
        if (STARTED_AT.get() == null) {
            STARTED_AT.set(Instant.now());
        }
    }

    public enum EventType {
        STEP,
        ACTION,
        WAIT,
        ASSERT,
        SCREENSHOT,
        ERROR
    }

    public enum EventStatus {
        INFO,
        PASSED,
        FAILED
    }

    public static final class Snapshot {
        private final List<Event> events;
        private final Instant startedAt;

        private Snapshot(List<Event> events, Instant startedAt) {
            this.events = events;
            this.startedAt = startedAt;
        }

        public List<Event> getEvents() {
            return Collections.unmodifiableList(events);
        }

        public Instant getStartedAt() {
            return startedAt;
        }
    }

    public static final class Event {
        private final String time;
        private final EventType type;
        private final EventStatus status;
        private final String message;
        private final String screenshotPath;

        private Event(String time, EventType type, EventStatus status, String message, String screenshotPath) {
            this.time = time;
            this.type = type;
            this.status = status;
            this.message = message;
            this.screenshotPath = screenshotPath;
        }

        public String getTime() {
            return time;
        }

        public EventType getType() {
            return type;
        }

        public EventStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getScreenshotPath() {
            return screenshotPath;
        }
    }
}
