package com.levelupjourney.learningservice.guides.domain.model.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdatePageCommand(
        @NotNull(message = "Page ID is required")
        UUID pageId,
        
        String content,
        
        @Min(value = 0, message = "Order must be non-negative")
        Integer order
) {
}
