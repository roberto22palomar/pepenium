# Guia Rapida

Esta guia muestra la forma mas rapida de entender y ejecutar Pepenium tal y como esta en `v0.8.0`.

Si este es tu primer contacto con el proyecto, usa primero [START-HERE.es.md](START-HERE.es.md) y vuelve aqui cuando quieras la guia mas completa.

## 1. Requisitos

- Java 11
- Maven 3.x
- Appium Server para ejecuciones mobile en local
- Un emulador o dispositivo Android si quieres ejecucion Android local
- Credenciales y configuracion de BrowserStack o AWS para ejecucion remota

Si prefieres no instalar Appium directamente en tu maquina, usa el `docker-compose.yaml` de la raiz para ejecutar Appium en Docker mientras el emulador Android sigue en el host.

## 2. Idea Clave

Ahora los tests declaran un target funcional, no una clase de config especifica del proveedor.

```java
public class ExampleAndroidNativeTest extends BaseTest {

    @Override
    protected TestTarget getTarget() {
        return TestTarget.ANDROID_NATIVE;
    }
}
```

El entorno se selecciona mediante un execution profile.

## 3. Tests Principales de Ejemplo

- Android nativo: [ExampleAndroidNativeTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS nativo: [ExampleIOSNativeTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Web desktop: [ExampleDesktopWebTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

El example web actual es un showcase funcional real sobre `https://the-internet.herokuapp.com/login` y demuestra:

- login real
- assertions y actions del toolkit
- step tracking
- screenshots
- navegacion por varias paginas demo publicas y estables

El ejemplo Android nativo actual es un showcase template, no un example funcional contra una app publica. Sirve para demostrar:

- `ActionsApp`
- `AssertionsApp`
- diseno de flows orientado a pasos
- page objects con limites de carga explicitos
- una estructura de test Android preparada para adaptarse a una app real

El ejemplo iOS nativo actual sigue la misma estrategia. Sirve para demostrar:

- `ActionsAppIOS`
- `AssertionsAppIOS`
- diseno de flows orientado a pasos
- page objects con limites de carga explicitos
- una estructura de test iOS preparada para adaptarse a una app real

## 4. Execution Profiles Incluidos

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

Estos perfiles incluidos se definen en:

- `pepenium-core/src/main/resources/execution-profiles.yml`

## 5. Ejecutar Desde el IDE

### Android Nativo

Ejecuta directamente [ExampleAndroidNativeTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java).

Resolucion por defecto:

- target: `ANDROID_NATIVE`
- perfil por defecto: `local-android`

Variables utiles en local:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=emulator-5554
ANDROID_DEVICE_NAME=Android Device
APP_PATH=C:\ruta\app.apk
APP_PACKAGE=com.example.app
APP_ACTIVITY=com.example.MainActivity
```

Si ejecutas Appium con Docker Compose y mantienes el emulador en el host, usa:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=host.docker.internal:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Flujo tipico:

1. Arranca el emulador Android en la maquina host.
2. Arranca Appium con `docker compose up -d appium`.
3. Espera a que `http://localhost:4723/status` responda.
4. Ejecuta los mismos tests Android locales de siempre.

El compose habilita ADB remoto para que el contenedor pueda conectarse de vuelta al emulador del host mediante `host.docker.internal:5555`.

Opcion experimental totalmente dockerizada:

- fichero base: `docker-compose.yaml`
- overlay del emulador: `docker-compose.emulator.yaml`
- recomendable solo en Linux o en Windows 11 + WSL2 con virtualizacion anidada y `/dev/kvm` disponible
- imagen comunitaria usada para el emulador: `budtmo/docker-android:emulator_13.0`

Arranque:

```text
docker compose -f docker-compose.yaml -f docker-compose.emulator.yaml up -d
```

Valores a usar en el entorno del test:

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=android-emulator:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Endpoints utiles:

- estado de Appium: `http://localhost:4723/status`
- noVNC del emulador: `http://localhost:6080`

Esta modalidad queda documentada como experimental a proposito porque los emuladores Android en Docker dependen mucho de la virtualizacion hardware y suelen ser bastante menos predecibles que un emulador en host.

### Web Desktop

Ejecuta directamente [ExampleDesktopWebTest.java](../../pepenium-examples/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java).

Resolucion por defecto:

- target: `WEB_DESKTOP`
- perfil por defecto: `local-web`

Opcional:

```text
PEPENIUM_BASE_URL=https://the-internet.herokuapp.com/login
PEPENIUM_WEB_USERNAME=tomsmith
PEPENIUM_WEB_PASSWORD=SuperSecretPassword!
```

Puedes ejecutar solo el showcase desktop web con:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

## 6. Ejecutar el Mismo Test en Otro Entorno

Sobrescribe el perfil con:

```text
-Dpepenium.profile=browserstack-android
```

o:

```text
PEPENIUM_PROFILE=browserstack-android
```

Ejemplos:

```text
-Dpepenium.profile=aws-android
-Dpepenium.profile=browserstack-ios
-Dpepenium.profile=browserstack-windows-web
```

## 7. Flujo Recomendado en IDE

Crea varias run configurations para el mismo test:

- `Android Native - Local`
- `Android Native - BrowserStack`
- `Android Native - AWS`

Cada configuracion apunta a la misma clase y solo cambia `pepenium.profile`.

Asi tienes cambio de entorno en un click sin editar el test.

## 8. Que Veras en Runtime

Cuando arranca una sesion, Pepenium imprime un banner ASCII y luego registra contexto de ejecucion compacto.

El contexto tipico incluye:

- execution profile
- target
- tipo de driver
- session id corta

## 9. Diagnostico de Fallos

Cuando falla un test, Pepenium reporta automaticamente:

- ruta del screenshot
- execution profile y target
- session id
- URL y titulo en sesiones web
- package, activity o contexto en sesiones mobile cuando estan disponibles
- los pasos recientes registrados antes del fallo

Por defecto, el step tracking:

- registra automaticamente operaciones comunes de `Actions*`
- conserva los ultimos `10` pasos
- se puede configurar con `PEPENIUM_STEP_TRACKER_LIMIT` o `-Dpepenium.step.tracker.limit=...`

Tambien puedes anadir pasos mas humanos desde tests o flows con:

```java
step("Aceptar aviso legal");
```

Si quieres mas detalle tecnico del framework, activa:

```text
PEPENIUM_DETAIL_LOGGING=true
```

o:

```text
-Dpepenium.detail.logging=true
```

## 10. Screenshots

Helpers disponibles:

- `takeScreenshot()`
- `takeScreenshotFast()`

`takeScreenshotFast()` es util para checkpoints rapidos donde quieres menos coste de captura.

## 11. Donde Se Resuelve la Ejecucion

Clases principales:

- [BaseTest.java](../../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/BaseTest.java)
- [TestTarget.java](../../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/TestTarget.java)
- [ExecutionProfiles.java](../../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfiles.java)
- [ExecutionProfileResolver.java](../../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/execution/ExecutionProfileResolver.java)
- [DefaultDriverSessionFactory.java](../../pepenium-core/src/main/java/io/github/roberto22palomar/pepenium/core/runtime/DefaultDriverSessionFactory.java)
- `pepenium-core/src/main/resources/execution-profiles.yml`

## 12. Primeros Pasos Recomendados

1. Ejecuta `ExampleAndroidNativeTest` en local.
2. Crea una segunda run configuration con `-Dpepenium.profile=browserstack-android`.
3. Ejecuta `ExampleDesktopWebTest` en local.
4. Revisa `core/configs/...` para ver como cada proveedor construye una `DriverRequest` neutral.
5. Explora `toolkit/actions/...` para helpers reutilizables de interaccion y `core/config/...` para la carga de configuracion de providers.
6. Usa [ENVIRONMENT.md](../ENVIRONMENT.md) para ver todas las variables de entorno y system properties soportadas en un unico sitio.

