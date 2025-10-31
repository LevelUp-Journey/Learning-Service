package com.levelupjourney.learningservice.topics.interfaces.rest;

import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.topics.domain.model.commands.DeleteTopicCommand;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetAllTopicsQuery;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetTopicByIdQuery;
import com.levelupjourney.learningservice.topics.domain.services.TopicCommandService;
import com.levelupjourney.learningservice.topics.domain.services.TopicQueryService;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.CreateTopicResource;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.TopicResource;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.UpdateTopicResource;
import com.levelupjourney.learningservice.topics.interfaces.rest.transform.TopicResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Topic management endpoints")
public class TopicsController {

    private final TopicCommandService topicCommandService;
    private final TopicQueryService topicQueryService;

    @GetMapping
    @Operation(
            summary = "Get all topics",
            description = "Retrieves a list of all available topics. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TopicResource.class)))
    })
    public ResponseEntity<List<TopicResource>> getAllTopics() {
        var topics = topicQueryService.handle(new GetAllTopicsQuery());
        var resources = topics.stream()
                .map(TopicResourceAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{topicId}")
    @Operation(
            summary = "Get topic by ID",
            description = "Retrieves a specific topic by its ID. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topic found",
                    content = @Content(schema = @Schema(implementation = TopicResource.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public ResponseEntity<TopicResource> getTopicById(@PathVariable UUID topicId) {
        var topic = topicQueryService.handle(new GetTopicByIdQuery(topicId))
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));
        var resource = TopicResourceAssembler.toResourceFromEntity(topic);
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(
            summary = "Create a new topic",
            description = "Creates a new topic. Requires ADMIN or TEACHER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Topic created successfully",
                    content = @Content(schema = @Schema(implementation = TopicResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Topic with the same name already exists")
    })
    public ResponseEntity<TopicResource> createTopic(@Valid @RequestBody CreateTopicResource resource) {
        var command = TopicResourceAssembler.toCommandFromResource(resource);
        var topic = topicCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Failed to create topic"));
        var topicResource = TopicResourceAssembler.toResourceFromEntity(topic);
        return new ResponseEntity<>(topicResource, HttpStatus.CREATED);
    }

    @PutMapping("/{topicId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Update a topic",
            description = "Updates an existing topic. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Topic updated successfully",
                    content = @Content(schema = @Schema(implementation = TopicResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Topic not found"),
            @ApiResponse(responseCode = "409", description = "Topic with the same name already exists")
    })
    public ResponseEntity<TopicResource> updateTopic(
            @PathVariable UUID topicId,
            @Valid @RequestBody UpdateTopicResource resource
    ) {
        var command = TopicResourceAssembler.toCommandFromResource(topicId, resource);
        var topic = topicCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));
        var topicResource = TopicResourceAssembler.toResourceFromEntity(topic);
        return ResponseEntity.ok(topicResource);
    }

    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Delete a topic",
            description = "Deletes a topic. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Topic deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Topic not found")
    })
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID topicId) {
        topicCommandService.handle(new DeleteTopicCommand(topicId));
        return ResponseEntity.noContent().build();
    }
}
