# Pepenium Maven Plugin

Build-time utilities for Pepenium consumer projects.

## Create a starter configuration

```text
mvn pepenium:init-config
```

Available templates:

- `local-web` (default)
- `local-android`
- `browserstack-web`

Select one with `-Dpepenium.init.template=local-android`. The goal refuses to overwrite an existing file unless
`-Dpepenium.init.force=true` is explicitly supplied.

## Validate before tests

```text
mvn pepenium:validate-config -Dpepenium.profile=local-web
```

Both goals use the project-root `pepenium.yml` by default and accept `-Dpepenium.config=path/to/file.yml`.
