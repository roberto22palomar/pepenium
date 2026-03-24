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

Pepenium es un framework de automatizacion en Java para Android, iOS y Web, construido sobre Appium, Selenium y JUnit 5.

La direccion actual del proyecto es un modelo simple de usar y guiado por perfiles: los tests describen que son y los execution profiles deciden donde se ejecutan.

## Por Que Pepenium

- Un test por target funcional, no un test por proveedor de infraestructura
- Un modelo de ejecucion compartido para local, BrowserStack y AWS Device Farm
- Helpers reutilizables `Actions*` y `Assertions*` para Web, Android e iOS
- Lifecycle de driver y sesion centralizado en una unica factoria de sesion
- Capturas pensadas para flujos rapidos sin screenshots borrosos

Consulta [QUICK-START.es.md](QUICK-START.es.md) para empezar rapido y [CHANGELOG.md](CHANGELOG.md) para el historico de versiones.

## Que Ha Cambiado Hasta v0.5.0

- Unificacion de la creacion de sesion alrededor de `DriverRequest`, `DriverSession` y `DefaultDriverSessionFactory`
- Introduccion de `TestTarget` y execution profiles para que los tests ya no devuelvan clases de config especificas de proveedor
- Simplificacion de los examples a un test por target funcional
- Nuevas aserciones reutilizables para Web, Android e iOS
- Mejora de la estabilizacion de screenshots y nueva API `takeScreenshotFast()`
- Ampliacion del soporte BrowserStack a todos los sets de dispositivos definidos en YAML

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
- `TestTarget`

Los builders de request especificos de proveedor viven actualmente en:

- `core/configs/local`
- `core/configs/browserstack`
- `core/configs/aws`

### `toolkit`

Bloques reutilizables:

- Web: `ActionsWeb`, `AssertionsWeb`
- Android: `ActionsApp`, `AssertionsApp`
- iOS: `ActionsAppIOS`, `AssertionsAppIOS`
- utilidades comunes como loaders YAML y mapeadores de BrowserStack
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

## Actions, Assertions y Screenshots

Pepenium incluye acciones y aserciones especificas por plataforma:

- Web: `ActionsWeb`, `AssertionsWeb`
- Android: `ActionsApp`, `AssertionsApp`
- iOS: `ActionsAppIOS`, `AssertionsAppIOS`

Mejoras recientes de screenshots:

- estabilizacion acotada antes de capturar
- `takeScreenshotFast()` para checkpoints ligeros
- fallback al directorio temporal cuando `DEVICEFARM_SCREENSHOT_PATH` no esta definido
- mejor comportamiento en flujos rapidos, especialmente en movil

## Estado Actual

Pepenium ya es util para trabajo real de automatizacion. Se ha ejercitado contra flujos reales de Android, configuraciones remotas y capas reutilizables de actions y assertions. La siguiente linea de mejora es seguir puliendo la ergonomia de ejecucion y la preparacion como libreria reusable.

## Documentacion

- Quick start en ingles: [QUICK-START.md](QUICK-START.md)
- Quick start en espanol: [QUICK-START.es.md](QUICK-START.es.md)
- README en ingles: [README.md](README.md)
