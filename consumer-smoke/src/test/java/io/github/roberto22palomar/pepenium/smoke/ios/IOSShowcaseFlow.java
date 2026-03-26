package io.github.roberto22palomar.pepenium.smoke.ios;

import java.util.function.Consumer;

final class IOSShowcaseFlow {

    private final IOSSearchPage searchPage;
    private final Consumer<String> stepRecorder;

    IOSShowcaseFlow(IOSSearchPage searchPage, Consumer<String> stepRecorder) {
        this.searchPage = searchPage;
        this.stepRecorder = stepRecorder;
    }

    void openQuickSearch() {
        stepRecorder.accept("Validate iOS search surface");
        searchPage.waitUntilLoaded();

        stepRecorder.accept("Open iOS quick search");
        searchPage.openQuickSearch();
    }
}
