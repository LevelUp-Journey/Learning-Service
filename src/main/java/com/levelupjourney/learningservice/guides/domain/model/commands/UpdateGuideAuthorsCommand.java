package com.levelupjourney.learningservice.guides.domain.model.commands;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record UpdateGuideAuthorsCommand(
        @NotNull(message = "Guide ID is required")
        UUID guideId,
        
        @NotNull(message = "Authors are required")
        Set<String> authorIds
) {
}
