package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Page resource representation")
public record PageResource(
        @Schema(description = "Page ID")
        UUID id,
        
        @Schema(description = "Page content in Markdown")
        String content,
        
        @Schema(description = "Page order number")
        Integer orderNumber,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
}
