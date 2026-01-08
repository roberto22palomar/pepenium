# 🧪 Pepenium – Test Automation Framework

**Pepenium** es un **framework de automatización de pruebas en Java** para aplicaciones **móviles (Android / iOS)** y **web**, construido sobre **Appium** y **Selenium**.

Está diseñado con una idea clara:  
👉 **arrancar rápido**, pero **escalar sin romperse** cuando el proyecto crece.

Soporta ejecución **local**, ejecución en **device farms** (AWS Device Farm / BrowserStack) y uso en **CI/CD**, con configuración desacoplada del código y una arquitectura pensada para proyectos reales.

---

## ✨ Características principales

- 📱 **Mobile-first**: Android e iOS como foco principal
- 🌐 Soporte **Web (desktop)** integrado
- 🧱 Arquitectura limpia y reutilizable (**core / toolkit / tests**)
- ☁️ Ejecución local o remota (**AWS Device Farm / BrowserStack**)
- ⚙️ Configuración externalizada por proveedor y plataforma
- ♻️ Extensible por proyecto sin tocar el core
- 🧪 Preparado para pipelines CI/CD

---

## ⚙️ Requisitos y configuración

### Requisitos generales

- Java
- Maven
- Appium Server

---

### 📱 Ejecución mobile en local

#### Android

- Appium Server instalado y en ejecución
- Dispositivo físico o emulador Android configurado

#### iOS

- Appium Server
- Driver **XCUITest** (driver de automatización usado por Appium en iOS)
- Entorno iOS configurado (Xcode, simulador o dispositivo físico)

---

### 🌐 Ejecución web en local (desktop)

- Driver del navegador (ej. ChromeDriver) en `src/test/resources`

---

## ☁️ Ejecución en BrowserStack y AWS Device Farm

### BrowserStack

Configurar `src/test/resources/browserstack.yml` con credenciales, plataformas y dispositivos.
Una vez configurado, los tests se pueden ejecutar directamente desde el IDE.

---

### AWS Device Farm

AWS Device Farm está orientado a ejecuciones empaquetadas y CI/CD.

Para empaquetar:
```
mvn clean package -P my-example-app-android -DskipTests
```

Subir a AWS:
- JAR generado
- Carpeta `dependency-jars`

---

## 🧠 Arquitectura

### Core (`core/`)

Configuración por proveedor y plataforma:
- `core/configs/aws/(android|ios)`
- `core/configs/browserstack/(android|ios|desktop)`

---

### Toolkit (`toolkit/`)

- `toolkit/utils`
- `toolkit/<proyecto>`

---

## 🧬 Modelo Page Object Model (POM)

### Pages

- IDs de la app (Android `resource-id`, iOS `accessibility id`)
- Acciones básicas

### Flows

- Composición de acciones de varias pages

### Tests

- Llaman a flows y validan resultados

---
---
## FINALIDAD
Pepenium intenta que automatizar sea aburrido.  
Y en testing, eso es una virtud.

