package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request to create a new guide")
public record CreateGuideResource(
        @Schema(description = "Guide title", required = true)
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        
        @Schema(description = "Guide description")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        
        @Schema(description = "Cover image URL")
        String coverImage,
        
        @Schema(description = "Author IDs (optional, defaults to current user)")
        Set<String> authorIds,
        
        @Schema(description = "Topic IDs", required = true)
        @NotNull(message = "At least one topic must be specified")
        Set<UUID> topicIds
) {
}
