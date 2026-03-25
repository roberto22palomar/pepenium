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
Usa [ENVIRONMENT.md](ENVIRONMENT.md) como referencia central de variables de entorno y properties de runtime.

## Que Aporta v0.6.0

- Division del repositorio en `pepenium-core`, `pepenium-toolkit` y `pepenium-examples`
- Movimiento del runtime del framework a fuentes reales de produccion en `src/main`
- Traslado de los modelos de BrowserStack y la carga YAML al `core`
- Enfoque de `toolkit` en helpers reutilizables de autoria de tests como acciones y utilidades de soporte
- Aislamiento de tests y page objects de ejemplo en `pepenium-examples`
- Conservacion de los flujos de build, test y packaging de ejemplos desde la raiz del repositorio

## Arquitectura Actual

Modulos del repositorio:

- `pepenium-core`: motor del framework, runtime, ejecucion y configuracion de providers
- `pepenium-toolkit`: helpers reutilizables para quien escribe tests, como acciones y utilidades de soporte
- `pepenium-examples`: tests, flows y page objects de ejemplo

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
- `StepTracker`
- `TestTarget`
- `core/config/browserstack`: modelos de configuracion de BrowserStack
- `core/config/yaml`: loaders YAML para catalogos de BrowserStack

Los builders de request especificos de proveedor viven actualmente en:

- `core/configs/local`
- `core/configs/browserstack`
- `core/configs/aws`

### `toolkit`

Bloques reutilizables:

- `toolkit/actions`: `ActionsWeb`, `ActionsApp`, `ActionsAppIOS`
- `toolkit/support`: helpers reutilizables de settle y scroll

### `examples`

Tests de ejemplo que muestran el patron de uso previsto:

- `pepenium-examples/src/test/java/.../tests/myProjectExample/android`
- `pepenium-examples/src/test/java/.../tests/myProjectExample/ios`
- `pepenium-examples/src/test/java/.../tests/myProjectExample/web`

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
- con la metadata de perfiles cargada desde `pepenium-core/src/main/resources/execution-profiles.yml`

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

El catalogo de perfiles incluido se define en:

- `pepenium-core/src/main/resources/execution-profiles.yml`

## Tests de Ejemplo

- Android nativo: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS nativo: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Web desktop: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

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

- `pepenium-core/src/main/resources/browserstackExamples/browserstackAndroid.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstackAndroidWEB.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstackIOS.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstackIOSWEB.yml.example`
- `pepenium-core/src/main/resources/browserstackExamples/browserstack.yml.example`

Los perfiles de AWS Device Farm siguen el mismo modelo de `TestTarget`, aunque continuan orientados a flujos de ejecucion empaquetados definidos en `pom.xml`.

El catalogo de execution profiles ahora esta externalizado en `pepenium-core/src/main/resources/execution-profiles.yml`, asi que los ids y descripciones disponibles se pueden consultar sin entrar al codigo Java.

## Screenshots, Logging y Diagnostico de Fallos

Pepenium incluye:

- `takeScreenshot()` para capturas mas seguras
- `takeScreenshotFast()` para checkpoints mas ligeros
- fallback al directorio temporal cuando `DEVICEFARM_SCREENSHOT_PATH` no esta definido
- un banner ASCII de Pepenium al arrancar una sesion
- logs compactos con profile, target, driver y sesion corta
- reporte automatico de fallo con screenshot y contexto de runtime
- tracking de pasos recientes dentro del resumen de fallo

El contexto automatico de fallo incluye:

- profile, target, driver y session id
- URL y titulo en sesiones web
- package, activity o contexto en sesiones mobile cuando estan disponibles
- un resumen de capabilities en lugar de dumps crudos y ruidosos
- los ultimos pasos del framework antes del fallo

Comportamiento del step tracking:

- registra automaticamente operaciones comunes de `Actions*`
- conserva solo los ultimos `10` pasos por defecto
- se puede ajustar con `PEPENIUM_STEP_TRACKER_LIMIT` o `-Dpepenium.step.tracker.limit=...`
- se puede enriquecer manualmente desde tests o flows con `step("Abrir buscador")`

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
- Referencia de entorno: [ENVIRONMENT.md](ENVIRONMENT.md)

