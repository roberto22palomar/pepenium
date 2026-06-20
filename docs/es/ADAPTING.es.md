# Adaptar Pepenium

Pepenium incluye perfiles para los proveedores locales y cloud soportados, pero un proyecto consumidor puede anadir perfiles propios sin modificar ni hacer fork del framework.

Este punto de extension sirve para un Selenium Grid interno, un laboratorio de dispositivos, capabilities especificas de un proveedor o cualquier entorno que pueda representarse mediante `DriverRequest`.

## 1. Implementar Un Proveedor

Crea un provider publico con constructor publico sin argumentos. El constructor implicito es suficiente si no declaras ninguno.

```java
public final class TeamGridProfileProvider implements ExecutionProfileProvider {

    @Override
    public Collection<ExecutionProfile> profiles() {
        return List.of(new ExecutionProfile(
                "team-grid-web",
                TestTarget.WEB_DESKTOP,
                "Selenium Grid interno",
                TeamGridConfig::new
        ));
    }
}
```

El supplier es lazy: Pepenium solo crea el `DriverConfig` cuando se selecciona el perfil. Lee credenciales y variables de entorno dentro de la configuracion, no en el constructor del provider.

## 2. Construir El Driver Request

La configuracion traduce los ajustes del consumidor al modelo neutral de Pepenium.

```java
final class TeamGridConfig implements DriverConfig {

    @Override
    public DriverRequest createRequest() throws Exception {
        String gridUrl = System.getProperty("team.grid.url", "http://localhost:4444/wd/hub");
        return DriverRequest.builder()
                .driverType(DriverType.REMOTE_WEB)
                .serverUrl(URI.create(gridUrl).toURL())
                .capabilities(new ChromeOptions())
                .description("Selenium Grid interno")
                .build();
    }
}
```

Pepenium anade el target y los metadatos del perfil antes de crear la sesion. La configuracion del consumidor controla el tipo de driver, endpoint, capabilities y descripcion.

## 3. Registrar El Provider

Crea este fichero de texto UTF-8 en el proyecto consumidor:

```text
src/test/resources/META-INF/services/io.github.roberto22palomar.pepenium.core.execution.ExecutionProfileProvider
```

Su contenido es el nombre completo de la clase provider:

```text
com.example.automation.TeamGridProfileProvider
```

Puedes registrar varios providers, uno por linea. Los IDs deben ser unicos entre perfiles incluidos y externos; Pepenium falla al inicio e indica el provider conflictivo si encuentra duplicados.

## 4. Seleccionar El Perfil

Usa el ID propio igual que uno incluido:

```bash
mvn test -Dpepenium.profile=team-grid-web -Dteam.grid.url=http://grid.internal:4444/wd/hub
```

Tambien puede ser el perfil por defecto de la anotacion:

```java
@PepeniumTest(target = TestTarget.WEB_DESKTOP, profile = "team-grid-web")
class LoginTest {
}
```

## Criterios De Diseno

- Prefija los IDs con el nombre del equipo o proveedor para evitar colisiones.
- Devuelve un `DriverConfig` nuevo si contiene estado mutable.
- No arranques servicios ni conectes con proveedores durante el descubrimiento.
- Guarda secretos en variables de entorno o secret stores, nunca en el descriptor de servicio.
- Usa perfiles incluidos si solo necesitas overrides estandar; crea un provider si cambia la construccion del request.

Hay un ejemplo externo completo en [TeamGridProfileProvider.java](../../consumer-smoke/src/test/java/io/github/roberto22palomar/pepenium/smoke/custom/TeamGridProfileProvider.java).
