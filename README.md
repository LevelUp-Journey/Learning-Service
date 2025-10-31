# 🎓 Learning Service - Microservicio de Aprendizaje

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Microservicio completo para gestión de contenido educativo basado en **Domain-Driven Design (DDD)** con **CQRS**, **JWT Security**, y **API Response Standardization**.

## � Inicio Rápido

### Prerrequisitos
- Java 25
- Maven 3.9+
- PostgreSQL 12+

### Instalación y Ejecución

```bash
# Clonar el repositorio
cd learning-service

# Configurar base de datos PostgreSQL
createdb learning_service_db

# Compilar
./mvnw clean compile

# Ejecutar
./mvnw spring-boot:run
```

La aplicación se iniciará en **http://localhost:8085** mostrando:
```
================================================================================
� Learning Service started successfully!
================================================================================
📖 Swagger UI: http://localhost:8085/swagger-ui/index.html
📡 API Docs: http://localhost:8085/v3/api-docs
🔧 H2 Console: http://localhost:8085/h2-console
================================================================================
```

### Ejecutar Tests

```bash
# Todos los tests
./mvnw test

# Test específico
./mvnw test -Dtest=StudentCompletesGuideIntegrationTest
```

## 📊 Arquitectura

### Bounded Contexts (DDD)

El microservicio está organizado en **7 contextos delimitados**:

#### 1. **Topics** (13 archivos) ✅
```
topics/
├── domain/
│   ├── model/
│   │   ├── aggregates/Topic.java
│   │   ├── commands/CreateTopicCommand.java
│   │   └── queries/GetAllTopicsQuery.java
│   └── services/TopicCommandService.java
├── application/internal/
│   ├── commandservices/TopicCommandServiceImpl.java
│   └── queryservices/TopicQueryServiceImpl.java
├── infrastructure/persistence/jpa/repositories/TopicRepository.java
└── interfaces/rest/
    ├── resources/TopicResource.java
    ├── transform/TopicResourceAssembler.java
    └── TopicsController.java
```

**Funcionalidades:**
- ✅ CRUD completo de tópicos
- ✅ Validación de nombres únicos
- ✅ Control de acceso: ADMIN/TEACHER pueden crear

#### 2. **Guides** (29 archivos) ✅
```
guides/
├── domain/model/
│   ├── aggregates/
│   │   ├── Guide.java (con páginas anidadas)
│   │   └── Page.java
│   ├── commands/ (8 comandos)
│   └── queries/ (3 queries)
└── interfaces/rest/
    ├── resources/ (7 resources)
    └── GuidesController.java (15 endpoints)
```

**Funcionalidades:**
- ✅ Guides con páginas ordenadas
- ✅ Multi-autoría (máx 5 autores configurables)
- ✅ Estados: DRAFT, PUBLISHED, ASSOCIATED_WITH_COURSE, DELETED
- ✅ Soft delete
- ✅ Visibilidad basada en estado y roles:
  - `PUBLISHED`: Público
  - `DRAFT`: Solo autores + ADMIN
  - `ASSOCIATED_WITH_COURSE`: Solo usuarios inscritos

#### 3. **Courses** (25 archivos) ✅
```
courses/
├── domain/model/
│   ├── aggregates/Course.java
│   ├── commands/ (7 comandos)
│   └── queries/ (2 queries)
└── interfaces/rest/
    └── CoursesController.java (10 endpoints)
```

**Funcionalidades:**
- ✅ Cursos con guides asociados
- ✅ Gestión de autores y tópicos
- ✅ Asociar/desasociar guides
- ✅ Al asociar guide: `guide.status = ASSOCIATED_WITH_COURSE`
- ✅ Estados sincronizados

#### 4. **Enrollments** (16 archivos) ✅
```
enrollments/
├── domain/model/
│   ├── aggregates/Enrollment.java
│   ├── valueobjects/EnrollmentStatus.java
│   └── commands/ (2 comandos)
└── interfaces/rest/
    └── EnrollmentsController.java (5 endpoints)
```

**Funcionalidades:**
- ✅ Inscripción a cursos
- ✅ Unique constraint: `(userId, courseId)`
- ✅ Prevención de duplicados → **409 Conflict**
- ✅ Estados: ACTIVE, CANCELLED, COMPLETED
- ✅ Usuarios solo pueden inscribirse a sí mismos (excepto ADMIN)

#### 5. **Learning Progress** (18 archivos) ✅ 🆕
```
learningprogress/
├── domain/model/
│   ├── aggregates/LearningProgress.java
│   ├── valueobjects/
│   │   ├── LearningEntityType.java (GUIDE, COURSE)
│   │   └── ProgressStatus.java (NOT_STARTED, IN_PROGRESS, COMPLETED)
│   └── commands/ (3 comandos)
└── interfaces/rest/
    └── LearningProgressController.java (5 endpoints)
```

**Funcionalidades:**
- ✅ Tracking de progreso para Guides y Courses
- ✅ Registro de items completados (páginas o guides)
- ✅ Tiempo de lectura acumulado
- ✅ Porcentaje de progreso calculado automáticamente
- ✅ Auto-completado al 100%
- ✅ Unique constraint: `(userId, entityType, entityId)`

#### 6. **Likes** ⏳ (Pendiente)
- Like/Unlike para Guides y Courses
- Actualización transaccional de `likesCount`
- Flag `likedByRequester` en responses

#### 7. **Comments** ⏳ (Pendiente)
- Comentarios anidados con `parentCommentId`
- Soft delete
- Autorización por contexto (Guide vs Course)

### Infraestructura Compartida

```
shared/
├── domain/model/
│   ├── AuditableModel.java (createdAt, updatedAt, version)
│   ├── EntityStatus.java
│   └── enums/
├── infrastructure/
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── SecurityConfiguration.java
│   │   └── SecurityContextHelper.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ConflictException.java (409)
│   │   └── ErrorResponse.java
│   └── web/
│       ├── ApiResponse.java (wrapper genérico)
│       └── ResponseInterceptor.java
└── infrastructure/config/
    └── OpenApiConfiguration.java
```

## 🔐 Seguridad

### JWT Authentication

**Configuración (`application.yml`):**
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-here}
  expiration: 86400000  # 24 horas
  refresh-expiration: 604800000  # 7 días
```

**Roles disponibles:**
- `ROLE_ADMIN`: Acceso total
- `ROLE_TEACHER`: Crear guides, courses, topics
- `ROLE_STUDENT`: Inscribirse, aprender, comentar

**Header de autenticación:**
```http
Authorization: Bearer <JWT_TOKEN>
```

### Endpoints de Autenticación

```http
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/refresh
```

## 📡 API Response Format

**Todas las respuestas siguen el formato estándar:**

```typescript
{
  data: T | null,           // Datos de respuesta (genérico)
  error: string | null,     // Mensaje de error (si hay)
  success: boolean,         // Indicador de éxito
  statusCode: number        // Código HTTP
}
```

**Ejemplo de respuesta exitosa:**
```json
{
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "Introduction to Java",
    "status": "PUBLISHED"
  },
  "error": null,
  "success": true,
  "statusCode": 200
}
```

**Ejemplo de error:**
```json
{
  "data": null,
  "error": "User is already enrolled in this course",
  "success": false,
  "statusCode": 409
}
```

## 🧪 Testing

### Estructura de Tests

```
src/test/java/
├── shared/infrastructure/security/
│   └── TestJwtTokenProvider.java
└── integration/
    ├── StudentCompletesGuideIntegrationTest.java ✅
    ├── StudentEnrollsCourseIntegrationTest.java ⏳
    ├── TeacherCreatesCourseIntegrationTest.java ⏳
    └── TeacherCreatesGuideIntegrationTest.java ⏳
```

### Test Coverage

**Implementado:**
- ✅ Flujo 1: Estudiante completa un guide
  - Lista guides disponibles
  - Obtiene detalles del guide
  - Inicia progreso de aprendizaje
  - Completa páginas con tracking de tiempo
  - Sistema auto-completa al 100%
- ✅ Error: Duplicado de progreso (409)
- ✅ Error: Acceso no autenticado (403)

**Pendiente:**
- ⏳ Flujo 2: Estudiante se inscribe a curso
- ⏳ Flujo 3: Profesor crea curso
- ⏳ Flujo 4: Profesor crea guide
- ⏳ Tests de autorización por rol
- ⏳ Tests de validación de datos

### Configuración de Test (`application-test.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop

jwt:
  secret: ThisIsAVeryLongSecretKeyForJWTTokenGenerationInTest...
```

## 📊 Base de Datos

### Modelo de Datos (Principales Entidades)

```sql
-- Topics
CREATE TABLE topics (
    id UUID PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Guides
CREATE TABLE guides (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    cover_image VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    likes_count INTEGER DEFAULT 0,
    pages_count INTEGER DEFAULT 0,
    course_id UUID,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Pages
CREATE TABLE pages (
    id UUID PRIMARY KEY,
    guide_id UUID NOT NULL REFERENCES guides(id),
    content TEXT NOT NULL,
    order_number INTEGER NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(guide_id, order_number)
);

-- Courses
CREATE TABLE courses (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    cover_image VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    likes_count INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Enrollments
CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, course_id)
);

-- Learning Progress
CREATE TABLE learning_progress (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress_percentage INTEGER DEFAULT 0,
    total_items INTEGER DEFAULT 0,
    completed_items INTEGER DEFAULT 0,
    total_reading_time_seconds BIGINT DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, entity_type, entity_id)
);
```

## �️ Configuración

### Variables de Entorno

```bash
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/learning_service_db
DB_USERNAME=admin
DB_PASSWORD=admin

# JWT
JWT_SECRET=your-very-long-secret-key-here
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8085

# Límites de negocio
MAX_AUTHORS=5
```

### application.yml

```yaml
spring:
  application:
    name: learning-service
  
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/learning_service_db}
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:admin}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

server:
  port: ${SERVER_PORT:8085}

jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}

application:
  max-authors: ${MAX_AUTHORS:5}
  pagination:
    default-page-size: 10
    max-page-size: 100

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 📈 Estadísticas del Proyecto

- **Total archivos fuente:** 127
- **Líneas de código:** ~15,000+
- **Bounded Contexts:** 7 (5 completos)
- **Aggregates:** 8
- **Commands:** 25+
- **Queries:** 15+
- **Controllers:** 7
- **Tests:** 3+ (en crecimiento)

## 🚦 Endpoints Principales

### Topics
```http
GET    /api/v1/topics
POST   /api/v1/topics
GET    /api/v1/topics/{id}
PUT    /api/v1/topics/{id}
DELETE /api/v1/topics/{id}
```

### Guides
```http
GET    /api/v1/guides
POST   /api/v1/guides
GET    /api/v1/guides/{id}
PUT    /api/v1/guides/{id}
DELETE /api/v1/guides/{id}
PUT    /api/v1/guides/{id}/status
PUT    /api/v1/guides/{id}/authors
GET    /api/v1/guides/{guideId}/pages
POST   /api/v1/guides/{guideId}/pages
PUT    /api/v1/guides/{guideId}/pages/{pageId}
DELETE /api/v1/guides/{guideId}/pages/{pageId}
GET    /api/v1/guides/by-topics
GET    /api/v1/guides/by-author/{authorId}
POST   /api/v1/guides/search
```

### Courses
```http
GET    /api/v1/courses
POST   /api/v1/courses
GET    /api/v1/courses/{id}
PUT    /api/v1/courses/{id}
DELETE /api/v1/courses/{id}
POST   /api/v1/courses/{courseId}/guides/{guideId}
DELETE /api/v1/courses/{courseId}/guides/{guideId}
PUT    /api/v1/courses/{id}/authors
GET    /api/v1/courses/by-topics
POST   /api/v1/courses/search
```

### Enrollments
```http
POST   /api/v1/enrollments
DELETE /api/v1/enrollments/{id}
GET    /api/v1/enrollments/user/{userId}
GET    /api/v1/enrollments/course/{courseId}
GET    /api/v1/enrollments/check
```

### Learning Progress
```http
POST   /api/v1/progress
PUT    /api/v1/progress/{id}
POST   /api/v1/progress/{id}/complete
GET    /api/v1/progress
GET    /api/v1/progress/user/{userId}
```

## 🎯 Roadmap

### ✅ Completado
- [x] Infraestructura base (JWT, Security, Exception Handling)
- [x] Response Interceptor con formato estándar
- [x] Topics bounded context
- [x] Guides bounded context (con páginas)
- [x] Courses bounded context (con asociación de guides)
- [x] Enrollments bounded context
- [x] Learning Progress bounded context
- [x] Tests de integración básicos
- [x] Configuración de puerto dinámico con logs

### 🚧 En Progreso
- [ ] Likes bounded context
- [ ] Comments bounded context
- [ ] Tests exhaustivos para todos los flujos

### 📋 Pendiente
- [ ] Notificaciones (WebSocket)
- [ ] Badges y Achievements
- [ ] Analytics y métricas
- [ ] Export de progreso (PDF)
- [ ] Integración con sistema de pagos
- [ ] Rate limiting
- [ ] Caching con Redis

## 🐛 Troubleshooting

### Problemas Comunes

**1. Error de conexión a base de datos:**
```bash
# Verificar que PostgreSQL esté corriendo
pg_isready

# Conectar manualmente
psql -U postgres -d learning_service_db
```

**2. Puerto ya en uso:**
```bash
# Matar proceso en puerto 8085
lsof -ti:8085 | xargs kill -9

# O cambiar puerto en application.yml
server:
  port: 8086
```

**3. JWT Token inválido:**
- Verificar que el token no haya expirado (24h por defecto)
- Asegurar que el secreto JWT coincida
- Formato correcto: `Bearer <token>`

**4. Tests fallan con 403:**
- Verificar que `application-test.yml` tenga JWT secret largo (>64 caracteres)
- Asegurar que H2 esté en `MODE=PostgreSQL`

## 📝 Licencia

Este proyecto está bajo la licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👥 Autores

- **Level Up Journey Team**

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea tu feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

**⚡ Built with Spring Boot, DDD, and ❤️ by Level Up Journey**
## 📧 Contact

For questions or issues, please refer to the project documentation or open an issue.

---

**Note**: This microservice is under active development. The Guides, Courses, Enrollments, Likes, and Comments bounded contexts need completion. See IMPLEMENTATION_STATUS.md for detailed tasks and code templates.
