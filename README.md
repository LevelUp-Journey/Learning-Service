# Learning Service Microservice

A comprehensive DDD-based microservice for managing learning guides, courses, enrollments, and user interactions.

## 🏗️ Architecture

This service follows **Domain-Driven Design (DDD)** principles with clear bounded contexts:

- **Topics**: Subject categories for guides and courses
- **Guides**: Learning content with pages
- **Courses**: Collections of guides with enrollment
- **Enrollments**: User course registrations
- **Likes**: User engagement tracking
- **Comments**: User discussions and feedback

## 🛠️ Technology Stack

- **Java 25**
- **Spring Boot 3.5.7**
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **Swagger/OpenAPI 3.0** for API documentation
- **Lombok** for reducing boilerplate
- **Maven** for dependency management

## 📋 Prerequisites

- Java 25 or later
- PostgreSQL 14 or later
- Maven 3.9+ (or use included wrapper)

## 🚀 Getting Started

### 1. Database Setup

```bash
# Create database
createdb learning_db

# Or using psql
psql -U postgres
CREATE DATABASE learning_db;
```

### 2. Configuration

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/learning_db
    username: your_username
    password: your_password
```

### 3. JWT Secret (Optional)

For production, set a custom JWT secret:

```bash
export JWT_SECRET=your-256-bit-secret-key-here
```

Or update in `application.yml`:

```yaml
jwt:
  secret: your-base64-encoded-secret
```

### 4. Run the Application

```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```

The application will start on `http://localhost:8081/api/v1`

### 5. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8081/api/v1/swagger-ui.html
```

## 🔐 Authentication

This service uses **JWT (JSON Web Tokens)** for authentication.

### JWT Token Structure

```json
{
  "sub": "username",
  "userId": "uuid-here",
  "roles": ["ROLE_ADMIN", "ROLE_TEACHER"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

### User Roles

- **ROLE_ADMIN**: Full system access
- **ROLE_TEACHER**: Can create/edit guides and courses
- **ROLE_STUDENT**: Can enroll, like, and comment

### Using JWT in Requests

Include the JWT token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

### Testing with Swagger

1. Click the **Authorize** button in Swagger UI
2. Enter: `Bearer <your-jwt-token>`
3. Click **Authorize**
4. All subsequent requests will include the token

## 📚 API Endpoints

### Topics

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/topics` | List all topics | No |
| GET | `/topics/{id}` | Get topic by ID | No |
| POST | `/topics` | Create topic | Yes (ADMIN/TEACHER) |
| PUT | `/topics/{id}` | Update topic | Yes (ADMIN) |
| DELETE | `/topics/{id}` | Delete topic | Yes (ADMIN) |

### Guides (Partial Implementation)

| Method | Endpoint | Description | Auth Required | Status |
|--------|----------|-------------|---------------|--------|
| GET | `/guides` | Search guides | No | ⏳ Pending |
| GET | `/guides/{id}` | Get guide | No | ⏳ Pending |
| POST | `/guides` | Create guide | Yes (ADMIN/TEACHER) | ⏳ Pending |
| PUT | `/guides/{id}` | Update guide | Yes (Author/ADMIN) | ⏳ Pending |
| DELETE | `/guides/{id}` | Delete guide | Yes (Author/ADMIN) | ⏳ Pending |
| GET | `/guides/{id}/pages` | List pages | No | ⏳ Pending |
| POST | `/guides/{id}/pages` | Create page | Yes (Author/ADMIN) | ⏳ Pending |

### Courses, Enrollments, Likes, Comments

See [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) for detailed implementation progress.

## 🗂️ Project Structure

```
src/main/java/com/levelupjourney/learningservice/
├── shared/                              # Shared domain models and infrastructure
│   ├── domain/model/                   # Base entities and enums
│   └── infrastructure/
│       ├── config/                     # Configuration classes
│       ├── exception/                  # Global exception handling
│       └── security/                   # JWT and security configuration
│
├── topics/                             # Topics bounded context (COMPLETE)
│   ├── domain/model/
│   │   ├── aggregates/                # Topic aggregate
│   │   ├── commands/                  # Command records
│   │   └── queries/                   # Query records
│   ├── domain/services/               # Domain service interfaces
│   ├── infrastructure/persistence/    # JPA repositories
│   ├── application/internal/          # Service implementations
│   └── interfaces/rest/               # REST controllers and DTOs
│
├── guides/                             # Guides bounded context (PARTIAL)
│   └── ... (similar structure)
│
├── courses/                            # Courses bounded context (TODO)
├── enrollments/                        # Enrollments bounded context (TODO)
├── likes/                              # Likes bounded context (TODO)
└── comments/                           # Comments bounded context (TODO)
```

## 🔧 Development

### Building the Project

```bash
./mvnw clean package
```

### Running Tests

```bash
./mvnw test
```

### Code Style

This project uses:
- Lombok for reducing boilerplate
- Records for immutable DTOs
- JPA for persistence
- Constructor-based dependency injection

## 📖 Business Rules

### Guide Visibility

- **PUBLISHED**: Visible to everyone
- **DRAFT**: Only visible to authors and admins
- **ASSOCIATED_WITH_COURSE**: Only visible to enrolled users
- **ARCHIVED**: Not shown in listings
- **DELETED**: Soft deleted

### Authorization

- Only authors and admins can edit/delete guides
- Maximum number of authors per guide: 5 (configurable)
- Teachers and admins can create guides
- Students can only view published content

### Enrollments

- Users can enroll in a course only once
- Duplicate enrollments return 409 Conflict
- Enrollment required to view course guides
- Soft delete for cancelled enrollments

### Likes

- One like per user per entity
- Duplicate likes prevented by database constraint
- Like counters updated transactionally
- Supports both guides and courses

## 🐛 Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
pg_isready

# Verify connection
psql -U postgres -d learning_db
```

### JWT Token Invalid

- Ensure the token is not expired (24h default)
- Check the JWT secret matches between token generation and verification
- Verify the token format: `Bearer <token>`

### Port Already in Use

Change the port in `application.yml`:

```yaml
server:
  port: 8082  # or any available port
```

## 📝 Implementation Status

This project is **partially implemented**. See [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md) for:

- Completed components
- Pending tasks
- Implementation guidelines
- Code examples and patterns
- Estimated completion time

### Current Completion: ~30%

- ✅ Infrastructure and configuration
- ✅ Topics bounded context (100%)
- ⏳ Guides bounded context (40%)
- ⏳ Courses, Enrollments, Likes, Comments (0%)

## 🤝 Contributing

1. Follow the DDD structure and patterns
2. Use records for commands, queries, and DTOs
3. Implement proper authorization checks
4. Add Swagger documentation for all endpoints
5. Write unit tests for services
6. Follow the specification in [SPECS.md](SPECS.md)

## 📄 License

This project is part of LevelUp Journey platform.

## 📧 Contact

For questions or issues, please refer to the project documentation or open an issue.

---

**Note**: This microservice is under active development. The Guides, Courses, Enrollments, Likes, and Comments bounded contexts need completion. See IMPLEMENTATION_STATUS.md for detailed tasks and code templates.
