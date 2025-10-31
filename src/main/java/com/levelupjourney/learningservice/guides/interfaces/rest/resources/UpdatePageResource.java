package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Request to update a page")
public record UpdatePageResource(
        @Schema(description = "Page content in Markdown")
        String content,
        
        @Schema(description = "Page order number")
        @Min(value = 0, message = "Order must be non-negative")
        Integer order
) {
}
