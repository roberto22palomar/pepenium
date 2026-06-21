# Pepenium Maven Plugin

Build-time utilities for Pepenium consumer projects.

## Create a starter configuration

```text
mvn pepenium:init-config
```

Available templates:

- `local-web` (default)
- `local-android`
- `local-ios`
- `browserstack-web`

Select one with `-Dpepenium.init.template=local-android`. The goal refuses to overwrite an existing file unless
`-Dpepenium.init.force=true` is explicitly supplied.

## Validate before tests

```text
mvn pepenium:validate-config -Dpepenium.profile=local-web
```

Both goals use the project-root `pepenium.yml` by default and accept `-Dpepenium.config=path/to/file.yml`.

The plugin classpath intentionally excludes Selenium, Appium, JUnit and logging APIs from its core dependency. Configuration goals load only Pepenium's configuration classes and SnakeYAML, avoiding duplicate automation stacks inside Maven.
