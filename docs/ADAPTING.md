# Adapting Pepenium

Pepenium includes profiles for its supported local and cloud providers, but consumer projects can add their own execution profiles without modifying or forking the framework.

Use this extension point for an internal Selenium Grid, a device laboratory, a provider-specific capability set or any environment represented by a `DriverRequest`.

## 1. Implement A Profile Provider

Create a public provider with a public no-argument constructor. The implicit constructor is enough when no explicit constructor is declared.

```java
public final class TeamGridProfileProvider implements ExecutionProfileProvider {

    @Override
    public Collection<ExecutionProfile> profiles() {
        return List.of(new ExecutionProfile(
                "team-grid-web",
                TestTarget.WEB_DESKTOP,
                "Internal Selenium Grid",
                TeamGridConfig::new
        ));
    }
}
```

The supplier is lazy: Pepenium creates the `DriverConfig` only when that profile is selected. Keep credentials and environment reads inside the config rather than in the provider constructor.

## 2. Build The Driver Request

The config translates consumer-owned settings into Pepenium's neutral request model.

```java
final class TeamGridConfig implements DriverConfig {

    @Override
    public DriverRequest createRequest() throws Exception {
        String gridUrl = System.getProperty("team.grid.url", "http://localhost:4444/wd/hub");
        return DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .serverUrl(URI.create(gridUrl).toURL())
                .capabilities(new ChromeOptions())
                .description("Internal Selenium Grid")
                .build();
    }
}
```

Pepenium adds the selected target and profile metadata before creating the session. The consumer config owns the driver type, endpoint, capabilities and description.

## 3. Register The Provider

Create this UTF-8 text file in the consumer project:

```text
src/test/resources/META-INF/services/io.github.roberto22palomar.pepenium.core.execution.ExecutionProfileProvider
```

Its content is the fully qualified provider class name:

```text
com.example.automation.TeamGridProfileProvider
```

Multiple providers can be registered, one class name per line. Profile IDs must be unique across built-in and consumer providers; Pepenium fails early with the conflicting provider name when duplicates exist.

## 4. Select The Profile

Use the custom ID exactly like a built-in profile:

```bash
mvn test -Dpepenium.profile=team-grid-web -Dteam.grid.url=http://grid.internal:4444/wd/hub
```

It can also be the annotation default:

```java
@PepeniumTest(target = TestTarget.WEB_DESKTOP, profile = "team-grid-web")
class LoginTest {
}
```

## Design Guidance

- Prefix IDs with a team or provider name to avoid collisions.
- Return a new `DriverConfig` from the supplier when it contains mutable state.
- Do not start services or connect to providers while profiles are being discovered.
- Keep secrets in environment variables or secret stores, never in the service descriptor.
- Use built-in profiles when only standard capability overrides are needed; add a provider when request construction itself differs.

A complete external compilation example lives in [TeamGridProfileProvider.java](../consumer-smoke/src/test/java/io/github/roberto22palomar/pepenium/smoke/custom/TeamGridProfileProvider.java).
