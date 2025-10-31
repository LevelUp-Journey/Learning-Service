# Configuración CORS 🌐

## Descripción General

El servicio **Learning Service** cuenta con una configuración completa de CORS (Cross-Origin Resource Sharing) que permite controlar el acceso desde diferentes orígenes, especialmente útil para aplicaciones frontend.

## 📋 Configuración Actual

### Orígenes Permitidos (Allowed Origins)

Por defecto, el servicio acepta peticiones desde los siguientes orígenes:

```yaml
http://localhost:3000    # React (Create React App)
http://localhost:4200    # Angular
http://localhost:5173    # Vite (Vue, React, Svelte)
http://localhost:8080    # Spring Boot frontend
```

### Métodos HTTP Permitidos

```
GET, POST, PUT, DELETE, PATCH, OPTIONS
```

### Headers Permitidos

- **Todos los headers** están permitidos (`*`)
- Incluye: `Authorization`, `Content-Type`, `Accept`, etc.

### Headers Expuestos

Los siguientes headers son expuestos al cliente:

```
Authorization
Content-Type
X-Total-Count
X-Page-Number
X-Page-Size
```

### Credenciales

- **Allow Credentials**: `true`
- Permite envío de cookies y headers de autenticación

### Max Age

- **Cache de preflight**: `3600 segundos (1 hora)`
- Las respuestas OPTIONS se cachean durante 1 hora

## ⚙️ Configuración Personalizada

### En `application.yml`

```yaml
application:
  security:
    allowed-origins: http://localhost:3000,http://localhost:4200,http://localhost:5173,http://localhost:8080
    allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
    allowed-headers: "*"
    exposed-headers: Authorization,Content-Type,X-Total-Count,X-Page-Number,X-Page-Size
    allow-credentials: true
    max-age: 3600
```

### Agregar Nuevos Orígenes

Para agregar un nuevo origen (por ejemplo, un dominio de producción):

```yaml
application:
  security:
    allowed-origins: http://localhost:3000,https://app.levelupjourney.com,https://admin.levelupjourney.com
```

### Variables de Entorno

También puedes configurar CORS mediante variables de entorno:

```bash
export APPLICATION_SECURITY_ALLOWED_ORIGINS=http://localhost:3000,https://app.example.com
export APPLICATION_SECURITY_ALLOWED_METHODS=GET,POST,PUT,DELETE
export APPLICATION_SECURITY_MAX_AGE=7200
```

## 🔒 Seguridad

### Producción

Para producción, se recomienda:

1. **Especificar orígenes exactos** en lugar de usar wildcards
2. **Limitar métodos** solo a los necesarios
3. **Revisar headers expuestos** para no revelar información sensible
4. **Ajustar max-age** según las necesidades de caché

Ejemplo de configuración para producción:

```yaml
application:
  security:
    allowed-origins: https://app.levelupjourney.com,https://admin.levelupjourney.com
    allowed-methods: GET,POST,PUT,DELETE
    allowed-headers: Authorization,Content-Type,Accept
    exposed-headers: Authorization,Content-Type
    allow-credentials: true
    max-age: 3600
```

### Desarrollo

La configuración actual es ideal para desarrollo local, permitiendo múltiples puertos comunes de frameworks frontend.

## 🧪 Probar CORS

### Usando cURL

```bash
# Preflight request (OPTIONS)
curl -X OPTIONS http://localhost:8085/api/v1/topics \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v

# Actual request
curl -X GET http://localhost:8085/api/v1/topics \
  -H "Origin: http://localhost:3000" \
  -v
```

### Headers de Respuesta Esperados

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
Access-Control-Expose-Headers: Authorization,Content-Type,X-Total-Count,X-Page-Number,X-Page-Size
```

## 📝 Implementación Técnica

### SecurityConfiguration.java

La configuración se implementa en `SecurityConfiguration.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Allowed Origins
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
    
    // Allowed Methods
    configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
    
    // Allowed Headers
    if ("*".equals(allowedHeaders)) {
        configuration.addAllowedHeader("*");
    } else {
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
    }
    
    // Exposed Headers
    configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
    
    // Allow Credentials
    configuration.setAllowCredentials(allowCredentials);
    
    // Max Age
    configuration.setMaxAge(maxAge);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Integración con Spring Security

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // ... resto de configuración
}
```

## 🎯 Casos de Uso

### Frontend React (localhost:3000)

```javascript
// Configuración de axios
const api = axios.create({
  baseURL: 'http://localhost:8085/api/v1',
  withCredentials: true, // Importante para CORS con credenciales
  headers: {
    'Content-Type': 'application/json'
  }
});

// Uso con token JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Frontend Angular (localhost:4200)

```typescript
// http.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';

constructor(private http: HttpClient) {}

private getHeaders(): HttpHeaders {
  const token = localStorage.getItem('token');
  return new HttpHeaders({
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  });
}

getTopics() {
  return this.http.get('http://localhost:8085/api/v1/topics', {
    headers: this.getHeaders(),
    withCredentials: true
  });
}
```

### Frontend Vue + Vite (localhost:5173)

```javascript
// api.js
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8085/api/v1',
  withCredentials: true
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
```

## 🔍 Troubleshooting

### Error: "No 'Access-Control-Allow-Origin' header is present"

**Causa**: El origen no está en la lista de orígenes permitidos.

**Solución**: Agregar el origen a `application.security.allowed-origins`

### Error: "CORS preflight request did not succeed"

**Causa**: El servidor no responde correctamente a las peticiones OPTIONS.

**Solución**: Verificar que Spring Security no bloquee las peticiones OPTIONS.

### Error: "Credentials flag is true, but Access-Control-Allow-Credentials is not"

**Causa**: `allow-credentials` está en `false` pero el cliente envía credenciales.

**Solución**: Configurar `allow-credentials: true` en `application.yml`

### Error: Headers personalizados no son enviados

**Causa**: Los headers no están en `allowed-headers`.

**Solución**: Agregar los headers específicos o usar `*` para permitir todos.

## 📚 Referencias

- [Spring Security CORS Documentation](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [MDN Web Docs - CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [W3C CORS Specification](https://www.w3.org/TR/cors/)

## ✅ Verificación

Para verificar que CORS está funcionando correctamente:

1. **Ejecutar el servicio**: `./mvnw spring-boot:run`
2. **Acceder desde un frontend** en uno de los orígenes permitidos
3. **Verificar en DevTools** que los headers CORS están presentes
4. **Comprobar que las peticiones** con Authorization funcionan

---

**Nota**: Esta configuración está optimizada para desarrollo. Para producción, ajusta los orígenes y métodos según tus necesidades específicas.
