package io.github.roberto22palomar.pepenium.smoke.android;

import java.util.function.Consumer;

final class AndroidShowcaseFlow {

    private final AndroidSearchPage searchPage;
    private final Consumer<String> stepRecorder;

    AndroidShowcaseFlow(AndroidSearchPage searchPage, Consumer<String> stepRecorder) {
        this.searchPage = searchPage;
        this.stepRecorder = stepRecorder;
    }

    void openQuickSearch() {
        stepRecorder.accept("Validate Android search surface");
        searchPage.waitUntilLoaded();

        stepRecorder.accept("Open Android quick search");
        searchPage.openQuickSearch();
    }
}
