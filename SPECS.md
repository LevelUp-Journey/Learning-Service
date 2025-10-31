# DDD Bounded Context Implementation Guide

This guide provides a comprehensive step-by-step approach for implementing a complete Domain-Driven Design (DDD) bounded context following the patterns established. Each step includes specific requirements, annotations, and best practices. **“Descriptive/Role-based Naming”** or  **“Explicit Transformation Naming”** should be a principle when naming files.
1. Completly avoid creating DTO classes.
2. Completly avoid mapper classes
3. Appropiate documented responses in Swagger
---
### 1. Value Objects Implementation. Location: `[context-name]/domain/model/valueobjects/`

- **Use Records:** All value objects should be implemented as Java records if necessary
- **Apply JPA Annotations**: Use `@Embeddable` for persistence
- **Add Lombok**: Use `@Getter` when needed (though records provide getters automatically)
- **Validation**: Include appropriate validation in record constructors
- **Immutable**: Value objects must be immutable by design

- [ ] Records used for value objects
- [ ] `@Embeddable` annotation applied
- [ ] Validation logic in constructor
- [ ] Appropriate JPA column mappings
- [ ] Null checks and business rule validation
- [ ] Enums for predefined values where applicable

IMPORTANT: FOR AGGREGATE ROOTS USE THE SHARED FOLDER, AS IT CONTAINS AUDITABLE MODELS FOR GENERATION.

---
### 2. Commands Implementation. Location: `[context-name]/domain/model/commands/`

Commands represent intentions to change the system state. They carry the data needed to perform an operation and are immutable data transfer objects.

- **Naming Convention**: All command files must end with `Command`
- **Use Records**: All commands must be implemented as Java records
- **Specific Naming**: Commands must be named as specifically as possible
- **Immutable**: Commands are immutable by design
- **Validation Ready**: Include all necessary data for operation

- [ ] All commands are records
- [ ] File names end with `Command`
- [ ] Specific and descriptive naming
- [ ] Input validation in constructor
- [ ] Use value objects for complex types
- [ ] Include all necessary operation data

---
### 3. Queries Implementation. Location: `[context-name]/domain/model/queries/`

Queries represent requests for information from the system. They define what data should be retrieved without specifying how.

- **Naming Convention**: All query files must end with `Query`
- **Use Records**: All queries must be implemented as Java records
- **Specific Naming**: Queries must be named as specifically as possible
- **Read-Only**: Queries should not modify system state
- **Validation**: Include appropriate validation for search criteria

- [ ] All queries are records
- [ ] File names end with `Query`
- [ ] Specific and descriptive naming
- [ ] Validation for search criteria
- [ ] Use Optional for optional parameters
- [ ] Include pagination parameters when needed

---
### 4. Events Implementation. Location: `[context-name]/domain/model/events/`

Domain events represent something important that happened in the domain. They enable loose coupling between bounded contexts and support event-driven architecture.

- **Naming Convention**: All event files must end with `Event`
- **Use Classes**: Events are implemented as classes (not records)
- **Past Tense**: Event names should be in past tense
- **Immutable**: Events should be immutable after creation
- **Essential Data**: Include only essential data, prefer IDs over full objects

- [ ] Events are classes, not records
- [ ] File names end with `Event`
- [ ] Past tense naming convention
- [ ] Include occurrence timestamp
- [ ] Essential data only (prefer IDs)
- [ ] Immutable after creation

---
### 5. Domain Services. Location: `[context-name]/domain/services/`

Domain services define contracts for command and query operations. They represent the business capabilities of the bounded context.

- **Interface Only**: All domain services are interfaces
- **Naming Convention**: Files end with either `CommandService` or `QueryService`
- **Separation**: Separate command and query services (CQRS pattern)
- **Handler Pattern**: Services handle commands and queries
- **Business Focus**: Focus on business operations, not technical concerns

- [ ] All services are interfaces
- [ ] Separate Command and Query services
- [ ] File names end with appropriate suffix
- [ ] Handle methods for each command/query
- [ ] Return appropriate types (Optional, List, etc.)
- [ ] Clear business-focused method names

---
### 6. Infrastructure Repositories. Location: `[context-name]/infrastructure/persistence/jpa/repositories/`

Repositories provide data access abstractions, implementing the repository pattern for aggregate persistence and retrieval.

- **Interface Only**: All repositories are interfaces
- **Naming Convention**: Files end with `Repository`
- **JPA Integration**: Extend appropriate JPA interfaces
- **Annotations**: Use `@Repository` annotation
- **Query Methods**: Use JPA query derivation and custom queries
- **Aggregate Focus**: One repository per aggregate root

- [ ] All repositories are interfaces
- [ ] Extend JpaRepository with correct types
- [ ] Use @Repository annotation
- [ ] File names end with `Repository`
- [ ] Include derived query methods
- [ ] Add custom @Query methods when needed
- [ ] Follow naming conventions for query methods

---
### 7. Command Service Implementations. Location: `[context-name]/application/internal/commandservices/`

Command service implementations contain the business logic for handling commands. They orchestrate domain operations and coordinate with repositories and external services.

- **Naming Convention**: Files end with `CommandServiceImpl`
- **Service Annotation**: Use `@Service` annotation
- **Interface Implementation**: Implement corresponding domain service interface
- **Transaction Management**: Handle transactions appropriately
- **Error Handling**: Include proper error handling and validation

- [ ] Implements domain service interface
- [ ] File name ends with `CommandServiceImpl`
- [ ] Uses @Service annotation
- [ ] Includes @Transactional where appropriate
- [ ] Proper business rule validation
- [ ] Error handling and meaningful exceptions
- [ ] Repository dependency injection

---
### 8. Query Service Implementations. Location: `[context-name]/application/internal/queryservices/`

Query service implementations handle read operations, retrieving and formatting data for presentation without modifying state.

- **Naming Convention**: Files end with `QueryServiceImpl`
- **Service Annotation**: Use `@Service` annotation
- **Interface Implementation**: Implement corresponding domain query service interface
- **Read-Only**: Operations should not modify state
- **Performance**: Optimize for read performance

- [ ] Implements domain query service interface
- [ ] File name ends with `QueryServiceImpl`
- [ ] Uses @Service annotation
- [ ] @Transactional(readOnly = true) for performance
- [ ] No state modifications
- [ ] Efficient data retrieval
- [ ] Proper pagination handling

---
### 9. REST Resources (DTOs). Location: `[context-name]/interfaces/rest/resources/`

Resources (DTOs) define the data contracts for REST API communication. They provide a stable API interface independent of domain model changes.

- **Naming Convention**: Files end with `Resource`
- **DTO Pattern**: Pure data transfer objects
- **API Focused**: Designed for external communication
- **Validation**: Include appropriate validation annotations
- **Documentation**: Include Swagger/OpenAPI annotations

- [ ] File names end with `Resource`
- [ ] Records used for immutability
- [ ] Validation annotations applied
- [ ] Swagger/OpenAPI documentation
- [ ] Appropriate field constraints
- [ ] Clear and descriptive field names

---
### 10. REST Controllers. Location: `[context-name]/interfaces/rest/transform/`

Controllers handle HTTP requests, coordinate with application services, and transform between resources and domain objects.

- **Naming Convention**: Files end with `Controller`
- **REST Annotations**: Use appropriate Spring REST annotations
- **Swagger Documentation**: Include comprehensive API documentation
- **Response Codes**: Implement appropriate HTTP response codes
- **Error Handling**: Include proper error handling
- **Transformation**: Convert between resources and commands/queries

- [ ] File names end with `Controller`
- [ ] @RestController annotation
- [ ] @RequestMapping for base path
- [ ] Swagger @Tag and @Operation annotations
- [ ] @ApiResponses for different scenarios
- [ ] Proper HTTP status codes
- [ ] Input validation with @Valid
- [ ] Resource-to-command/query transformation
- [ ] Entity-to-resource transformation

---
### 11. Implementation Timeline

	1. **Value Objects** - Start with IDs and basic value objects
	2. **Enums** - Define status and type enumerations
	3. **Commands** - Create all command records
	4. **Queries** - Create all query records
	5. **Domain Services** - Define service interfaces
	6. **Entities** - Implement domain entities with proper relationships
	7. **Aggregates** - Create aggregate roots with business logic
	8. **Events** - Implement domain events (if required)
	9. **Repositories** - JPA repository interfaces
	10. **Command Services** - Implement command handlers
	11. **Query Services** - Implement query handlers
	12. **Resources (DTOs)** - Create API data contracts
	13. **Controllers** - Implement REST endpoints
	14. **Transformers** - Create resource-entity transformation logic

### 12. ACL Implementation

**Step 1: Identify Communication Needs**

```
1. What data does Context A need from Context B?

2. What operations does Context A need to perform on Context B?
```

**Step 2: Map Domain Models**

```

Context A (Consumer) Model ↔ Context B (Provider) Model

```

**Step 3: Define ACL Interface Contract**

```

Questions to define:

1. What methods will the facade expose?

2. What parameters are needed?

3. What return types make sense?

4. How to handle errors?

Design principle: Keep it simple and focused on consumer needs

```

#### Implementation in Provider Context (Context B)


**Step 4: Create ACL Facade Interface**

```java
// File: [context-b]/interfaces/acl/[ContextB]ContextFacade.java

package com.acme.center.platform.[contextb].interfaces.acl;

public interface [ContextB]ContextFacade {

  Long create[Entity](String param1, String param2, ...);

  Long find[Entity]IdBy[Field](String identifier);

```


**Step 5: Implement ACL Facade**

```java
// File: [context-b]/application/acl/[ContextB]ContextFacadeImpl.java

import com.acme.center.platform.[contextb].domain.model.commands.Create[Entity]Command;

import com.acme.center.platform.[contextb].domain.model.queries.Get[Entity]By[Field]Query;

import com.acme.center.platform.[contextb].domain.services.[Entity]CommandService;

import com.acme.center.platform.[contextb].domain.services.[Entity]QueryService;

import com.acme.center.platform.[contextb].interfaces.acl.[ContextB]ContextFacade;

import org.springframework.stereotype.Service;

@Service

public class [ContextB]ContextFacadeImpl implements [ContextB]ContextFacade {

  private final [Entity]CommandService [entity]CommandService;

  private final [Entity]QueryService [entity]QueryService;


  public [ContextB]ContextFacadeImpl([Entity]CommandService [entity]CommandService,

                   [Entity]QueryService [entity]QueryService) {

    this.[entity]CommandService = [entity]CommandService;

    this.[entity]QueryService = [entity]QueryService;

  }


  @Override

  public Long create[Entity](String param1, String param2, ...) {

    var command = new Create[Entity]Command(param1, param2, ...);

    var entity = [entity]CommandService.handle(command);

    return entity.isEmpty() ? 0L : entity.get().getId();

  }

  
  @Override

  public Long find[Entity]IdBy[Field](String identifier) {

    var query = new Get[Entity]By[Field]Query(new [Field](identifier));

    var entity = [entity]QueryService.handle(query);

    return entity.isEmpty() ? 0L : entity.get().getId();
  }

}

```

##### Implementation in Consumer Context (Context A)

**Step 6: Create Value Objects in Consumer Context**

```java

// File: [context-a]/domain/model/valueobjects/[Entity]Id.java

package com.acme.center.platform.[contexta].domain.model.valueobjects;


import jakarta.persistence.Embeddable;

/**

* Value object representing [Entity] ID from external context

*/

@Embeddable

public record [Entity]Id(Long [entity]Id) {

  public [Entity]Id {

    if ([entity]Id == null || [entity]Id <= 0) {

      throw new IllegalArgumentException("[Entity] ID must be a positive number");

    }

  }

}

```


**Step 7: Create External Service (ACL Layer)**

```java

// File: [context-a]/application/internal/outboundservices/acl/External[Entity]Service.java

package com.acme.center.platform.[contexta].application.internal.outboundservices.acl;
  

import com.acme.center.platform.[contexta].domain.model.valueobjects.[Entity]Id;

import com.acme.center.platform.[contextb].interfaces.acl.[ContextB]ContextFacade;

import org.springframework.stereotype.Service;
  

import java.util.Optional;
  
/**

* External [Entity] Service

* ACL implementation for accessing [Entity] from external context

*/

@Service

public class External[Entity]Service {

  private final [ContextB]ContextFacade [contextb]ContextFacade;

  

  public External[Entity]Service([ContextB]ContextFacade [contextb]ContextFacade) {

    this.[contextb]ContextFacade = [contextb]ContextFacade;

  }


  /**

  * Fetch [Entity] by [field]

  * @param [field] The [field] value

  * @return An Optional of [Entity]Id

  */

  public Optional<[Entity]Id> fetch[Entity]By[Field](String [field]) {

    var [entity]Id = [contextb]ContextFacade.find[Entity]IdBy[Field]([field]);

    return [entity]Id == 0L ? Optional.empty() : Optional.of(new [Entity]Id([entity]Id));

  }

  /**

  * Create [Entity]

  * @param param1 Parameter 1

  * @param param2 Parameter 2

  * @return An Optional of [Entity]Id

  */

  public Optional<[Entity]Id> create[Entity](String param1, String param2, ...) {

    var [entity]Id = [contextb]ContextFacade.create[Entity](param1, param2, ...);

    return [entity]Id == 0L ? Optional.empty() : Optional.of(new [Entity]Id([entity]Id));

  }

}

```

##### Phase 4: Integration in Consumer Context


**Step 8: Update Command Services**

```java

// File: [context-a]/application/internal/commandservices/[ConsumerEntity]CommandServiceImpl.java


// Add dependency injection

private final External[Entity]Service external[Entity]Service;

public [ConsumerEntity]CommandServiceImpl([ConsumerEntity]Repository repository,

                    External[Entity]Service external[Entity]Service) {

  this.repository = repository;

  this.external[Entity]Service = external[Entity]Service;

}

// Update command handler

@Override

public [ConsumerEntity]Id handle(Create[ConsumerEntity]Command command) {

  // Step 1: Try to fetch existing entity from external context

  var [entity]Id = external[Entity]Service.fetch[Entity]By[Field](command.[field]());

  // Step 2: If not found, create in external context

  if ([entity]Id.isEmpty()) {

    [entity]Id = external[Entity]Service.create[Entity](

      command.param1(),

      command.param2(),

      ...

    );

  } else {

    // Step 3: Validate no duplicate exists in current context

    repository.findBy[Entity]Id([entity]Id.get()).ifPresent([entity] -> {

      throw new IllegalArgumentException("Duplicate entity found");

    });

  }

  

  // Step 4: Ensure external operation succeeded

  if ([entity]Id.isEmpty()) {

    throw new IllegalArgumentException("Unable to create/fetch external entity");

  }

  // Step 5: Create consumer entity with external reference

  var [consumerEntity] = new [ConsumerEntity]([entity]Id.get());

  repository.save([consumerEntity]);

  return [consumerEntity].getId();

}

```

**Common Pitfalls to Avoid:**

1. **Don't expose complex domain objects** - Use simple types in ACL interfaces
2. **Don't create circular dependencies** - Context A → Context B, not both ways
3. **Don't skip error handling** - Always handle external context failures
4. **Don't forget testing** - Test ACL integration thoroughly
5. **Don't tight-couple** - Always go through the ACL interface, never direct calls



Perfecto — te dejo una **especificación formal** (en castellano) que aplica buenas prácticas RESTful (recursos en plural), principios SOLID y DDD. Incluye: modelo de dominio, contratos (DTOs / JSON), endpoints REST, reglas de negocio, validaciones, consideraciones de concurrencia y diseño de base de datos, y comportamientos en tiempo real. Puedes copiar/pegar y adaptarlo a tu stack (Spring Boot, Node, etc.).

# Resumen rápido

* Recursos: `/guides`, `/courses`, `/topics`, `/pages`, `/enrollments`, `/comments`, `/likes`, `/users`, `/progress`
* Roles: `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`
* Autenticación: middleware que extrae `userId` (ej. desde JWT) y lo añade al request (p. ej. `req.user.id`).
* Visibilidad: solo `PUBLISHED` visible para consultas públicas; `DRAFT` visible sólo para autores y admins; `ASSOCIATED_WITH_COURSE` visible sólo para usuarios inscritos en el curso.
* Likes: tabla separada `likes` con constraint único (user + entity) para prevenir duplicados; contador en `guides`/`courses` actualizado en transacción y push en tiempo real.
* Comentarios: estructura completa + soft delete; restricción: sólo usuarios inscritos pueden comentar en cursos; cualquier usuario (autenticado) puede comentar en guías públicas si políticas lo permiten.
* Páginas: CRUD como recurso anidado `/guides/{guideId}/pages`.
* Búsqueda: `/guides/search` y `/courses/search` con filtros por título, topics, authors, paginación y orden.

---

# 1. Modelos de dominio (DDD — entidades agregadas)

## Guide (Aggregate Root)

Campos:

```json
{
  "guideId": "uuid",
  "title": "string",
  "description": "string",
  "topicIds": ["uuid"],
  "authorIds": ["uuid"],            // máximo N autores (configurable)
  "status": "DRAFT|PUBLISHED|ARCHIVED|DELETED|ASSOCIATED_WITH_COURSE",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "likesCount": 0,
  "pagesCount": 0,
  "coverImage": "url"
}
```

Notas:

* `coverImage` es solo una URL.
* `pages` son entidades distintas (agregadas) relacionadas por `guideId`. El orden de páginas se mantiene por campo `order` (integer).
* `likesCount` debe mantenerse de forma consistente (transactional / event-driven).

## Page (Entidad dentro del agregado Guide)

Campos:

```json
{
  "pageId": "uuid",
  "guideId": "uuid",
  "content": "markdown",
  "order": 1,               // número entero, único por guideId
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

Reglas:

* `order` determina secuencia; validación para evitar gaps o duplicados (o se permite y normaliza).

## Topic

Campos:

```json
{
  "topicId": "uuid",
  "name": "string"
}
```

## Like (Entidad de interacción)

Campos:

```json
{
  "likeId": "uuid",
  "userId": "uuid",
  "entityType": "GUIDE|COURSE",
  "entityId": "uuid",
  "createdAt": "datetime"
}
```

Reglas:

* Única combinación `(userId, entityType, entityId)` (constraint DB) → previene likes duplicados.
* Incremento/decremento de `likesCount` en la entidad objetivo dentro de la misma transacción o vía event + idempotencia.

## Course (Aggregate Root)

Campos:

```json
{
  "courseId": "uuid",
  "title": "string",
  "description": "string",
  "coverImage": "url",
  "topicIds": ["uuid"],
  "authorIds": ["uuid"],
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "status": "DRAFT|PUBLISHED|ARCHIVED|DELETED",
  "likesCount": 0
}
```

Notas:

* Multiplies guides *pertenecen conceptualmente al curso* pero la relación se representa por estado `ASSOCIATED_WITH_COURSE` en Guide y/o tabla de asociación `course_guides(courseId, guideId)` si se necesita (ver consideraciones).

## Enrollment

Campos:

```json
{
  "enrollmentId": "uuid",
  "userId": "uuid",
  "courseId": "uuid",
  "createdAt": "datetime",
  "status": "ACTIVE|CANCELLED"
}
```

Reglas:

* Unique (userId, courseId).
* Validación previa para no inscribir duplicadamente.

## Comment

Campos:

```json
{
  "commentId": "uuid",
  "content": "string",
  "authorId": "uuid",
  "parentCommentId": "uuid|null",
  "entityType": "GUIDE|COURSE",
  "entityId": "uuid",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "isDeleted": false
}
```

Reglas:

* Soporta replies por `parentCommentId`.
* Soft delete: `isDeleted=true` y blankear `content` o mantener texto según normativa.

## Progress (opcional para tracking)

Campos:

```json
{
  "progressId": "uuid",
  "userId": "uuid",
  "courseId": "uuid",
  "guideId": "uuid|null",
  "pageId": "uuid",
  "completedAt": "datetime"
}
```

Reglas:

* Cálculo de % = pages completadas / totalPages del curso.

---

# 2. Contratos (DTOs): request / response (ejemplos esenciales)

## Crear Guide (POST /guides)

Request:

```json
{
  "title": "string",
  "description": "string",
  "coverImage": "url",
  "topicIds": ["uuid"],
  "authorIds": ["uuid"] // opcional: si no viene, usar userId del JWT
}
```

Response `201 Created`:

```json
{ "guideId": "uuid", "location": "/guides/{guideId}" }
```

## Obtener Guide (GET /guides/{guideId})

Response `200` (si PUBLISHED o visible para el usuario):

```json
{
  "guideId": "uuid",
  "title": "string",
  "description": "string",
  "topics": [{ "topicId": "uuid", "name": "string" }],
  "authors": [{ "userId":"uuid", "name":"string" }],
  "status": "PUBLISHED",
  "createdAt":"datetime",
  "updatedAt":"datetime",
  "likes": { "count": 10, "likedByRequester": true },
  "pages": [
    { "pageId":"uuid", "order": 1, "content": "..." }
  ],
  "coverImage": "url"
}
```

## Crear Course (POST /courses)

Request body similar a guides (role check required). Response `201`.

## Enroll (POST /courses/{courseId}/enrollments)

Request body vacío (courseId en path). Response `201` o `409` si ya inscrito.

## Like Guide (POST /guides/{guideId}/likes)

Request: vacío. Response `200` con estado del like y contador:

```json
{ "likesCount": 11, "likedByRequester": true }
```

## Search (GET /guides/search)

Query params: `q` (title), `topicIds` (csv), `authorIds` (csv), `page`, `size`, `sort` (`createdAt,desc`), `status` (optional — server en general filtra por `PUBLISHED` si usuario no autorizado).
Response: standard paginated.

---

# 3. Endpoints REST (lista completa y reglas)

> **Autenticación**: todas las rutas que lo requieran deben usar middleware JWT que añade `req.user = { userId, roles[] }`. En Spring Boot: filtro que llena `SecurityContext`.

## Guides

* `GET /guides` — Listar guías públicas (PUBLISHED) con paginación/sorting/filter por topics/authors. (Open)
* `GET /guides/search` — Búsqueda avanzada (filtros opcionales). (Open)
* `GET /guides/{guideId}` — Obtener guía individual. Visible si:

    * `status = PUBLISHED`, o
    * requester es author de la guía, o
    * requester es admin, o
    * `ASSOCIATED_WITH_COURSE` y requester está inscrito en el curso correspondiente.
* `POST /guides` — Crear guía. Auth required. Roles: `ROLE_ADMIN|ROLE_TEACHER`. Si `authorIds` no provido, por defecto el `userId` del JWT.
* `PUT /guides/{guideId}` — Actualizar título, description, coverImage, status. Roles: autor(es) o admin.
* `PUT /guides/{guideId}/authors` — Body `{ "authors": ["uuid", ...] }`. Reglas: límite máximo de autores; sólo admin o autor actual puede modificar.
* `DELETE /guides/{guideId}` — Soft delete (cambiar status = DELETED o flag). Autores o admin.
* `POST /guides/{guideId}/likes` — Like toggle (o crear). Auth required. Prevención duplicados en DB. Retornar contador y estado.
* `GET /guides/{guideId}/comments` — List comments paginated (visible rules apply).
* `POST /guides/{guideId}/comments` — Create comment. Auth required (if guide is associated, check enrollment if needed).
* Pages (CRUD):

    * `POST /guides/{guideId}/pages` — Crear página (auth + authors/admin). Body: `{ content, order }`.
    * `GET /guides/{guideId}/pages/{pageId}` — Obtener page.
    * `PUT /guides/{guideId}/pages/{pageId}` — Actualizar page.
    * `DELETE /guides/{guideId}/pages/{pageId}` — Eliminar page.
    * `GET /guides/{guideId}/pages` — Listar pages ordenadas.

## Courses

* `GET /courses` — Listar cursos (solo PUBLISHED por defecto). Pag/paginación.
* `GET /courses/search` — Búsqueda avanzada.
* `GET /courses/{courseId}` — Obtener curso. Si requester inscrito, incluir `guides` (mapear solo: `guideId, title, coverImage, topics(full), likes` y `likedByRequester`), si no inscrito, `guides: []`. (Autenticación opcional; si no autenticado, no show guides).
* `GET /courses/{courseId}/guides/{guideId}` — Obtener guide dentro del contexto de curso. Validación: requester debe estar inscrito para ver si guide.status = ASSOCIATED_WITH_COURSE.
* `POST /courses` — Crear curso. Auth required. Roles: `ROLE_ADMIN|ROLE_TEACHER`.
* `PUT /courses/{courseId}` — Actualizar core fields (authors separado).
* `PUT /courses/{courseId}/authors` — Modificar autores (roles/ownership).
* `DELETE /courses/{courseId}` — Soft delete. Authors/admin.
* `POST /courses/{courseId}/likes` — Similar a guides.
* Enroll:

    * `POST /courses/{courseId}/enrollments` — Inscripción. Auth required. Validar no duplicado. Return `201`.
    * `DELETE /courses/{courseId}/enrollments` — Unenroll. Auth required. Return `204` or `404` si no inscrito.

## Topics

* `GET /topics` — List all topics.
* `GET /topics/{topicId}` — Get topic.
* `GET /topics/{topicId}/guides` — Guides filtered by topic.
* `GET /topics/{topicId}/courses` — Courses filtered by topic.
* `POST /topics` — Create topic (admin/teacher).
* `PUT /topics/{topicId}` — Update topic (admin).
* `DELETE /topics/{topicId}` — Delete topic (admin) — soft delete recommended.

## Comments

* `POST /guides/{guideId}/comments` — Create comment on guide.
* `POST /comments/{commentId}/replies` — Reply to a comment.
* `PUT /comments/{commentId}` — Edit comment (only author or admin).
* `DELETE /comments/{commentId}` — Soft delete (author or admin).
* `GET /guides/{guideId}/comments` — List comments with pagination.
* Same endpoints for courses: `/courses/{courseId}/comments` and `/comments/{commentId}/replies` reused.

## Progress / Certificates (opcional)

* `POST /courses/{courseId}/progress` — Mark page completed. Body: `{ pageId }`. Auth required + enrollment.
* `GET /courses/{courseId}/progress` — Get user progress (pages completed, percentage).
* `GET /courses/{courseId}/certificate` — If completed, download certificate.

---

# 4. Reglas de negocio y validaciones (detalladas)

1. **Roles & Ownership**

    * Solo `ROLE_ADMIN` o alguno de los `authorIds` puede editar o borrar guía/curso.
    * `ROLE_TEACHER` puede crear guías/cursos; `ROLE_STUDENT` no.
2. **Visibilidad**

    * `PUBLISHED` → público (o a inscritos si `ASSOCIATED_WITH_COURSE`).
    * `DRAFT` → solo visible a autores y admins.
    * `ASSOCIATED_WITH_COURSE` → no listado en `/guides` público; visible solamente a inscritos del course.
3. **Likes**

    * DB constraint UNIQUE `(userId, entityType, entityId)` para prevenir duplicados.
    * Operación `POST /.../likes` hace: si no existe, insertar like y `likesCount++`; si ya existe, return `409` o convertir endpoint en toggle (`POST` crea, `DELETE` remueve).
    * Mantener `likesCount` con transacción o con eventos eventual-consistent si se usa cola; para consistencia fuerte usar transacción.
    * Emisión de evento (p. ej. `LikeCreated/LikeDeleted`) para notificar via websocket/SSE a clientes (real-time).
4. **Enrolments**

    * Unique constraint `(userId, courseId)`.
    * `POST /enrollments` devuelve `201` o `409` si ya inscrito.
    * `DELETE` cambia estado a `CANCELLED` y no borra físicamente.
5. **Comentarios**

    * Para cursos: validar que requester está inscrito para permitir comentar.
    * Para guías públicas: cualquier usuario autenticado puede comentar (o anon allowed if policy).
    * No likes en comentarios (según requisito).
6. **Pages**

    * CRUD restringido a autores/admin.
    * Actualizar `pagesCount` y recalcular orden si es necesario.
7. **Límites**

    * Límite de autores por guía/curso (configurable, p. ej. max 5). Validar en endpoints `/authors`.
8. **Estado DRAFT**

    * Solo autores pueden listar/obtener sus DRAFTs.
9. **Validación adicional**

    * Validar que `topicIds` referencien topics existentes.
    * Validar `authorIds` existan y tengan role correcto (opcional).
10. **Race conditions**

    * Para likes y contador usar `SELECT ... FOR UPDATE` o `optimistic locking (version)` para evitar conteos incorrectos.
11. **Soft delete**

    * Usar soft delete (`status = DELETED` o `isDeleted=true`) y evitar exponer datos eliminados en listados.

---

# 5. Diseño de base de datos (esquema simplificado / índices)

Tablas principales:

* `guides` (guideId PK, title, description, status, coverImage, likesCount, pagesCount, createdAt, updatedAt, version)
* `pages` (pageId PK, guideId FK, content, order, createdAt, updatedAt)
* `topics` (topicId PK, name)
* `guide_topics` (guideId, topicId) — index por topicId
* `courses` (courseId PK, title, description, coverImage, likesCount, createdAt, updatedAt)
* `course_topics` (courseId, topicId)
* `course_guides` (if decides to map explicitly course -> guide)
* `users` (userId PK, name, roles, ...)
* `enrollments` (enrollmentId PK, userId FK, courseId FK, UNIQUE(userId, courseId))
* `likes` (likeId PK, userId FK, entityType, entityId, UNIQUE(userId, entityType, entityId), index entityType+entityId)
* `comments` (commentId PK, entityType, entityId, authorId, parentCommentId, content, isDeleted, createdAt)
* `progress` (progressId PK, userId, courseId, pageId, createdAt) — unique (userId, pageId)

Índices:

* `guides(title)`, `guides(status)`, `guide_topics(topicId)`, `likes(entityType, entityId)`, `enrollments(userId, courseId)`.

---

# 6. Contratos de error / códigos HTTP recomendados

* `200 OK` — lectura exitosa.
* `201 Created` — recurso creado.
* `204 No Content` — eliminación exitosa sin body.
* `400 Bad Request` — validación fallida.
* `401 Unauthorized` — auth requerida o token inválido.
* `403 Forbidden` — role/ownership no permitido.
* `404 Not Found` — recurso no existe o no visible al requester.
* `409 Conflict` — duplicado (ej. enroll doble, like duplicado).
* `422 Unprocessable Entity` — reglas de dominio no satisfechas.
* `500` — error servidor.

---

# 7. Consideraciones de seguridad y middleware

* Middleware de autenticación (JWT) que inyecta `userId` y `roles`.
* Middleware de autorización reutilizable: e.g. `requireRole('ROLE_TEACHER')`, `requireAuthorOrAdmin(resourceType, resourceId)`.
* Sanitización del markdown al renderizar (CSP + sanitización para XSS).
* Rate limiting (protege endpoints de comentarios/likes).
* Validación de tamaño máximo de contenido (pages, comments).

---

# 8. Consistencia y escalabilidad (likes / contador / tiempo real)

* Prevención duplicados con constraint DB + manejo de error `409`.
* Para alto tráfico:

    * Opción A (fuerte consistencia): transacción que inserta en `likes` y actualiza contador `guides.likesCount` con `SELECT FOR UPDATE`.
    * Opción B (eventual): insertar en `likes` y emitir evento a cola; worker procesa agregación periódica. Asegurar idempotencia.
* Tiempo real: websocket/SSE para emitir `like.updated` events con `{ entityType, entityId, likesCount }`.
* Para mostrar `likedByRequester` en GET: hacer join con `likes` para requester (left join boolean).

---

# 9. Flujo de ejemplos y reglas puntuales (casos de uso)

### Crear guía

1. `POST /guides` auth required role TEACHER|ADMIN.
2. Backend valida `topicIds` y `authorIds`.
3. Si `authorIds` vacío, se pone `userId` del JWT.
4. Return 201 con Location header.

### Inscribirse en curso

1. `POST /courses/{courseId}/enrollments` (auth).
2. Check si `enrollments` existe → 409 si ya inscrito.
3. Insert y return 201.

### Comentar en curso

1. `POST /courses/{courseId}/comments` (auth).
2. Validar enrollment (si no inscrito → 403).
3. Insert comment.

### Obtener curso (con guías si inscrito)

1. `GET /courses/{courseId}`
2. Check enrollment:

    * Si inscrito: incluir `guides` asociados (mapear fields reducidos y `likedByRequester`).
    * Si no: `guides: []`.

### Buscar guías

`GET /guides/search?q=react&topicIds=1,2&authorIds=3&page=1&size=20&sort=createdAt,desc`

* El servidor aplica filtros; si requester no es admin/author, sólo devuelve PUBLISHED (y posiblemente ASSOCIATED_WITH_COURSE si está inscrito).

---

# 10. Endpoints faltantes que agrego (según tu sección "Faltante")

* `GET /guides/{guideId}` — ya incluido. (Devolver `404` si no visible).
* `GET /courses` — listado de todos los cursos disponibles (PUBLISHED).
* `GET /topics/{topicId}/guides` — guías por topic.
* `GET /topics/{topicId}/courses` — cursos por topic.

---

# 11. Recomendaciones técnicas (implementación)

* **DDD**: implementa `GuideAggregate`, `CourseAggregate`, `EnrollmentService`, `LikeService`, `CommentService`, `PagesService`. Mantén lógica de dominio en servicios/aggregates, validaciones en application/domain layer.
* **SOLID**: seccionar responsabilidades:

    * Repositorios para acceso a datos (IGuideRepository).
    * Services para reglas y transacciones (GuideService).
    * Controllers para mapping HTTP → DTOs → services.
* **DTOs & Mappers**: no exponer entidades DB directamente en responses.
* **Events**: emitir eventos `LikeCreated`, `EnrollmentCreated`, `CommentCreated` para notificaciones y analítica.
* **Tests**: unitarios en domain rules (p. ej. no enroll duplicates, only authors edit), e2e para endpoints y autorización.
* **Pagos/monetización**: si el curso puede ser pagado, añade `purchase`/`payments` fuera de este diseño.

---

# 12. Ejemplos JSON rápidos

### Response de `GET /courses/{courseId}` (inscrito)

```json
{
  "courseId":"c1",
  "title":"Curso X",
  "description":"...",
  "coverImage":"https://...",
  "topics":[{"topicId":"t1","name":"JS"}],
  "authors":[{"userId":"u1","name":"Ana"}],
  "status":"PUBLISHED",
  "likes": { "count": 20, "likedByRequester": false },
  "guides":[
    {
      "guideId":"g1",
      "title":"Guía A",
      "coverImage":"https://...",
      "topics":[{"topicId":"t1","name":"JS"}],
      "likes": { "count": 5, "likedByRequester": true }
    }
  ]
}
```

---

# 13. Checklist de implementación (prioridades)

1. Middleware auth (inyectar userId/roles).
2. Modelos DB y constraints (unique on likes, enrollments).
3. Endpoints CRUD básicos para guides/courses/pages/topics.
4. Search endpoints con paginación.
5. Likes endpoints con prevención duplicados.
6. Enrollment endpoints y validación.
7. Comments con reglas (enrolled-only para cursos).
8. Ownership checks (authors/admin for edit/delete).
9. Real-time notifications para likes (websocket/SSE).
10. Progress tracking y certificados (opcional, fase 2).

---

# 14. Notas finales / decisiones de diseño (opinión)

* Recomiendo **almacenar explicitamente la asociación `course_guides(courseId, guideId)`** además del `status=ASSOCIATED_WITH_COURSE` en `guides`. Esto facilita consultas `GET /courses/{courseId}` y evita que una guía esté asociada a múltiples cursos sin intención. Si decides solo usar `status`, necesitas mapear también `courseId` en Guide o una tabla de asociación.
* Para escalabilidad de `likesCount`, usar una estrategia híbrida: transaccional para sistemas pequeños; events + eventual aggregation para sistemas con mucho tráfico.
* Evita exponer `authorIds` completos sin transformar (usar DTOs con nombre y link).

---

Si quieres, te lo puedo transformar a:

* contrato OpenAPI (YAML) con todos los endpoints y esquemas,
* esquema de base de datos SQL DDL,
* o ejemplos de controladores en Spring Boot (con security annotations) o en NestJS/Express.

¿Cuál prefieres que genere ahora?
