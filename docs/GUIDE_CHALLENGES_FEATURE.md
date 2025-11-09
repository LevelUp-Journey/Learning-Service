# Guide Challenges Feature

## Descripción

Los **Guides** ahora pueden tener referencias a **Challenges** para permitir que los estudiantes practiquen y refuercen su aprendizaje. Cuando un desafío es agregado a una guía, se publica un evento a Kafka para notificar a otros servicios.

## Cambios Implementados

### 1. Modelo de Dominio

#### Guide Entity
- **Nuevo campo**: `relatedChallenges` - Set<UUID> de IDs de challenges
- **Nuevos métodos**:
  - `addChallenge(UUID challengeId)` - Agrega un challenge a la guía
  - `removeChallenge(UUID challengeId)` - Elimina un challenge de la guía
  - `hasChallenge(UUID challengeId)` - Verifica si un challenge está asociado

#### Tabla de Base de Datos
- **Nueva tabla**: `guide_challenges`
  - `guide_id` (UUID) - Foreign key a guides
  - `challenge_id` (UUID) - ID del challenge

### 2. Eventos

#### GuideChallengeAddedEvent
Evento publicado cuando se agrega un challenge a una guía:
```json
{
  "guideId": "uuid",
  "challengeId": "uuid",
  "occurredAt": "2025-11-09T10:30:00Z"
}
```

**Tópico Kafka**: `guides.challenge.added.v1`

### 3. Comandos

#### AddChallengeToGuideCommand
```java
record AddChallengeToGuideCommand(UUID guideId, UUID challengeId)
```

#### RemoveChallengeFromGuideCommand
```java
record RemoveChallengeFromGuideCommand(UUID guideId, UUID challengeId)
```

### 4. Endpoints REST

#### Agregar Challenge a Guide
```http
POST /api/v1/guides/{guideId}/challenges?challengeId={challengeId}
Authorization: Bearer {jwt-token}
```

**Respuesta exitosa** (201 Created):
```json
{
  "id": "guide-uuid",
  "title": "Introduction to Java",
  "relatedChallenges": ["challenge-uuid-1", "challenge-uuid-2"],
  ...
}
```

**Errores**:
- `400 Bad Request` - Challenge ya está agregado
- `401 Unauthorized` - No hay token válido
- `403 Forbidden` - Usuario no es autor ni admin
- `404 Not Found` - Guía no encontrada

#### Eliminar Challenge de Guide
```http
DELETE /api/v1/guides/{guideId}/challenges/{challengeId}
Authorization: Bearer {jwt-token}
```

**Respuesta exitosa** (204 No Content)

**Errores**:
- `401 Unauthorized` - No hay token válido
- `403 Forbidden` - Usuario no es autor ni admin
- `404 Not Found` - Guía o challenge no encontrado

### 5. Autorizaciones

Solo pueden agregar/eliminar challenges:
- **Autores** de la guía
- **Administradores** (ROLE_ADMIN)

### 6. Infraestructura

#### KafkaEventPublisher
Servicio para publicar eventos a Kafka:
```java
@Service
public class KafkaEventPublisher {
    void publishEvent(String topic, String key, Object event);
    void publishEvent(String topic, Object event);
    String getGuideChallengeAddedTopic();
}
```

#### Configuración Kafka
```yaml
kafka:
  topics:
    guide-challenge-added: guides.challenge.added.v1
```

## Flujo de Trabajo

### Agregar Challenge

1. Cliente envía `POST /api/v1/guides/{guideId}/challenges?challengeId={challengeId}`
2. Sistema valida autenticación y autorización
3. Verifica que el challenge no esté ya agregado
4. Agrega el challenge a la guía
5. Persiste en base de datos
6. **Publica evento a Kafka** `guides.challenge.added.v1`
7. Retorna la guía actualizada (201 Created)

### Eliminar Challenge

1. Cliente envía `DELETE /api/v1/guides/{guideId}/challenges/{challengeId}`
2. Sistema valida autenticación y autorización
3. Verifica que el challenge exista en la guía
4. Elimina el challenge de la guía
5. Persiste en base de datos
6. Retorna 204 No Content

## Ejemplos de Uso

### Crear una guía (sin cambios)
```http
POST /api/v1/guides
Content-Type: application/json
Authorization: Bearer {token}

{
  "title": "Introduction to Spring Boot",
  "description": "Learn the basics of Spring Boot",
  "topicIds": ["topic-uuid"],
  "coverImage": "https://example.com/image.jpg"
}
```

### Agregar challenges a la guía
```http
POST /api/v1/guides/guide-uuid/challenges?challengeId=challenge-1-uuid
Authorization: Bearer {token}

POST /api/v1/guides/guide-uuid/challenges?challengeId=challenge-2-uuid
Authorization: Bearer {token}
```

### Consultar guía con challenges
```http
GET /api/v1/guides/guide-uuid
```

**Respuesta**:
```json
{
  "id": "guide-uuid",
  "title": "Introduction to Spring Boot",
  "description": "Learn the basics of Spring Boot",
  "relatedChallenges": [
    "challenge-1-uuid",
    "challenge-2-uuid"
  ],
  "authorIds": ["teacher-id"],
  "topics": [...],
  ...
}
```

### Eliminar un challenge
```http
DELETE /api/v1/guides/guide-uuid/challenges/challenge-1-uuid
Authorization: Bearer {token}
```

## Integración con Challenge Service

El **Challenge Service** puede suscribirse al tópico `guides.challenge.added.v1` para:
- Actualizar estadísticas de challenges
- Enviar notificaciones a estudiantes
- Crear referencias bidireccionales (si es necesario)
- Registrar métricas de uso

## Validaciones

- ✅ Challenge ID no puede ser null
- ✅ No se pueden agregar challenges duplicados
- ✅ Solo autores y admins pueden modificar
- ✅ Guía debe existir
- ✅ Evento Kafka se publica automáticamente

## Consideraciones Técnicas

- **Persistencia**: ElementCollection con tabla separada `guide_challenges`
- **Fetch**: EAGER para optimizar queries
- **Eventos**: Asíncronos vía Kafka (no bloquean la operación)
- **Transacciones**: Operaciones de BD dentro de @Transactional
- **Idempotencia**: Agregar challenge duplicado retorna error (400)

## Testing

Para probar la funcionalidad:

1. Crear una guía
2. Agregar uno o más challenges
3. Verificar que el evento se publique en Kafka
4. Consultar la guía y verificar `relatedChallenges`
5. Eliminar un challenge
6. Verificar que se elimine correctamente
