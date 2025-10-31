package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a new page")
public record CreatePageResource(
        @Schema(description = "Page content in Markdown", required = true)
        @NotBlank(message = "Content is required")
        String content,
        
        @Schema(description = "Page order number", required = true)
        @NotNull(message = "Order is required")
        @Min(value = 0, message = "Order must be non-negative")
        Integer order
) {
}
