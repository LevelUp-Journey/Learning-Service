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
            description = "Enroll a user in a course. Users can only enroll themselves unless they are admin. Returns 409 Conflict if already enrolled.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<EnrollmentResource> enrollUser(@Valid @RequestBody EnrollUserResource resource) {
        EnrollUserCommand command = assembler.toCommandFromResource(resource);
        Enrollment enrollment = enrollmentCommandService.handle(command);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toResourceFromEntity(enrollment));
    }
    
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel enrollment",
            description = "Cancel a user's enrollment in a course. Users can only cancel their own enrollments unless they are admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<EnrollmentResource> cancelEnrollment(@PathVariable UUID id) {
        CancelEnrollmentCommand command = new CancelEnrollmentCommand(id);
        Enrollment enrollment = enrollmentCommandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(enrollment));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user enrollments",
            description = "Get all enrollments for a specific user. Users can only view their own enrollments unless they are admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<EnrollmentResource>> getUserEnrollments(@PathVariable String userId) {
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
            description = "Get all enrollments for a specific course. Only admin or course authors can view.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<EnrollmentResource>> getCourseEnrollments(@PathVariable UUID courseId) {
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
            description = "Check if a user is enrolled in a specific course.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<EnrollmentResource> checkEnrollment(
            @RequestParam String userId,
            @RequestParam UUID courseId) {
        
        GetEnrollmentByUserAndCourseQuery query = new GetEnrollmentByUserAndCourseQuery(userId, courseId);
        return enrollmentQueryService.handle(query)
                .map(enrollment -> ResponseEntity.ok(assembler.toResourceFromEntity(enrollment)))
                .orElse(ResponseEntity.notFound().build());
    }
}
