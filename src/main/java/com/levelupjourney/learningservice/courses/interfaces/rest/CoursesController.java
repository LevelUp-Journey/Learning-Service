package com.levelupjourney.learningservice.courses.interfaces.rest;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.domain.model.commands.*;
import com.levelupjourney.learningservice.courses.domain.model.queries.GetCourseByIdQuery;
import com.levelupjourney.learningservice.courses.domain.model.queries.SearchCoursesQuery;
import com.levelupjourney.learningservice.courses.domain.services.CourseCommandService;
import com.levelupjourney.learningservice.courses.domain.services.CourseQueryService;
import com.levelupjourney.learningservice.courses.interfaces.rest.resources.*;
import com.levelupjourney.learningservice.courses.interfaces.rest.transform.CourseResourceAssembler;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course management endpoints")
public class CoursesController {
    
    private final CourseCommandService courseCommandService;
    private final CourseQueryService courseQueryService;
    private final CourseResourceAssembler assembler;
    
    public CoursesController(
            CourseCommandService courseCommandService,
            CourseQueryService courseQueryService,
            CourseResourceAssembler assembler) {
        this.courseCommandService = courseCommandService;
        this.courseQueryService = courseQueryService;
        this.assembler = assembler;
    }
    
    @GetMapping
    @Operation(
            summary = "Search courses",
            description = """
                    Search courses with optional filters.
                    - Anonymous users see only PUBLISHED courses
                    - Authenticated users see PUBLISHED + their DRAFT courses
                    - Filter by title (partial match), topics, authors, or status
                    - Example: /api/v1/courses?title=Java&topicIds=uuid1,uuid2&status=PUBLISHED
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<CourseResource>> searchCourses(
            @io.swagger.v3.oas.annotations.Parameter(description = "Filter by course title (partial match)")
            @RequestParam(required = false) String title,
            @io.swagger.v3.oas.annotations.Parameter(description = "Filter by topic UUIDs (comma-separated)")
            @RequestParam(required = false) List<UUID> topicIds,
            @io.swagger.v3.oas.annotations.Parameter(description = "Filter by author user IDs (comma-separated)")
            @RequestParam(required = false) List<String> authorIds,
            @io.swagger.v3.oas.annotations.Parameter(description = "Filter by status (DRAFT, PUBLISHED, etc.)")
            @RequestParam(required = false) EntityStatus status) {
        
        var query = new SearchCoursesQuery(title, topicIds, authorIds, status);
        List<Course> courses = courseQueryService.handle(query);
        
        List<CourseResource> resources = courses.stream()
                .map(assembler::toResourceFromEntity)
                .toList();
        
        return ResponseEntity.ok(resources);
    }
    
    @GetMapping("/{id}")
    @Operation(
            summary = "Get course by ID",
            description = """
                    Retrieves detailed course information including associated guides.
                    - Anonymous users can only see PUBLISHED courses
                    - Authors/ADMIN can see their DRAFT courses
                    - Returns 404 if course not found or not accessible
                    - Includes full guide list with metadata
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "404", description = "Course not found or not accessible")
    })
    public ResponseEntity<CourseResource> getCourseById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID id
    ) {
        var query = new GetCourseByIdQuery(id);
        Course course = courseQueryService.handle(query)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @PostMapping
    @Operation(
            summary = "Create new course",
            description = """
                    Creates a new course with DRAFT status.
                    - Requires TEACHER or ADMIN role
                    - Course starts with no associated guides (add via /courses/{id}/guides/{guideId})
                    - Can have multiple authors and topics
                    - totalPages calculated automatically from associated guides
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course created successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires TEACHER or ADMIN role")
    })
    public ResponseEntity<CourseResource> createCourse(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course creation data", required = true)
            @Valid @RequestBody CreateCourseResource resource
    ) {
        CreateCourseCommand command = assembler.toCommandFromResource(resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toResourceFromEntity(course));
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Update course details",
            description = """
                    Updates course title, description, or metadata.
                    - Only course authors or ADMIN can update
                    - Cannot update status (use PUT /courses/{id}/status)
                    - Cannot update guides association (use POST/DELETE /courses/{id}/guides/{guideId})
                    - Authors can be updated via dedicated endpoint
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseResource> updateCourse(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.Parameter(description = "Updated course data", required = true)
            @Valid @RequestBody UpdateCourseResource resource) {
        
        UpdateCourseCommand command = assembler.toCommandFromResource(id, resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update course status",
            description = """
                    Changes the publication status of a course.
                    - Supported statuses: DRAFT, PUBLISHED, DELETED
                    - Only course authors or ADMIN can change status
                    - PUBLISHED courses become visible to all users and open for enrollment
                    - DRAFT courses are only visible to authors and admins
                    - DELETED courses are hidden from all queries
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course status updated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseResource> updateCourseStatus(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.Parameter(description = "New status data", required = true)
            @Valid @RequestBody UpdateCourseStatusResource resource) {
        
        UpdateCourseStatusCommand command = assembler.toCommandFromResource(id, resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @PutMapping("/{id}/authors")
    @Operation(
            summary = "Update course authors",
            description = """
                    Replaces the entire list of course authors.
                    - Only ADMIN or current authors can update authors
                    - At least one author must remain
                    - Maximum of 5 authors per course
                    - All author IDs must correspond to existing users with TEACHER or ADMIN role
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course authors updated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (exceeds max authors, empty list, etc.)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or author permission"),
            @ApiResponse(responseCode = "404", description = "Course not found or author ID not found")
    })
    public ResponseEntity<CourseResource> updateCourseAuthors(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.Parameter(description = "Updated authors data (max 5)", required = true)
            @Valid @RequestBody UpdateCourseAuthorsResource resource) {
        
        UpdateCourseAuthorsCommand command = assembler.toCommandFromResource(id, resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete course",
            description = """
                    Soft deletes a course (sets status to DELETED).
                    - Only course authors or ADMIN can delete
                    - Deleted courses are hidden from all queries
                    - Associated guides revert to DRAFT status
                    - Enrollment records remain for historical tracking
                    - Learning progress entries remain intact
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<Void> deleteCourse(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID id
    ) {
        DeleteCourseCommand command = new DeleteCourseCommand(id);
        courseCommandService.handle(command);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{courseId}/guides/{guideId}")
    @Operation(
            summary = "Associate guide with course",
            description = """
                    Adds a guide to a course (creates the relationship).
                    - Only course authors or ADMIN can associate guides
                    - Guide's status automatically changes to ASSOCIATED_WITH_COURSE
                    - Guide must be in PUBLISHED or DRAFT status before association
                    - Course's totalPages automatically recalculated
                    - A guide can only be associated with one course at a time
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide associated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid association (guide already associated, etc.)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Course or guide not found")
    })
    public ResponseEntity<CourseResource> associateGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID courseId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID to associate", required = true)
            @PathVariable UUID guideId) {
        
        AssociateGuideCommand command = new AssociateGuideCommand(courseId, guideId);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @DeleteMapping("/{courseId}/guides/{guideId}")
    @Operation(
            summary = "Disassociate guide from course",
            description = """
                    Removes a guide from a course (breaks the relationship).
                    - Only course authors or ADMIN can disassociate guides
                    - Guide's status automatically reverts to DRAFT
                    - Course's totalPages automatically recalculated
                    - Learning progress for the guide within this course remains intact
                    - Students' enrollment progress is updated accordingly
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide disassociated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid disassociation (guide not part of course, etc.)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Course or guide not found")
    })
    public ResponseEntity<CourseResource> disassociateGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID courseId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID to disassociate", required = true)
            @PathVariable UUID guideId) {
        
        DisassociateGuideCommand command = new DisassociateGuideCommand(courseId, guideId);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
}
