package io.github.roberto22palomar.pepenium.core.execution;

import io.github.roberto22palomar.pepenium.core.configs.aws.android.AndroidConfigAWS;
import io.github.roberto22palomar.pepenium.core.configs.aws.android.AndroidWebConfigAWS;
import io.github.roberto22palomar.pepenium.core.configs.aws.ios.IOSConfigAWS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.android.AndroidWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.MacWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.desktop.WindowsWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.browserstack.ios.IOSWebConfigBS;
import io.github.roberto22palomar.pepenium.core.configs.local.android.AndroidConfigLocal;
import io.github.roberto22palomar.pepenium.core.configs.local.android.AndroidWebConfigLocal;
import io.github.roberto22palomar.pepenium.core.configs.local.desktop.ChromeWebConfigLocal;
import io.github.roberto22palomar.pepenium.core.configs.local.desktop.EdgeWebConfigLocal;
import io.github.roberto22palomar.pepenium.core.configs.local.desktop.FirefoxWebConfigLocal;

import java.util.function.Supplier;

enum BuiltInDriverConfigKey {
    LOCAL_ANDROID(AndroidConfigLocal::new),
    LOCAL_ANDROID_WEB(AndroidWebConfigLocal::new),
    LOCAL_DESKTOP_CHROME(ChromeWebConfigLocal::new),
    LOCAL_DESKTOP_FIREFOX(FirefoxWebConfigLocal::new),
    LOCAL_DESKTOP_EDGE(EdgeWebConfigLocal::new),
    AWS_ANDROID(AndroidConfigAWS::new),
    AWS_ANDROID_WEB(AndroidWebConfigAWS::new),
    AWS_IOS(IOSConfigAWS::new),
    BROWSERSTACK_ANDROID(AndroidConfigBS::new),
    BROWSERSTACK_ANDROID_WEB(AndroidWebConfigBS::new),
    BROWSERSTACK_IOS(IOSConfigBS::new),
    BROWSERSTACK_IOS_WEB(IOSWebConfigBS::new),
    BROWSERSTACK_WINDOWS_WEB(WindowsWebConfigBS::new),
    BROWSERSTACK_MAC_WEB(MacWebConfigBS::new);

    private final Supplier<DriverConfig> supplier;

    BuiltInDriverConfigKey(Supplier<DriverConfig> supplier) {
        this.supplier = supplier;
    }

    Supplier<DriverConfig> supplier() {
        return supplier;
    }
}
