package io.github.roberto22palomar.pepenium.smoke.ios;

import io.github.roberto22palomar.pepenium.core.runtime.PepeniumInject;
import io.github.roberto22palomar.pepenium.core.runtime.PepeniumSteps;

final class IOSShowcaseFlow {

    @PepeniumInject
    private IOSSearchPage searchPage;

    @PepeniumInject
    private PepeniumSteps stepRecorder;

    void openQuickSearch() {
        stepRecorder.step("Validate iOS search surface");
        searchPage.waitUntilLoaded();

        stepRecorder.step("Open iOS quick search");
        searchPage.openQuickSearch();
    }
}
