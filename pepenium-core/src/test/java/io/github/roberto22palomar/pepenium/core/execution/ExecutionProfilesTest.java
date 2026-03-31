package io.github.roberto22palomar.pepenium.core.execution;

import io.github.roberto22palomar.pepenium.core.configs.local.desktop.ChromeWebConfigLocal;
import io.github.roberto22palomar.pepenium.core.configs.local.desktop.EdgeWebConfigLocal;
import io.github.roberto22palomar.pepenium.core.configs.local.desktop.FirefoxWebConfigLocal;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionProfilesTest {

    @Test
    void includesNewLocalDesktopBrowserProfiles() {
        List<String> webProfiles = ExecutionProfiles.list().stream()
                .filter(profile -> profile.getTarget() == TestTarget.WEB_DESKTOP)
                .map(ExecutionProfile::getId)
                .collect(Collectors.toList());

        assertTrue(webProfiles.contains("local-web"));
        assertTrue(webProfiles.contains("local-web-firefox"));
        assertTrue(webProfiles.contains("local-web-edge"));
    }

    @Test
    void resolvesTypedConfigSuppliersForBuiltInDesktopProfiles() {
        assertInstanceOf(ChromeWebConfigLocal.class, ExecutionProfiles.get("local-web").createConfig());
        assertInstanceOf(FirefoxWebConfigLocal.class, ExecutionProfiles.get("local-web-firefox").createConfig());
        assertInstanceOf(EdgeWebConfigLocal.class, ExecutionProfiles.get("local-web-edge").createConfig());
    }

    @Test
    void rejectsProfileDefinitionsWithoutConfigKey() {
        ExecutionProfiles.ProfileDefinition definition = new ExecutionProfiles.ProfileDefinition();
        definition.setId("broken-profile");
        definition.setTarget(TestTarget.WEB_DESKTOP);
        definition.setDescription("Broken profile");

        ExecutionProfiles.ProfilesFile yamlFile = new ExecutionProfiles.ProfilesFile();
        yamlFile.setProfiles(List.of(definition));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ExecutionProfiles.loadProfiles(yamlFile)
        );

        assertEquals("Execution profile configKey is required for 'broken-profile'", error.getMessage());
    }

    @Test
    void rejectsDuplicateProfileIds() {
        ExecutionProfiles.ProfileDefinition first = new ExecutionProfiles.ProfileDefinition();
        first.setId("duplicated");
        first.setTarget(TestTarget.WEB_DESKTOP);
        first.setDescription("First");
        first.setConfigKey(BuiltInDriverConfigKey.LOCAL_DESKTOP_CHROME);

        ExecutionProfiles.ProfileDefinition second = new ExecutionProfiles.ProfileDefinition();
        second.setId("duplicated");
        second.setTarget(TestTarget.WEB_DESKTOP);
        second.setDescription("Second");
        second.setConfigKey(BuiltInDriverConfigKey.LOCAL_DESKTOP_FIREFOX);

        ExecutionProfiles.ProfilesFile yamlFile = new ExecutionProfiles.ProfilesFile();
        yamlFile.setProfiles(List.of(first, second));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> ExecutionProfiles.loadProfiles(yamlFile)
        );

        assertTrue(error.getMessage().contains("Duplicate execution profile id 'duplicated'"));
    }
}
