# Historia Del Proyecto

Pepenium empezo como un proyecto practico de automatizacion con Appium/Selenium y ha ido evolucionando hacia un framework de testing reutilizable y publicable como open source.

Esta pagina resume el recorrido historico: que aporto cada fase, por que importo y que partes del proyecto conviene proteger ahora.

## Linea Temporal

| Linea | Foco | Por que importo |
| --- | --- | --- |
| `0.1.x` | Soporte inicial Android, iOS y Web | Establecio la base con Appium, Selenium y JUnit. |
| `0.2.x` | API publica de acciones en ingles | Hizo la superficie de autoria mas accesible para usuarios externos. |
| `0.3.x` | Modernizacion de dependencias y CI | Actualizo Selenium, Appium, SLF4J, Log4j y plugins Maven. |
| `0.4.x` | Fiabilidad de screenshots | Sustituyo esperas lentas o borrosas por helpers acotados y capturas rapidas. |
| `0.5.x` | Modelo target/profile | Desacoplo los tests de clases de config por proveedor mediante `TestTarget`, perfiles y session factories. |
| `0.6.x` | Modulos Maven reales | Separo runtime, toolkit y ejemplos en `pepenium-core`, `pepenium-toolkit` y `pepenium-examples`. |
| `0.7.x` | Toolkit reutilizable y consumo externo | Anadio assertions, action logging, API docs, `consumer-smoke`, guia Docker/Appium y mejores ejemplos. |
| `0.8.x` | Quality gates de libreria | Anadio Enforcer, JaCoCo, Checkstyle, SpotBugs, metadata de release y preparacion para Maven Central. |
| `0.9.0` | Reporting nativo | Anadio reportes HTML/JSON por test, indice de suite, timeline y previews de screenshots. |
| `0.9.3` | Disciplina de compatibilidad | Anadio `japicmp`, release preflight, registry tipado de perfiles y politica de API publica. |
| `0.9.4` | Autoria por anotaciones | Anadio `@PepeniumTest`, `@PepeniumInject`, `@PepeniumPage` y `PepeniumSteps` como camino recomendado. |
| `0.9.7` | Pulido de configuracion y reportes | Mejoro ejemplos de entorno, paths de screenshots, logging de capabilities y lectura de reportes. |
| Unreleased | Hardening open source y contratos cross-platform | Refuerza contratos publicos, consistencia Android/iOS, configuracion segura, higiene del repo y comandos repetibles. |

## Direccion Actual

La promesa actual del proyecto es sencilla:

- los tests declaran el target funcional que necesitan
- los execution profiles deciden donde se ejecuta ese target
- Pepenium gestiona lifecycle de driver/sesion, diagnosticos y reporting
- los contratos del toolkit mantienen la autoria portable entre Web, Android e iOS cuando tiene sentido
- los ejemplos y `consumer-smoke` demuestran que el framework se puede aprender y consumir desde fuera del reactor principal

## Que Debe Mantenerse Estable

Estas areas son ahora los anclajes principales de compatibilidad:

- valores de `TestTarget` e ids de execution profiles incluidos
- autoria por anotaciones con `@PepeniumTest`, `@PepeniumInject`, `@PepeniumPage` y `PepeniumSteps`
- autoria clasica con `BaseTest`
- contratos del toolkit como `WebActions`, `MobileActions`, `WebAssertions` y `MobileAssertions`
- locators `PepeniumBy` para page objects compatibles Android/iOS
- la experiencia de reporte HTML como diagnostico visible para usuarios

El JSON de reporting sigue siendo util, pero evolutivo; no deberia tratarse como schema publico estable hasta que exista versionado explicito.

## Forma Open Source

El trabajo reciente sobre el repositorio busca que Pepenium sea mas facil de entender:

- la raiz queda reservada para entry points, politicas y launch helpers
- los templates de entorno viven en `docs/env/`
- la configuracion de build vive en `config/`
- los comandos repetibles para contributors viven en `scripts/`
- el consumo de API publica se valida con `consumer-smoke`
- las reglas de ubicacion del repositorio viven en [REPOSITORY.md](../REPOSITORY.md)

El resultado deberia ser un proyecto mas facil de inspeccionar, ejecutar y mantener como libreria, no como una carpeta privada de automatizacion.

## Siguiente Hito Historico

El siguiente hito natural es `1.0.0`.

Antes de llegar ahi, Pepenium deberia seguir cerrando:

- la superficie documentada de API publica
- la cobertura de `consumer-smoke` para usos externos reales
- el camino demo desde clonar hasta obtener un reporte util
- las expectativas de compatibilidad entre releases menores
- la frontera entre HTML soportado y JSON de reporting aun evolutivo
