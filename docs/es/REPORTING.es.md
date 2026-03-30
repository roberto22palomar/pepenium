# Reporting

Pepenium incluye ahora un flujo nativo de reporting que funciona de serie despues de ejecutar los tests.

Por defecto, los reportes se escriben en:

```text
target/pepenium-reports/
```

Los artefactos generados incluyen:

- `index.html`: punto de entrada de suite con tarjetas resumen, desglose por profile/provider y filtros rapidos
- `summary.json`: resumen de suite en formato legible por maquinas
- `report-*.html`: reportes HTML ricos por test
- `report-*.json`: payload estructurado por test
- `screenshots/`: capturas enlazadas desde el reporte cuando hay evidencia disponible

Los reportes HTML por test incluyen:

- execution story y failure story
- diagnostic focus e highlights
- badges pass/fail para assertions
- previews de screenshots agrupadas
- timeline con steps, actions, waits, assertions, screenshots y errores
- contexto de runtime como target, profile, provider, device, platform y browser cuando esta disponible

El directorio de reportes puede redirigirse con:

```text
PEPENIUM_REPORT_DIR=/ruta/personalizada
```

o:

```text
-Dpepenium.report.dir=/ruta/personalizada
```

La consola tambien imprime enlaces directos `file:///...` al reporte individual y al indice de suite para acelerar la investigacion local.
