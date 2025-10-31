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
            description = "Search courses by title, topics, authors, or status. Public courses can be accessed without authentication."
    )
    public ResponseEntity<List<CourseResource>> searchCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) List<UUID> topicIds,
            @RequestParam(required = false) List<String> authorIds,
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
            description = "Get a course with its associated guides. Visibility depends on course status and user authentication."
    )
    public ResponseEntity<CourseResource> getCourseById(@PathVariable UUID id) {
        var query = new GetCourseByIdQuery(id);
        Course course = courseQueryService.handle(query)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @PostMapping
    @Operation(
            summary = "Create course",
            description = "Create a new course. Only ADMIN and TEACHER roles can create courses.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CourseResource> createCourse(@Valid @RequestBody CreateCourseResource resource) {
        CreateCourseCommand command = assembler.toCommandFromResource(resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toResourceFromEntity(course));
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Update course",
            description = "Update course details. Only course authors or ADMIN can update.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CourseResource> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseResource resource) {
        
        UpdateCourseCommand command = assembler.toCommandFromResource(id, resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update course status",
            description = "Change course status (DRAFT, PUBLISHED, DELETED). Only authors or ADMIN can change status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CourseResource> updateCourseStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseStatusResource resource) {
        
        UpdateCourseStatusCommand command = assembler.toCommandFromResource(id, resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @PutMapping("/{id}/authors")
    @Operation(
            summary = "Update course authors",
            description = "Update the list of course authors. Only ADMIN role can update authors.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CourseResource> updateCourseAuthors(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseAuthorsResource resource) {
        
        UpdateCourseAuthorsCommand command = assembler.toCommandFromResource(id, resource);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete course",
            description = "Soft delete a course (sets status to DELETED). Only authors or ADMIN can delete.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        DeleteCourseCommand command = new DeleteCourseCommand(id);
        courseCommandService.handle(command);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{courseId}/guides/{guideId}")
    @Operation(
            summary = "Associate guide with course",
            description = "Add a guide to a course. The guide's status will be changed to ASSOCIATED_WITH_COURSE. Only authors or ADMIN can associate guides.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CourseResource> associateGuide(
            @PathVariable UUID courseId,
            @PathVariable UUID guideId) {
        
        AssociateGuideCommand command = new AssociateGuideCommand(courseId, guideId);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
    
    @DeleteMapping("/{courseId}/guides/{guideId}")
    @Operation(
            summary = "Disassociate guide from course",
            description = "Remove a guide from a course. The guide's status will revert to DRAFT. Only authors or ADMIN can disassociate guides.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<CourseResource> disassociateGuide(
            @PathVariable UUID courseId,
            @PathVariable UUID guideId) {
        
        DisassociateGuideCommand command = new DisassociateGuideCommand(courseId, guideId);
        Course course = courseCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(course));
    }
}
