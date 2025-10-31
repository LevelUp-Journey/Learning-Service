package com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProgressResource(
        @NotNull(message = "Completed items is required")
        @Min(value = 0, message = "Completed items must be non-negative")
        Integer completedItems,
        
        @NotNull(message = "Reading time is required")
        @Min(value = 0, message = "Reading time must be non-negative")
        Long readingTimeSeconds
) {
}
