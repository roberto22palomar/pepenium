# Guia Rapida

Esta guia muestra la forma mas rapida de entender y ejecutar Pepenium despues del refactor del modelo de ejecucion y de la simplificacion de los examples.

## 1. Requisitos

- Java 11
- Maven 3.x
- Appium Server para ejecuciones mobile en local
- Un emulador o dispositivo Android si quieres ejecucion Android local
- Credenciales y configuracion de BrowserStack o AWS para ejecucion remota

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

- Android nativo: [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java)
- Android web: [ExampleAndroidWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidWebTest.java)
- iOS nativo: [ExampleIOSNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSNativeTest.java)
- iOS web: [ExampleIOSWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/ios/ExampleIOSWebTest.java)
- Web desktop: [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java)

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

## 5. Ejecutar Desde el IDE

### Android Nativo

Ejecuta directamente [ExampleAndroidNativeTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/android/ExampleAndroidNativeTest.java).

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

### Web Desktop

Ejecuta directamente [ExampleDesktopWebTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/tests/myProjectExample/web/ExampleDesktopWebTest.java).

Resolucion por defecto:

- target: `WEB_DESKTOP`
- perfil por defecto: `local-web`

Opcional:

```text
PEPENIUM_BASE_URL=https://example.com
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

## 8. Donde Se Resuelve la Ejecucion

Clases principales:

- [BaseTest.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/BaseTest.java)
- [TestTarget.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/TestTarget.java)
- [ExecutionProfiles.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/ExecutionProfiles.java)
- [ExecutionProfileResolver.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/ExecutionProfileResolver.java)
- [DefaultDriverSessionFactory.java](/C:/dev/workspace/personal/pepenium/src/test/java/io/github/roberto22palomar/pepenium/core/DefaultDriverSessionFactory.java)

## 9. Actions, Assertions y Screenshots

Hay helpers reutilizables para todas las plataformas soportadas:

- Web: `ActionsWeb`, `AssertionsWeb`
- Android: `ActionsApp`, `AssertionsApp`
- iOS: `ActionsAppIOS`, `AssertionsAppIOS`

Los screenshots soportan:

- `takeScreenshot()`
- `takeScreenshotFast()`

## 10. Primeros Pasos Recomendados

1. Ejecuta `ExampleAndroidNativeTest` en local.
2. Crea una segunda run configuration con `-Dpepenium.profile=browserstack-android`.
3. Ejecuta `ExampleDesktopWebTest` en local.
4. Revisa `core/configs/...` para ver como cada proveedor construye una `DriverRequest` neutral.
