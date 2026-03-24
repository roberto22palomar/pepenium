package io.github.roberto22palomar.pepenium.core;

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

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExecutionProfiles {

    private static final Map<String, ExecutionProfile> PROFILES = new LinkedHashMap<>();

    static {
        register(new ExecutionProfile("local-android", TestTarget.ANDROID_NATIVE, "Local Android native app", AndroidConfigLocal::new));
        register(new ExecutionProfile("local-android-web", TestTarget.ANDROID_WEB, "Local Android mobile web", AndroidWebConfigLocal::new));
        register(new ExecutionProfile("local-web", TestTarget.WEB_DESKTOP, "Local desktop web with Chrome", ChromeWebConfigLocal::new));

        register(new ExecutionProfile("aws-android", TestTarget.ANDROID_NATIVE, "AWS Device Farm Android native app", AndroidConfigAWS::new));
        register(new ExecutionProfile("aws-android-web", TestTarget.ANDROID_WEB, "AWS Device Farm Android mobile web", AndroidWebConfigAWS::new));
        register(new ExecutionProfile("aws-ios", TestTarget.IOS_NATIVE, "AWS Device Farm iOS native app", IOSConfigAWS::new));

        register(new ExecutionProfile("browserstack-android", TestTarget.ANDROID_NATIVE, "BrowserStack Android native app", AndroidConfigBS::new));
        register(new ExecutionProfile("browserstack-android-web", TestTarget.ANDROID_WEB, "BrowserStack Android mobile web", AndroidWebConfigBS::new));
        register(new ExecutionProfile("browserstack-ios", TestTarget.IOS_NATIVE, "BrowserStack iOS native app", IOSConfigBS::new));
        register(new ExecutionProfile("browserstack-ios-web", TestTarget.IOS_WEB, "BrowserStack iOS mobile web", IOSWebConfigBS::new));
        register(new ExecutionProfile("browserstack-windows-web", TestTarget.WEB_DESKTOP, "BrowserStack Windows desktop web", WindowsWebConfigBS::new));
        register(new ExecutionProfile("browserstack-mac-web", TestTarget.WEB_DESKTOP, "BrowserStack Mac desktop web", MacWebConfigBS::new));
    }

    private ExecutionProfiles() {
    }

    public static ExecutionProfile get(String profileId) {
        ExecutionProfile profile = PROFILES.get(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown Pepenium execution profile: " + profileId);
        }
        return profile;
    }

    public static void validateCompatibility(ExecutionProfile profile, TestTarget target) {
        if (profile.getTarget() != target) {
            throw new IllegalStateException(
                    "Execution profile '" + profile.getId() + "' targets " + profile.getTarget()
                            + " but the test targets " + target
            );
        }
    }

    private static void register(ExecutionProfile profile) {
        PROFILES.put(profile.getId(), profile);
    }
}
