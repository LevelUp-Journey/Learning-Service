# Course Difficulty Level Feature

## Overview

The Course entity now includes a **difficulty level** field that categorizes courses by their complexity and learning requirements. This helps students choose courses appropriate for their skill level and allows teachers to properly classify their content.

## Difficulty Levels

The system supports four difficulty levels (in English):

| Level | Description | Target Audience |
|-------|-------------|----------------|
| **BEGINNER** | Entry-level courses with no prerequisites | Students new to the subject |
| **INTERMEDIATE** | Courses requiring basic knowledge | Students with foundational understanding |
| **ADVANCED** | In-depth courses requiring solid background | Experienced learners |
| **EXPERT** | Specialized, cutting-edge content | Professionals and subject matter experts |

## Technical Implementation

### Database Schema

- **Column Name**: `difficulty_level`
- **Type**: `VARCHAR(255)` (stored as String)
- **Constraint**: `NOT NULL` with check constraint
- **Default Value**: `BEGINNER`
- **Valid Values**: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`

SQL constraint:
```sql
ALTER TABLE courses 
ADD COLUMN difficulty_level VARCHAR(255) NOT NULL 
CHECK (difficulty_level IN ('BEGINNER','INTERMEDIATE','ADVANCED','EXPERT'));
```

### Domain Model

**Enum Location**: `com.levelupjourney.learningservice.courses.domain.model.valueobjects.DifficultyLevel`

```java
public enum DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}
```

**Entity Field**: Added to `Course` aggregate
```java
@Enumerated(EnumType.STRING)
@Column(name = "difficulty_level", nullable = false)
private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;
```

### API Changes

#### Create Course (POST `/api/v1/courses`)

**Request Body** (CreateCourseResource):
```json
{
  "title": "Introduction to Java Programming",
  "description": "Learn Java from scratch",
  "coverImage": "https://example.com/cover.jpg",
  "authorIds": ["user-123"],
  "topicIds": ["topic-uuid"],
  "difficultyLevel": "BEGINNER"
}
```

**Validation**: `difficultyLevel` is **required** (`@NotNull` validation)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "course-uuid",
    "title": "Introduction to Java Programming",
    "description": "Learn Java from scratch",
    "coverImage": "https://example.com/cover.jpg",
    "status": "DRAFT",
    "difficultyLevel": "BEGINNER",
    "likesCount": 0,
    "totalPages": 0,
    "createdAt": "2025-10-31T15:00:00Z",
    "updatedAt": "2025-10-31T15:00:00Z"
  }
}
```

#### Update Course (PUT `/api/v1/courses/{id}`)

**Request Body** (UpdateCourseResource):
```json
{
  "title": "Advanced Java Programming",
  "description": "Master advanced Java concepts",
  "difficultyLevel": "ADVANCED"
}
```

**Validation**: `difficultyLevel` is **optional** (allows partial updates)

#### Get Course (GET `/api/v1/courses/{id}`)

**Response** includes `difficultyLevel` field:
```json
{
  "success": true,
  "data": {
    "id": "course-uuid",
    "title": "Advanced Java Programming",
    "difficultyLevel": "ADVANCED",
    // ... other fields
  }
}
```

#### Search Courses (GET `/api/v1/courses`)

All courses returned include the `difficultyLevel` field.

**Example Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "course-1",
      "title": "Java Basics",
      "difficultyLevel": "BEGINNER",
      // ... other fields
    },
    {
      "id": "course-2",
      "title": "Spring Boot Mastery",
      "difficultyLevel": "EXPERT",
      // ... other fields
    }
  ]
}
```

### Swagger Documentation

All Course endpoints now document the difficulty level field:

- **POST /api/v1/courses**: "Difficulty level: BEGINNER (default), INTERMEDIATE, ADVANCED, EXPERT"
- **PUT /api/v1/courses/{id}**: "Difficulty level: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT"
- **GET /api/v1/courses**: "Courses include difficulty level: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT"
- **GET /api/v1/courses/{id}**: "Course includes difficulty level: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT"

## Business Logic

### Default Value

When creating a course:
1. If `difficultyLevel` is provided, it is used
2. If `difficultyLevel` is `null`, it defaults to `BEGINNER`
3. Validation ensures a valid enum value is provided

### Update Behavior

When updating a course:
1. If `difficultyLevel` is provided, it updates the course's difficulty
2. If `difficultyLevel` is `null`, the existing difficulty is preserved (partial update)

### Access Control

- **Create Course**: Requires `TEACHER` or `ADMIN` role
- **Update Course**: Only course authors or `ADMIN` can update difficulty level
- **View Course**: All users can see the difficulty level

## Modified Files

### Domain Layer
- `DifficultyLevel.java` - New enum value object
- `Course.java` - Added `difficultyLevel` field, constructor parameter, and `updateDifficultyLevel()` method

### Application Layer
- `CreateCourseCommand.java` - Added `difficultyLevel` parameter
- `UpdateCourseCommand.java` - Added `difficultyLevel` parameter
- `CourseCommandServiceImpl.java` - Updated handlers to process difficulty level

### Interface Layer (REST)
- `CourseResource.java` - Added `difficultyLevel` field to response DTO
- `CreateCourseResource.java` - Added `@NotNull difficultyLevel` field to request DTO
- `UpdateCourseResource.java` - Added optional `difficultyLevel` field to request DTO
- `CourseResourceAssembler.java` - Updated mappings to include difficulty level
- `CoursesController.java` - Updated Swagger documentation

## Testing

All existing tests continue to pass with the new feature:
- ✅ 7 tests executed
- ✅ 0 failures
- ✅ Integration tests verify the feature works end-to-end

Tests verify:
1. Default value is applied when not specified
2. Custom difficulty level is persisted correctly
3. Difficulty level is returned in API responses
4. Difficulty level can be updated via PUT endpoint

## Future Enhancements

Potential improvements for this feature:

1. **Filtering**: Add query parameter to filter courses by difficulty level
   ```
   GET /api/v1/courses?difficultyLevel=BEGINNER
   ```

2. **Search Enhancement**: Include difficulty in search results ranking

3. **Prerequisites**: Link difficulty levels to prerequisite courses

4. **Analytics**: Track enrollment patterns by difficulty level

5. **Recommendations**: Use difficulty level in course recommendation algorithms

6. **Progress Tracking**: Adapt progress tracking based on difficulty level

## Migration Notes

- **Backward Compatibility**: Existing courses will automatically get `BEGINNER` as default
- **Database Migration**: Handled automatically by Hibernate on application startup
- **API Compatibility**: New field is included in all responses; clients should handle gracefully

## Version Information

- **Feature Added**: Version 0.0.1-SNAPSHOT
- **Date**: October 31, 2025
- **Implementation**: Full DDD/CQRS architecture
- **Database**: PostgreSQL with automatic schema migration
