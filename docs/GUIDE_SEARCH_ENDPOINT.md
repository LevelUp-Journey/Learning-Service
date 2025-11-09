# Guide Search Endpoint Feature

## 📋 Descripción General

Se ha implementado un endpoint avanzado de búsqueda de guías que permite filtrar resultados utilizando múltiples criterios opcionales. Este endpoint está diseñado para ser extensible, eficiente y fácil de usar.

## 🔗 Endpoint

```
GET /api/v1/guides/search
```

### ✅ Características

- ✅ **Búsqueda por título**: Coincidencia parcial (case-insensitive)
- ✅ **Filtro por autores**: Uno o más IDs de autores
- ✅ **Filtro por likes**: Número mínimo de likes
- ✅ **Filtro por topics**: Uno o más IDs de topics
- ✅ **Todos los filtros son opcionales**: Usa los que necesites
- ✅ **Paginación completa**: Soporta page, size y sort
- ✅ **Solo guías publicadas**: Por seguridad, solo retorna guías con status PUBLISHED
- ✅ **Respuesta optimizada**: Solo retorna campos básicos (id, title, description, coverImage)

## 📝 Parámetros de Query

| Parámetro | Tipo | Requerido | Descripción | Ejemplo |
|-----------|------|-----------|-------------|---------|
| `title` | String | No | Búsqueda parcial en el título (case-insensitive) | `title=Java` |
| `authorIds` | Set<String> | No | IDs de autores (separados por comas) | `authorIds=author1,author2` |
| `likesCount` | Integer | No | Número mínimo de likes | `likesCount=10` |
| `topicIds` | Set<UUID> | No | IDs de topics (separados por comas) | `topicIds=uuid1,uuid2` |
| `page` | Integer | No | Número de página (inicia en 0) | `page=0` |
| `size` | Integer | No | Tamaño de página | `size=20` |
| `sort` | String | No | Campo y dirección de ordenamiento | `sort=likesCount,desc` |

### 📌 Notas Importantes

1. **Al menos un filtro es requerido**: Debes proporcionar al menos uno de los parámetros de búsqueda (title, authorIds, likesCount, o topicIds)
2. **Paginación por defecto**: Si no se especifica, usa los valores por defecto de Spring (page=0, size=20)
3. **Ordenamiento**: Puedes ordenar por cualquier campo (ej: `createdAt,desc`, `title,asc`, `likesCount,desc`)

## 🚀 Ejemplos de Uso

### 1. Búsqueda Simple por Título

```bash
GET /api/v1/guides/search?title=Java
```

**Respuesta:**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Introduction to Java Programming",
      "description": "A comprehensive guide to learn Java from scratch",
      "coverImage": "https://example.com/images/java-guide.jpg"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Búsqueda por Autor

```bash
GET /api/v1/guides/search?authorIds=author123
```

### 3. Búsqueda por Múltiples Autores

```bash
GET /api/v1/guides/search?authorIds=author1,author2,author3
```

### 4. Búsqueda por Likes Mínimos

```bash
GET /api/v1/guides/search?likesCount=10
```

### 5. Búsqueda por Topics

```bash
GET /api/v1/guides/search?topicIds=550e8400-e29b-41d4-a716-446655440000
```

### 6. Búsqueda Combinada

```bash
GET /api/v1/guides/search?title=Spring&likesCount=5&topicIds=topic-uuid-1
```

### 7. Búsqueda con Paginación y Ordenamiento

```bash
GET /api/v1/guides/search?title=Java&page=0&size=10&sort=likesCount,desc
```

### 8. Búsqueda Avanzada

```bash
GET /api/v1/guides/search?title=programming&authorIds=author1,author2&likesCount=5&page=0&size=20&sort=createdAt,desc
```

## 📤 Estructura de Respuesta

### Respuesta Exitosa (200 OK)

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Guide Title",
      "description": "Guide description",
      "coverImage": "https://example.com/image.jpg"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 42,
  "totalPages": 3,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 20,
  "empty": false
}
```

### Error - Sin Criterios de Búsqueda (400 Bad Request)

```json
{
  "success": false,
  "message": "At least one search parameter must be provided (title, authorIds, likesCount, or topicIds)",
  "data": null,
  "statusCode": 400
}
```

### Error - Parámetros Inválidos (400 Bad Request)

```json
{
  "success": false,
  "message": "Invalid argument: likesCount must be a positive number",
  "data": null,
  "statusCode": 400
}
```

## 🔧 Implementación Técnica

### Componentes Creados

#### 1. **GuideSearchResource.java**
```java
public record GuideSearchResource(
    UUID id,
    String title,
    String description,
    String coverImage
)
```

#### 2. **SearchGuidesByFiltersQuery.java**
```java
public record SearchGuidesByFiltersQuery(
    String title,
    Set<String> authorIds,
    Integer minLikesCount,
    Set<UUID> topicIds,
    Pageable pageable
)
```

#### 3. **GuideRepository - Método de búsqueda**
```java
@Query("""
    SELECT DISTINCT g FROM Guide g
    LEFT JOIN g.topics t
    WHERE g.status = PUBLISHED
    AND (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:minLikesCount IS NULL OR g.likesCount >= :minLikesCount)
    AND (COALESCE(:authorIds, NULL) IS NULL OR EXISTS (
        SELECT 1 FROM g.authorIds a WHERE a IN :authorIds
    ))
    AND (COALESCE(:topicIds, NULL) IS NULL OR t.id IN :topicIds)
    """)
Page<Guide> searchGuidesByFilters(/*...*/);
```

#### 4. **InvalidSearchCriteriaException.java**
Excepción personalizada para errores de criterios de búsqueda inválidos.

### Manejo de Errores

El endpoint incluye manejo robusto de errores:

1. **Sin criterios de búsqueda**: 
   - Código: 400 Bad Request
   - Mensaje: "At least one search parameter must be provided..."

2. **Parámetros inválidos**:
   - Código: 400 Bad Request
   - Mensaje descriptivo del error específico

3. **Errores de servidor**:
   - Código: 500 Internal Server Error
   - Mensaje: "An unexpected error occurred"

### Validaciones

✅ Al menos un parámetro de búsqueda debe ser proporcionado
✅ Los UUIDs deben ser válidos
✅ El valor de likesCount debe ser positivo (si se proporciona)
✅ Solo se retornan guías con status PUBLISHED

## 🎯 Casos de Uso

### 1. **Búsqueda de usuarios finales**
Los estudiantes pueden buscar guías por título para encontrar contenido relevante.

```bash
GET /api/v1/guides/search?title=Python&sort=likesCount,desc
```

### 2. **Vista de perfil de autor**
Mostrar todas las guías de un autor específico.

```bash
GET /api/v1/guides/search?authorIds=author123&sort=createdAt,desc
```

### 3. **Guías populares**
Encontrar guías con alta popularidad.

```bash
GET /api/v1/guides/search?likesCount=100&sort=likesCount,desc
```

### 4. **Guías por categoría**
Filtrar guías por topics específicos.

```bash
GET /api/v1/guides/search?topicIds=backend-uuid,database-uuid
```

### 5. **Búsqueda avanzada**
Combinar múltiples filtros para resultados precisos.

```bash
GET /api/v1/guides/search?title=microservices&topicIds=backend-uuid&likesCount=10
```

## 🔐 Seguridad

- **Acceso público**: Este endpoint es público (no requiere autenticación)
- **Solo guías publicadas**: Solo retorna guías con status PUBLISHED
- **Sin información sensible**: La respuesta solo incluye campos públicos básicos

## 📊 Performance

- **Query optimizada**: Usa índices de base de datos para búsqueda eficiente
- **Paginación**: Limita el número de resultados por página
- **DISTINCT**: Evita duplicados cuando se filtran por topics
- **LEFT JOIN**: Eficiente manejo de relaciones

## 🧪 Pruebas

### Ejemplo con cURL

```bash
# Búsqueda simple
curl -X GET "http://localhost:8085/api/v1/guides/search?title=Java"

# Búsqueda con múltiples filtros
curl -X GET "http://localhost:8085/api/v1/guides/search?title=Spring&likesCount=5&page=0&size=10&sort=likesCount,desc"

# Búsqueda por autor
curl -X GET "http://localhost:8085/api/v1/guides/search?authorIds=author123,author456"
```

### Ejemplo con JavaScript (fetch)

```javascript
const searchGuides = async (filters) => {
  const params = new URLSearchParams();
  
  if (filters.title) params.append('title', filters.title);
  if (filters.authorIds) params.append('authorIds', filters.authorIds.join(','));
  if (filters.likesCount) params.append('likesCount', filters.likesCount);
  if (filters.topicIds) params.append('topicIds', filters.topicIds.join(','));
  if (filters.page !== undefined) params.append('page', filters.page);
  if (filters.size) params.append('size', filters.size);
  if (filters.sort) params.append('sort', filters.sort);
  
  const response = await fetch(`/api/v1/guides/search?${params}`);
  return await response.json();
};

// Uso
const guides = await searchGuides({
  title: 'Java',
  likesCount: 10,
  page: 0,
  size: 20,
  sort: 'likesCount,desc'
});
```

## 🚀 Extensibilidad

El endpoint está diseñado para ser fácilmente extensible. Para agregar nuevos filtros:

1. **Actualizar SearchGuidesByFiltersQuery**: Agregar el nuevo parámetro
2. **Actualizar GuideRepository**: Modificar la query JPQL
3. **Actualizar el Controller**: Agregar el nuevo @RequestParam
4. **Actualizar la documentación**: Swagger se actualiza automáticamente

### Ejemplo: Agregar filtro por status

```java
// 1. Query
public record SearchGuidesByFiltersQuery(
    // ... existing params
    EntityStatus status  // NEW
) {}

// 2. Repository
@Query("""
    SELECT DISTINCT g FROM Guide g
    WHERE (:status IS NULL OR g.status = :status)
    // ... rest of conditions
    """)

// 3. Controller
@GetMapping("/search")
public ResponseEntity<Page<GuideSearchResource>> searchGuidesByFilters(
    // ... existing params
    @RequestParam(required = false) EntityStatus status  // NEW
) {}
```

## ✅ Checklist de Implementación

- ✅ Endpoint creado en GuidesController
- ✅ Query JPQL optimizada en GuideRepository
- ✅ Servicio de query implementado
- ✅ Resource de respuesta optimizada (GuideSearchResource)
- ✅ Manejo de errores robusto
- ✅ Validación de criterios de búsqueda
- ✅ Documentación Swagger completa
- ✅ Soporte de paginación
- ✅ Todos los filtros son opcionales
- ✅ Compilación exitosa
- ✅ Documentación técnica creada

## 🎉 Resumen

Has creado un endpoint de búsqueda de guías completo, robusto y extensible que:

- Permite búsqueda flexible con múltiples filtros opcionales
- Incluye manejo de errores con mensajes claros
- Está completamente documentado con Swagger/OpenAPI
- Soporta paginación y ordenamiento
- Es eficiente y escalable
- Fácil de usar y extender

¡Listo para usar! 🚀
