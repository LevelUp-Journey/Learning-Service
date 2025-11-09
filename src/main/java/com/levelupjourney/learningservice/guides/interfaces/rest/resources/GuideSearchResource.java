package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Guide search result with basic information")
public record GuideSearchResource(
        @Schema(description = "Unique identifier of the guide", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Guide title", example = "Introduction to Java Programming")
        String title,
        
        @Schema(description = "Guide description", example = "A comprehensive guide to learn Java from scratch")
        String description,
        
        @Schema(description = "Cover image URL", example = "https://example.com/images/java-guide.jpg")
        String coverImage
) {
}
