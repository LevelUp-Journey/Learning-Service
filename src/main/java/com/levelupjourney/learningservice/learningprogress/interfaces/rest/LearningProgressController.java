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
            description = "Initialize learning progress for a guide or course. Returns 409 if progress already exists.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LearningProgressResource> startLearning(@Valid @RequestBody StartLearningResource resource) {
        StartLearningCommand command = assembler.toCommandFromResource(resource);
        LearningProgress progress = commandService.handle(command);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toResourceFromEntity(progress));
    }
    
    @PutMapping("/{id}")
    @Operation(
            summary = "Update progress",
            description = "Update completed items and reading time. Progress is automatically completed when all items are done.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LearningProgressResource> updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgressResource resource) {
        
        UpdateProgressCommand command = assembler.toCommandFromResource(id, resource);
        LearningProgress progress = commandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(progress));
    }
    
    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Complete progress",
            description = "Manually mark learning progress as completed.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LearningProgressResource> completeProgress(@PathVariable UUID id) {
        CompleteProgressCommand command = assembler.toCompleteCommand(id);
        LearningProgress progress = commandService.handle(command);
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(progress));
    }
    
    @GetMapping
    @Operation(
            summary = "Get progress for entity",
            description = "Get learning progress for a specific guide or course.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LearningProgressResource> getProgress(
            @RequestParam String userId,
            @RequestParam LearningEntityType entityType,
            @RequestParam UUID entityId) {
        
        GetProgressQuery query = new GetProgressQuery(userId, entityType, entityId);
        LearningProgress progress = queryService.handle(query)
                .orElseThrow(() -> new ResourceNotFoundException("Progress not found"));
        
        return ResponseEntity.ok(assembler.toResourceFromEntity(progress));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get user progress",
            description = "Get all learning progress for a user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<LearningProgressResource>> getUserProgress(@PathVariable String userId) {
        GetUserProgressQuery query = new GetUserProgressQuery(userId);
        List<LearningProgress> progressList = queryService.handle(query);
        
        List<LearningProgressResource> resources = progressList.stream()
                .map(assembler::toResourceFromEntity)
                .toList();
        
        return ResponseEntity.ok(resources);
    }
}
