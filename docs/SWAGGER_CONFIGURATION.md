# Configuración de Swagger/OpenAPI

## 📋 Resumen

Esta documentación describe la configuración de Swagger/OpenAPI para el Learning Service, incluyendo las URLs de acceso y la configuración de seguridad.

## 🔗 URLs de Acceso

### Swagger UI (Interfaz Gráfica)
```
http://localhost:8085/swagger-ui.html
http://localhost:8085/swagger-ui/index.html
```

### OpenAPI JSON
```
http://localhost:8085/v3/api-docs
```

### OpenAPI YAML
```
http://localhost:8085/v3/api-docs.yaml
```

## 🔧 Configuración

### 1. Dependencia Maven (pom.xml)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

### 2. Configuración en application.yml

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
    filter: true
    displayRequestDuration: true
    displayOperationId: false
  show-actuator: false
  packages-to-scan: com.levelupjourney.learningservice
  paths-to-match: /api/**
```

### 3. Clase de Configuración (OpenApiConfiguration.java)

La configuración se implementa mediante un `@Bean` que retorna un objeto `OpenAPI` con toda la información de la API.

**Características principales:**
- **Información de la API**: Título, versión, descripción, contacto y licencia
- **Servidores**: URLs de los servidores (local y por defecto)
- **Seguridad**: Esquema de autenticación JWT con Bearer Token
- **Componentes**: Definición del esquema de seguridad Bearer Auth

## 🔐 Autenticación JWT

### Esquema de Seguridad

La API utiliza autenticación JWT mediante el esquema Bearer:

```
Authorization: Bearer <your-jwt-token>
```

### Uso en Swagger UI

1. Accede a Swagger UI
2. Haz clic en el botón **"Authorize"** (candado verde en la parte superior)
3. Ingresa tu token JWT en el formato: `Bearer <token>`
4. Haz clic en **"Authorize"**
5. Ahora puedes probar los endpoints protegidos

### Endpoints Públicos (Sin autenticación)

Los siguientes endpoints NO requieren autenticación:

- `GET /api/v1/guides/**` - Consulta de guías
- `GET /api/v1/courses/**` - Consulta de cursos
- `GET /api/v1/topics/**` - Consulta de tópicos
- `/swagger-ui/**` - Interfaz de Swagger
- `/v3/api-docs/**` - Documentación OpenAPI
- `/actuator/**` - Endpoints de monitoreo

## 🛡️ Configuración de Seguridad

### Endpoints Permitidos en SecurityConfiguration

```java
.requestMatchers(
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/v3/api-docs.yaml",
    "/api-docs/**",
    "/swagger-resources/**",
    "/webjars/**"
).permitAll()
```

Esta configuración permite el acceso a todos los recursos estáticos de Swagger sin necesidad de autenticación.

## 📝 Documentación de Endpoints

### Anotaciones Recomendadas

Para documentar tus endpoints, utiliza las siguientes anotaciones de OpenAPI:

```java
@Operation(
    summary = "Crear una nueva guía",
    description = "Crea una nueva guía de aprendizaje con el contenido proporcionado"
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Guía creada exitosamente"),
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
    @ApiResponse(responseCode = "401", description = "No autorizado")
})
@PostMapping
public ResponseEntity<GuideResource> createGuide(
    @Parameter(description = "Datos de la nueva guía")
    @Valid @RequestBody CreateGuideResource resource
) {
    // Implementation
}
```

### Anotaciones Comunes

- `@Tag(name = "Guides")` - Agrupa endpoints en el mismo tag
- `@Operation` - Describe la operación
- `@ApiResponses` - Define las respuestas posibles
- `@ApiResponse` - Define una respuesta específica
- `@Parameter` - Describe un parámetro
- `@Schema` - Describe un modelo de datos

## 🚀 Características Habilitadas

### En Swagger UI

- **operationsSorter: method** - Ordena operaciones por método HTTP
- **tagsSorter: alpha** - Ordena tags alfabéticamente
- **tryItOutEnabled: true** - Habilita el botón "Try it out"
- **filter: true** - Habilita el filtro de búsqueda
- **displayRequestDuration: true** - Muestra la duración de las peticiones
- **displayOperationId: false** - Oculta el ID de operación

## 🔍 Troubleshooting

### Problema: Error al cargar recursos de Swagger

**Solución aplicada:**
1. Cambiamos de anotaciones `@OpenAPIDefinition` a configuración programática con `@Bean`
2. Actualizamos los endpoints permitidos en `SecurityConfiguration`
3. Agregamos configuración explícita en `application.yml`
4. Ajustamos la versión de SpringDoc a 2.7.0 (compatible con Spring Boot 3.5.x)

### Problema: 401 Unauthorized en Swagger UI

**Solución:**
Verifica que los siguientes endpoints estén permitidos en `SecurityConfiguration`:
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/swagger-resources/**`
- `/webjars/**`

### Problema: Endpoints no aparecen en Swagger

**Solución:**
1. Verifica que tus controladores estén en el paquete: `com.levelupjourney.learningservice`
2. Verifica que tus endpoints coincidan con el patrón: `/api/**`
3. Asegúrate de que tus controladores tengan `@RestController` o `@Controller` + `@ResponseBody`

## 📚 Referencias

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

## ✅ Cambios Realizados

### Archivos Modificados

1. **OpenApiConfiguration.java**
   - Cambio de anotaciones a configuración programática
   - Uso de `@Bean` para retornar objeto `OpenAPI`
   - Configuración más flexible y debuggeable

2. **SecurityConfiguration.java**
   - Actualización de endpoints permitidos para Swagger
   - Inclusión de rutas adicionales: `/v3/api-docs/**`, `/swagger-resources/**`, `/webjars/**`

3. **application.yml**
   - Agregada sección de configuración de SpringDoc
   - Configuración de paths y comportamiento de Swagger UI

4. **pom.xml**
   - Versión de SpringDoc ajustada a 2.7.0 para mejor compatibilidad

### Beneficios de los Cambios

✅ Configuración más explícita y mantenible  
✅ Mejor control sobre la configuración de OpenAPI  
✅ Solución de problemas de carga de recursos  
✅ Compatibilidad garantizada con Spring Boot 3.5.x  
✅ Configuración centralizada en `application.yml`

## 🎯 Próximos Pasos

1. Inicia tu aplicación
2. Accede a http://localhost:8085/swagger-ui.html
3. Verifica que todos los endpoints aparezcan correctamente
4. Prueba la autenticación JWT con un token válido
5. Documenta tus nuevos endpoints con las anotaciones de OpenAPI
