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

## Empieza Aqui

Si eres nuevo en el proyecto, empieza por [START-HERE.es.md](/C:/dev/workspace/personal/pepenium/docs/es/START-HERE.es.md).

Primeros pasos recomendados:

1. Ejecuta `mvn -q -DskipTests test-compile`
2. Ejecuta el showcase web desktop para tener tu primer exito real
3. Pasa a Android solo cuando la ruta web ya funcione

Ficheros de entorno listos para copiar:

- [`.env.web.example`](/C:/dev/workspace/personal/pepenium/.env.web.example)
- [`.env.android.host-emulator.example`](/C:/dev/workspace/personal/pepenium/.env.android.host-emulator.example)
- [`.env.android.docker-emulator.example`](/C:/dev/workspace/personal/pepenium/.env.android.docker-emulator.example)

## Por Que Pepenium

- Un test por target funcional, no un test por proveedor
- Un modelo de ejecucion compartido para local, BrowserStack y AWS Device Farm
- Lifecycle de driver y sesion centralizado en una unica factoria
- Helpers reutilizables `Actions*` para Web, Android e iOS
- Capturas pensadas para flujos rapidos sin screenshots borrosos
- Logs mas limpios con contexto automatico y evidencia de fallo

Consulta [START-HERE.es.md](/C:/dev/workspace/personal/pepenium/docs/es/START-HERE.es.md) para el camino mas rapido al primer uso, [QUICK-START.es.md](/C:/dev/workspace/personal/pepenium/docs/es/QUICK-START.es.md) para la guia mas completa y [CHANGELOG.md](CHANGELOG.md) para el historico de versiones.
Usa [ENVIRONMENT.md](/C:/dev/workspace/personal/pepenium/docs/ENVIRONMENT.md) como referencia central de variables de entorno y properties de runtime.
Usa el `docker-compose.yaml` de la raiz si quieres ejecutar el servidor Appium local en Docker mientras el emulador Android sigue en el host.

## Que Aporta v0.8.0

- Quality gates reales de Maven con Enforcer, JaCoCo, Checkstyle y SpotBugs
- Un camino de `verify` mas fuerte en CI para comprobar higiene de libreria de forma continua
- Metadata y packaging orientados a release con sources y Javadocs
- Un workflow dedicado de GitHub Actions para publicar releases etiquetadas
- Endurecimiento de `core` y `toolkit` para mantener util y en verde la nueva linea base de analisis estatico

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
- `toolkit/assertions`: `AssertionsWeb`, `AssertionsApp`, `AssertionsAppIOS`
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

### Showcase Web Funcional

Los ejemplos web ahora son examples funcionales reales sobre [The Internet](https://the-internet.herokuapp.com/), no solo plantillas de estructura.

El showcase actual de desktop/mobile-web demuestra:

- ejecucion por profile reutilizando la misma clase de test
- uso de `ActionsWeb` y `AssertionsWeb`
- trazabilidad semantica con `StepTracker`
- screenshots como evidencia
- page objects y orquestacion mediante flows
- un flujo publico real de varias pantallas:
  - login
  - validacion del area segura
  - interaccion con dropdown
  - validacion de estados en checkboxes
  - navegacion al example de add/remove elements

Valores por defecto del example funcional:

- `PEPENIUM_BASE_URL=https://the-internet.herokuapp.com/login`
- `PEPENIUM_WEB_USERNAME=tomsmith`
- `PEPENIUM_WEB_PASSWORD=SuperSecretPassword!`

Puedes ejecutar el showcase desktop web con:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

### Showcase Template De Android Nativo

El ejemplo Android nativo queda ahora planteado como un showcase template mas serio, no como un example funcional contra una app publica.

Ese showcase Android pone el foco en:

- pasos semanticos mediante `StepTracker`
- uso reutilizable de `ActionsApp` y `AssertionsApp`
- limites de carga de pagina explicitos
- screenshots como evidencia
- un flujo corto pero representativo de busqueda y bottom navigation, pensado para adaptarse a una app real

Asi el ejemplo Android sigue siendo honesto y util sin depender de una app publica de terceros que no sea lo bastante estable como carta de presentacion oficial del framework.

### Showcase Template De iOS Nativo

El ejemplo iOS nativo sigue la misma estrategia que Android nativo: queda planteado como un showcase template mas serio, no como un example funcional contra una app publica.

Ese showcase iOS pone el foco en:

- pasos semanticos mediante `StepTracker`
- uso reutilizable de `ActionsAppIOS` y `AssertionsAppIOS`
- limites de carga de pagina explicitos
- screenshots como evidencia
- un flujo corto pero representativo de busqueda y bottom navigation, pensado para adaptarse a una app real

Asi el ejemplo iOS queda alineado con Android y deja una historia mas coherente para la parte mobile del framework.

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

Appium dockerizado con emulador en host:

```text
docker compose up -d appium
APPIUM_URL=http://localhost:4723
ANDROID_UDID=host.docker.internal:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Stack experimental totalmente dockerizado con emulador:

```text
docker compose -f docker-compose.yaml -f docker-compose.emulator.yaml up -d
APPIUM_URL=http://localhost:4723
ANDROID_UDID=android-emulator:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Esta modalidad de emulador queda como experimental y encaja mejor en Linux o en Windows 11 + WSL2 cuando `/dev/kvm` esta disponible.

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
PEPENIUM_BASE_URL=https://the-internet.herokuapp.com/login
PEPENIUM_WEB_USERNAME=tomsmith
PEPENIUM_WEB_PASSWORD=SuperSecretPassword!
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

- Quick start en ingles: [QUICK-START.md](/C:/dev/workspace/personal/pepenium/docs/QUICK-START.md)
- Quick start en espanol: [QUICK-START.es.md](/C:/dev/workspace/personal/pepenium/docs/es/QUICK-START.es.md)
- README en ingles: [README.md](README.md)
- Referencia de entorno: [ENVIRONMENT.md](/C:/dev/workspace/personal/pepenium/docs/ENVIRONMENT.md)

