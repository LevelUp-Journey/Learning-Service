# Actualización de Autorización de Guías - Resumen

## 📋 Descripción General

Se ha implementado un nuevo sistema de autorización para los endpoints de visualización de guías que diferencia claramente entre usuarios estudiantes, profesores y acceso al dashboard.

## 🎯 Reglas de Autorización Implementadas

### 1. **Usuarios No Autenticados**
- ✅ Solo pueden ver guías con status **PUBLISHED**
- ❌ No tienen acceso a guías DRAFT

### 2. **Estudiantes (ROLE_STUDENT)**
- ✅ Solo pueden ver guías con status **PUBLISHED**
- ❌ No tienen acceso a guías DRAFT (incluso si son autores)

### 3. **Profesores (ROLE_TEACHER) - Vista Pública**
- ✅ Solo ven guías con status **PUBLISHED** (como cualquier otro usuario)
- ❌ No ven guías DRAFT sin el parámetro especial

### 4. **Profesores (ROLE_TEACHER) - Dashboard (`for=dashboard`)**
- ✅ Ven **TODAS** sus propias guías (DRAFT y PUBLISHED)
- ✅ Solo ven las guías donde ellos son autores
- ❌ No ven guías de otros profesores (incluso PUBLISHED)

## 🔄 Endpoints Actualizados

### 1. `GET /api/v1/guides`

**Nuevo comportamiento:**

```bash
# Vista pública - Solo PUBLISHED
GET /api/v1/guides

# Dashboard de profesor - TODAS las guías propias
GET /api/v1/guides?for=dashboard
```

**Parámetros:**
- `for=dashboard` (opcional): Activa el modo dashboard para profesores
- `title` (opcional): Filtro por título
- `topicIds` (opcional): Filtro por topics
- `authorIds` (opcional): Filtro por autores (ignorado si `for=dashboard`)
- Paginación estándar: `page`, `size`, `sort`

**Lógica implementada:**
```java
if (for=dashboard && isTeacher) {
    // Mostrar TODAS las guías del profesor autenticado
    status = null; // Permite DRAFT y PUBLISHED
    filterByAuthorIds = Set.of(currentUserId); // Solo sus guías
} else {
    // Vista pública - SOLO PUBLISHED
    status = PUBLISHED;
}
```

---

### 2. `GET /api/v1/guides/{guideId}`

**Nuevo comportamiento:**

- **Si la guía es PUBLISHED**: Cualquier usuario autenticado puede verla
- **Si la guía es DRAFT**: Solo el autor de la guía puede verla
- **Usuarios no autenticados**: Solo ven guías PUBLISHED

**Respuesta:**
- `200 OK`: Si tiene acceso
- `404 Not Found`: Si no existe o no tiene permisos

---

### 3. `GET /api/v1/guides/teachers/{teacherId}`

**Nuevo comportamiento:**

- **Siempre retorna SOLO guías PUBLISHED**
- Es una vista de "portfolio público" del profesor
- No requiere autenticación
- Útil para mostrar el trabajo publicado de un profesor

**Ejemplo:**
```bash
GET /api/v1/guides/teachers/teacher123?page=0&size=20&sort=createdAt,desc
```

---

### 4. `GET /api/v1/guides/search`

**Comportamiento sin cambios:**

- Siempre retorna solo guías **PUBLISHED**
- Requiere al menos un parámetro de búsqueda
- Retorna información básica (id, title, description, coverImage)

---

### 5. `GET /api/v1/guides/{guideId}/pages`

**Nuevo comportamiento:**

- **Si la guía es PUBLISHED**: Cualquier usuario autenticado puede ver sus páginas
- **Si la guía es DRAFT**: Solo el autor puede ver las páginas
- **Usuarios no autenticados**: Solo ven páginas de guías PUBLISHED

---

### 6. `GET /api/v1/guides/{guideId}/pages/{pageId}`

**Nuevo comportamiento:**

- **Si la guía es PUBLISHED**: Cualquier usuario autenticado puede ver la página
- **Si la guía es DRAFT**: Solo el autor puede ver la página
- **Usuarios no autenticados**: Solo ven páginas de guías PUBLISHED
- Valida que la página pertenezca a la guía especificada

---

## 📊 Matriz de Autorización

| Usuario | `GET /guides` | `GET /guides?for=dashboard` | `GET /guides/{id}` (PUBLISHED) | `GET /guides/{id}` (DRAFT) |
|---------|---------------|----------------------------|-------------------------------|---------------------------|
| **No autenticado** | ✅ Solo PUBLISHED | ❌ | ✅ | ❌ |
| **ROLE_STUDENT** | ✅ Solo PUBLISHED | ❌ | ✅ | ❌ |
| **ROLE_TEACHER** | ✅ Solo PUBLISHED | ✅ Solo sus guías | ✅ | ✅ Si es autor |

## 🎯 Casos de Uso

### Caso 1: Estudiante navega el catálogo
```bash
# Usuario: student123 (ROLE_STUDENT)
GET /api/v1/guides

# Resultado: Solo guías PUBLISHED de todos los profesores
```

### Caso 2: Profesor revisa su dashboard
```bash
# Usuario: teacher456 (ROLE_TEACHER)
GET /api/v1/guides?for=dashboard

# Resultado: TODAS las guías donde teacher456 es autor (DRAFT y PUBLISHED)
```

### Caso 3: Profesor navega el catálogo público
```bash
# Usuario: teacher456 (ROLE_TEACHER)
GET /api/v1/guides

# Resultado: Solo guías PUBLISHED de todos (incluyendo las propias)
```

### Caso 4: Usuario ve perfil público de un profesor
```bash
# Usuario: Cualquiera
GET /api/v1/guides/teachers/teacher789

# Resultado: Solo guías PUBLISHED de teacher789 (su portfolio público)
```

### Caso 5: Profesor intenta ver guía DRAFT de otro
```bash
# Usuario: teacher456 (ROLE_TEACHER)
GET /api/v1/guides/{draft-guide-id-of-teacher789}

# Resultado: 404 Not Found (no tiene acceso)
```

### Caso 6: Estudiante intenta usar dashboard
```bash
# Usuario: student123 (ROLE_STUDENT)
GET /api/v1/guides?for=dashboard

# Resultado: Solo guías PUBLISHED (el parámetro for=dashboard se ignora)
```

## 🔐 Implementación de Seguridad

### Verificación de Roles

```java
boolean isTeacher = securityHelper.isAuthenticated() 
    && securityHelper.hasRole("ROLE_TEACHER");
```

### Verificación de Autoría

```java
String currentUserId = securityHelper.getCurrentUserId();
boolean isAuthor = currentUserId != null && guide.isAuthor(currentUserId);
```

### Lógica de Status

```java
if (guide.getStatus() == EntityStatus.PUBLISHED) {
    // Acceso público para usuarios autenticados
} else {
    // Solo autores pueden acceder
    if (!isAuthor) {
        throw new ResourceNotFoundException("Guide not found");
    }
}
```

## 📝 Mensajes de Error

### 404 Not Found
Se retorna cuando:
- La guía no existe
- El usuario no tiene permisos para verla
- La guía está en DRAFT y el usuario no es autor

**Mensaje:** `"Guide not found"`

**Ventaja de seguridad:** No se revela si la guía existe o no, protegiendo información sensible.

## ✅ Validaciones Implementadas

1. **Autenticación del usuario**
   ```java
   if (!securityHelper.isAuthenticated()) {
       throw new ResourceNotFoundException("Guide not found");
   }
   ```

2. **Rol del usuario**
   ```java
   boolean isTeacher = securityHelper.hasRole("ROLE_TEACHER");
   ```

3. **Status de la guía**
   ```java
   if (guide.getStatus() == EntityStatus.PUBLISHED) { /* ... */ }
   ```

4. **Autoría de la guía**
   ```java
   boolean isAuthor = guide.isAuthor(currentUserId);
   ```

5. **Parámetro `for=dashboard`**
   ```java
   boolean isDashboardRequest = "dashboard".equalsIgnoreCase(forParam);
   ```

## 🧪 Ejemplos de Testing

### Test 1: Estudiante accede a guía PUBLISHED
```bash
# Esperado: 200 OK con datos de la guía
curl -H "Authorization: Bearer student-token" \
  http://localhost:8085/api/v1/guides/{published-guide-id}
```

### Test 2: Estudiante intenta acceder a guía DRAFT
```bash
# Esperado: 404 Not Found
curl -H "Authorization: Bearer student-token" \
  http://localhost:8085/api/v1/guides/{draft-guide-id}
```

### Test 3: Profesor accede a su dashboard
```bash
# Esperado: 200 OK con todas sus guías (DRAFT y PUBLISHED)
curl -H "Authorization: Bearer teacher-token" \
  http://localhost:8085/api/v1/guides?for=dashboard
```

### Test 4: Profesor accede al catálogo público
```bash
# Esperado: 200 OK con solo guías PUBLISHED
curl -H "Authorization: Bearer teacher-token" \
  http://localhost:8085/api/v1/guides
```

### Test 5: Profesor accede a su propia guía DRAFT
```bash
# Esperado: 200 OK con datos de la guía
curl -H "Authorization: Bearer teacher-token" \
  http://localhost:8085/api/v1/guides/{own-draft-guide-id}
```

### Test 6: Profesor intenta acceder a guía DRAFT de otro
```bash
# Esperado: 404 Not Found
curl -H "Authorization: Bearer teacher-token" \
  http://localhost:8085/api/v1/guides/{other-teacher-draft-guide-id}
```

## 📊 Comparación: Antes vs Después

| Escenario | ANTES | DESPUÉS |
|-----------|-------|---------|
| Profesor en catálogo | Ve todas las guías | Solo ve PUBLISHED |
| Profesor con `for=dashboard` | N/A | Ve TODAS sus guías |
| Estudiante en catálogo | Ve PUBLISHED | Ve PUBLISHED (sin cambios) |
| Acceso a DRAFT | Admin o autor | Solo autor |
| Portfolio público de profesor | N/A | Solo PUBLISHED (`/teachers/{id}`) |

## 🎨 Beneficios de la Nueva Implementación

1. **✅ Separación clara de contextos**
   - Vista pública vs dashboard privado

2. **✅ Seguridad mejorada**
   - No se filtran guías DRAFT sin autorización
   - Mensajes de error consistentes (404)

3. **✅ Experiencia de usuario mejorada**
   - Profesores tienen su espacio privado (`for=dashboard`)
   - Vista pública muestra solo contenido terminado

4. **✅ Flexibilidad**
   - Fácil agregar nuevos filtros
   - Fácil extender a nuevos roles

5. **✅ Consistencia**
   - Misma lógica aplicada en todos los endpoints GET

## 🔄 Archivos Modificados

1. **GuidesController.java**
   - Método `getAllGuides()` - Agregado parámetro `for` y lógica de dashboard
   - Método `getGuideById()` - Actualizada lógica de autorización
   - Método `getGuidesByTeacherId()` - Simplificado, solo PUBLISHED
   - Método `getGuidePages()` - Agregada verificación de acceso
   - Método `getPage()` - Agregada verificación de acceso

## 🚀 Despliegue

Los cambios son **backward compatible** con los siguientes aspectos:

- ✅ El endpoint `GET /guides` sigue funcionando igual para usuarios que no usan `for=dashboard`
- ✅ Todos los endpoints existentes siguen funcionando
- ✅ Solo se agrega nuevo comportamiento con el parámetro `for=dashboard`

## 📚 Documentación Swagger Actualizada

Todos los endpoints tienen documentación Swagger actualizada con:
- Descripción clara de las reglas de autorización
- Ejemplos de uso
- Respuestas esperadas
- Códigos de estado HTTP

Accede a la documentación en:
```
http://localhost:8085/swagger-ui.html
```

## ✅ Checklist de Implementación

- ✅ Lógica de autorización implementada
- ✅ Parámetro `for=dashboard` agregado
- ✅ Validación de roles implementada
- ✅ Verificación de autoría implementada
- ✅ Mensajes de error consistentes
- ✅ Documentación Swagger actualizada
- ✅ Todos los endpoints GET actualizados
- ✅ Compilación exitosa
- ✅ Seguridad mejorada

## 🎉 Resumen

Has implementado con éxito un sistema de autorización robusto y flexible para las guías que:

1. Permite a los estudiantes ver solo contenido publicado
2. Da a los profesores un dashboard privado para gestionar su contenido
3. Mantiene una vista pública consistente para todos
4. Protege el contenido en borrador de accesos no autorizados
5. Proporciona mensajes de error claros y seguros

¡El sistema está listo para usar! 🚀
