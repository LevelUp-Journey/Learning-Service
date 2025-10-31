package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

@Schema(description = "Request to update a guide")
public record UpdateGuideResource(
        @Schema(description = "Guide title")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        
        @Schema(description = "Guide description")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        
        @Schema(description = "Cover image URL")
        String coverImage,
        
        @Schema(description = "Topic IDs")
        Set<UUID> topicIds
) {
}
