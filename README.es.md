<p align="center">
  <a href="https://github.com/roberto22palomar/pepenium/actions/workflows/ci-build.yml">
    <img alt="Build" src="https://github.com/roberto22palomar/pepenium/actions/workflows/ci-build.yml/badge.svg" />
  </a>
</p>

<p align="center">
  <a href="LICENSE">
    <img alt="License" src="https://img.shields.io/badge/License-MIT-green.svg" />
  </a>
  <img alt="Java" src="https://img.shields.io/badge/Java-11-blue.svg" />
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.x-orange.svg" />
  <img alt="JUnit" src="https://img.shields.io/badge/JUnit-5-purple.svg" />
  <img alt="Selenium" src="https://img.shields.io/badge/Selenium-4-43B02A.svg" />
  <img alt="Appium Client" src="https://img.shields.io/badge/Appium%20Client-10-00BFFF.svg" />
</p>

# Pepenium

<p align="center">
  <a href="README.md">English</a> |
  <strong>Espanol</strong>
</p>

Pepenium es un framework de automatizacion en Java para Android, iOS y Web construido sobre Appium, Selenium y JUnit 5.

La direccion actual del proyecto es simple de entender y practica de ejecutar: los tests declaran un target funcional, los execution profiles deciden donde corren y el framework se encarga del ciclo de vida de sesion, el logging y el diagnostico de fallos.

## Por Que Pepenium

- Un test por target funcional, no un test por proveedor
- Un modelo de ejecucion compartido para local, BrowserStack y AWS Device Farm
- Lifecycle de driver y sesion centralizado en una unica factoria
- Helpers reutilizables `Actions*` para Web, Android e iOS
- Capturas pensadas para flujos rapidos sin screenshots borrosos
- Logs mas limpios con contexto automatico y evidencia de fallo

Consulta [QUICK-START.es.md](QUICK-START.es.md) para empezar rapido y [CHANGELOG.md](CHANGELOG.md) para el historico de versiones.

## Que Aporta v0.5.0

- Unificacion de la creacion de sesion alrededor de `DriverRequest`, `DriverSession` y `DefaultDriverSessionFactory`
- Introduccion de `TestTarget` y execution profiles para que los tests ya no devuelvan clases de config especificas de proveedor
- Simplificacion de los examples a un test por target funcional
- Nuevo banner ASCII de Pepenium al crear una sesion
- Mejora de la estabilizacion de screenshots y nueva API `takeScreenshotFast()`
- Logging compacto con contexto de profile, target, driver y sesion
- Diagnostico automatico de fallos con ruta de screenshot y contexto web o mobile
- Logging detallado opcional mediante `PEPENIUM_DETAIL_LOGGING=true`

## Arquitectura Actual

### `core`

Piezas de runtime y ejecucion del framework:

- `BaseTest`
- `DriverConfig`
- `DriverRequest`
- `DriverSession`
- `DriverSessionFactory`
- `DefaultDriverSessionFactory`
- `ExecutionProfile`
- `ExecutionProfiles`
- `ExecutionProfileResolver`
- `FailureContextReporter`
- `LoggingContext`
- `PepeniumBanner`
- `TestTarget`

Los builders de request especificos de proveedor viven actualmente en:

- `core/configs/local`
- `core/configs/browserstack`
- `core/configs/aws`

### `toolkit`

Bloques reutilizables:

- Web: `ActionsWeb`
- Android: `ActionsApp`
- iOS: `ActionsAppIOS`
- utilidades comunes como loaders YAML, mapeadores de BrowserStack y helpers de estabilizacion de screenshots
- page objects y flows de ejemplo bajo `toolkit/myProjectExample`

### `tests`

Tests de ejemplo que muestran el patron de uso previsto:

- `tests/myProjectExample/android`
- `tests/myProjectExample/ios`
- `tests/myProjectExample/web`

Ahora los examples estan agrupados por target funcional y no por entorno.

## Modelo de Ejecucion

Los tests declaran un `TestTarget`:

```java
public class ExampleAndroidNativeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }
}
```

En runtime, Pepenium resuelve un execution profile:

- desde `-Dpepenium.profile=...`
- o desde `PEPENIUM_PROFILE`
- o desde el perfil por defecto del target cuando existe

Eso permite ejecutar el mismo test en varios entornos sin tocar su codigo.

## Targets Soportados

- `ANDROID_NATIVE`
- `ANDROID_WEB`
- `IOS_NATIVE`
- `IOS_WEB`
- `WEB_DESKTOP`

## Execution Profiles Incluidos

- `local-android`
- `local-android-web`
- `local-web`
- `aws-android`
- `aws-android-web`
- `aws-ios`
- `browserstack-android`
- `browserstack-android-web`
- `browserstack-ios`
- `browserstack-ios-web`
- `browserstack-windows-web`
- `browserstack-mac-web`

## Tests de Ejemplo

- Android nativo: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS nativo: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Web desktop: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

## Ejecucion Desde el IDE

El flujo recomendado es:

1. Mantener un test por target.
2. Crear una run configuration del IDE por cada execution profile que te interese.
3. Apuntar todas esas configuraciones a la misma clase de test.

Ejemplo para un mismo test Android nativo:

- `Android Native - Local`
- `Android Native - BrowserStack`
- `Android Native - AWS`

Cada run configuration solo cambia `pepenium.profile`.

Asi consigues ejecucion de un click sin editar el test.

## Ejecucion Local

### Android Nativo

Perfil por defecto de `ANDROID_NATIVE`: `local-android`

Variables utiles:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
APP_PATH=C:\ruta\app.apk
APP_PACKAGE=com.example.app
APP_ACTIVITY=com.example.MainActivity
```

### Android Web

Perfil por defecto de `ANDROID_WEB`: `local-android-web`

Variables utiles:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
PEPENIUM_BASE_URL=https://example.com
```

### Web Desktop

Perfil por defecto de `WEB_DESKTOP`: `local-web`

Variables utiles:

```text
PEPENIUM_BASE_URL=https://example.com
```

## BrowserStack y AWS

Los perfiles de BrowserStack se apoyan en los YAML de ejemplo de:

- `src/test/resources/browserstackExamples/browserstackAndroid.yml.example`
- `src/test/resources/browserstackExamples/browserstackAndroidWEB.yml.example`
- `src/test/resources/browserstackExamples/browserstackIOS.yml.example`
- `src/test/resources/browserstackExamples/browserstackIOSWEB.yml.example`
- `src/test/resources/browserstackExamples/browserstack.yml.example`

Los perfiles de AWS Device Farm siguen el mismo modelo de `TestTarget`, aunque continuan orientados a flujos de ejecucion empaquetados definidos en `pom.xml`.

## Screenshots, Logging y Diagnostico de Fallos

Pepenium incluye:

- `takeScreenshot()` para capturas mas seguras
- `takeScreenshotFast()` para checkpoints mas ligeros
- fallback al directorio temporal cuando `DEVICEFARM_SCREENSHOT_PATH` no esta definido
- un banner ASCII de Pepenium al arrancar una sesion
- logs compactos con profile, target, driver y sesion corta
- reporte automatico de fallo con screenshot y contexto de runtime

El contexto automatico de fallo incluye:

- profile, target, driver y session id
- URL y titulo en sesiones web
- package, activity o contexto en sesiones mobile cuando estan disponibles
- un resumen de capabilities en lugar de dumps crudos y ruidosos

Si necesitas mas detalle tecnico del framework, activa:

```text
PEPENIUM_DETAIL_LOGGING=true
```

o:

```text
-Dpepenium.detail.logging=true
```

## Estado Actual

Pepenium ya es util para trabajo real de automatizacion. Se ha ejercitado contra flujos reales de apps Android, emuladores locales, caminos de configuracion remota y capas reutilizables de actions. La siguiente linea importante es seguir mejorando la preparacion como libreria reusable y los diagnosticos de mas alto nivel.

## Documentacion

- Quick start en ingles: [QUICK-START.md](QUICK-START.md)
- Quick start en espanol: [QUICK-START.es.md](QUICK-START.es.md)
- README en ingles: [README.md](README.md)
