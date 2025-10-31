package com.levelupjourney.learningservice.learningprogress.interfaces.rest;

import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.CompleteProgressCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.StartLearningCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.UpdateProgressCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.queries.GetProgressQuery;
import com.levelupjourney.learningservice.learningprogress.domain.model.queries.GetUserProgressQuery;
import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import com.levelupjourney.learningservice.learningprogress.domain.services.LearningProgressCommandService;
import com.levelupjourney.learningservice.learningprogress.domain.services.LearningProgressQueryService;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.LearningProgressResource;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.StartLearningResource;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.UpdateProgressResource;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.transform.LearningProgressResourceAssembler;
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
@RequestMapping("/api/v1/progress")
@Tag(name = "Learning Progress", description = "Learning progress tracking endpoints")
public class LearningProgressController {
    
    private final LearningProgressCommandService commandService;
    private final LearningProgressQueryService queryService;
    private final LearningProgressResourceAssembler assembler;
    
    public LearningProgressController(
            LearningProgressCommandService commandService,
            LearningProgressQueryService queryService,
            LearningProgressResourceAssembler assembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.assembler = assembler;
    }
    
    @PostMapping
    @Operation(
            summary = "Start learning",
            description = """
                    Initializes learning progress tracking for a guide or course.
                    - Creates progress record with 0% completion
                    - Supports both GUIDE and COURSE entity types
                    - Users can only start learning for themselves
                    - Returns 409 Conflict if progress already exists
                    - Automatically tracks totalItems based on entity (pages for guide, guides for course)
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Learning progress created successfully",
                    content = @Content(schema = @Schema(implementation = LearningProgressResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or entity not accessible"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot start learning for other users"),
            @ApiResponse(responseCode = "404", description = "Entity (guide or course) not found"),
            @ApiResponse(responseCode = "409", description = "Progress already exists for this user and entity")
    })
    public ResponseEntity<LearningProgressResource> startLearning(
            @io.swagger.v3.oas.annotations.Parameter(description = "Start learning data (userId, entityType, entityId)", required = true)
            @Valid @RequestBody StartLearningResource resource
    ) {
        StartLearningCommand command = assembler.toCommandFromResource(resource);
        LearningProgress progress = commandService.handle(command);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toResourceFromEntity(progress));
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Update learning progress",
            description = """
                    Updates progress completion and reading time.
                    - Updates completedItems count (e.g., pages read)
                    - Adds to totalReadingTime (in minutes)
                    - Users can only update their own progress
                    - Progress automatically marked as COMPLETED when completedItems equals totalItems
                    - completionPercentage is auto-calculated
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress updated successfully",
                    content = @Content(schema = @Schema(implementation = LearningProgressResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (completedItems exceeds totalItems, negative values, etc.)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot update other users' progress"),
            @ApiResponse(responseCode = "404", description = "Progress not found")
    })
    public ResponseEntity<LearningProgressResource> updateProgress(
            @io.swagger.v3.oas.annotations.Parameter(description = "Progress UUID", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.Parameter(description = "Updated progress data (completedItems, readingTime)", required = true)
            @Valid @RequestBody UpdateProgressResource resource) {
        
        UpdateProgressCommand command = assembler.toCommandFromResource(id, resource);
        LearningProgress progress = commandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(progress));
    }
    
    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Manually complete progress",
            description = """
                    Marks learning progress as completed regardless of item count.
                    - Sets status to COMPLETED
                    - Sets completionPercentage to 100%
                    - Records completion timestamp
                    - Users can only complete their own progress
                    - Useful for marking entire course/guide as done
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress marked as completed",
                    content = @Content(schema = @Schema(implementation = LearningProgressResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot complete other users' progress"),
            @ApiResponse(responseCode = "404", description = "Progress not found")
    })
    public ResponseEntity<LearningProgressResource> completeProgress(
            @io.swagger.v3.oas.annotations.Parameter(description = "Progress UUID", required = true)
            @PathVariable UUID id
    ) {
        CompleteProgressCommand command = assembler.toCompleteCommand(id);
        LearningProgress progress = commandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(progress));
    }
    
    @GetMapping
    @Operation(
            summary = "Get progress for specific entity",
            description = """
                    Retrieves learning progress for a specific guide or course.
                    - Query parameters: userId, entityType (GUIDE or COURSE), entityId
                    - Users can only view their own progress
                    - ADMIN can view any user's progress
                    - Returns 404 if no progress found for this combination
                    - Example: /api/v1/progress?userId=user123&entityType=GUIDE&entityId=uuid
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LearningProgressResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot view other users' progress without ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Progress not found")
    })
    public ResponseEntity<LearningProgressResource> getProgress(
            @io.swagger.v3.oas.annotations.Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Entity type (GUIDE or COURSE)", required = true)
            @RequestParam LearningEntityType entityType,
            @io.swagger.v3.oas.annotations.Parameter(description = "Entity UUID (guide or course ID)", required = true)
            @RequestParam UUID entityId) {
        
        GetProgressQuery query = new GetProgressQuery(userId, entityType, entityId);
        LearningProgress progress = queryService.handle(query)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(progress));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get all progress for user",
            description = """
                    Retrieves all learning progress records for a specific user.
                    - Includes both GUIDE and COURSE progress
                    - Shows IN_PROGRESS and COMPLETED items
                    - Users can only view their own progress
                    - ADMIN can view any user's progress
                    - Useful for student dashboard showing all active learning
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot view other users' progress without ADMIN role")
    })
    public ResponseEntity<List<LearningProgressResource>> getUserProgress(
            @io.swagger.v3.oas.annotations.Parameter(description = "User ID", required = true)
            @PathVariable String userId
    ) {
        GetUserProgressQuery query = new GetUserProgressQuery(userId);
        List<LearningProgress> progressList = queryService.handle(query);
        
        List<LearningProgressResource> resources = progressList.stream()
                .map(assembler::toResourceFromEntity)
                .toList();
        
        return ResponseEntity.ok(resources);
    }
}
