package com.levelupjourney.learningservice.enrollments.interfaces.rest;

import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.CancelEnrollmentCommand;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.EnrollUserCommand;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetCourseEnrollmentsQuery;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetEnrollmentByUserAndCourseQuery;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetUserEnrollmentsQuery;
import com.levelupjourney.learningservice.enrollments.domain.services.EnrollmentCommandService;
import com.levelupjourney.learningservice.enrollments.domain.services.EnrollmentQueryService;
import com.levelupjourney.learningservice.enrollments.interfaces.rest.resources.EnrollUserResource;
import com.levelupjourney.learningservice.enrollments.interfaces.rest.resources.EnrollmentResource;
import com.levelupjourney.learningservice.enrollments.interfaces.rest.transform.EnrollmentResourceAssembler;
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
@RequestMapping("/api/v1/enrollments")
@Tag(name = "Enrollments", description = "Enrollment management endpoints")
public class EnrollmentsController {
    
    private final EnrollmentCommandService enrollmentCommandService;
    private final EnrollmentQueryService enrollmentQueryService;
    private final EnrollmentResourceAssembler assembler;
    
    public EnrollmentsController(
            EnrollmentCommandService enrollmentCommandService,
            EnrollmentQueryService enrollmentQueryService,
            EnrollmentResourceAssembler assembler) {
        this.enrollmentCommandService = enrollmentCommandService;
        this.enrollmentQueryService = enrollmentQueryService;
        this.assembler = assembler;
    }
    
    @PostMapping
    @Operation(
            summary = "Enroll user in course",
            description = """
                    Enrolls a user in a course.
                    - Users can only enroll themselves (userId must match authenticated user)
                    - ADMIN can enroll any user
                    - Course must be in PUBLISHED status
                    - Returns 409 Conflict if user already enrolled
                    - Creates initial enrollment progress tracking
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User enrolled successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or course not available for enrollment"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot enroll other users without ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "409", description = "User already enrolled in this course")
    })
    public ResponseEntity<EnrollmentResource> enrollUser(
            @io.swagger.v3.oas.annotations.Parameter(description = "Enrollment data (userId and courseId)", required = true)
            @Valid @RequestBody EnrollUserResource resource
    ) {
        EnrollUserCommand command = assembler.toCommandFromResource(resource);
        Enrollment enrollment = enrollmentCommandService.handle(command);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toResourceFromEntity(enrollment));
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel enrollment",
            description = """
                    Cancels (unenrolls) a user from a course.
                    - Users can only cancel their own enrollments
                    - ADMIN can cancel any enrollment
                    - Sets enrollment status to CANCELLED
                    - Learning progress entries remain for historical tracking
                    - User can re-enroll later if needed
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment cancelled successfully",
                    content = @Content(schema = @Schema(implementation = EnrollmentResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot cancel other users' enrollments without ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<EnrollmentResource> cancelEnrollment(
            @io.swagger.v3.oas.annotations.Parameter(description = "Enrollment UUID", required = true)
            @PathVariable UUID id
    ) {
        CancelEnrollmentCommand command = new CancelEnrollmentCommand(id);
        Enrollment enrollment = enrollmentCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(enrollment));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user enrollments",
            description = """
                    Retrieves all enrollments for a specific user.
                    - Users can only view their own enrollments
                    - ADMIN can view any user's enrollments
                    - Includes enrollment status and progress information
                    - Returns only ACTIVE enrollments (excludes CANCELLED)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot view other users' enrollments without ADMIN role")
    })
    public ResponseEntity<List<EnrollmentResource>> getUserEnrollments(
            @io.swagger.v3.oas.annotations.Parameter(description = "User ID", required = true)
            @PathVariable String userId
    ) {
        GetUserEnrollmentsQuery query = new GetUserEnrollmentsQuery(userId);
        List<Enrollment> enrollments = enrollmentQueryService.handle(query);
        
        List<EnrollmentResource> resources = enrollments.stream()
                .map(assembler::toResourceFromEntity)
                .toList();
        
        return ResponseEntity.ok(resources);
    }
    
    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "Get course enrollments",
            description = """
                    Retrieves all enrollments for a specific course.
                    - Only ADMIN or course authors can view course enrollments
                    - Includes student progress information
                    - Useful for instructors to track student engagement
                    - Returns only ACTIVE enrollments (excludes CANCELLED)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or course author permission"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<List<EnrollmentResource>> getCourseEnrollments(
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID", required = true)
            @PathVariable UUID courseId
    ) {
        GetCourseEnrollmentsQuery query = new GetCourseEnrollmentsQuery(courseId);
        List<Enrollment> enrollments = enrollmentQueryService.handle(query);
        
        List<EnrollmentResource> resources = enrollments.stream()
                .map(assembler::toResourceFromEntity)
                .toList();
        
        return ResponseEntity.ok(resources);
    }
    
    @GetMapping("/check")
    @Operation(
            summary = "Check enrollment status",
            description = """
                    Checks if a user is enrolled in a specific course.
                    - Users can only check their own enrollment status
                    - ADMIN can check any user's enrollment status
                    - Returns 404 if no enrollment found
                    - Returns enrollment details including status and progress if found
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrollment found",
                    content = @Content(schema = @Schema(implementation = EnrollmentResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot check other users' enrollment without ADMIN role"),
            @ApiResponse(responseCode = "404", description = "No enrollment found for this user and course")
    })
    public ResponseEntity<EnrollmentResource> checkEnrollment(
            @io.swagger.v3.oas.annotations.Parameter(description = "User ID to check", required = true)
            @RequestParam String userId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Course UUID to check", required = true)
            @RequestParam UUID courseId) {
        
        GetEnrollmentByUserAndCourseQuery query = new GetEnrollmentByUserAndCourseQuery(userId, courseId);
        return enrollmentQueryService.handle(query)
                .map(enrollment -> ResponseEntity.ok(assembler.toResourceFromEntity(enrollment)))
                .orElse(ResponseEntity.notFound().build());
    }
}
