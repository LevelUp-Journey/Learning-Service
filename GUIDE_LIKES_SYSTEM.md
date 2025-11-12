# Sistema de Likes para Guides

## Resumen

Se ha implementado un sistema completo de likes para los guides que permite a los usuarios autenticados dar "me gusta" a las guías y ver qué guías les han gustado.

## Características Implementadas

### 1. Base de Datos
- **Tabla `guide_likes`**: Almacena los likes de usuarios a guides
  - Campos: `id`, `guide_id`, `user_id`, `created_at`, `updated_at`
  - Constraint único: Un usuario solo puede dar like una vez a cada guide
  - Relación Many-to-One con Guide

### 2. Modelo de Dominio
- **Entidad `GuideLike`**: Representa un like de un usuario a un guide
- **Comandos**:
  - `LikeGuideCommand`: Para dar like
  - `UnlikeGuideCommand`: Para quitar like

### 3. Repositorio
- **`GuideLikeRepository`**: Gestiona los likes
  - `existsByGuideIdAndUserId()`: Verifica si un usuario ya dio like
  - `findByGuideIdAndUserId()`: Obtiene el like específico
  - `deleteByGuideIdAndUserId()`: Elimina el like
  - `countByGuideId()`: Cuenta likes de un guide
  - `findGuideIdsLikedByUser()`: Obtiene IDs de guides que el usuario ha likeado

### 4. Servicios
- **`GuideCommandService`**: Métodos para like/unlike
- **`GuideLikeQueryService`**: Consultas optimizadas para verificar likes
  - `hasUserLikedGuide()`: Verifica si un usuario específico dio like a un guide
  - `getGuidesLikedByUser()`: Obtiene todos los guides likeados por un usuario (batch)

### 5. API Endpoints

#### Like a Guide
```http
POST /api/v1/guides/{guideId}/like
Authorization: Bearer <token>
```
**Response:**
- `204 No Content`: Like agregado exitosamente
- `400 Bad Request`: El usuario ya dio like a este guide
- `401 Unauthorized`: No autenticado
- `404 Not Found`: Guide no encontrado

#### Unlike a Guide
```http
DELETE /api/v1/guides/{guideId}/like
Authorization: Bearer <token>
```
**Response:**
- `204 No Content`: Like removido exitosamente
- `400 Bad Request`: El usuario no había dado like a este guide
- `401 Unauthorized`: No autenticado
- `404 Not Found`: Guide no encontrado

### 6. Response de Guides Actualizado

Todos los endpoints que devuelven guides ahora incluyen:

```json
{
  "id": "uuid",
  "title": "Guide Title",
  "description": "Guide Description",
  "likesCount": 42,
  "likedByRequester": true,
  "pagesCount": 10,
  "authorIds": ["author1", "author2"],
  "topics": [...],
  "pages": [...],
  "createdAt": "2025-11-11T10:00:00",
  "updatedAt": "2025-11-11T12:00:00"
}
```

**Nuevos campos:**
- `likesCount` (Integer): Número total de likes que tiene el guide
- `likedByRequester` (Boolean): `true` si el usuario actual ha dado like, `false` en caso contrario

## Endpoints Actualizados

Todos estos endpoints ahora calculan automáticamente si el usuario actual ha dado like:

1. **GET `/api/v1/guides`** - Lista de guides con likes
2. **GET `/api/v1/guides/{id}`** - Guide individual con likes
3. **GET `/api/v1/guides/teachers/{teacherId}`** - Guides de un teacher con likes

## Optimizaciones Implementadas

### Batch Loading de Likes
Para evitar el problema N+1, cuando se obtiene una lista de guides:

1. Se recopilan todos los IDs de guides
2. Se hace **UNA SOLA query** para obtener todos los guides que el usuario ha likeado
3. Se verifica en memoria si cada guide está en ese Set

```java
// Código optimizado
var guideIds = guides.stream()
    .map(Guide::getId)
    .collect(Collectors.toSet());
    
var likedGuideIds = guideLikeQueryService.getGuidesLikedByUser(guideIds, currentUserId);

// Mapeo eficiente
var resources = guides.map(guide ->
    GuideResourceAssembler.toResourceFromEntity(
        guide, 
        likedGuideIds.contains(guide.getId()),  // O(1) lookup
        false
    )
);
```

### Usuarios No Autenticados
- Si el usuario no está autenticado, `likedByRequester` siempre es `false`
- No se hacen queries innecesarias a la base de datos

## Flujo de Uso

### 1. Usuario da Like
```
POST /api/v1/guides/123e4567-e89b-12d3-a456-426614174000/like
Authorization: Bearer eyJhbGc...

→ Se crea un registro en guide_likes
→ Se incrementa likesCount en el guide
→ Response 204 No Content
```

### 2. Usuario obtiene Guide
```
GET /api/v1/guides/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer eyJhbGc...

Response:
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Introduction to Spring Boot",
  "likesCount": 15,
  "likedByRequester": true,  ← Usuario ha dado like
  ...
}
```

### 3. Usuario quita Like
```
DELETE /api/v1/guides/123e4567-e89b-12d3-a456-426614174000/like
Authorization: Bearer eyJhbGc...

→ Se elimina el registro de guide_likes
→ Se decrementa likesCount en el guide
→ Response 204 No Content
```

## Validaciones

1. **Like duplicado**: No se permite dar like dos veces al mismo guide
2. **Unlike sin like previo**: No se permite quitar un like que no existe
3. **Autenticación requerida**: Solo usuarios autenticados pueden dar/quitar likes
4. **Guide existente**: Se valida que el guide exista antes de permitir like/unlike
5. **Contador nunca negativo**: `likesCount` nunca puede ser menor que 0

## Migración de Datos

Si ya existen guides con `likesCount > 0` pero sin registros en `guide_likes`:
- Los contadores permanecerán como están
- Los nuevos likes se agregarán correctamente
- No hay inconsistencia porque el sistema calcula `likedByRequester` desde `guide_likes`

## Testing

### Casos de Prueba Recomendados

1. ✅ Usuario puede dar like a un guide
2. ✅ Usuario no puede dar like dos veces al mismo guide
3. ✅ Usuario puede quitar like de un guide
4. ✅ Usuario no puede quitar like que no dio
5. ✅ `likesCount` se incrementa correctamente
6. ✅ `likesCount` se decrementa correctamente
7. ✅ `likesCount` no baja de 0
8. ✅ `likedByRequester` es `true` cuando el usuario dio like
9. ✅ `likedByRequester` es `false` cuando el usuario no dio like
10. ✅ `likedByRequester` es `false` para usuarios no autenticados
11. ✅ Batch loading funciona correctamente con múltiples guides

## Ejemplos de Uso con cURL

### Dar Like
```bash
curl -X POST http://localhost:8081/api/v1/guides/123e4567-e89b-12d3-a456-426614174000/like \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Quitar Like
```bash
curl -X DELETE http://localhost:8081/api/v1/guides/123e4567-e89b-12d3-a456-426614174000/like \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Obtener Guide con Like Status
```bash
curl http://localhost:8081/api/v1/guides/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Estructura de Archivos Creados/Modificados

### Nuevos Archivos
```
src/main/java/com/levelupjourney/learningservice/guides/
├── domain/
│   └── model/
│       ├── commands/
│       │   ├── LikeGuideCommand.java
│       │   └── UnlikeGuideCommand.java
│       └── entities/
│           └── GuideLike.java
├── infrastructure/
│   └── persistence/
│       └── jpa/
│           └── repositories/
│               └── GuideLikeRepository.java
└── application/
    └── internal/
        └── queryservices/
            └── GuideLikeQueryService.java
```

### Archivos Modificados
```
src/main/java/com/levelupjourney/learningservice/guides/
├── domain/
│   └── services/
│       └── GuideCommandService.java (agregados métodos like/unlike)
├── application/
│   └── internal/
│       └── commandservices/
│           └── GuideCommandServiceImpl.java (implementación like/unlike)
└── interfaces/
    └── rest/
        ├── GuidesController.java (endpoints y lógica de likes)
        └── resources/
            └── GuideResource.java (ya tenía los campos)
```

## Próximas Mejoras Posibles

1. **Eventos de Kafka**: Publicar eventos cuando un guide recibe/pierde likes
2. **Caché**: Cachear el contador de likes para reducir carga en DB
3. **Top Guides**: Endpoint para obtener guides más likeados
4. **Notificaciones**: Notificar a autores cuando su guide recibe likes
5. **Analytics**: Trackear tendencias de likes en el tiempo
