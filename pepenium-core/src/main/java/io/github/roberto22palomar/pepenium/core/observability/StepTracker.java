package io.github.roberto22palomar.pepenium.core.observability;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class StepTracker {

    private static final ThreadLocal<Deque<String>> RECENT_STEPS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> TOTAL_RECORDED = ThreadLocal.withInitial(() -> 0);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int DEFAULT_STEP_LIMIT = 10;

    private StepTracker() {
    }

    public static void record(String stepDescription) {
        if (stepDescription == null || stepDescription.isBlank()) {
            return;
        }

        int limit = stepLimit();
        Deque<String> steps = RECENT_STEPS.get();
        if (steps.size() >= limit) {
            steps.removeFirst();
        }
        steps.addLast(formatStep(stepDescription));
        TOTAL_RECORDED.set(TOTAL_RECORDED.get() + 1);
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                new ArrayList<>(RECENT_STEPS.get()),
                TOTAL_RECORDED.get(),
                stepLimit()
        );
    }

    public static void clear() {
        RECENT_STEPS.remove();
        TOTAL_RECORDED.remove();
    }

    private static String formatStep(String stepDescription) {
        return LocalTime.now().format(TIME_FORMAT) + " | " + stepDescription.trim();
    }

    private static int stepLimit() {
        String property = System.getProperty("pepenium.step.tracker.limit");
        if (property == null || property.isBlank()) {
            property = System.getenv("PEPENIUM_STEP_TRACKER_LIMIT");
        }

        if (property == null || property.isBlank()) {
            return DEFAULT_STEP_LIMIT;
        }

        try {
            int parsed = Integer.parseInt(property.trim());
            return parsed > 0 ? parsed : DEFAULT_STEP_LIMIT;
        } catch (NumberFormatException ignored) {
            return DEFAULT_STEP_LIMIT;
        }
    }

    public static final class Snapshot {
        private final List<String> steps;
        private final int totalRecorded;
        private final int limit;

        private Snapshot(List<String> steps, int totalRecorded, int limit) {
            this.steps = steps;
            this.totalRecorded = totalRecorded;
            this.limit = limit;
        }

        public List<String> getSteps() {
            return steps;
        }

        public int getTotalRecorded() {
            return totalRecorded;
        }

        public int getLimit() {
            return limit;
        }

        public boolean isTruncated() {
            return totalRecorded > steps.size();
        }
    }
}
