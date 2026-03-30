# Empieza Aqui

Si es tu primera vez con Pepenium, no empieces leyendo todo el repositorio.

Usa esta pagina para conseguir un primer resultado rapido y entra luego en mas detalle solo cuando te haga falta.

## Elige Tu Objetivo

| Objetivo | Camino recomendado | Tiempo |
| --- | --- | --- |
| Verificar que el repo compila | Ejecutar la comprobacion de compilacion | 1-2 min |
| Ver un test real funcionando rapido | Ejecutar el showcase web desktop | 3-5 min |
| Probar Android local con la menor friccion | Appium en Docker + emulador en host | 5-15 min |
| Experimentar con Android totalmente dockerizado | Appium + emulador en Docker | 10-20 min |
| Validar consumo de API publica desde otro proyecto Maven | Ejecutar consumer smoke | 2-3 min |

## 1. Verifica Que El Repo Compila

Desde la raiz del repositorio:

```text
mvn -q -DskipTests test-compile
```

Si esto pasa, el proyecto multi-modulo esta correctamente conectado en tu maquina.

## 2. Consigue Un Primer Exito Real Rapido

El example vivo mas facil es el showcase web desktop.

Desde la raiz del repositorio:

```text
mvn -pl pepenium-examples -am "-Dpepenium.examples.skip.tests=false" "-Dpepenium.excludedTags=" "-Dtest=ExampleDesktopWebTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Esto ejecuta un flujo real publico contra `https://the-internet.herokuapp.com/login`.

Usa primero este camino si:

- eres nuevo en Pepenium
- quieres verificar lo basico del framework antes de tocar la configuracion mobile
- quieres la ruta mas corta hacia una ejecucion con sentido

## 3. Setup Android Local Recomendado

Camino recomendado:

- emulador Android en el host
- Appium en Docker

Por que este camino:

- menos instalacion local que mantener Appium manualmente
- mas predecible que un emulador completamente dockerizado
- es lo mas parecido a una configuracion practica del dia a dia

### 3.1 Arranca Appium

```text
docker compose up -d appium
```

### 3.2 Arranca Tu Emulador En Host

Arranca tu emulador Android de la forma habitual en tu maquina.

### 3.3 Usa Estos Valores De Entorno

Fichero ejemplo: [`.env.android.host-emulator.example`](/C:/dev/workspace/personal/pepenium/.env.android.host-emulator.example)

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=host.docker.internal:5555
ANDROID_DEVICE_NAME=Android Emulator
```

### 3.4 Ejecuta Tu Test Android

Ejecuta tu test Android nativo con el perfil local por defecto o desde tu IDE.

## 4. Setup Android Totalmente Dockerizado Experimental

Esto es util para labs, demos y experimentacion, pero no es la recomendacion principal.

Arranque:

```text
docker compose -f docker-compose.yaml -f docker-compose.emulator.yaml up -d
```

Fichero ejemplo: [`.env.android.docker-emulator.example`](/C:/dev/workspace/personal/pepenium/.env.android.docker-emulator.example)

```text
APPIUM_URL=http://localhost:4723
ANDROID_UDID=android-emulator:5555
ANDROID_DEVICE_NAME=Android Emulator
```

Endpoints utiles:

- Appium: `http://localhost:4723/status`
- emulador noVNC: `http://localhost:6080`

Usa este modo solo si:

- quieres explicitamente un stack mobile totalmente containerizado
- tu maquina soporta bien la virtualizacion hardware
- aceptas que puede ser menos predecible que un emulador en host

## 5. Valida El Consumo De API Publica

Si quieres saber si Pepenium es utilizable desde otro proyecto Maven, ejecuta consumer smoke.

```text
mvn -q -pl pepenium-core,pepenium-toolkit -am install -DskipTests
mvn -q -U -f consumer-smoke/pom.xml clean test-compile
```

## 6. Donde Seguir

- Usa [README.es.md](/C:/dev/workspace/personal/pepenium/README.es.md) para la vision general del proyecto.
- Usa [QUICK-START.es.md](/C:/dev/workspace/personal/pepenium/docs/es/QUICK-START.es.md) para la guia mas completa.
- Usa [ENVIRONMENT.md](/C:/dev/workspace/personal/pepenium/docs/ENVIRONMENT.md) para todas las variables soportadas.
- Usa [API.md](/C:/dev/workspace/personal/pepenium/docs/API.md) para entender que es publico y que es interno.
