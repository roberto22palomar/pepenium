package io.github.roberto22palomar.pepenium.smoke.android;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;

final class AndroidShowcaseFlow {

    @PepeniumInject
    private AndroidSearchPage searchPage;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    void openQuickSearch() {
        stepRecorder.step("Validate Android search surface");
        searchPage.waitUntilLoaded();

        stepRecorder.step("Open Android quick search");
        searchPage.openQuickSearch();
    }
}
