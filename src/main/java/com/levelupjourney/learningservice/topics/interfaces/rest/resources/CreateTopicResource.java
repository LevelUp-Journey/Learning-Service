package com.levelupjourney.learningservice.topics.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new topic")
public record CreateTopicResource(
        @Schema(description = "Name of the topic", example = "JavaScript", required = true)
        @NotBlank(message = "Topic name is required")
        @Size(max = 100, message = "Topic name must not exceed 100 characters")
        String name,
        
        @Schema(description = "Description of the topic", example = "Programming language for web development")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
