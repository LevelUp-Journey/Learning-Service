package com.levelupjourney.learningservice.topics.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Topic resource representation")
public record TopicResource(
        @Schema(description = "Unique identifier of the topic", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Name of the topic", example = "JavaScript")
        String name,
        
        @Schema(description = "Description of the topic", example = "Programming language for web development")
        String description,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
}
