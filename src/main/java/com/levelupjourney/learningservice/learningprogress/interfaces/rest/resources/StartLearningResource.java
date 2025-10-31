package com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources;

import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartLearningResource(
        @NotBlank(message = "User ID is required")
        String userId,
        
        @NotNull(message = "Entity type is required")
        LearningEntityType entityType,
        
        @NotNull(message = "Entity ID is required")
        UUID entityId
) {
}
