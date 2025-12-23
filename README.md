# 🧪 Pepenium – Test Automation Framework

Pepenium es un **framework de automatización de pruebas en Java** orientado a aplicaciones **móviles (Android / iOS)** y **web**, construido sobre **Appium** y **Selenium**.

Está diseñado para ser **simple de arrancar**, pero **robusto y escalable** cuando el proyecto crece: ejecución local, device farms, CI/CD, múltiples plataformas y configuraciones desacopladas del código.

---

## ✨ Características principales

- Automatización **mobile-first** (Android / iOS) y **web**
- Arquitectura limpia y reutilizable
- Ejecución local o remota (Device Farm / CI)
- Configuración externalizada (properties / variables de entorno)
- Preparado para proyectos reales, no demos
- Fácil de extender y mantener

---

## 📦 Estructura del Proyecto




La separación por paquetes permite crecer sin romper el core ni duplicar lógica.

---

## 🧠 Arquitectura

### 🔹 Core

El paquete `core` contiene el corazón del framework:

- **DriverFactory**  
  Decide y construye el driver adecuado (Android, iOS, Web) según configuración.
- **BaseTest**  
  Clase base de todos los tests. Maneja:
    - ciclo de vida
    - inicialización y cierre del driver
    - gestión de errores
- **DriverConfig**  
  Centraliza la lectura de properties y variables de entorno.

El core es **agnóstico del dominio** y no depende de ninguna app concreta.

---

### 🔹 Toolkit

Conjunto de utilidades reutilizables que abstraen Appium/Selenium.

Ejemplo:

```java
androidActions.click(locator);
androidActions.scrollToElement(locator);
androidActions.swipeUp();
