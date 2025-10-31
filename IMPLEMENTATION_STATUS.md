# Learning Service - Implementation Status

## 🎯 Project Overview
Complete DDD-based microservice for managing learning guides, courses, enrollments, and user interactions.

---

## ✅ Completed Components

### 1. Infrastructure & Configuration
- ✅ **pom.xml** - Updated with all dependencies (Spring Security, JWT, PostgreSQL, Swagger, etc.)
- ✅ **application.yml** - Complete configuration with JWT, database, Swagger, CORS
- ✅ **Spring Security** - JWT authentication, authorization filters, security configuration
- ✅ **Exception Handling** - Global exception handler with custom business exceptions
- ✅ **OpenAPI/Swagger** - Configured with security scheme
- ✅ **JPA Auditing** - Enabled with AuditableModel base class

### 2. Shared Domain
- ✅ **AuditableModel** - Base class with createdAt, updatedAt, version
- ✅ **Enums** - EntityStatus, EntityType, EnrollmentStatus
- ✅ **Security Helper** - SecurityContextHelper for accessing current user
- ✅ **JWT Components** - JwtTokenProvider, JwtAuthenticationFilter

### 3. Topics Bounded Context (100% Complete)
- ✅ **Aggregate** - Topic entity
- ✅ **Commands** - Create, Update, Delete
- ✅ **Queries** - GetById, GetAll
- ✅ **Domain Services** - TopicCommandService, TopicQueryService
- ✅ **Repository** - TopicRepository
- ✅ **Command Service Impl** - Full CRUD with validation
- ✅ **Query Service Impl** - Read operations
- ✅ **Resources (DTOs)** - TopicResource, CreateTopicResource, UpdateTopicResource
- ✅ **Controller** - Full REST API with Swagger documentation
- ✅ **Authorization** - ROLE_ADMIN, ROLE_TEACHER for write operations

### 4. Guides Bounded Context (40% Complete)
- ✅ **Aggregates** - Guide, Page entities with relationships
- ✅ **Commands** - All commands (Create/Update/Delete for Guide and Page)
- ✅ **Queries** - Basic queries (GetById, Search, GetPagesByGuideId)
- ⏳ **Domain Services** - Interfaces need implementation
- ⏳ **Repository** - Interface created, needs queries
- ⏳ **Service Implementations** - Need to implement
- ⏳ **Resources** - Need to create DTOs
- ⏳ **Controller** - Need to create REST endpoints

---

## 📋 Remaining Implementation Tasks

### High Priority

#### 1. Complete Guides Bounded Context
**Files to Create:**

```
guides/
├── domain/services/
│   ├── GuideCommandService.java (interface)
│   ├── GuideQueryService.java (interface)
│   ├── PageCommandService.java (interface)
│   └── PageQueryService.java (interface)
├── infrastructure/persistence/jpa/repositories/
│   ├── GuideRepository.java
│   └── PageRepository.java
├── application/internal/
│   ├── commandservices/
│   │   ├── GuideCommandServiceImpl.java
│   │   └── PageCommandServiceImpl.java
│   └── queryservices/
│       ├── GuideQueryServiceImpl.java
│       └── PageQueryServiceImpl.java
├── interfaces/rest/resources/
│   ├── GuideResource.java
│   ├── CreateGuideResource.java
│   ├── UpdateGuideResource.java
│   ├── PageResource.java
│   ├── CreatePageResource.java
│   └── UpdatePageResource.java
├── interfaces/rest/transform/
│   ├── GuideResourceAssembler.java
│   └── PageResourceAssembler.java
└── interfaces/rest/
    └── GuidesController.java
```

**Key Business Rules:**
- Only ADMIN or TEACHER can create guides
- Only authors or ADMIN can edit/delete
- Visibility based on status (PUBLISHED, DRAFT, ASSOCIATED_WITH_COURSE)
- Max authors limit from application.yml
- Auto-increment pagesCount on page add/remove

#### 2. Courses Bounded Context
**Structure:** Similar to Guides context

**Files to Create:**

```
courses/
├── domain/model/
│   ├── aggregates/Course.java
│   ├── commands/
│   │   ├── CreateCourseCommand.java
│   │   ├── UpdateCourseCommand.java
│   │   ├── UpdateCourseAuthorsCommand.java
│   │   └── DeleteCourseCommand.java
│   └── queries/
│       ├── GetCourseByIdQuery.java
│       └── SearchCoursesQuery.java
├── domain/services/
│   ├── CourseCommandService.java
│   └── CourseQueryService.java
├── infrastructure/persistence/jpa/repositories/
│   └── CourseRepository.java
├── application/internal/
│   ├── commandservices/CourseCommandServiceImpl.java
│   └── queryservices/CourseQueryServiceImpl.java
└── interfaces/rest/
    ├── resources/...
    ├── transform/CourseResourceAssembler.java
    └── CoursesController.java
```

**Course Entity Fields:**
```java
- UUID id
- String title
- String description
- String coverImage
- Set<String> authorIds
- Set<Topic> topics
- Set<Guide> guides (associated guides)
- EntityStatus status
- Integer likesCount
- AuditableModel fields (createdAt, updatedAt, version)
```

**Key Features:**
- Associate guides with courses
- Only enrolled users can see course guides
- Response includes guide summaries when enrolled

#### 3. Enrollments Bounded Context
**Files to Create:**

```
enrollments/
├── domain/model/
│   ├── aggregates/Enrollment.java
│   ├── commands/
│   │   ├── EnrollUserCommand.java
│   │   └── CancelEnrollmentCommand.java
│   └── queries/
│       ├── GetEnrollmentByUserAndCourseQuery.java
│       └── GetUserEnrollmentsQuery.java
├── domain/services/...
├── infrastructure/persistence/jpa/repositories/
│   └── EnrollmentRepository.java
├── application/internal/...
└── interfaces/rest/
    └── EnrollmentsController.java
```

**Enrollment Entity Fields:**
```java
- UUID id
- String userId
- UUID courseId
- EnrollmentStatus status (ACTIVE, CANCELLED)
- AuditableModel fields
- @UniqueConstraint(userId, courseId)
```

**Key Business Rules:**
- Prevent duplicate enrollments (return 409 Conflict)
- Validate course exists before enrolling
- Soft delete (status = CANCELLED) instead of physical delete

#### 4. Likes Bounded Context
**Files to Create:**

```
likes/
├── domain/model/
│   ├── aggregates/Like.java
│   ├── commands/
│   │   ├── CreateLikeCommand.java
│   │   └── RemoveLikeCommand.java
│   └── queries/
│       ├── GetLikeByUserAndEntityQuery.java
│       └── GetLikesCountQuery.java
├── domain/services/...
├── infrastructure/persistence/jpa/repositories/
│   └── LikeRepository.java
├── application/internal/...
└── interfaces/rest/
    └── LikesController.java (or nested in Guides/Courses controllers)
```

**Like Entity Fields:**
```java
- UUID id
- String userId
- EntityType entityType (GUIDE, COURSE)
- UUID entityId
- LocalDateTime createdAt
- @UniqueConstraint(userId, entityType, entityId)
```

**Key Features:**
- Unique constraint prevents duplicates
- Use @Transactional with SELECT FOR UPDATE for counter updates
- Increment/decrement likesCount in Guide/Course
- Endpoint can be toggle: POST creates, DELETE removes

**REST Endpoints:**
```
POST /guides/{guideId}/likes - Like a guide
DELETE /guides/{guideId}/likes - Unlike a guide
POST /courses/{courseId}/likes - Like a course
DELETE /courses/{courseId}/likes - Unlike a course
```

#### 5. Comments Bounded Context
**Files to Create:**

```
comments/
├── domain/model/
│   ├── aggregates/Comment.java
│   ├── commands/
│   │   ├── CreateCommentCommand.java
│   │   ├── UpdateCommentCommand.java
│   │   └── DeleteCommentCommand.java
│   └── queries/
│       ├── GetCommentsByEntityQuery.java
│       └── GetCommentRepliesQuery.java
├── domain/services/...
├── infrastructure/persistence/jpa/repositories/
│   └── CommentRepository.java
├── application/internal/...
└── interfaces/rest/
    └── CommentsController.java
```

**Comment Entity Fields:**
```java
- UUID id
- String content
- String authorId
- UUID parentCommentId (nullable for replies)
- EntityType entityType
- UUID entityId
- Boolean isDeleted (soft delete)
- AuditableModel fields
```

**Key Business Rules:**
- For courses: Only enrolled users can comment
- For guides: Any authenticated user (or based on guide visibility)
- Soft delete: set isDeleted=true, optionally blank content
- Support nested replies via parentCommentId

**REST Endpoints:**
```
GET /guides/{guideId}/comments
POST /guides/{guideId}/comments
GET /courses/{courseId}/comments
POST /courses/{courseId}/comments
PUT /comments/{commentId}
DELETE /comments/{commentId}
POST /comments/{commentId}/replies
```

---

## 🔧 Implementation Guidelines

### Repository Pattern Example
```java
@Repository
public interface GuideRepository extends JpaRepository<Guide, UUID> {
    Optional<Guide> findByIdAndStatus(UUID id, EntityStatus status);
    
    Page<Guide> findByStatus(EntityStatus status, Pageable pageable);
    
    @Query("SELECT g FROM Guide g WHERE g.status = :status " +
           "AND (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:topics IS NULL OR EXISTS (SELECT t FROM g.topics t WHERE t.id IN :topics))")
    Page<Guide> searchGuides(
        @Param("title") String title,
        @Param("topics") Set<UUID> topicIds,
        @Param("status") EntityStatus status,
        Pageable pageable
    );
    
    boolean existsByIdAndAuthorIdsContaining(UUID id, String userId);
}
```

### Command Service Implementation Pattern
```java
@Service
@RequiredArgsConstructor
public class GuideCommandServiceImpl implements GuideCommandService {
    
    private final GuideRepository guideRepository;
    private final TopicRepository topicRepository;
    private final SecurityContextHelper securityHelper;
    
    @Value("${application.guides.max-authors}")
    private int maxAuthors;
    
    @Override
    @Transactional
    public Optional<Guide> handle(CreateGuideCommand command) {
        // 1. Validate topics exist
        Set<Topic> topics = topicRepository.findAllById(command.topicIds())
                .stream().collect(Collectors.toSet());
                
        if (topics.size() != command.topicIds().size()) {
            throw new BusinessException("Some topics not found", HttpStatus.BAD_REQUEST);
        }
        
        // 2. Determine authors
        Set<String> authors = command.authorIds() != null && !command.authorIds().isEmpty()
                ? command.authorIds()
                : Set.of(securityHelper.getCurrentUserId());
        
        // 3. Create and save guide
        var guide = new Guide(
                command.title(),
                command.description(),
                command.coverImage(),
                authors,
                topics
        );
        
        return Optional.of(guideRepository.save(guide));
    }
    
    @Override
    @Transactional
    public Optional<Guide> handle(UpdateGuideCommand command) {
        var guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));
        
        // Check authorization
        String userId = securityHelper.getCurrentUserId();
        if (!guide.isAuthor(userId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to edit this guide");
        }
        
        // Update fields
        if (command.title() != null) guide.updateTitle(command.title());
        if (command.description() != null) guide.updateDescription(command.description());
        if (command.coverImage() != null) guide.updateCoverImage(command.coverImage());
        
        if (command.topicIds() != null) {
            Set<Topic> topics = topicRepository.findAllById(command.topicIds())
                    .stream().collect(Collectors.toSet());
            guide.setTopics(topics);
        }
        
        return Optional.of(guideRepository.save(guide));
    }
}
```

### Controller with Authorization Pattern
```java
@RestController
@RequestMapping("/guides")
@RequiredArgsConstructor
@Tag(name = "Guides", description = "Learning guides management")
public class GuidesController {
    
    private final GuideCommandService commandService;
    private final GuideQueryService queryService;
    private final SecurityContextHelper securityHelper;
    
    @GetMapping
    @Operation(summary = "Search guides", description = "Search guides with filters")
    public ResponseEntity<Page<GuideResource>> searchGuides(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Set<UUID> topicIds,
            @RequestParam(required = false) Set<String> authorIds,
            Pageable pageable
    ) {
        // For non-authenticated users, only show PUBLISHED
        EntityStatus status = securityHelper.isAuthenticated() ? null : EntityStatus.PUBLISHED;
        
        var query = new SearchGuidesQuery(title, topicIds, authorIds, status, pageable);
        var guides = queryService.handle(query);
        
        // Transform to resources with likedByRequester flag
        var resources = guides.map(guide -> 
                GuideResourceAssembler.toResourceFromEntity(guide, 
                        checkIfLikedByUser(guide.getId()))
        );
        
        return ResponseEntity.ok(resources);
    }
    
    @GetMapping("/{guideId}")
    @Operation(summary = "Get guide by ID")
    public ResponseEntity<GuideResource> getGuideById(@PathVariable UUID guideId) {
        var guide = queryService.handle(new GetGuideByIdQuery(guideId))
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));
        
        // Check visibility
        if (!isGuideVisibleToUser(guide)) {
            throw new ResourceNotFoundException("Guide not found");
        }
        
        var resource = GuideResourceAssembler.toResourceFromEntity(guide, 
                checkIfLikedByUser(guideId));
        return ResponseEntity.ok(resource);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Create guide", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<GuideResource> createGuide(@Valid @RequestBody CreateGuideResource resource) {
        var command = GuideResourceAssembler.toCommandFromResource(resource);
        var guide = commandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Failed to create guide"));
        
        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false);
        return new ResponseEntity<>(guideResource, HttpStatus.CREATED);
    }
    
    private boolean isGuideVisibleToUser(Guide guide) {
        if (guide.getStatus() == EntityStatus.PUBLISHED) return true;
        if (!securityHelper.isAuthenticated()) return false;
        
        String userId = securityHelper.getCurrentUserId();
        if (guide.isAuthor(userId) || securityHelper.isAdmin()) return true;
        
        if (guide.getStatus() == EntityStatus.ASSOCIATED_WITH_COURSE) {
            // Check if user is enrolled in the course
            return enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
                    userId, guide.getCourseId(), EnrollmentStatus.ACTIVE);
        }
        
        return false;
    }
}
```

---

## 🚀 Quick Start Commands

### Run the Application
```bash
cd /Users/nanakusa/Desktop/LevelUpJourney/learning-service
./mvnw spring-boot:run
```

### Access Swagger UI
```
http://localhost:8081/api/v1/swagger-ui.html
```

### Test JWT Authentication
1. Generate a test JWT token with the configured secret
2. Include in requests: `Authorization: Bearer <token>`
3. Token should include claims: `userId`, `roles[]`

### Database Setup
```bash
# Create PostgreSQL database
createdb learning_db

# Update application.yml with your credentials
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

## 📝 Testing Strategy

### Unit Tests Template
```java
@ExtendWith(MockitoExtension.class)
class GuideCommandServiceImplTest {
    
    @Mock
    private GuideRepository guideRepository;
    
    @Mock
    private TopicRepository topicRepository;
    
    @Mock
    private SecurityContextHelper securityHelper;
    
    @InjectMocks
    private GuideCommandServiceImpl guideCommandService;
    
    @Test
    void should_CreateGuide_When_ValidCommand() {
        // Arrange
        var command = new CreateGuideCommand(
                "Test Guide", "Description", null, 
                Set.of("user1"), Set.of(UUID.randomUUID())
        );
        
        when(topicRepository.findAllById(any()))
                .thenReturn(List.of(new Topic("Test", "Desc")));
        when(guideRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Act
        var result = guideCommandService.handle(command);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Guide", result.get().getTitle());
        verify(guideRepository).save(any(Guide.class));
    }
}
```

---

## 📚 Additional Resources

### DDD Patterns Used
- **Aggregates**: Guide, Course, Topic, Enrollment, Like, Comment
- **Value Objects**: Embedded in aggregates (could extract more)
- **Domain Events**: Can be added for likes, enrollments, comments
- **Repository Pattern**: One per aggregate root
- **CQRS**: Separate Command and Query services
- **ACL**: Not yet implemented (for external contexts)

### API Design Principles
- RESTful resources (plural nouns)
- HTTP verbs (GET, POST, PUT, DELETE)
- Proper status codes (200, 201, 204, 400, 401, 403, 404, 409, 500)
- Pagination for lists
- Filtering and search
- No DTOs in domain layer (Resources in interfaces layer)

### Security Considerations
- JWT-based authentication
- Role-based authorization (ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT)
- Resource ownership checks
- Visibility rules based on entity status
- CORS configuration for allowed origins

---

## ✅ Checklist for Completion

- [x] Infrastructure setup (Security, JWT, Swagger, JPA)
- [x] Topics bounded context (100%)
- [ ] Guides bounded context (40% - needs services, repos, controllers)
- [ ] Courses bounded context (0%)
- [ ] Enrollments bounded context (0%)
- [ ] Likes bounded context (0%)
- [ ] Comments bounded context (0%)
- [ ] Integration tests
- [ ] API documentation review
- [ ] Database indexes optimization
- [ ] Real-time notifications (WebSocket/SSE) - optional
- [ ] Progress tracking - optional
- [ ] Certificates - optional

---

## 🎯 Next Steps

1. **Complete Guides Context** (Highest Priority)
   - Implement GuideRepository with search queries
   - Implement GuideCommandServiceImpl with authorization
   - Implement GuideQueryServiceImpl with visibility checks
   - Create REST resources and assembler
   - Create GuidesController with full endpoints

2. **Implement Courses Context**
   - Follow same pattern as Guides
   - Add course-guide association logic

3. **Add Enrollments**
   - Simple aggregate with validation
   - Integrate with course visibility

4. **Implement Likes**
   - Transaction management for counters
   - Toggle endpoint

5. **Add Comments**
   - Nested replies support
   - Authorization based on enrollment

**Estimated Time:** 
- Guides completion: 3-4 hours
- Each additional context: 2-3 hours
- Total remaining: 12-15 hours for full implementation

---

*Last Updated: October 31, 2025*
